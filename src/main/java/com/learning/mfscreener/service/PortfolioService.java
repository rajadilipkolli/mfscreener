package com.learning.mfscreener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.mfscreener.adapter.ConversionServiceAdapter;
import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.PortfolioDetailsDTO;
import com.learning.mfscreener.models.portfolio.CasDTO;
import com.learning.mfscreener.models.portfolio.UserSchemeDTO;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import com.learning.mfscreener.models.response.PortfolioResponse;
import com.learning.mfscreener.repository.InvestorInfoEntityRepository;
import com.learning.mfscreener.repository.UserCASDetailsEntityRepository;
import com.learning.mfscreener.repository.UserTransactionDetailsEntityRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final ObjectMapper objectMapper;
    private final ConversionServiceAdapter conversionServiceAdapter;
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
        UserCASDetailsEntity casDetailsEntity;
        if (this.investorInfoEntityRepository.existsByEmailAndName(email, name)) {
            casDetailsEntity = findDelta(email, name, casDTO);
        } else {
            casDetailsEntity = this.conversionServiceAdapter.mapCasDTOToUserCASDetailsEntity(casDTO);
        }
        if (casDetailsEntity != null) {
            UserCASDetailsEntity persistedCasDetailsEntity = this.casDetailsEntityRepository.save(casDetailsEntity);
            CompletableFuture.runAsync(schemeService::setAMFIIfNull);
            int transactions = persistedCasDetailsEntity.getFolioEntities().stream()
                    .map(UserFolioDetailsEntity::getSchemeEntities)
                    .flatMap(List::stream)
                    .map(UserSchemeDetailsEntity::getTransactionEntities)
                    .toList()
                    .size();
            return "Imported %d folios and %d transactions"
                    .formatted(persistedCasDetailsEntity.getFolioEntities().size(), transactions);
        } else {
            return "Nothing to Update";
        }
    }

    UserCASDetailsEntity findDelta(String email, String name, CasDTO casDTO) {
        List<UserTransactionDTO> userTransactionDTOList = casDTO.folios().stream()
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
            return new UserCASDetailsEntity();
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
