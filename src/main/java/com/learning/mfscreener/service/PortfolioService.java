package com.learning.mfscreener.service;

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
import com.learning.mfscreener.repository.UserTransactionDetailsEntityRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final ObjectMapper objectMapper;
    private final ConversionServiceAdapter conversionServiceAdapter;
    private final CasDetailsMapper casDetailsMapper;
    private final UserCASDetailsEntityRepository casDetailsEntityRepository;
    private final InvestorInfoEntityRepository investorInfoEntityRepository;
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
        List<UserFolioDTO> folios = casDTO.folios();
        List<UserTransactionDTO> userTransactionDTOList = folios.stream()
                .map(userFolioDTO -> userFolioDTO.schemes().stream()
                        .map(UserSchemeDTO::transactions)
                        .flatMap(List::stream)
                        .toList())
                .flatMap(List::stream)
                .toList();
        List<UserTransactionDetailsEntity> userTransactionDetailsEntityList =
                this.userTransactionDetailsEntityRepository.findAllTransactionsByEmailAndName(email, name);
        if (userTransactionDTOList.size() == userTransactionDetailsEntityList.size()) {
            return null;
        } else {
            // verify if new folio is added
            UserCASDetailsEntity userCASDetailsEntity =
                    casDetailsEntityRepository.findByInvestorEmailAndName(email, name);

            if (folios.size() == userCASDetailsEntity.getFolioEntities().size()) {
                // Either Scheme or transaction is added
                List<UserSchemeDTO> userSchemeDTOList = folios.stream()
                        .map(UserFolioDTO::schemes)
                        .flatMap(List::stream)
                        .toList();

                List<UserSchemeDetailsEntity> userSchemeDetailsEntityList =
                        schemeService.getSchemesByEmailAndName(email, name);

                if (userSchemeDTOList.size() == userSchemeDetailsEntityList.size()) {
                    // TODO new transaction added
                } else {
                    List<String> isinList = userSchemeDetailsEntityList.stream()
                            .map(UserSchemeDetailsEntity::getIsin)
                            .toList();
                    userSchemeDTOList.forEach(userSchemeDTO -> {
                        if (!isinList.contains(userSchemeDTO.isin())) {
                            // newly added scheme
                        }
                    });
                }
            } else {
                List<String> folioList = userCASDetailsEntity.getFolioEntities().stream()
                        .map(UserFolioDetailsEntity::getFolio)
                        .toList();
                folios.forEach(userFolioDTO -> {
                    if (!folioList.contains(userFolioDTO.folio())) {
                        // adding new folio
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
            return new UploadResponseHolder(userCASDetailsEntity, folioCounter.get(), transactionsCounter.get());
        }
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
