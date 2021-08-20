package com.example.mfscreener.service.impl;

import com.example.mfscreener.convertor.NavServiceConvertor;
import com.example.mfscreener.entities.MFScheme;
import com.example.mfscreener.entities.MFSchemeNav;
import com.example.mfscreener.entities.MFSchemeType;
import com.example.mfscreener.model.Meta;
import com.example.mfscreener.model.NAVData;
import com.example.mfscreener.model.NavResponse;
import com.example.mfscreener.model.Scheme;
import com.example.mfscreener.repository.MFSchemeRepository;
import com.example.mfscreener.repository.MFSchemeTypeRepository;
import com.example.mfscreener.service.NavService;
import com.example.mfscreener.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NavServiceImpl implements NavService {

    private final MFSchemeRepository mfSchemesRepository;
    private final NavServiceConvertor navServiceConvertor;
    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final RestTemplate restTemplate;

    @Override
    public void loadNavForAllFunds() throws IOException {
            long start = System.currentTimeMillis();
            log.info("Loading All Funds...");
            URL url = new URL(Constants.AMFI_WEBSITE_LINK);
            List<Scheme> chopArrayList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String fileRead = br.readLine();
                for (int i = 0; i < 4; ++i) {
                    fileRead = br.readLine();
                }
                while (fileRead != null) {
                    int check = 0;
                    final String[] tokenize = fileRead.split(Constants.SEPARATOR);
                    if (tokenize.length == 1) {
                        check = 1;
                    }
                    if (check == 0) {
                        final String schemecode = tokenize[0];
                        final String payout = tokenize[1];
                        final String reinvestment = tokenize[2];
                        final String schemename = tokenize[3];
                        final String nav = tokenize[4];
//                  final String repurchaseprice = tokenize[5];
//                  final String saleprice = tokenize[6];
                        final String date = tokenize[5];
                        final Scheme tempObj = new Scheme(schemecode, payout, schemename, nav, date);
                        chopArrayList.add(tempObj);
                    }
                    fileRead = br.readLine();
                    if ("".equals(fileRead)) {
                        fileRead = br.readLine();
                    }
                }
            }
            log.info("All Funds loaded in {} milliseconds, total funds loaded :{}", (System.currentTimeMillis() - start), chopArrayList.size());

        if (mfSchemesRepository.count() != chopArrayList.size()) {
            List<MFScheme> list = new ArrayList<>();
            chopArrayList.forEach(scheme -> {
                    MFScheme mfSchemeEntity = navServiceConvertor.convert(scheme);
                    list.add(mfSchemeEntity);
            });
            mfSchemesRepository.saveAll(list);
            log.info("saved in db");
        }
    }

    @Override
    public Scheme getNav(boolean forceUpdate, Long schemeCode) {
        return mfSchemesRepository.findBySchemeIdAndNavDate(schemeCode, getAdjustedDate(LocalDate.now()))
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Scheme Not Found"));
    }

    @Override
    public Scheme getNavOnDate(Long schemeCode, String inputDate) {
        LocalDate adjustedDate = getAdjustedDateForNAV(inputDate);
        return getNavByDate(schemeCode, adjustedDate);
    }

    private Scheme convertToDTO(MFScheme mfScheme) {
        Scheme scheme = new Scheme();
        scheme.setSchemeCode(String.valueOf(mfScheme.getSchemeId()));
        scheme.setSchemeName(mfScheme.getSchemeName());
        scheme.setPayout(mfScheme.getPayOut());
        scheme.setDate(String.valueOf(mfScheme.getMfSchemeNavies().get(0).getNavDate()));
        scheme.setNav(String.valueOf(mfScheme.getMfSchemeNavies().get(0).getNav()));
        return scheme;
    }

    private void mergeList(NavResponse navResponse, MFScheme mfScheme) {
        List<NAVData> navList = Objects.requireNonNull(navResponse).getData();
        List<MFSchemeNav> newNavs = navList.stream()
                .map(navData -> {
                    MFSchemeNav mfSchemeNav = new MFSchemeNav();
                    mfSchemeNav.setNav(Double.parseDouble(navData.getNav()));
                    mfSchemeNav.setNavDate(LocalDate.parse(navData.getDate(), Constants.DATE_FORMATTER));
                    return mfSchemeNav;
                })
                .filter(nav -> !mfScheme.getMfSchemeNavies().contains(nav))
                .collect(Collectors.toList());

        if (!newNavs.isEmpty()) {
            for (MFSchemeNav newSchemeNav : newNavs) {
                mfScheme.addSchemeNav(newSchemeNav);
            }
            final Meta meta = navResponse.getMeta();
            MFSchemeType mfschemeType = this.mfSchemeTypeRepository.findBySchemeCategory(meta.getSchemeCategory())
                    .orElseGet(() -> {
                        MFSchemeType entity = new MFSchemeType();
                        entity.setSchemeType(meta.getSchemeType());
                        entity.setSchemeCategory(meta.getSchemeCategory());
                        return this.mfSchemeTypeRepository.save(entity);
                    });
            mfScheme.setFundHouse(meta.getFundHouse());
            mfschemeType.addMFScheme(mfScheme);
            this.mfSchemesRepository.save(mfScheme);
        }
    }

    private Scheme getNavByDate(Long schemeCode, LocalDate navDate) {
        return this.mfSchemesRepository.findBySchemeIdAndNavDate(schemeCode, navDate).map(this::convertToDTO)
                .orElseGet(() -> schemeSupplier(schemeCode, navDate));
    }

    private Scheme schemeSupplier(Long schemeCode, LocalDate navDate) {
        URI uri = UriComponentsBuilder.fromHttpUrl(Constants.MFAPI_WEBSITE_BASE_URL + schemeCode).build().toUri();

        ResponseEntity<NavResponse> navResponseResponseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, null, NavResponse.class);
        if (navResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
            NavResponse entityBody = navResponseResponseEntity.getBody();
            MFScheme mfScheme = mfSchemesRepository.findBySchemeId(schemeCode).orElseThrow(() -> new RuntimeException("Fund with schemeCode Not Found"));
            mergeList(entityBody, mfScheme);
        }
        return this.mfSchemesRepository.findBySchemeIdAndNavDate(schemeCode, navDate).map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Nav Not Found for given Date"));

    }

    private LocalDate getAdjustedDateForNAV(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_PATTERN_DD_MM_YYYY);
        LocalDate adjustedDate = LocalDate.parse(inputDate, formatter);
        return getAdjustedDate(adjustedDate);
    }

    private LocalDate getAdjustedDate(LocalDate adjustedDate) {
        if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY || adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjustedDate = adjustedDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        return adjustedDate;
    }
}
