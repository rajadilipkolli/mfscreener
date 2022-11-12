/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.service.impl;

import com.example.mfscreener.entities.*;
import com.example.mfscreener.exception.NavNotFoundException;
import com.example.mfscreener.exception.SchemeNotFoundException;
import com.example.mfscreener.model.*;
import com.example.mfscreener.repository.ErrorMessageRepository;
import com.example.mfscreener.repository.MFSchemeRepository;
import com.example.mfscreener.repository.MFSchemeTypeRepository;
import com.example.mfscreener.service.NavService;
import com.example.mfscreener.util.Constants;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class NavServiceImpl implements NavService {

    private final MFSchemeRepository mfSchemesRepository;
    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final RestTemplate restTemplate;
    private final ErrorMessageRepository errorMessageRepository;

    Function<NAVData, MFSchemeNav> navDataToMFSchemeNavFunction =
            navData -> {
                MFSchemeNav mfSchemeNav = new MFSchemeNav();
                mfSchemeNav.setNav(Double.parseDouble(navData.nav()));
                mfSchemeNav.setNavDate(LocalDate.parse(navData.date(), Constants.DATE_FORMATTER));
                return mfSchemeNav;
            };

    @Override
    public Scheme getNav(Long schemeCode) {
        return mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, getAdjustedDate(LocalDate.now()))
                .map(this::convertToDTO)
                .orElseThrow(() -> new SchemeNotFoundException("Scheme Not Found"));
    }

    @Override
    public Scheme getNavOnDate(Long schemeCode, String inputDate) {
        LocalDate adjustedDate = getAdjustedDateForNAV(inputDate);
        return getNavByDate(schemeCode, adjustedDate);
    }

    @Override
    public void fetchSchemeDetails(Long schemeCode) {
        URI uri =
                UriComponentsBuilder.fromHttpUrl(Constants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                        .build()
                        .toUri();

        ResponseEntity<NavResponse> navResponseResponseEntity =
                this.restTemplate.exchange(uri, HttpMethod.GET, null, NavResponse.class);
        if (navResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
            NavResponse entityBody = navResponseResponseEntity.getBody();
            Assert.notNull(entityBody, () -> "Body Can't be Null");
            MFScheme mfScheme =
                    mfSchemesRepository
                            .findBySchemeId(schemeCode)
                            .orElseThrow(
                                    () ->
                                            new SchemeNotFoundException(
                                                    "Fund with schemeCode "
                                                            + schemeCode
                                                            + " Not Found"));
            mergeList(entityBody, mfScheme);
        }
    }

    @Override
    public List<FundDetailDTO> fetchSchemes(String schemeName) {
        return this.mfSchemesRepository.findBySchemeNameIgnoringCaseLike("%" + schemeName + "%");
    }

    @Override
    public List<FundDetailDTO> fetchSchemesByFundName(String fundName) {
        return this.mfSchemesRepository.findByFundHouseIgnoringCaseLike("%" + fundName + "%");
    }

    private Scheme getSchemeDetails(Long schemeCode, LocalDate navDate) {
        fetchSchemeDetails(schemeCode);
        return this.mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, navDate)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NavNotFoundException("Nav Not Found for given Date"));
    }

    private void mergeList(@NonNull NavResponse navResponse, MFScheme mfScheme) {
        List<NAVData> navList = navResponse.getData();

        List<MFSchemeNav> newNavs =
                navList.stream()
                        .map(navDataToMFSchemeNavFunction)
                        .filter(nav -> !mfScheme.getMfSchemeNavies().contains(nav))
                        .toList();

        if (!newNavs.isEmpty()) {
            for (MFSchemeNav newSchemeNav : newNavs) {
                mfScheme.addSchemeNav(newSchemeNav);
            }
            final Meta meta = navResponse.getMeta();
            MFSchemeType mfschemeType =
                    this.mfSchemeTypeRepository
                            .findBySchemeCategoryAndSchemeType(
                                    meta.getSchemeCategory(), meta.getSchemeType())
                            .orElseGet(
                                    () -> {
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
        return this.mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, navDate)
                .map(this::convertToDTO)
                .orElseGet(() -> getSchemeDetails(schemeCode, navDate));
    }

    private LocalDate getAdjustedDate(LocalDate adjustedDate) {
        if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjustedDate = adjustedDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        return adjustedDate;
    }

    private LocalDate getAdjustedDateForNAV(String inputDate) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(Constants.DATE_PATTERN_DD_MM_YYYY);
        LocalDate adjustedDate = LocalDate.parse(inputDate, formatter);
        return getAdjustedDate(adjustedDate);
    }

    private Scheme convertToDTO(MFScheme mfScheme) {
        return new Scheme(
                String.valueOf(mfScheme.getSchemeId()),
                mfScheme.getPayOut(),
                mfScheme.getSchemeName(),
                String.valueOf(mfScheme.getMfSchemeNavies().get(0).getNav()),
                String.valueOf(mfScheme.getMfSchemeNavies().get(0).getNavDate()));
    }

    @Override
    public PortfolioDTO getPortfolio() {
        // List<PortfolioDetails> portfolioDetailsList = transactionRecordRepository.getPortfolio();
        List<PortfolioDetailsDTO> portfolioDetailsDTOS = new ArrayList<>();
        List<PortfolioDetails> portfolioDetailsList = new ArrayList<>();
        portfolioDetailsList.forEach(
                portfolioDetails -> {
                    float totalValue = 0;
                    if (portfolioDetails.getSchemeId() != null) {
                        Scheme scheme =
                                getNavByDate(
                                        portfolioDetails.getSchemeId(),
                                        getAdjustedDate(LocalDate.now()));
                        totalValue =
                                portfolioDetails.getBalanceUnits() * Float.parseFloat(scheme.nav());
                    }
                    PortfolioDetailsDTO portfolioDetailsDTO =
                            new PortfolioDetailsDTO(
                                    totalValue,
                                    portfolioDetails.getSchemaName(),
                                    portfolioDetails.getFolioNumber());
                    portfolioDetailsDTOS.add(portfolioDetailsDTO);
                });
        return new PortfolioDTO(
                portfolioDetailsDTOS.stream()
                        .map(PortfolioDetailsDTO::totalValue)
                        .filter(Objects::nonNull)
                        .reduce(0f, Float::sum),
                portfolioDetailsDTOS);
    }

    @Override
    public void loadFundDetailsIfNotSet() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("loadDetails");
        // List<Long> distinctSchemeIds = transactionRecordRepository.findDistinctSchemeId();
        List<Long> distinctSchemeIds = new ArrayList<>();

        for (Long schemeId : distinctSchemeIds) {
            {
                try {
                    fetchSchemeDetails(schemeId);
                } catch (SchemeNotFoundException | NavNotFoundException exception) {
                    log.error(exception.getMessage());
                    ErrorMessage errorMessage = new ErrorMessage();
                    errorMessage.setMessage(exception.getMessage());
                    errorMessageRepository.save(errorMessage);
                }
            }
        }
        stopWatch.stop();
        log.info("Fund House and Scheme Type Set in : {} sec", stopWatch.getTotalTimeSeconds());
    }
}
