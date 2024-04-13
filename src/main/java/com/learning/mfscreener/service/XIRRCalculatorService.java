package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.exception.XIRRCalculationException;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import com.learning.mfscreener.models.response.XIRRResponse;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.decampo.xirr.NewtonRaphson;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Loggable
public class XIRRCalculatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XIRRCalculatorService.class);
    private static final double TOLERANCE = 0.001; // tolerance for Newton's method

    private final UserFolioDetailsService userFolioDetailsService;
    private final UserTransactionDetailsService userTransactionDetailsService;
    private final NavService navService;

    public XIRRCalculatorService(
            UserFolioDetailsService userFolioDetailsService,
            UserTransactionDetailsService userTransactionDetailsService,
            NavService navService) {
        this.userFolioDetailsService = userFolioDetailsService;
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.navService = navService;
    }

    // method to calculate the total XIRR for a given PAN number
    public List<XIRRResponse> calculateTotalXIRRByPan(String pan, LocalDate asOfDate) {
        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }
        return calculateXIRRForAllFundsByPAN(pan, LocalDateUtility.getAdjustedDate(asOfDate));
    }

    // method to calculate XIRR for all funds
    List<XIRRResponse> calculateXIRRForAllFundsByPAN(String pan, LocalDate asOfDate) {
        return userFolioDetailsService.findByPanAndAsOfDate(pan, asOfDate).parallelStream()
                .map(userFolioDetailsProjection -> getXirrResponse(userFolioDetailsProjection, asOfDate))
                .filter(Objects::nonNull)
                .toList();
    }

    XIRRResponse getXirrResponse(UserFolioDetailsProjection folioDetailsProjection, LocalDate asOfDate) {
        Long schemeIdInDb = folioDetailsProjection.id();
        Long amfiId = folioDetailsProjection.amfi();
        // calculate the XIRR for the folioDetailsProjection
        double xirr = calculateXIRRBySchemeId(amfiId, schemeIdInDb, asOfDate);

        if (xirr != 0.0d) {
            LOGGER.debug("adding XIRR for schemeId : {}", amfiId);
            return new XIRRResponse(
                    folioDetailsProjection.folio(), amfiId, folioDetailsProjection.scheme(), xirr * 100);
        } else {
            LOGGER.info("Consolidated portfolio");
        }
        return null;
    }

    public double calculateXIRRBySchemeId(Long fundId, Long schemeIdInDb, LocalDate asOfDate) {
        LOGGER.debug("Calculating XIRR for fund ID : {} & schemeIdInDB :{}", fundId, schemeIdInDb);
        List<UserTransactionDetailsProjection> byUserSchemeDetailsEntityId =
                userTransactionDetailsService.fetchTransactions(schemeIdInDb, asOfDate);
        return computeXIRR(byUserSchemeDetailsEntityId, fundId, asOfDate);
    }

    double computeXIRR(List<UserTransactionDetailsProjection> transactions, Long fundId, LocalDate asOfDate) {
        double xirrValue = 0.0d;
        if (!transactions.isEmpty()) {
            double currentBalance = getBalance(transactions);
            List<Transaction> transactionList = buildTransactionList(transactions, currentBalance, fundId, asOfDate);
            xirrValue = calculateXirrValue(transactionList, fundId);
        }
        return xirrValue;
    }

    List<Transaction> buildTransactionList(
            List<UserTransactionDetailsProjection> transactions,
            double currentBalance,
            Long fundId,
            LocalDate asOfDate) {
        List<Transaction> transactionList = new ArrayList<>(transactions.stream()
                // in case if Additional Allotment is done then amount will be null
                .filter(t -> t.getAmount() != null)
                .map(t -> new Transaction(-t.getAmount(), t.getTransactionDate()))
                .toList());
        // XIRR cant be calculated when there are only 2 transactions and both has same date.
        if (currentBalance != 0.0
                && !(transactionList.size() == 1
                        && transactionList.get(0).getWhen().equals(asOfDate))) {
            // Add current Value and current date
            transactionList.add(new Transaction(getCurrentValuation(fundId, currentBalance, asOfDate), asOfDate));
        }
        return transactionList;
    }

    double calculateXirrValue(List<Transaction> transactionList, Long fundId) {
        double xirrValue = -0.00001; // Default value if unable to calculate
        if (transactionList.size() > 1) {
            try {
                xirrValue = Xirr.builder()
                        .withTransactions(transactionList)
                        .withGuess(0.01)
                        .withNewtonRaphsonBuilder(NewtonRaphson.builder()
                                .withFunction(x -> x)
                                .withIterations(1000)
                                .withTolerance(TOLERANCE))
                        .xirr();
            } catch (IllegalArgumentException e) {
                LOGGER.error("Unable to calculate XIRR for fundId :{}", fundId, e);
                throw new XIRRCalculationException("Unable to calculate XIRR for fundId " + fundId);
            }
        }
        return xirrValue;
    }

    // ensures that balance will never be null
    Double getBalance(List<UserTransactionDetailsProjection> byUserSchemeDetailsEntityId) {
        Double balance = byUserSchemeDetailsEntityId
                .get(byUserSchemeDetailsEntityId.size() - 1)
                .getBalance();
        if (balance == null) {
            LOGGER.debug("Balance units Not found hence, attempting for 2nd last row");
            balance = byUserSchemeDetailsEntityId
                    .get(byUserSchemeDetailsEntityId.size() - 2)
                    .getBalance();
        }
        return balance;
    }

    double getCurrentValuation(Long fundId, Double balance, LocalDate asOfDate) {
        MFSchemeDTO scheme = navService.getNavByDateWithRetry(fundId, asOfDate);
        try {
            return balance * Double.parseDouble(scheme.nav());
        } catch (NumberFormatException e) {
            LOGGER.error("Error parsing NAV value for fundId: {}", fundId, e);
            throw new IllegalArgumentException("Invalid NAV value");
        }
    }
}
