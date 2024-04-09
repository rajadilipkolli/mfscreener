package com.learning.mfscreener.service;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.FundDetailProjection;
import com.learning.mfscreener.models.projection.SchemeNameAndISIN;
import com.learning.mfscreener.models.projection.UserFolioDetailsPanProjection;
import com.learning.mfscreener.models.response.NavResponse;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.repository.UserFolioDetailsEntityRepository;
import com.learning.mfscreener.repository.UserSchemeDetailsEntityRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemeService.class);

    private final RestClient restClient;
    private final MFSchemeRepository mfSchemeRepository;
    private final UserFolioDetailsEntityRepository userFolioDetailsEntityRepository;
    private final UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;

    public SchemeService(
            RestClient restClient,
            MFSchemeRepository mfSchemeRepository,
            UserFolioDetailsEntityRepository userFolioDetailsEntityRepository,
            UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository,
            ConversionServiceAdapter conversionServiceAdapter) {
        this.restClient = restClient;
        this.mfSchemeRepository = mfSchemeRepository;
        this.userFolioDetailsEntityRepository = userFolioDetailsEntityRepository;
        this.userSchemeDetailsEntityRepository = userSchemeDetailsEntityRepository;
        this.conversionServiceAdapter = conversionServiceAdapter;
    }

    @Loggable
    public void fetchSchemeDetails(Long schemeCode) {
        processResponseEntity(schemeCode, getNavResponseResponseEntity(schemeCode));
    }

    @Loggable
    public void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode) {
        processResponseEntity(newSchemeCode, getNavResponseResponseEntity(Long.valueOf(oldSchemeCode)));
    }

    @Loggable
    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String sName = "%" + schemeName.toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes with :{}", sName);
        return this.mfSchemeRepository.findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(sName);
    }

    @Loggable
    public List<FundDetailProjection> fetchSchemesByFundName(String fundName) {
        String fName = "%" + fundName.toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes available for fundHouse :{}", fName);
        return this.mfSchemeRepository.findByFundHouseLikeIgnoringCaseOrderBySchemeIdAsc(fName);
    }

    @Loggable
    public void setAMFIIfNull() {
        List<UserSchemeDetailsEntity> userSchemeDetailsEntities = userSchemeDetailsEntityRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(userSchemeDetailsEntity -> {
            String scheme = userSchemeDetailsEntity.getScheme();
            LOGGER.info("amfi is Null for scheme :{}", scheme);
            // attempting to find ISIN
            if (scheme.contains("ISIN:")) {
                String isin = scheme.substring(scheme.lastIndexOf("ISIN:") + 5).strip();
                if (StringUtils.hasText(isin)) {
                    Optional<MFSchemeEntity> mfSchemeEntity = mfSchemeRepository.findByPayOut(isin);
                    mfSchemeEntity.ifPresent(schemeEntity -> userSchemeDetailsEntityRepository.updateAmfiAndIsinById(
                            schemeEntity.getSchemeId(), isin, userSchemeDetailsEntity.getId()));
                }
            }
        });
    }

    @Loggable
    public List<UserSchemeDetailsEntity> getSchemesByEmailAndName(String email, String name) {
        return this.userSchemeDetailsEntityRepository.findByUserEmailAndName(email, name);
    }

    // if panKYC is NOT OK then PAN is not set. hence manually setting it.
    @Loggable
    public void setPANIfNotSet(Long userCasID) {
        // find pan by id
        UserFolioDetailsPanProjection panProjection =
                userFolioDetailsEntityRepository.findFirstByUserCasDetailsEntity_IdAndPanKyc(userCasID, "OK");
        userFolioDetailsEntityRepository.updatePanByCasId(panProjection.getPan(), userCasID);
    }

    @Loggable
    public Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate) {
        return this.mfSchemeRepository
                .findBySchemeIdAndMfSchemeNavEntities_NavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO);
    }

    @Loggable
    public void loadHistoricalDataIfNotExists() {
        List<Long> historicalDataNotLoadedSchemeIdList =
                userSchemeDetailsEntityRepository.getHistoricalDataNotLoadedSchemeIdList();
        if (!historicalDataNotLoadedSchemeIdList.isEmpty()) {
            List<CompletableFuture<Void>> allSchemesWhereHistoricalDetailsNotLoadedCf =
                    historicalDataNotLoadedSchemeIdList.stream()
                            .map(schemeId -> CompletableFuture.runAsync(() -> fetchSchemeDetails(schemeId)))
                            .toList();
            CompletableFuture.allOf(allSchemesWhereHistoricalDetailsNotLoadedCf.toArray(new CompletableFuture<?>[0]))
                    .join();
            LOGGER.info("Loaded loadHistoricalDataIfNotExists");
        }
    }

    void processResponseEntity(Long schemeCode, NavResponse navResponse) {
        Optional<MFSchemeEntity> entityBySchemeId = mfSchemeRepository.findBySchemeId(schemeCode);
        if (entityBySchemeId.isEmpty()) {
            // Scenario where scheme is discontinued or merged with other
            SchemeNameAndISIN firstByAmfi = userSchemeDetailsEntityRepository
                    .findFirstByAmfi(schemeCode)
                    .orElseThrow(
                            () -> new SchemeNotFoundException("Fund with schemeCode " + schemeCode + " Not Found"));
            String isin = firstByAmfi.getIsin();
            LOGGER.error("Found Discontinued IsIn : {}", isin);
        } else {
            mergeList(navResponse, entityBySchemeId.get(), schemeCode);
        }
    }

    NavResponse getNavResponseResponseEntity(Long schemeCode) {
        return this.restClient
                .get()
                .uri(getUri(schemeCode))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new SchemeNotFoundException("scheme with id %d not found".formatted(schemeCode));
                })
                .body(NavResponse.class);
    }

    URI getUri(Long schemeCode) {
        LOGGER.info("Fetching SchemeDetails for AMFISchemeCode :{} ", schemeCode);
        return UriComponentsBuilder.fromHttpUrl(AppConstants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                .build()
                .toUri();
    }

    void mergeList(NavResponse navResponse, MFSchemeEntity mfSchemeEntity, Long schemeCode) {
        if (navResponse.data().size() != mfSchemeEntity.getMfSchemeNavEntities().size()) {
            List<MFSchemeNavEntity> navList = navResponse.data().stream()
                    .map(navDataDTO -> navDataDTO.withSchemeId(schemeCode))
                    .map(conversionServiceAdapter::mapNAVDataDTOToMFSchemeNavEntity)
                    .toList();
            LOGGER.info("No of entries from Server :{} for schemeCode/amfi :{}", navList.size(), schemeCode);
            List<MFSchemeNavEntity> newNavs = navList.stream()
                    .filter(nav -> !mfSchemeEntity.getMfSchemeNavEntities().contains(nav))
                    .toList();

            LOGGER.info("No of entities to insert :{} for schemeCode/amfi :{}", newNavs.size(), schemeCode);

            if (!newNavs.isEmpty()) {
                for (MFSchemeNavEntity newSchemeNav : newNavs) {
                    mfSchemeEntity.addSchemeNav(newSchemeNav);
                }
                try {
                    this.mfSchemeRepository.save(mfSchemeEntity);
                } catch (ConstraintViolationException | DataIntegrityViolationException exception) {
                    LOGGER.error("ConstraintViolationException or DataIntegrityViolationException ", exception);
                }
            }
        } else {
            LOGGER.info("data in db and from service is same hence ignoring");
        }
    }
}
