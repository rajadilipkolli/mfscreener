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
import com.learning.mfscreener.repository.InvestorInfoEntityRepository;
import com.learning.mfscreener.repository.UserCASDetailsEntityRepository;
import com.learning.mfscreener.repository.UserFolioDetailsEntityRepository;
import com.learning.mfscreener.repository.UserTransactionDetailsEntityRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.io.IOException;
import java.time.LocalDate;
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
        AtomicInteger folioCounter = new AtomicInteger();
        AtomicInteger transactionsCounter = new AtomicInteger();
        List<UserFolioDTO> folioDTOList = casDTO.folios();
        List<UserTransactionDTO> userTransactionDTOList = folioDTOList.stream()
                .map(userFolioDTO -> userFolioDTO.schemes().stream()
                        .map(UserSchemeDTO::transactions)
                        .flatMap(List::stream)
                        .toList())
                .flatMap(List::stream)
                .toList();
        List<UserTransactionDetailsEntity> userTransactionDetailsEntityList =
                this.userTransactionDetailsEntityRepository.findAllTransactionsByEmailAndName(email, name);
        UserCASDetailsEntity userCASDetailsEntity;
        if (userTransactionDTOList.size() == userTransactionDetailsEntityList.size()) {
            log.info("No new transactions are added");
            return null;
        } else {
            userCASDetailsEntity = casDetailsEntityRepository.findByInvestorEmailAndName(email, name);

            // verify if new folio is added
            if (folioDTOList.size() != userCASDetailsEntity.getFolioEntities().size()) {
                List<String> folioList = userCASDetailsEntity.getFolioEntities().stream()
                        .map(UserFolioDetailsEntity::getFolio)
                        .toList();
                folioDTOList.forEach(userFolioDTO -> {
                    if (!folioList.contains(userFolioDTO.folio())) {
                        // adding new folio along with scheme and transactions
                        log.info("new folio :{} created that is not present in database", userFolioDTO.folio());
                        userCASDetailsEntity.addFolioEntity(
                                casDetailsMapper.mapUserFolioDTOToUserFolioDetailsEntity(userFolioDTO));
                        folioCounter.getAndIncrement();
                        int newTransactions = userFolioDTO.schemes().stream()
                                .map(UserSchemeDTO::transactions)
                                .flatMap(List::stream)
                                .toList()
                                .size();
                        transactionsCounter.addAndGet(newTransactions);
                    }
                });
            }
            if (userTransactionDTOList.size()
                    == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
                // added all new transactions as part of adding folioDTOList, hence skipping
                log.info("Added all new transactions as part of adding folioDTOList, hence skipping");
            } else {
                // Either Scheme or transaction is added
                // Use flatMapping directly to flatten schemes in the groupingBy collector
                Map<String, List<UserSchemeDTO>> userFolioSchemaMap = folioDTOList.stream()
                        .collect(groupingBy(
                                UserFolioDTO::folio, flatMapping(folio -> folio.schemes().stream(), toList())));

                List<UserFolioDetailsEntity> userFolioDetailsEntityList =
                        userFolioDetailsEntityRepository.findByUserEmailAndName(email, name);
                Map<String, List<UserSchemeDetailsEntity>> userSchemeDetailsEntityMap =
                        userFolioDetailsEntityList.stream()
                                .collect(groupingBy(
                                        UserFolioDetailsEntity::getFolio,
                                        flatMapping(
                                                userFolioDetailsEntity ->
                                                        userFolioDetailsEntity.getSchemeEntities().stream(),
                                                toList())));
                for (Map.Entry<String, List<UserSchemeDTO>> requestEntry : userFolioSchemaMap.entrySet()) {
                    for (Map.Entry<String, List<UserSchemeDetailsEntity>> dbEntry :
                            userSchemeDetailsEntityMap.entrySet()) {
                        String folioFromDB = dbEntry.getKey();
                        if (requestEntry.getKey().equals(folioFromDB)
                                && requestEntry.getValue().size()
                                        != dbEntry.getValue().size()) {
                            // new scheme added to folio
                            List<String> isInListDB = dbEntry.getValue().stream()
                                    .map(UserSchemeDetailsEntity::getIsin)
                                    .toList();
                            requestEntry.getValue().forEach(userSchemeDTO -> {
                                if (!isInListDB.contains(userSchemeDTO.isin())) {
                                    // newly added scheme along with transactions
                                    log.info(
                                            "new ISIN :{} created that is not present in database",
                                            userSchemeDTO.isin());
                                    UserSchemeDetailsEntity userSchemeDetailsEntity =
                                            casDetailsMapper.schemeDTOToSchemeEntity(userSchemeDTO);

                                    userFolioDetailsEntityList.forEach(userFolioDetailsEntity -> {
                                        if (folioFromDB.equals(userFolioDetailsEntity.getFolio())) {
                                            userFolioDetailsEntity.addSchemeEntity(userSchemeDetailsEntity);
                                            int newTransactions =
                                                    userSchemeDTO.transactions().size();
                                            transactionsCounter.addAndGet(newTransactions);
                                        }
                                    });
                                }
                            });
                            break;
                        }
                    }
                }
                userCASDetailsEntity.setFolioEntities(userFolioDetailsEntityList);
                if (userTransactionDTOList.size()
                        == (userTransactionDetailsEntityList.size() + transactionsCounter.get())) {
                    log.info(
                            "All new transactions are added as part of adding folioDTOList or schemes or both, hence skipping");
                }
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
