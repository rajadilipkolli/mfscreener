/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener.service.impl;

import com.example.mfscreener.models.Transaction;
import com.example.mfscreener.models.TransactionType;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.springframework.stereotype.Service;

@Service
public class XIRRService {

    private static final double ACCURACY = 0.000001;
    private static final int MAX_EVALUATIONS = 1000;

    public double calculateXIRR(List<Transaction> cashflowsList) {
        List<Transaction> buys = new ArrayList<>();
        List<Transaction> redeems = new ArrayList<>();

        for (Transaction cashflow : cashflowsList) {
            if (cashflow.getType() == TransactionType.BUY) {
                buys.add(cashflow);
            } else if (cashflow.getType() == TransactionType.REDEEM) {
                redeems.add(cashflow);
            }
        }

        buys.sort(Comparator.comparing(Transaction::getDate));
        redeems.sort(Comparator.comparing(Transaction::getDate));

        double[] values = new double[cashflowsList.size()];
        LocalDate startDate = null;
        LocalDate endDate = null;

        for (int i = 0; i < buys.size(); i++) {
            Transaction buy = buys.get(i);
            values[i] = -buy.getAmount();
            if (startDate == null || buy.getDate().isBefore(startDate)) {
                startDate = buy.getDate();
            }
            if (endDate == null || buy.getDate().isAfter(endDate)) {
                endDate = buy.getDate();
            }
        }

        for (int i = 0; i < redeems.size(); i++) {
            Transaction redeem = redeems.get(i);
            values[i + buys.size()] = redeem.getAmount();
            if (startDate == null || redeem.getDate().isBefore(startDate)) {
                startDate = redeem.getDate();
            }
            if (endDate == null || redeem.getDate().isAfter(endDate)) {
                endDate = redeem.getDate();
            }
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        double[] times = new double[cashflowsList.size()];

        for (int i = 0; i < buys.size(); i++) {
            Transaction buy = buys.get(i);
            long daysSinceStart = ChronoUnit.DAYS.between(startDate, buy.getDate());
            times[i] = (double) daysSinceStart / daysBetween;
        }

        for (int i = 0; i < redeems.size(); i++) {
            Transaction redeem = redeems.get(i);
            long daysSinceStart = ChronoUnit.DAYS.between(startDate, redeem.getDate());
            times[i + buys.size()] = (double) daysSinceStart / daysBetween;
        }

        PolynomialSplineFunction function = new LinearInterpolator().interpolate(times, values);
        UnivariateSolver solver = new BrentSolver(ACCURACY);
        double guess = 0.1;

        try {
            return solver.solve(MAX_EVALUATIONS, function, 0.0, 1.0, guess);
        } catch (TooManyEvaluationsException e) {
            throw new RuntimeException("Unable to calculate XIRR: " + e.getMessage(), e);
        }
    }
}
