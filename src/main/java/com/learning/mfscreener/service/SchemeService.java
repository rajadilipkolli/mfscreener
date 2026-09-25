package com.learning.mfscreener.service;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.MFSchemeEntity;
import com.learning.mfscreener.entities.MFSchemeNavEntity;
import com.learning.mfscreener.entities.MFSchemeTypeEntity;
import com.learning.mfscreener.exception.FileNotFoundException;
import com.learning.mfscreener.exception.SchemeNotFoundException;
import com.learning.mfscreener.mapper.MfSchemeDtoToEntityMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.FundDetailProjection;
import com.learning.mfscreener.models.projection.UserFolioDetailsPanProjection;
import com.learning.mfscreener.models.response.NavResponse;
import com.learning.mfscreener.repository.MFSchemeNavEntityRepository;
import com.learning.mfscreener.repository.MFSchemeRepository;
import com.learning.mfscreener.utils.AppConstants;
import com.learning.mfscreener.utils.ColumnParsingUtility;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Loggable
@Transactional(readOnly = true)
public class SchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemeService.class);

    private final RestClient restClient;
    private final MFSchemeRepository mfSchemeRepository;
    private final MFSchemeNavEntityRepository mfSchemeNavEntityRepository;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper;
    private final UserFolioDetailsService userFolioDetailsService;
    private final ResourceLoader resourceLoader;
    private final TransactionTemplate transactionTemplate;

    public SchemeService(
            RestClient restClient,
            MFSchemeRepository mfSchemeRepository,
            MFSchemeNavEntityRepository mfSchemeNavEntityRepository,
            ConversionServiceAdapter conversionServiceAdapter,
            MfSchemeDtoToEntityMapper mfSchemeDtoToEntityMapper,
            UserFolioDetailsService userFolioDetailsService,
            ResourceLoader resourceLoader,
            TransactionTemplate transactionTemplate) {
        this.restClient = restClient;
        this.mfSchemeRepository = mfSchemeRepository;
        this.mfSchemeNavEntityRepository = mfSchemeNavEntityRepository;
        this.conversionServiceAdapter = conversionServiceAdapter;
        this.mfSchemeDtoToEntityMapper = mfSchemeDtoToEntityMapper;
        this.userFolioDetailsService = userFolioDetailsService;
        this.resourceLoader = resourceLoader;
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        this.transactionTemplate = transactionTemplate;
    }

    public void fetchSchemeDetails(Long schemeCode) {
        processResponseEntity(schemeCode, getNavResponseResponseEntity(schemeCode));
    }

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
    public void setPANIfNotSet(Long userCasID) {
        // find pan by id
        UserFolioDetailsPanProjection panProjection =
                userFolioDetailsService.findFirstByUserCasIdAndPanKyc(userCasID, "OK");
        int rowsUpdated = userFolioDetailsService.updatePanByCasId(panProjection.getPan(), userCasID);
        LOGGER.debug("Updated {} rows with PAN for casID :{}", rowsUpdated, userCasID);
    }

    @Loggable(result = false)
    public Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate) {
        return this.mfSchemeNavEntityRepository
                .findNavBySchemeIdAndDate(schemeCode, navDate)
                .map(nav -> {
                    MFSchemeEntity scheme = nav.getMfSchemeEntity();
                    MFSchemeTypeEntity type = scheme.getMfSchemeTypeEntity();
                    String categoryAndSubCategory = type.getCategory();
                    if (StringUtils.hasText(type.getSubCategory())) {
                        categoryAndSubCategory += " - " + type.getSubCategory();
                    }
                    return new MFSchemeDTO(
                            scheme.getFundHouse(),
                            scheme.getSchemeId(),
                            scheme.getPayOut(),
                            scheme.getSchemeName(),
                            nav.getNav().toString(),
                            nav.getNavDate().toString(),
                            type.getType() + "(" + categoryAndSubCategory + ")");
                });
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
        return UriComponentsBuilder.fromUriString(AppConstants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                .build()
                .toUri();
    }

    void mergeList(NavResponse navResponse, MFSchemeEntity mfSchemeEntity, Long schemeCode) {
        Set<LocalDate> existingDates = new HashSet<>(mfSchemeNavEntityRepository.findNavDatesBySchemeId(schemeCode));
        List<MFSchemeNavEntity> newNavs = navResponse.data().stream()
                .map(navDataDTO -> navDataDTO.withSchemeId(schemeCode))
                .map(conversionServiceAdapter::mapNAVDataDTOToMFSchemeNavEntity)
                .filter(nav -> existingDates.add(nav.getNavDate()))
                .toList();
        LOGGER.info(
                "No of entries from Server :{} for schemeCode/amfi :{}",
                navResponse.data().size(),
                schemeCode);
        LOGGER.info("No of entities to insert :{} for schemeCode/amfi :{}", newNavs.size(), schemeCode);

        if (!newNavs.isEmpty()) {
            newNavs.forEach(nav -> nav.setMfSchemeEntity(mfSchemeEntity));
            try {
                transactionTemplate.executeWithoutResult(status -> mfSchemeNavEntityRepository.saveAll(newNavs));
            } catch (ConstraintViolationException | DataIntegrityViolationException exception) {
                LOGGER.error("ConstraintViolationException or DataIntegrityViolationException ", exception);
            }
        }
    }

    public Optional<MFSchemeEntity> findByPayOut(String isin) {
        return mfSchemeRepository.findByPayOut(isin);
    }

    @Cacheable(value = "schemeIdByISIN")
    public List<Long> getSchemeIdByISIN(String isin) {
        return mfSchemeRepository.getSchemeIdByISIN(isin);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Loggable(result = false, params = false)
    public MFSchemeEntity saveEntity(MFSchemeEntity mfSchemeEntity) {
        return mfSchemeRepository.save(mfSchemeEntity);
    }

    @Loggable(result = false)
    public Optional<MFSchemeEntity> findBySchemeCode(Long schemeCode) {
        return mfSchemeRepository.findBySchemeId(schemeCode);
    }

    public long count() {
        return mfSchemeRepository.count();
    }

    @Loggable(result = false)
    public List<Long> findAllSchemeIds() {
        return mfSchemeRepository.findAllSchemeIds();
    }

    @Loggable(result = false, params = false)
    @Transactional
    public List<MFSchemeEntity> saveAllEntities(List<MFSchemeEntity> mfSchemeEntityList) {
        return mfSchemeRepository.saveAll(mfSchemeEntityList);
    }

    /** Loads bundled scheme and NAV data for schemes that have since closed or merged. */
    public void loadHistoricalDataForClosedOrMergedSchemes() {
        Resource resource = resourceLoader.getResource("classpath:/nav/31Jan2018Navdatadump.csv");
        try {
            Path path = resource.getFile().toPath();
            List<String> lines = Files.lines(path).parallel().toList();
            if (lines.isEmpty()) return;
            ColumnParsingUtility utility = new ColumnParsingUtility(lines.get(0), ",");
            List<MFSchemeEntity> mfSchemeEntities = lines.stream()
                    .skip(1)
                    .map(csvRow -> {
                        String[] fields = csvRow.split(",");

                        // Trim and remove quotes
                        for (int i = 0; i < fields.length; i++) {
                            fields[i] = fields[i].strip().replaceAll("^\"+|\"+$", "");
                        }

                        String nav = utility.extractFieldValue(fields, AppConstants.CSV_NAV);
                        String navDate = utility.extractFieldValue(fields, AppConstants.CSV_NAV_DATE);
                        String schemeId = utility.extractFieldValue(fields, AppConstants.CSV_SCHEME_ID);
                        String fundHouse = utility.extractFieldValue(fields, AppConstants.CSV_FUND_HOUSE);
                        String schemeName = utility.extractFieldValue(fields, AppConstants.CSV_SCHEME_NAME);
                        String payOut = utility.extractFieldValue(fields, AppConstants.CSV_PAY_OUT);
                        String type = utility.extractFieldValue(fields, AppConstants.CSV_TYPE);
                        String category = utility.extractFieldValue(fields, AppConstants.CSV_CATEGORY);
                        String subCategory = utility.extractFieldValue(fields, AppConstants.CSV_SUB_CATEGORY);

                        String schemeType;
                        if ("NULL".equals(subCategory)) {
                            schemeType = (type != null ? type.strip() : "") + "("
                                    + (category != null ? category.strip() : "") + ")";
                        } else {
                            schemeType = (type != null ? type.strip() : "") + "("
                                    + (category != null ? category.strip() : "") + " - "
                                    + (subCategory != null ? subCategory.strip() : "") + ")";
                        }
                        return new MFSchemeDTO(
                                fundHouse,
                                schemeId != null ? Long.valueOf(schemeId) : null,
                                payOut,
                                schemeName,
                                nav,
                                navDate,
                                schemeType);
                    })
                    .map(mfSchemeDtoToEntityMapper::mapMFSchemeDTOToMFSchemeEntity)
                    .toList();
            List<MFSchemeEntity> persistedEntities =
                    transactionTemplate.execute(status -> mfSchemeRepository.saveAll(mfSchemeEntities));
            Assert.notNull(persistedEntities, () -> "persistedEntities cant be null");
            LOGGER.info("Persisted : {} rows", persistedEntities.size());
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("DataIntegrityViolationException occurred ", e);
        }
    }
}
