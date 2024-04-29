package com.learning.mfscreener.service;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.FundDetailProjection;
import com.learning.mfscreener.models.projection.UserFolioDetailsPanProjection;
import com.learning.mfscreener.models.response.NavResponse;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.AppConstants;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Loggable
public class SchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemeService.class);

    private final RestClient restClient;
    private final MFSchemeRepository mfSchemeRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;
    private final UserFolioDetailsService userFolioDetailsService;
    private final ResourceLoader resourceLoader;

    public SchemeService(
            RestClient restClient,
            MFSchemeRepository mfSchemeRepository,
            ConversionServiceAdapter conversionServiceAdapter,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper,
            UserFolioDetailsService userFolioDetailsService,
            ResourceLoader resourceLoader) {
        this.restClient = restClient;
        this.mfSchemeRepository = mfSchemeRepository;
        this.conversionServiceAdapter = conversionServiceAdapter;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
        this.userFolioDetailsService = userFolioDetailsService;
        this.resourceLoader = resourceLoader;
    }

    @Loggable
    @Transactional
    public void fetchSchemeDetails(Long schemeCode) {
        processResponseEntity(schemeCode, getNavResponseResponseEntity(schemeCode));
    }

    @Loggable
    @Transactional
    public void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode) {
        processResponseEntity(newSchemeCode, getNavResponseResponseEntity(Long.valueOf(oldSchemeCode)));
    }

    @Loggable(result = false)
    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String sName = "%" + schemeName.toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes with :{}", sName);
        return this.mfSchemeRepository.findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(sName);
    }

    @Loggable(result = false)
    public List<FundDetailProjection> fetchSchemesByFundName(String fundName) {
        String fName = "%" + fundName.toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes available for fundHouse :{}", fName);
        return this.mfSchemeRepository.findByFundHouseLikeIgnoringCaseOrderBySchemeIdAsc(fName);
    }

    // if panKYC is NOT OK then PAN is not set. hence manually setting it.
    @Loggable
    public void setPANIfNotSet(Long userCasID) {
        // find pan by id
        UserFolioDetailsPanProjection panProjection =
                userFolioDetailsService.findFirstByUserCasIdAndPanKyc(userCasID, "OK");
        int rowsUpdated = userFolioDetailsService.updatePanByCasId(panProjection.getPan(), userCasID);
        LOGGER.debug("Updated {} rows with PAN", rowsUpdated);
    }

    @Loggable(result = false)
    public Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate) {
        return this.mfSchemeRepository
                .findBySchemeIdAndMfSchemeNavEntities_NavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO);
    }

    void processResponseEntity(Long schemeCode, NavResponse navResponse) {
        Optional<MFSchemeEntity> entityBySchemeId = findBySchemeCode(schemeCode);
        if (entityBySchemeId.isEmpty()) {
            // Scenario where scheme is discontinued or merged with other
            LOGGER.error("Found Discontinued SchemeCode : {}", schemeCode);
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

    @Transactional(readOnly = true)
    public Optional<MFSchemeEntity> findByPayOut(String isin) {
        return mfSchemeRepository.findByPayOut(isin);
    }

    @Cacheable(value = "schemeIdByISIN")
    @Transactional(readOnly = true)
    public List<Long> getSchemeIdByISIN(String isin) {
        return mfSchemeRepository.getSchemeIdByISIN(isin);
    }

    @Transactional
    @Loggable(result = false, params = false)
    public MFSchemeEntity saveEntity(MFSchemeEntity mfSchemeEntity) {
        return mfSchemeRepository.save(mfSchemeEntity);
    }

    @Transactional(readOnly = true)
    @Loggable(result = false)
    public Optional<MFSchemeEntity> findBySchemeCode(Long schemeCode) {
        return mfSchemeRepository.findBySchemeId(schemeCode);
    }

    @Transactional(readOnly = true)
    public long count() {
        return mfSchemeRepository.count();
    }

    @Transactional(readOnly = true)
    @Loggable(result = false)
    public List<Long> findAllSchemeIds() {
        return mfSchemeRepository.findAllSchemeIds();
    }

    @Transactional
    @Loggable(result = false, params = false)
    public List<MFSchemeEntity> saveAllEntities(List<MFSchemeEntity> mfSchemeEntityList) {
        return mfSchemeRepository.saveAll(mfSchemeEntityList);
    }

    @Transactional
    public void loadHistoricalDataForClosedOrMergedSchemes() {
        Resource resource = resourceLoader.getResource("classpath:/nav/31Jan2018Navdatadump.csv");
        try {
            Path path = resource.getFile().toPath();
            List<String> lines = Files.lines(path).parallel().toList();
            List<MFSchemeEntity> mfSchemeEntities = lines.stream()
                    .skip(1)
                    .map(csvRow -> {
                        // Split the row , format nav	nav_date	scheme_id	fund_house	scheme_name	pay_out	type	category
                        //	sub_category
                        String[] fields = csvRow.split(",");

                        // Trim and remove quotes
                        for (int i = 0; i < fields.length; i++) {
                            fields[i] = fields[i].strip().replaceAll("^\"+|\"+$", "");
                        }
                        //                        Open Ended Schemes(Debt Scheme - Banking and PSU Fund)
                        String schemeType;
                        if (fields[8].equals("NULL")) {
                            schemeType = fields[6].strip() + "(" + fields[7].strip() + ")";
                        } else {
                            schemeType = fields[6].strip() + "(" + fields[7].strip() + " - " + fields[8].strip() + ")";
                        }
                        return new MFSchemeDTO(
                                fields[3],
                                Long.valueOf(fields[2]),
                                fields[5],
                                fields[4],
                                fields[0],
                                fields[1],
                                schemeType);
                    })
                    .map(mfSchemeDtoToEntityMapper::mapMFSchemeDTOToMFSchemeEntity)
                    .toList();
            List<MFSchemeEntity> persistedEntities = mfSchemeRepository.saveAll(mfSchemeEntities);
            LOGGER.info("Persisted : {} rows", persistedEntities.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
