package com.learning.mfscreener.service;

import static java.util.stream.Collectors.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.mapper.CasDetailsMapper;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.PortfolioDetailsDTO;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.response.PortfolioResponse;
import com.learning.mfscreener.models.response.UploadResponseHolder;
import com.learning.mfscreener.repository.*;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {

    private final ObjectMapper objectMapper;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final CasDetailsMapper casDetailsMapper;
    private final UserCASDetailsEntityRepository casDetailsEntityRepository;
    private final InvestorInfoEntityRepository investorInfoEntityRepository;
    private final UserFolioDetailsEntityRepository userFolioDetailsEntityRepository;
    private final UserTransactionDetailsEntityRepository userTransactionDetailsEntityRepository;
    private final UserSchemeDetailsEntityRepository userSchemeDetailsEntityRepository;
    private final NavService navService;
    private final SchemeService schemeService;

    public String upload(MultipartFile multipartFile) throws IOException {
        CasDTO casDTO = this.objectMapper.readValue(multipartFile.getBytes(), CasDTO.class);
        // check if user and email exits
        String email = casDTO.investorInfo().email();
        String name = casDTO.investorInfo().name();
        UserCASDetailsEntity casDetailsEntity = null;
        int folios = 0;
        int transactions = 0;
        if (this.investorInfoEntityRepository.existsByEmailAndName(email, name)) {
            var holder = findDelta(email, name, casDTO);
            if (holder != null) {
                casDetailsEntity = holder.userCASDetailsEntity();
                folios = holder.folioCount();
                transactions = holder.transactionsCount();
            }
        } else {
            casDetailsEntity = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
            List<UserFolioDetailsEntity> folioEntities = casDetailsEntity.getFolioEntities();
            transactions = folioEntities.stream()
                    .map(UserFolioDetailsEntity::getSchemeEntities)
                    .flatMap(List::stream)
                    .map(UserSchemeDetailsEntity::getTransactionEntities)
                    .toList()
                    .size();
            folios = folioEntities.size();
        }
        if (casDetailsEntity != null) {
            this.casDetailsEntityRepository.save(casDetailsEntity);
            CompletableFuture.runAsync(schemeService::setAMFIIfNull);
            return "Imported %d folios and %d transactions".formatted(folios, transactions);
        } else {
            return "Nothing to Update";
        }
    }

    UploadResponseHolder findDelta(String email, String name, CasDTO casDTO) {
        List<UserFolioDTO> folioDTOList = casDTO.folios();

        // Extract all transactions from folios
        List<UserTransactionDTO> userTransactionDTOList = folioDTOList.stream()
                .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                .flatMap(userSchemeDTO -> userSchemeDTO.transactions().stream())
                .toList();

        List<UserTransactionDetailsEntity> userTransactionDetailsEntityList =
                this.userTransactionDetailsEntityRepository.findAllTransactionsByEmailAndName(email, name);

        UserCASDetailsEntity userCASDetailsEntity = casDetailsEntityRepository.findByInvestorEmailAndName(email, name);

        if (userTransactionDTOList.size() == userTransactionDetailsEntityList.size()) {
            log.info("No new transactions are added");
            return null;
        }

        AtomicInteger folioCounter = new AtomicInteger();
        AtomicInteger transactionsCounter = new AtomicInteger();

        // Verify if new folios are added
        List<String> existingFolios = userCASDetailsEntity.getFolioEntities().stream()
                .map(UserFolioDetailsEntity::getFolio)
                .toList();

        folioDTOList.forEach(userFolioDTO -> {
            String folio = userFolioDTO.folio();
            if (!existingFolios.contains(folio)) {
                log.info("New folio: {} created that is not present in the database", folio);
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

        // Check if all new transactions are added as part of adding folios
        if (userTransactionDTOList.size() == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
            log.info("All new transactions are added as part of adding folios, hence skipping");
        } else {
            // New schemes or transactions are added

            // Grouping by folio for userFolioSchemaRequestMap
            Map<String, List<UserSchemeDTO>> userFolioSchemaRequestMap = folioDTOList.stream()
                    .collect(groupingBy(UserFolioDTO::folio, flatMapping(folio -> folio.schemes().stream(), toList())));

            // Grouping by folio for existingUserFolioSchemaRequestMap
            List<UserFolioDetailsEntity> existingUserFolioDetailsEntityList =
                    userFolioDetailsEntityRepository.findByUserEmailAndName(email, name);
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
                            log.info(
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
            if (userTransactionDTOList.size()
                    == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
                log.info("All new transactions are added as part of adding schemes, hence skipping");
            } else {
                // New transactions are added

                // Grouping by ISIN for userSchemaTransactionMap
                Map<String, List<UserTransactionDTO>> userSchemaTransactionMap = folioDTOList.stream()
                        .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                        .collect(groupingBy(
                                UserSchemeDTO::isin,
                                flatMapping(schemeDTO -> schemeDTO.transactions().stream(), toList())));

                List<UserSchemeDetailsEntity> existingUserSchemeDetailsList =
                        schemeService.getSchemesByEmailAndName(email, name);

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
                                log.info(
                                        "New transaction on date: {} created for isin {} that is not present in the database",
                                        newTransactionDate,
                                        isinFromRequest);
                                UserTransactionDetailsEntity userTransactionDetailsEntity =
                                        casDetailsMapper.transactionDTOToTransactionEntity(userTransactionDTO);
                                existingUserSchemeDetailsList.forEach(userSchemeDetailsEntity -> {
                                    if (isinFromRequest.equals(userSchemeDetailsEntity.getIsin())) {
                                        userSchemeDetailsEntity.addTransactionEntity(userTransactionDetailsEntity);
                                        // TODO convert to bulkInsert
                                        userSchemeDetailsEntityRepository.save(userSchemeDetailsEntity);
                                        transactionsCounter.incrementAndGet();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
        return new UploadResponseHolder(userCASDetailsEntity, folioCounter.get(), transactionsCounter.get());
    }

    public PortfolioResponse getPortfolioByPAN(String panNumber, LocalDate asOfDate) {

        List<CompletableFuture<PortfolioDetailsDTO>> completableFutureList =
                casDetailsEntityRepository.getPortfolioDetails(panNumber, asOfDate).stream()
                        .map(portfolioDetails -> CompletableFuture.supplyAsync(() -> {
                            MFSchemeDTO scheme = navService.getNavByDateWithRetry(
                                    portfolioDetails.getSchemeId(), LocalDateUtility.getAdjustedDate(asOfDate));
                            Double totalValue = portfolioDetails.getBalanceUnits() * Double.parseDouble(scheme.nav());

                            return new PortfolioDetailsDTO(
                                    totalValue,
                                    portfolioDetails.getSchemeName(),
                                    portfolioDetails.getFolioNumber(),
                                    scheme.date());
                        }))
                        .toList();

        List<PortfolioDetailsDTO> portfolioDetailsDTOS =
                completableFutureList.stream().map(CompletableFuture::join).toList();

        return new PortfolioResponse(
                portfolioDetailsDTOS.stream()
                        .map(PortfolioDetailsDTO::totalValue)
                        .reduce(0d, Double::sum),
                portfolioDetailsDTOS);
    }
}
