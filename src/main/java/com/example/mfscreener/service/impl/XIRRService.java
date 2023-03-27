/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener.service.impl;

import com.example.mfscreener.models.Transaction;
import com.example.mfscreener.models.TransactionType;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.springframework.stereotype.Service;

@Service
public class XIRRService {

    public double calculateXIRR(List<Transaction> transactions) {
        double[] cashFlows = new double[transactions.size()];
        double[] dates = new double[transactions.size()];
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            cashFlows[i] =
                    transaction.getAmount()
                            * (transaction.getType() == TransactionType.BUY ? -1 : 1);
            dates[i] = daysSinceEpoch(transaction.getDate());
        }
        UnivariateFunction f =
                t -> {
                    double value = cashFlows[0];
                    for (int i = 1; i < cashFlows.length; i++) {
                        value += cashFlows[i] / Math.pow(1 + t, (dates[i] - dates[0]) / 365.0);
                    }
                    return value;
                };
        BrentSolver solver = new BrentSolver(1e-10);
        //                .withMaximalIterationCount(100);
        return solver.solve(100, f, -1.0, 1.0);
    }

    private double daysSinceEpoch(LocalDate date) {
        return date.toEpochDay() / 365.0;
    }
}
