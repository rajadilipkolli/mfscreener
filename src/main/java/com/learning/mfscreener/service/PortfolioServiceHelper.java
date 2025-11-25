package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.NavNotFoundException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.PortfolioDetailsDTO;
import com.learning.mfscreener.models.portfolio.UserFolioDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Loggable
public class PortfolioServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioServiceHelper.class);

    private final JsonMapper jsonMapper;
    private final UserCASDetailsService userCASDetailsService;
    private final NavService navService;
    private final XIRRCalculatorService xIRRCalculatorService;

    public PortfolioServiceHelper(
            JsonMapper jsonMapper,
            UserCASDetailsService userCASDetailsService,
            NavService navService,
            XIRRCalculatorService xIRRCalculatorService) {
        this.jsonMapper = jsonMapper;
        this.userCASDetailsService = userCASDetailsService;
        this.navService = navService;
        this.xIRRCalculatorService = xIRRCalculatorService;
    }

    public <T> T readValue(byte[] bytes, Class<T> tClass) {
        return this.jsonMapper.readValue(bytes, tClass);
    }

    public <T> List<T> joinFutures(List<CompletableFuture<T>> futures) {
        return futures.stream().map(CompletableFuture::join).toList();
    }

    public List<PortfolioDetailsDTO> getPortfolioDetailsByPANAndAsOfDate(String panNumber, LocalDate asOfDate) {
        return joinFutures(userCASDetailsService.getPortfolioDetailsByPanAndAsOfDate(panNumber, asOfDate).stream()
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
                .toList());
    }

    public long countTransactionsByUserFolioDTOList(List<UserFolioDTO> folioDTOList) {
        return folioDTOList.stream()
                .flatMap(userFolioDTO -> userFolioDTO.schemes().stream())
                .mapToLong(userSchemeDTO -> userSchemeDTO.transactions().size())
                .sum();
    }
}
