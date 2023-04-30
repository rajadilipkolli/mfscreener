/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.service.impl;

import com.example.mfscreener.adapter.ConversionServiceAdapter;
import com.example.mfscreener.entities.ErrorMessageEntity;
import com.example.mfscreener.entities.MFSchemeEntity;
import com.example.mfscreener.entities.MFSchemeNavEntity;
import com.example.mfscreener.entities.MFSchemeTypeEntity;
import com.example.mfscreener.entities.UserCASDetailsEntity;
import com.example.mfscreener.entities.UserFolioDetailsEntity;
import com.example.mfscreener.entities.UserSchemeDetailsEntity;
import com.example.mfscreener.entities.UserTransactionDetailsEntity;
import com.example.mfscreener.exception.NavNotFoundException;
import com.example.mfscreener.exception.SchemeNotFoundException;
import com.example.mfscreener.models.CasDTO;
import com.example.mfscreener.models.MFSchemeDTO;
import com.example.mfscreener.models.MetaDTO;
import com.example.mfscreener.models.NAVDataDTO;
import com.example.mfscreener.models.PortfolioDetailsDTO;
import com.example.mfscreener.models.UserFolioDTO;
import com.example.mfscreener.models.UserSchemeDTO;
import com.example.mfscreener.models.UserTransactionDTO;
import com.example.mfscreener.models.projection.FundDetailProjection;
import com.example.mfscreener.models.response.NavResponse;
import com.example.mfscreener.models.response.PortfolioResponse;
import com.example.mfscreener.repository.CASDetailsEntityRepository;
import com.example.mfscreener.repository.ErrorMessageRepository;
import com.example.mfscreener.repository.InvestorInfoEntityRepository;
import com.example.mfscreener.repository.MFSchemeRepository;
import com.example.mfscreener.repository.MFSchemeTypeRepository;
import com.example.mfscreener.repository.UserSchemeDetailsEntityRepository;
import com.example.mfscreener.service.NavService;
import com.example.mfscreener.utils.AppConstants;
import com.example.mfscreener.utils.LocalDateUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final InvestorInfoEntityRepository investorInfoEntityRepository;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final ConversionServiceAdapter conversionServiceAdapter;

    @Override
    public MFSchemeDTO getNav(Long schemeCode) {
        return mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, LocalDateUtility.getAdjustedDate(LocalDate.now()))
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseThrow(() -> new SchemeNotFoundException("Scheme Not Found"));
    }

    @Override
    public MFSchemeDTO getNavOnDate(Long schemeCode, String inputDate) {
        LocalDate adjustedDate = LocalDateUtility.getAdjustedDateForNAV(inputDate);
        return getNavByDate(schemeCode, adjustedDate);
    }

    @Override
    public void fetchSchemeDetails(Long schemeCode) {
        log.info("Fetching SchemeDetails for AMFISchemeCode :{} ", schemeCode);
        URI uri = UriComponentsBuilder.fromHttpUrl(AppConstants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                .build()
                .toUri();

        ResponseEntity<NavResponse> navResponseResponseEntity =
                this.restTemplate.exchange(uri, HttpMethod.GET, null, NavResponse.class);
        if (navResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
            NavResponse entityBody = navResponseResponseEntity.getBody();
            Assert.notNull(entityBody, () -> "Body Can't be Null");
            MFSchemeEntity mfSchemeEntity = mfSchemesRepository
                    .findBySchemeId(schemeCode)
                    .orElseThrow(
                            () -> new SchemeNotFoundException("Fund with schemeCode " + schemeCode + " Not Found"));
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

    MFSchemeDTO getSchemeDetails(Long schemeCode, LocalDate navDate) {
        log.info("Fetching Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        fetchSchemeDetails(schemeCode);
        log.info("Fetched Nav for SchemeCode :{} for date :{} from Server", schemeCode, navDate);
        return this.mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseThrow(() -> new NavNotFoundException("Nav Not Found for given Date"));
    }

    void mergeList(@NonNull NavResponse navResponse, MFSchemeEntity mfSchemeEntity) {
        List<NAVDataDTO> navList = navResponse.getData();

        List<MFSchemeNavEntity> newNavs = navList.stream()
                .map(conversionServiceAdapter::mapNAVDataDTOToMFSchemeNavEntity)
                .filter(nav -> !mfSchemeEntity.getMfSchemeNavEntities().contains(nav))
                .toList();

        if (!newNavs.isEmpty()) {
            for (MFSchemeNavEntity newSchemeNav : newNavs) {
                mfSchemeEntity.addSchemeNav(newSchemeNav);
            }
            final MetaDTO meta = navResponse.getMeta();
            MFSchemeTypeEntity mfschemeTypeEntity = this.mfSchemeTypeRepository
                    .findBySchemeCategoryAndSchemeType(meta.schemeCategory(), meta.schemeType())
                    .orElseGet(() -> {
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

    MFSchemeDTO getNavByDate(Long schemeCode, LocalDate navDate) {
        log.info("Fetching Nav for AMFISchemeCode : {} for date : {} from Database", schemeCode, navDate);
        return this.mfSchemesRepository
                .findBySchemeIdAndNavDate(schemeCode, navDate)
                .map(conversionServiceAdapter::mapMFSchemeEntityToMFSchemeDTO)
                .orElseGet(() -> getSchemeDetails(schemeCode, navDate));
    }

    @Override
    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {
        if (asOfDate.isAfter(LocalDate.now())) {
            asOfDate = LocalDate.now().minusDays(1);
        }
        LocalDate finalAsOfDate = asOfDate;
        List<CompletableFuture<PortfolioDetailsDTO>> completableFutureList =
                casDetailsEntityRepository.getPortfolioDetails(panNumber, asOfDate).stream()
                        .filter(portfolioDetailsProjection -> portfolioDetailsProjection.getSchemeId() != null)
                        .map(portfolioDetails -> CompletableFuture.supplyAsync(() -> {
                            MFSchemeDTO scheme = getNavByDate(
                                    portfolioDetails.getSchemeId(), LocalDateUtility.getAdjustedDate(finalAsOfDate));
                            float totalValue = portfolioDetails.getBalanceUnits() * Float.parseFloat(scheme.nav());

                            return new PortfolioDetailsDTO(
                                    totalValue, portfolioDetails.getSchemeName(), portfolioDetails.getFolioNumber());
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
        // check if user and email exits
        String email = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();
        UserCASDetailsEntity casDetailsEntity;
        if (folioExistsInDB(email, name)) {
            casDetailsEntity = findDelta(email, name, casDTO);
        } else {
            casDetailsEntity = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
        }
        var persistedCasDetailsEntity = this.casDetailsEntityRepository.save(casDetailsEntity);

        return "Uploaded with id " + persistedCasDetailsEntity.getId();
    }

    protected UserCASDetailsEntity findDelta(String email, String name, CasDTO casDTO) {
        UserCASDetailsEntity userCASDetailsEntity =
                this.casDetailsEntityRepository.findByInvestorInfoEntity_EmailAndInvestorInfoEntity_Name(email, name);
        // get Entities present in DB
        List<UserFolioDetailsEntity> userFolioDetailsEntities = userCASDetailsEntity.getFolioEntities();
        if (userFolioDetailsEntities.size() == casDTO.folios().size()) {
            // as both sizes are equal check for schemeNames in each folio
            for (UserFolioDetailsEntity userFolioDetailsEntity : userFolioDetailsEntities) {
                String folio = userFolioDetailsEntity.getFolio();
                for (UserFolioDTO folioFromJson : casDTO.folios()) {
                    if (folio.equals(folioFromJson.folio())) {
                        // as folio is same, check schemes
                        if (userFolioDetailsEntity.getSchemeEntities().size()
                                == folioFromJson.schemes().size()) {
                            // as schemes are same no new scheme was added, check for new transactions
                            for (UserSchemeDetailsEntity schemeFromDb : userFolioDetailsEntity.getSchemeEntities()) {
                                String schemeName = schemeFromDb.getScheme();
                                for (UserSchemeDTO schemeFromJson : folioFromJson.schemes()) {
                                    if (schemeName.equals(schemeFromJson.scheme())) {
                                        // Scheme Matched, hence check transaction size
                                        if (schemeFromDb
                                                        .getTransactionEntities()
                                                        .size()
                                                != schemeFromJson.transactions().size()) {
                                            List<UserTransactionDTO> userTransactionDTOS = findNewTransactions(
                                                    schemeFromDb.getTransactionEntities(),
                                                    schemeFromJson.transactions());
                                            userTransactionDTOS.forEach(
                                                    userTransactionDTO -> schemeFromDb.addTransactionEntity(
                                                            conversionServiceAdapter
                                                                    .mapUserTransactionDTOToUserTransactionDetailsEntity(
                                                                            userTransactionDTO)));
                                        } else {
                                            log.debug("No new transactions for scheme");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // check if new folio is added then it should be automatically added to object from db
            for (UserFolioDTO folioFromJson : casDTO.folios()) {
                String folio = folioFromJson.folio();
                for (UserFolioDetailsEntity userFolioDetailsEntity : userFolioDetailsEntities) {
                    if (folio.equals(userFolioDetailsEntity.getFolio())) {
                        // as folio is same, check schemes
                        if (userFolioDetailsEntity.getSchemeEntities().size()
                                == folioFromJson.schemes().size()) {
                            // as schemes are same no new scheme was added, check for new transactions
                            for (UserSchemeDetailsEntity schemeFromDb : userFolioDetailsEntity.getSchemeEntities()) {
                                String schemeName = schemeFromDb.getScheme();
                                for (UserSchemeDTO schemeFromJson : folioFromJson.schemes()) {
                                    if (schemeName.equals(schemeFromJson.scheme())) {
                                        // Scheme Matched, hence check transaction size
                                        if (schemeFromDb
                                                        .getTransactionEntities()
                                                        .size()
                                                != schemeFromJson.transactions().size()) {
                                            List<UserTransactionDTO> userTransactionDTOS = findNewTransactions(
                                                    schemeFromDb.getTransactionEntities(),
                                                    schemeFromJson.transactions());
                                            userTransactionDTOS.forEach(
                                                    userTransactionDTO -> schemeFromDb.addTransactionEntity(
                                                            conversionServiceAdapter
                                                                    .mapUserTransactionDTOToUserTransactionDetailsEntity(
                                                                            userTransactionDTO)));
                                        } else {
                                            log.debug("No new transactions for scheme");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // for each existing folio check if new scheme is added then add to object from db

        // for each scheme check the new transactions added.
        return userCASDetailsEntity;
    }

    private List<UserTransactionDTO> findNewTransactions(
            List<UserTransactionDetailsEntity> transactionEntities, List<UserTransactionDTO> transactions) {

        List<UserTransactionDTO> userTransactionDTOS = new ArrayList<>();
        for (UserTransactionDTO userTransactionDTO : transactions) {
            LocalDate newDate = userTransactionDTO.date();
            String type = userTransactionDTO.type();
            Double units = userTransactionDTO.units();
            for (UserTransactionDetailsEntity userTransactionDetails : transactionEntities) {
                if (newDate.isEqual(userTransactionDetails.getTransactionDate())
                        && type.equals(userTransactionDetails.getType())
                        && Objects.equals(units, userTransactionDetails.getUnits())) {
                    break;
                } else {
                    userTransactionDTOS.add(userTransactionDTO);
                }
            }
        }
        return userTransactionDTOS;
    }

    private boolean folioExistsInDB(String email, String name) {
        return this.investorInfoEntityRepository.existsByEmailAndName(email, name);
    }
}
