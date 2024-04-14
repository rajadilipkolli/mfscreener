package com.learning.mfscreener.utils;

import static com.learning.mfscreener.service.XIRRCalculatorService.LOGGER;

import com.learning.mfscreener.exception.XIRRCalculationException;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import java.util.List;
import org.decampo.xirr.NewtonRaphson;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;

public class XirrCalculatorHelper {

    private static final double TOLERANCE = 0.001; // tolerance for Newton's method

    // ensures that balance will never be null
    public static Double getBalance(List<UserTransactionDetailsProjection> byUserSchemeDetailsEntityId) {
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

    public static double calculateXirrValue(List<Transaction> transactionList, Long fundId) {
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

    private XirrCalculatorHelper() {
        throw new UnsupportedOperationException("Constructor can't be initialized");
    }
}
