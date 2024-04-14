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
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.response.PortfolioResponse;
import com.learning.mfscreener.models.response.UploadResponseHolder;
import com.learning.mfscreener.repository.InvestorInfoEntityRepository;
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
@Loggable
public class PortfolioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioService.class);

    private final ConversionServiceAdapter conversionServiceAdapter;
    private final CasDetailsMapper casDetailsMapper;
    private final InvestorInfoEntityRepository investorInfoEntityRepository;
    private final SchemeService schemeService;
    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserFolioDetailsService userFolioDetailsService;
    private final UserSchemeDetailsService userSchemeDetailsService;
    private final PortfolioServiceHelper portfolioServiceHelper;
    private final UserCASDetailsService userCASDetailsService;

    public PortfolioService(
            ConversionServiceAdapter conversionServiceAdapter,
            CasDetailsMapper casDetailsMapper,
            InvestorInfoEntityRepository investorInfoEntityRepository,
            UserTransactionDetailsService userTransactionDetailsService,
            SchemeService schemeService,
            UserFolioDetailsService userFolioDetailsService,
            UserSchemeDetailsService userSchemeDetailsService,
            PortfolioServiceHelper portfolioServiceHelper,
            UserCASDetailsService userCASDetailsService) {
        this.conversionServiceAdapter = conversionServiceAdapter;
        this.casDetailsMapper = casDetailsMapper;
        this.userCASDetailsService = userCASDetailsService;
        this.investorInfoEntityRepository = investorInfoEntityRepository;
        this.schemeService = schemeService;
        this.userFolioDetailsService = userFolioDetailsService;
        this.userSchemeDetailsService = userSchemeDetailsService;
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.portfolioServiceHelper = portfolioServiceHelper;
    }

    public String upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = parseCasDTO(multipartFile);
        return processCasDTO(casDTO);
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {
        List<PortfolioDetailsDTO> portfolioDetailsDTOList = portfolioServiceHelper.getPortfolioDetailsByPANAndAsOfDate(
                panNumber, LocalDateUtility.getAdjustedDateOrDefault(asOfDate));
        Double totalPortfolioValue = portfolioDetailsDTOList.stream()
                .map(PortfolioDetailsDTO::totalValue)
                .reduce((double) 0, Double::sum);
        return new PortfolioResponse(Math.round(totalPortfolioValue * 100.0) / 100.0, portfolioDetailsDTOList);
    }

    CasDTO parseCasDTO(MultipartFile multipartFile) throws IOException {
        return portfolioServiceHelper.readValue(multipartFile.getBytes(), CasDTO.class);
    }

    String processCasDTO(CasDTO casDTO) {
        String email = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();
        UserCASDetailsEntity casDetailsEntity = null;
        int folios = 0;
        long transactions = 0;
        if (this.investorInfoEntityRepository.existsByEmailAndName(email, name)) {
            var holder = findDelta(email, name, casDTO);
            if (holder != null) {
                casDetailsEntity = holder.userCASDetailsEntity();
                folios = holder.folioCount();
                transactions = holder.transactionsCount();
            }
        } else {
            casDetailsEntity = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
            folios = casDetailsEntity.getFolioEntities().size();
            transactions = casDetailsEntity.getFolioEntities().stream()
                    .flatMap(f -> f.getSchemeEntities().stream())
                    .mapToLong(s -> s.getTransactionEntities().size())
                    .sum();
        }
        return finalizeUpload(casDetailsEntity, folios, transactions);
    }

    String finalizeUpload(UserCASDetailsEntity casDetailsEntity, int folios, long transactions) {
        if (casDetailsEntity != null) {
            UserCASDetailsEntity savedCasDetailsEntity = this.userCASDetailsService.saveEntity(casDetailsEntity);
            CompletableFuture.runAsync(() -> schemeService.setPANIfNotSet(savedCasDetailsEntity.getId()));
            CompletableFuture.runAsync(userSchemeDetailsService::setAMFIIfNull);
            return "Imported %d folios and %d transactions".formatted(folios, transactions);
        } else {
            return "Nothing to Update";
        }
    }

    UploadResponseHolder findDelta(String email, String name, CasDTO casDTO) {
        List<UserFolioDTO> folioDTOList = casDTO.folios();
        long userTransactionDTOListCount = portfolioServiceHelper.countTransactionsByUserFolioDTOList(folioDTOList);
        List<UserTransactionDetailsEntity> userTransactionDetailsEntityList =
                this.userTransactionDetailsService.findAllTransactionsByEmailAndName(email, name);
        UserCASDetailsEntity userCASDetailsEntity = userCASDetailsService.findByInvestorEmailAndName(email, name);

        if (userTransactionDTOListCount == userTransactionDetailsEntityList.size()) {
            LOGGER.info("No new transactions are added");
            return null;
        }

        return processFoliosAndTransactions(
                email,
                name,
                casDTO,
                userCASDetailsEntity,
                userTransactionDTOListCount,
                userTransactionDetailsEntityList);
    }

    UploadResponseHolder processFoliosAndTransactions(
            String email,
            String name,
            CasDTO casDTO,
            UserCASDetailsEntity userCASDetailsEntity,
            long userTransactionDTOListCount,
            List<UserTransactionDetailsEntity> userTransactionDetailsEntityList) {
        AtomicInteger folioCounter = new AtomicInteger();
        AtomicInteger transactionsCounter = new AtomicInteger();

        processNewFolios(casDTO.folios(), userCASDetailsEntity, folioCounter, transactionsCounter);
        updateSchemesAndTransactions(
                email,
                name,
                casDTO,
                userCASDetailsEntity,
                userTransactionDTOListCount,
                userTransactionDetailsEntityList,
                folioCounter,
                transactionsCounter);

        return new UploadResponseHolder(userCASDetailsEntity, folioCounter.get(), transactionsCounter.get());
    }

    void updateSchemesAndTransactions(
            String email,
            String name,
            CasDTO casDTO,
            UserCASDetailsEntity userCASDetailsEntity,
            long userTransactionDTOListCount,
            List<UserTransactionDetailsEntity> userTransactionDetailsEntityList,
            AtomicInteger folioCounter,
            AtomicInteger transactionsCounter) {
        // Check if all new transactions are added as part of adding folios
        if (userTransactionDTOListCount == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
            LOGGER.info("All new transactions are added as part of adding folios, hence skipping");
        } else {
            // New schemes or transactions are added

            // Grouping by folio for userFolioSchemaRequestMap
            List<UserFolioDTO> folioDTOList = casDTO.folios();
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
                                            userFolioDetailsEntity ->
                                                    userFolioDetailsEntity.getSchemeEntities().stream(),
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

            // Check if all new transactions are added as part of adding schemes
            if (userTransactionDTOListCount == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
                LOGGER.info("All new transactions are added as part of adding schemes, hence skipping");
            } else {
                // New transactions are added

                // Grouping by ISIN for userSchemaTransactionMap
                Map<String, List<UserTransactionDTO>> userSchemaTransactionMap = folioDTOList.stream()
                        .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                        .collect(groupingBy(
                                UserSchemeDTO::isin,
                                flatMapping(schemeDTO -> schemeDTO.transactions().stream(), toList())));

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
        }
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
}
