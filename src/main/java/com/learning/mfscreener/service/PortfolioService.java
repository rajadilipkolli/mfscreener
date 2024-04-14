package com.learning.mfscreener.service;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.mapper.CasDetailsMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.PortfolioDetailsDTO;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.response.PortfolioResponse;
import com.learning.mfscreener.models.response.UploadResponseHolder;
import com.learning.mfscreener.repository.InvestorInfoEntityRepository;
import com.learning.mfscreener.repository.UserCASDetailsEntityRepository;
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
import org.springframework.web.multipart.MultipartFile;

@Service
public class PortfolioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioService.class);

    private final ObjectMapper objectMapper;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final CasDetailsMapper casDetailsMapper;
    private final UserCASDetailsEntityRepository casDetailsEntityRepository;
    private final InvestorInfoEntityRepository investorInfoEntityRepository;
    private final NavService navService;
    private final SchemeService schemeService;
    private final XIRRCalculatorService xIRRCalculatorService;
    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserFolioDetailsService userFolioDetailsService;
    private final UserSchemeDetailsService userSchemeDetailsService;

    public PortfolioService(
            ObjectMapper objectMapper,
            ConversionServiceAdapter conversionServiceAdapter,
            CasDetailsMapper casDetailsMapper,
            UserCASDetailsEntityRepository casDetailsEntityRepository,
            InvestorInfoEntityRepository investorInfoEntityRepository,
            UserTransactionDetailsService userTransactionDetailsService,
            NavService navService,
            SchemeService schemeService,
            XIRRCalculatorService xIRRCalculatorService,
            UserFolioDetailsService userFolioDetailsService,
            UserSchemeDetailsService userSchemeDetailsService) {
        this.objectMapper = objectMapper;
        this.conversionServiceAdapter = conversionServiceAdapter;
        this.casDetailsMapper = casDetailsMapper;
        this.casDetailsEntityRepository = casDetailsEntityRepository;
        this.investorInfoEntityRepository = investorInfoEntityRepository;
        this.navService = navService;
        this.schemeService = schemeService;
        this.xIRRCalculatorService = xIRRCalculatorService;
        this.userFolioDetailsService = userFolioDetailsService;
        this.userSchemeDetailsService = userSchemeDetailsService;
        this.userTransactionDetailsService = userTransactionDetailsService;
    }

    CasDTO parseCasDTO(MultipartFile multipartFile) throws IOException {
        return this.objectMapper.readValue(multipartFile.getBytes(), CasDTO.class);
    }

    public String upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = parseCasDTO(multipartFile);
        // check if user and email exits
        String email = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();

        if (this.investorInfoEntityRepository.existsByEmailAndName(email, name)) {
            return handleExistingUser(email, name, casDTO);
        } else {
            return handleNewUser(casDTO);
        }
    }

    String handleNewUser(CasDTO casDTO) {
        UserCASDetailsEntity casDetailsEntity = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
        long transactions = countTransactions(casDetailsEntity.getFolioEntities());
        int folios = casDetailsEntity.getFolioEntities().size();
        UserCASDetailsEntity savedCasDetailsEntity = this.casDetailsEntityRepository.save(casDetailsEntity);
        CompletableFuture.runAsync(() -> schemeService.setPANIfNotSet(savedCasDetailsEntity.getId()));
        CompletableFuture.runAsync(userSchemeDetailsService::setAMFIIfNull);
        return formatResponse(folios, transactions);
    }

    String handleExistingUser(String email, String name, CasDTO casDTO) {
        var holder = findDelta(email, name, casDTO);
        if (holder != null) {
            return formatResponse(holder.folioCount(), holder.transactionsCount());
        }
        return "Nothing to Update";
    }

    String formatResponse(int folios, long transactions) {
        return "Imported %d folios and %d transactions".formatted(folios, transactions);
    }

    UploadResponseHolder findDelta(String email, String name, CasDTO casDTO) {
        List<UserFolioDTO> folioDTOList = casDTO.folios();
        long userTransactionDTOListCount = countTransactionsByUserFolioDTOList(folioDTOList);
        List<UserTransactionDetailsEntity> userTransactionDetailsEntityList =
                this.userTransactionDetailsService.findAllTransactionsByEmailAndName(email, name);
        UserCASDetailsEntity userCASDetailsEntity = casDetailsEntityRepository.findByInvestorEmailAndName(email, name);

        if (userTransactionDTOListCount == userTransactionDetailsEntityList.size()) {
            LOGGER.info("No new transactions are added");
            return null;
        }

        AtomicInteger folioCounter = new AtomicInteger();
        AtomicInteger transactionsCounter = new AtomicInteger();

        processNewFolios(folioDTOList, userCASDetailsEntity, folioCounter, transactionsCounter);

        // Check if all new transactions are added as part of adding folios
        boolean allTransactionsAdded = verifyTransactionCount(
                userTransactionDTOListCount,
                transactionsCounter,
                "All new transactions are added as part of adding folios, hence skipping");

        if (!allTransactionsAdded) {
            processExistingFolios(folioDTOList, email, name, userCASDetailsEntity, transactionsCounter);
        }

        // Check if all new transactions are added as part of adding schemes
        allTransactionsAdded = verifyTransactionCount(
                userTransactionDTOListCount,
                transactionsCounter,
                "All new transactions are added as part of adding schemes, hence skipping");

        if (!allTransactionsAdded) {
            processExistingFolioWithNewSchemes(folioDTOList, email, name, userCASDetailsEntity, transactionsCounter);
        }

        return new UploadResponseHolder(userCASDetailsEntity, folioCounter.get(), transactionsCounter.get());
    }

    boolean verifyTransactionCount(long expectedCount, AtomicInteger actualCount, String logMessage) {
        if (expectedCount == actualCount.get()) {
            LOGGER.info(logMessage);
            return true;
        }
        return false;
    }

    void processExistingFolioWithNewSchemes(
            List<UserFolioDTO> folioDTOList,
            String email,
            String name,
            UserCASDetailsEntity userCASDetailsEntity,
            AtomicInteger transactionsCounter) {
        // New transactions are added

        // Grouping by ISIN for userSchemaTransactionMap
        Map<String, List<UserTransactionDTO>> userSchemaTransactionMap = folioDTOList.stream()
                .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                .collect(groupingBy(
                        UserSchemeDTO::isin, flatMapping(schemeDTO -> schemeDTO.transactions().stream(), toList())));

        List<UserSchemeDetailsEntity> existingUserSchemeDetailsList =
                userSchemeDetailsService.getSchemesByEmailAndName(email, name);

        // Grouping by ISIN for userSchemaTransactionMapFromDB
        Map<String, List<UserTransactionDetailsEntity>> userSchemaTransactionMapFromDB =
                existingUserSchemeDetailsList.stream()
                        .collect(groupingBy(
                                UserSchemeDetailsEntity::getIsin,
                                flatMapping(
                                        userSchemeDetailsEntity ->
                                                userSchemeDetailsEntity.getTransactionEntities().stream(),
                                        toList())));

        // Update transactions in existing schemes
        userSchemaTransactionMap.forEach((isinFromRequest, requestTransactions) -> {
            List<UserTransactionDetailsEntity> dbTransactions =
                    userSchemaTransactionMapFromDB.getOrDefault(isinFromRequest, List.of());
            if (requestTransactions.size() != dbTransactions.size()) {
                // New transactions added to scheme
                List<LocalDate> transactionDateListDB = dbTransactions.stream()
                        .map(UserTransactionDetailsEntity::getTransactionDate)
                        .toList();
                requestTransactions.forEach(userTransactionDTO -> {
                    LocalDate newTransactionDate = userTransactionDTO.date();
                    if (!transactionDateListDB.contains(newTransactionDate)) {
                        LOGGER.info(
                                "New transaction on date: {} created for isin {} that is not present in the database",
                                newTransactionDate,
                                isinFromRequest);
                        UserTransactionDetailsEntity userTransactionDetailsEntity =
                                casDetailsMapper.transactionDTOToTransactionEntity(userTransactionDTO);
                        existingUserSchemeDetailsList.forEach(userSchemeDetailsEntity -> {
                            if (isinFromRequest.equals(userSchemeDetailsEntity.getIsin())) {
                                userSchemeDetailsEntity.addTransactionEntity(userTransactionDetailsEntity);
                                // TODO convert to bulkInsert
                                userSchemeDetailsService.saveUserScheme(userSchemeDetailsEntity);
                                transactionsCounter.incrementAndGet();
                            }
                        });
                    }
                });
            }
        });
    }

    void processExistingFolios(
            List<UserFolioDTO> folioDTOList,
            String email,
            String name,
            UserCASDetailsEntity userCASDetailsEntity,
            AtomicInteger transactionsCounter) {

        // New schemes or transactions are added

        // Grouping by folio for userFolioSchemaRequestMap
        Map<String, List<UserSchemeDTO>> userFolioSchemaRequestMap = folioDTOList.stream()
                .collect(groupingBy(UserFolioDTO::folio, flatMapping(folio -> folio.schemes().stream(), toList())));

        // Grouping by folio for existingUserFolioSchemaRequestMap
        List<UserFolioDetailsEntity> existingUserFolioDetailsEntityList =
                userFolioDetailsService.findByUserEmailAndName(email, name);
        Map<String, List<UserSchemeDetailsEntity>> existingUserFolioSchemaRequestMap =
                existingUserFolioDetailsEntityList.stream()
                        .collect(groupingBy(
                                UserFolioDetailsEntity::getFolio,
                                flatMapping(
                                        userFolioDetailsEntity -> userFolioDetailsEntity.getSchemeEntities().stream(),
                                        toList())));

        // Update schemes in existing folios
        userFolioSchemaRequestMap.forEach((folioFromRequest, requestSchemes) -> {
            List<UserSchemeDetailsEntity> existingSchemesFromDB =
                    existingUserFolioSchemaRequestMap.getOrDefault(folioFromRequest, new ArrayList<>());
            if (requestSchemes.size() != existingSchemesFromDB.size()) {
                // New schemes added to folio
                List<String> isInListDB = existingSchemesFromDB.stream()
                        .map(UserSchemeDetailsEntity::getIsin)
                        .toList();
                requestSchemes.forEach(userSchemeDTO -> {
                    if (!isInListDB.contains(userSchemeDTO.isin())) {
                        LOGGER.info(
                                "New ISIN: {} created for folio :{} that is not present in the database",
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
        userCASDetailsEntity.setFolioEntities(existingUserFolioDetailsEntityList);
    }

    long countTransactionsByUserFolioDTOList(List<UserFolioDTO> folioDTOList) {
        return folioDTOList.stream()
                .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                .mapToLong(userSchemeDTO -> userSchemeDTO.transactions().size())
                .sum();
    }

    long countTransactions(List<UserFolioDetailsEntity> folioEntities) {
        return folioEntities.stream()
                .flatMap(userFolioDetailsEntity -> userFolioDetailsEntity.getSchemeEntities().stream())
                .mapToLong(value -> value.getTransactionEntities().size())
                .sum();
    }

    void processNewFolios(
            List<UserFolioDTO> folioDTOList,
            UserCASDetailsEntity userCASDetailsEntity,
            AtomicInteger folioCounter,
            AtomicInteger transactionsCounter) {
        // Logic to process new folios
        List<String> existingFolios = userCASDetailsEntity.getFolioEntities().stream()
                .map(UserFolioDetailsEntity::getFolio)
                .toList();

        folioDTOList.forEach(userFolioDTO -> {
            String folio = userFolioDTO.folio();
            if (!existingFolios.contains(folio)) {
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

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {
        List<CompletableFuture<PortfolioDetailsDTO>> completableFutureList =
                preparePortfolioFutures(panNumber, LocalDateUtility.adjustDate(asOfDate));
        List<PortfolioDetailsDTO> portfolioDetailsDTOS = joinFutures(completableFutureList);
        Double totalPortfolioValue = calculateTotalPortfolioValue(portfolioDetailsDTOS);
        return new PortfolioResponse(Math.round(totalPortfolioValue * 100.0) / 100.0, portfolioDetailsDTOS);
    }

    List<CompletableFuture<PortfolioDetailsDTO>> preparePortfolioFutures(String panNumber, LocalDate asOfDate) {
        return casDetailsEntityRepository.getPortfolioDetails(panNumber, asOfDate).stream()
                .map(portfolioDetails -> CompletableFuture.supplyAsync(() -> {
                    MFSchemeDTO scheme;
                    try {
                        scheme = navService.getNavByDateWithRetry(portfolioDetails.getSchemeId(), asOfDate);
                    } catch (NavNotFoundException navNotFoundException) {
                        // Will happen in case of NFO where units are allocated but not ready for subscription
                        LOGGER.error(
                                "NavNotFoundException occurred for scheme : {} on adjusted date :{}",
                                portfolioDetails.getSchemeId(),
                                asOfDate,
                                navNotFoundException);
                        scheme = new MFSchemeDTO(null, null, null, null, "10", asOfDate.toString(), null);
                    }
                    double totalValue = portfolioDetails.getBalanceUnits() * Double.parseDouble(scheme.nav());
                    return new PortfolioDetailsDTO(
                            Math.round(totalValue * 100.0) / 100.0,
                            portfolioDetails.getSchemeName(),
                            portfolioDetails.getFolioNumber(),
                            scheme.date(),
                            xIRRCalculatorService.calculateXIRRBySchemeId(
                                    portfolioDetails.getSchemeId(), portfolioDetails.getSchemeDetailId(), asOfDate));
                }))
                .toList();
    }

    List<PortfolioDetailsDTO> joinFutures(List<CompletableFuture<PortfolioDetailsDTO>> futures) {
        return futures.stream().map(CompletableFuture::join).toList();
    }

    Double calculateTotalPortfolioValue(List<PortfolioDetailsDTO> portfolioDetailsDTOS) {
        return portfolioDetailsDTOS.stream()
                .map(PortfolioDetailsDTO::totalValue)
                .reduce((double) 0, Double::sum);
    }
}
