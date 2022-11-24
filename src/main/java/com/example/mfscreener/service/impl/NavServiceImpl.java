/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.service.impl;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import com.example.mfscreener.entities.ErrorMessageEntity;
import com.example.mfscreener.entities.MFSchemeEntity;
import com.example.mfscreener.entities.MFSchemeNavEntity;
import com.example.mfscreener.entities.MFSchemeTypeEntity;
import com.example.mfscreener.entities.UserCASDetailsEntity;
import com.example.mfscreener.exception.NavNotFoundException;
import com.example.mfscreener.exception.SchemeNotFoundException;
import com.example.mfscreener.models.CasDTO;
import com.example.mfscreener.models.MFSchemeDTO;
import com.example.mfscreener.models.MetaDTO;
import com.example.mfscreener.models.NAVDataDTO;
import com.example.mfscreener.models.PortfolioDetailsDTO;
import com.example.mfscreener.models.projection.FundDetailProjection;
import com.example.mfscreener.models.response.NavResponse;
import com.example.mfscreener.models.response.PortfolioResponse;
import com.example.mfscreener.repository.CASDetailsEntityRepository;
import com.example.mfscreener.repository.ErrorMessageRepository;
import com.example.mfscreener.repository.MFSchemeRepository;
import com.example.mfscreener.repository.MFSchemeTypeRepository;
import com.example.mfscreener.repository.UserSchemeDetailsEntityRepository;
import com.example.mfscreener.service.NavService;
import com.example.mfscreener.utils.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor
public class NavServiceImpl implements NavService {

    private final MFSchemeRepository mfSchemesRepository;
    private final MFSchemeTypeRepository mfSchemeTypeRepository;
    private final ErrorMessageRepository errorMessageRepository;
    private final CASDetailsEntityRepository casDetailsEntityRepository;
    private final UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final ConversionServiceAdapter conversionServiceAdapter;

    @Override
    public MFSchemeDTO getNav(Long schemeCode) {
        return mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, getAdjustedDate(LocalDate.now()))
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseThrow(() -> new SchemeNotFoundException("Scheme Not Found"));
    }

    @Override
    public MFSchemeDTO getNavOnDate(Long schemeCode, String inputDate) {
        LocalDate adjustedDate = getAdjustedDateForNAV(inputDate);
        return getNavByDate(schemeCode, adjustedDate);
    }

    @Override
    public void fetchSchemeDetails(Long schemeCode) {
        log.info("Fetching SchemeDetails for AMFISchemeCode :{} ", schemeCode);
        URI uri =
                UriComponentsBuilder.fromHttpUrl(AppConstants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                        .build()
                        .toUri();

        ResponseEntity<NavResponse> navResponseResponseEntity =
                this.restTemplate.exchange(uri, HttpMethod.GET, null, NavResponse.class);
        if (navResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
            NavResponse entityBody = navResponseResponseEntity.getBody();
            Assert.notNull(entityBody, () -> "Body Can't be Null");
            MFSchemeEntity mfSchemeEntity =
                    mfSchemesRepository
                            .findBySchemeId(schemeCode)
                            .orElseThrow(
                                    () ->
                                            new SchemeNotFoundException(
                                                    "Fund with schemeCode "
                                                            + schemeCode
                                                            + " Not Found"));
            mergeList(entityBody, mfSchemeEntity);
        }
    }

    @Override
    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        return this.mfSchemesRepository.findBySchemeNameIgnoringCaseLike("%" + schemeName + "%");
    }

    @Override
    public List<FundDetailProjection> fetchSchemesByFundName(String fundName) {
        return this.mfSchemesRepository.findByFundHouseIgnoringCaseLike("%" + fundName + "%");
    }

    private MFSchemeDTO getSchemeDetails(Long schemeCode, LocalDate navDate) {
        log.info("Fetching Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        fetchSchemeDetails(schemeCode);
        log.info("Fetched Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        return this.mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseThrow(() -> new NavNotFoundException("Nav Not Found for given Date"));
    }

    private void mergeList(@NonNull NavResponse navResponse, MFSchemeEntity mfSchemeEntity) {
        List<NAVDataDTO> navList = navResponse.getData();

        List<MFSchemeNavEntity> newNavs =
                navList.stream()
                        .map(conversionServiceAdapter::mapNAVDataDTOToMFSchemeNavEntity)
                        .filter(nav -> !mfSchemeEntity.getMfSchemeNavEntities().contains(nav))
                        .toList();

        if (!newNavs.isEmpty()) {
            for (MFSchemeNavEntity newSchemeNav : newNavs) {
                mfSchemeEntity.addSchemeNav(newSchemeNav);
            }
            final MetaDTO meta = navResponse.getMeta();
            MFSchemeTypeEntity mfschemeTypeEntity =
                    this.mfSchemeTypeRepository
                            .findBySchemeCategoryAndSchemeType(
                                    meta.schemeCategory(), meta.schemeType())
                            .orElseGet(
                                    () -> {
                                        MFSchemeTypeEntity entity = new MFSchemeTypeEntity();
                                        entity.setSchemeType(meta.schemeType());
                                        entity.setSchemeCategory(meta.schemeCategory());
                                        return this.mfSchemeTypeRepository.save(entity);
                                    });
            mfSchemeEntity.setFundHouse(meta.fundHouse());
            mfschemeTypeEntity.addMFScheme(mfSchemeEntity);
            this.mfSchemesRepository.save(mfSchemeEntity);
        }
    }

    private MFSchemeDTO getNavByDate(Long schemeCode, LocalDate navDate) {
        log.info(
                "Fetching Nav for AMFISchemeCode : {} for date : {} from Database",
                schemeCode,
                navDate);
        return this.mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
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
                DateTimeFormatter.ofPattern(AppConstants.DATE_PATTERN_DD_MM_YYYY);
        LocalDate adjustedDate = LocalDate.parse(inputDate, formatter);
        return getAdjustedDate(adjustedDate);
    }

    @Override
    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {
        if (null == asOfDate) {
            asOfDate = LocalDate.now().minusDays(1);
        } else if (asOfDate.isAfter(LocalDate.now())) {
            asOfDate = LocalDate.now().minusDays(1);
        }
        LocalDate finalAsOfDate = asOfDate;
        List<CompletableFuture<PortfolioDetailsDTO>> completableFutureList =
                casDetailsEntityRepository.getPortfolioDetails(panNumber, asOfDate).stream()
                        .map(
                                portfolioDetails ->
                                        CompletableFuture.supplyAsync(
                                                () -> {
                                                    MFSchemeDTO scheme =
                                                            getNavByDate(
                                                                    portfolioDetails.getSchemeId(),
                                                                    getAdjustedDate(finalAsOfDate));
                                                    float totalValue =
                                                            portfolioDetails.getBalanceUnits()
                                                                    * Float.parseFloat(
                                                                            scheme.nav());

                                                    return new PortfolioDetailsDTO(
                                                            totalValue,
                                                            portfolioDetails.getSchemeName(),
                                                            portfolioDetails.getFolioNumber());
                                                }))
                        .toList();

        List<PortfolioDetailsDTO> portfolioDetailsDTOS =
                completableFutureList.stream().map(CompletableFuture::join).toList();

        return new PortfolioResponse(
                portfolioDetailsDTOS.stream()
                        .map(PortfolioDetailsDTO::totalValue)
                        .reduce(0f, Float::sum),
                portfolioDetailsDTOS);
    }

    @Override
    public void loadFundDetailsIfNotSet() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("loadDetails");
        List<Long> distinctSchemeIds = userSchemeDetailsEntityRepository.findDistinctByAmfi();

        for (Long schemeId : distinctSchemeIds) {
            {
                try {
                    fetchSchemeDetails(schemeId);
                } catch (SchemeNotFoundException | NavNotFoundException exception) {
                    log.error(exception.getMessage());
                    ErrorMessageEntity errorMessageEntity = new ErrorMessageEntity();
                    errorMessageEntity.setMessage(exception.getMessage());
                    errorMessageRepository.save(errorMessageEntity);
                }
            }
        }
        stopWatch.stop();
        log.info("Fund House and Scheme Type Set in : {} sec", stopWatch.getTotalTimeSeconds());
    }

    @Override
    public String upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = this.objectMapper.readValue(multipartFile.getBytes(), CasDTO.class);
        UserCASDetailsEntity casDetailsEntity =
                this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
        UserCASDetailsEntity persistedCasDetailsEntity =
                this.casDetailsEntityRepository.save(casDetailsEntity);
        return "Uploaded with id " + persistedCasDetailsEntity.getId();
    }
}
