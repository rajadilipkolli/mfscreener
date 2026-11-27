package com.learning.mfscreener.service;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.mapper.CasDetailsMapper;
import com.learning.mfscreener.models.PortfolioDetailsDTO;
import com.learning.mfscreener.models.entityviews.UserCASDetailsEntityView;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.response.PortfolioResponse;
import com.learning.mfscreener.models.response.ProcessCasResponse;
import com.learning.mfscreener.models.response.UploadResponseHolder;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Loggable
@Transactional(readOnly = true)
public class PortfolioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioService.class);

    private final ConversionServiceAdapter conversionServiceAdapter;
    private final CasDetailsMapper casDetailsMapper;

    private final SchemeService schemeService;
    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserSchemeDetailsService userSchemeDetailsService;
    private final PortfolioServiceHelper portfolioServiceHelper;
    private final UserCASDetailsService userCASDetailsService;
    private final InvestorInfoService investorInfoService;
    private final CapitalGainsService capitalGainsService;

    public PortfolioService(
            ConversionServiceAdapter conversionServiceAdapter,
            CasDetailsMapper casDetailsMapper,
            UserTransactionDetailsService userTransactionDetailsService,
            SchemeService schemeService,
            UserSchemeDetailsService userSchemeDetailsService,
            PortfolioServiceHelper portfolioServiceHelper,
            UserCASDetailsService userCASDetailsService,
            InvestorInfoService investorInfoService,
            CapitalGainsService capitalGainsService) {
        this.conversionServiceAdapter = conversionServiceAdapter;
        this.casDetailsMapper = casDetailsMapper;
        this.userCASDetailsService = userCASDetailsService;
        this.schemeService = schemeService;
        this.userSchemeDetailsService = userSchemeDetailsService;
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.portfolioServiceHelper = portfolioServiceHelper;
        this.investorInfoService = investorInfoService;
        this.capitalGainsService = capitalGainsService;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ProcessCasResponse upload(MultipartFile portfolioFile) throws IOException {
        CasDTO casDTO = parseCasDTO(portfolioFile);
        String response = processCasDTO(casDTO);
        ProcessCasResponse processCasResponse = capitalGainsService.processData(casDTO);
        return processCasResponse.withImportSummary(response);
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate evaluationDate) {
        List<PortfolioDetailsDTO> portfolioDetailsDTOList = portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(
                panNumber, LocalDateUtility.getAdjustedDateOrDefault(evaluationDate));
        Double totalPortfolioValue = portfolioDetailsDTOList.stream()
                .map(PortfolioDetailsDTO::totalValue)
                .reduce((double) 0, Double::sum);
        return new PortfolioResponse(Math.round(totalPortfolioValue * 100.0) / 100.0, portfolioDetailsDTOList);
    }

    CasDTO parseCasDTO(MultipartFile casFile) throws IOException {
        return portfolioServiceHelper.readValue(casFile.getBytes(), CasDTO.class);
    }

    String processCasDTO(CasDTO casDTO) {
        String email = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();
        UserCASDetailsEntity userCASDetailsEntity = null;
        int folioCount = 0;
        long transactionCount = 0;
        if (this.investorInfoService.existsByEmailAndName(email, name)) {
            var holder = findDelta(email, name, casDTO);
            if (holder != null) {
                userCASDetailsEntity = holder.userCASDetailsEntity();
                folioCount = holder.folioCount();
                transactionCount = holder.transactionsCount();
            }
        } else {
            userCASDetailsEntity = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
            folioCount = userCASDetailsEntity.getFolioEntities().size();
            transactionCount = userCASDetailsEntity.getFolioEntities().stream()
                    .flatMap(f -> f.getSchemeEntities().stream())
                    .mapToLong(s -> s.getTransactionEntities().size())
                    .sum();
        }
        return finalizeUpload(userCASDetailsEntity, folioCount, transactionCount);
    }

    String finalizeUpload(UserCASDetailsEntity userCASDetailsEntity, int folioCount, long transactionCount) {
        if (userCASDetailsEntity != null) {
            UserCASDetailsEntity savedCasDetailsEntity = this.userCASDetailsService.saveEntity(userCASDetailsEntity);
            CompletableFuture.runAsync(() -> schemeService.setPANIfNotSet(savedCasDetailsEntity.getId()));
            CompletableFuture.runAsync(userSchemeDetailsService::setAMFIIfNull);
            return "Imported %d folios and %d transactions".formatted(folioCount, transactionCount);
        } else {
            return "Nothing to Update";
        }
    }

    UploadResponseHolder findDelta(String email, String name, CasDTO casDTO) {
        List<UserFolioDTO> inputUserFolioDTOList = casDTO.folios();
        long userTransactionCount = portfolioServiceHelper.countTransactionsByUserFolioDTOList(inputUserFolioDTOList);
        List<UserTransactionDetailsEntity> userTransactionDetailsEntityList =
                this.userTransactionDetailsService.findAllTransactionsByEmailAndName(email, name);
        if (userTransactionCount == userTransactionDetailsEntityList.size()) {
            LOGGER.info("No new transactions are added");
            return null;
        }

        UserCASDetailsEntityView userCASDetailsEntityView =
                userCASDetailsService.findByInvestorEmailAndName(email, name);
        return processFoliosAndTransactions(
                casDTO, userCASDetailsEntityView, userTransactionCount, userTransactionDetailsEntityList);
    }

    UploadResponseHolder processFoliosAndTransactions(
            CasDTO casDTO,
            UserCASDetailsEntityView userCASDetailsEntityView,
            long userTransactionDTOListCount,
            List<UserTransactionDetailsEntity> userTransactionDetailsEntityList) {
        AtomicInteger folioCounter = new AtomicInteger();
        AtomicInteger transactionsCounter = new AtomicInteger();
        UserCASDetailsEntity userCASDetailsEntity =
                userCASDetailsService.findEntityById(userCASDetailsEntityView.getId());

        processNewFolios(
                casDTO.folios(), userCASDetailsEntityView, userCASDetailsEntity, folioCounter, transactionsCounter);
        updateSchemesAndTransactions(
                casDTO,
                userCASDetailsEntity,
                userTransactionDTOListCount,
                userTransactionDetailsEntityList,
                transactionsCounter);

        return new UploadResponseHolder(userCASDetailsEntity, folioCounter.get(), transactionsCounter.get());
    }

    void updateSchemesAndTransactions(
            CasDTO casDTO,
            UserCASDetailsEntity userCASDetailsEntity,
            long userTransactionDTOListCount,
            List<UserTransactionDetailsEntity> userTransactionDetailsEntityList,
            AtomicInteger transactionsCounter) {
        // Check if all new transactions are added as part of adding folios
        if (userTransactionDTOListCount == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
            LOGGER.info("All new transactions are added as part of adding folios, hence skipping");
        } else {
            // New schemes or transactions are added

            // Grouping by folio for requestedFolioSchemesMap
            Map<String, List<UserSchemeDTO>> requestedFolioSchemesMap = groupSchemesByFolio(casDTO.folios());

            // Grouping by folio for existingFolioSchemesMap
            List<UserFolioDetailsEntity> existingUserFolioDetailsEntityList = userCASDetailsEntity.getFolioEntities();
            Map<String, List<UserSchemeDetailsEntity>> existingFolioSchemesMap =
                    groupExistingSchemesByEmailAndName(existingUserFolioDetailsEntityList);

            // Update schemes in existing folios
            updateExistingFoliosWithNewSchemes(
                    requestedFolioSchemesMap,
                    existingFolioSchemesMap,
                    existingUserFolioDetailsEntityList,
                    transactionsCounter);
            // Check if all new transactions are added as part of adding schemes
            if (userTransactionDTOListCount == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
                LOGGER.info("All new transactions are added as part of adding schemes, hence skipping");
            } else {
                processNewTransactions(
                        transactionsCounter, requestedFolioSchemesMap, existingUserFolioDetailsEntityList);
            }
        }
    }

    void processNewTransactions(
            AtomicInteger transactionsCounter,
            Map<String, List<UserSchemeDTO>> requestedFolioSchemes,
            List<UserFolioDetailsEntity> existingFolios) {
        for (UserFolioDetailsEntity folio : existingFolios) {
            Map<String, List<UserTransactionDTO>> requestedTransactionsByIsin =
                    requestedFolioSchemes.getOrDefault(folio.getFolio(), List.of()).stream()
                            .filter(scheme -> scheme.isin() != null)
                            .collect(groupingBy(
                                    UserSchemeDTO::isin,
                                    flatMapping(scheme -> scheme.transactions().stream(), toList())));

            for (UserSchemeDetailsEntity scheme : folio.getSchemeEntities()) {
                List<UserTransactionDTO> requestTransactions = requestedTransactionsByIsin.get(scheme.getIsin());
                List<UserTransactionDetailsEntity> existingTransactions = scheme.getTransactionEntities();
                if (requestTransactions == null || requestTransactions.size() == existingTransactions.size()) {
                    continue;
                }

                List<LocalDate> existingDates = existingTransactions.stream()
                        .map(UserTransactionDetailsEntity::getTransactionDate)
                        .toList();
                for (UserTransactionDTO transaction : requestTransactions) {
                    if (!existingDates.contains(transaction.date())) {
                        LOGGER.info(
                                "New transaction on date {} for folio {} and ISIN {}",
                                transaction.date(),
                                folio.getFolio(),
                                scheme.getIsin());
                        scheme.addTransactionEntity(casDetailsMapper.transactionDTOToTransactionEntity(transaction));
                        transactionsCounter.incrementAndGet();
                    }
                }
            }
        }
    }

    void updateExistingFoliosWithNewSchemes(
            Map<String, List<UserSchemeDTO>> requestedFolioSchemesMap,
            Map<String, List<UserSchemeDetailsEntity>> existingFolioSchemesMap,
            List<UserFolioDetailsEntity> existingUserFolioDetailsEntityList,
            AtomicInteger transactionsCounter) {
        requestedFolioSchemesMap.forEach((folioFromRequest, requestSchemes) -> {
            List<UserSchemeDetailsEntity> existingSchemesFromDB =
                    existingFolioSchemesMap.getOrDefault(folioFromRequest, new ArrayList<>());
            if (requestSchemes.size() != existingSchemesFromDB.size()) {
                // New schemes added to folio
                List<String> isInListDB = existingSchemesFromDB.stream()
                        .map(UserSchemeDetailsEntity::getIsin)
                        .toList();
                requestSchemes.forEach(userSchemeDTO -> {
                    if (!isInListDB.contains(userSchemeDTO.isin())) {
                        LOGGER.info(
                                "New ISIN: {} created for folio : {} that is not present in the database",
                                userSchemeDTO.isin(),
                                folioFromRequest);
                        UserSchemeDetailsEntity userSchemeDetailsEntity =
                                casDetailsMapper.schemeDTOToSchemeEntity(userSchemeDTO);

                        existingUserFolioDetailsEntityList.forEach(userFolioDetailsEntity -> {
                            if (folioFromRequest.equals(userFolioDetailsEntity.getFolio())) {
                                userFolioDetailsEntity.addSchemeEntity(userSchemeDetailsEntity);
                                int newTransactions =
                                        userSchemeDTO.transactions().size();
                                transactionsCounter.addAndGet(newTransactions);
                            }
                        });
                    }
                });
            }
        });
    }

    Map<String, List<UserSchemeDetailsEntity>> groupExistingSchemesByEmailAndName(
            List<UserFolioDetailsEntity> existingUserFolioDetailsEntityList) {

        return existingUserFolioDetailsEntityList.stream()
                .collect(groupingBy(
                        UserFolioDetailsEntity::getFolio,
                        flatMapping(
                                userFolioDetailsEntity -> userFolioDetailsEntity.getSchemeEntities().stream(),
                                toList())));
    }

    Map<String, List<UserSchemeDTO>> groupSchemesByFolio(List<UserFolioDTO> userFolioDTOList) {
        return userFolioDTOList.stream()
                .collect(groupingBy(UserFolioDTO::folio, flatMapping(folio -> folio.schemes().stream(), toList())));
    }

    void processNewFolios(
            List<UserFolioDTO> userFolioDTOList,
            UserCASDetailsEntityView userCASDetailsEntityView,
            UserCASDetailsEntity userCASDetailsEntity,
            AtomicInteger folioCounter,
            AtomicInteger transactionsCounter) {
        // Logic to process new folios
        List<String> existingFolioNumbers = userCASDetailsEntityView.getFolioEntities().stream()
                .map(folio -> folio.getFolio())
                .toList();

        userFolioDTOList.forEach(userFolioDTO -> {
            String folio = userFolioDTO.folio();
            if (!existingFolioNumbers.contains(folio)) {
                LOGGER.info("New folio: {} created that is not present in the database", folio);
                userCASDetailsEntity.addFolioEntity(
                        casDetailsMapper.mapUserFolioDTOToUserFolioDetailsEntity(userFolioDTO));
                folioCounter.incrementAndGet();
                int newTransactions = userFolioDTO.schemes().stream()
                        .map(UserSchemeDTO::transactions)
                        .flatMap(List::stream)
                        .toList()
                        .size();
                transactionsCounter.addAndGet(newTransactions);
            }
        });
    }
}
