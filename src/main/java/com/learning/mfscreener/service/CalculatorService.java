package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import com.learning.mfscreener.repository.UserFolioDetailsEntityRepository;
import com.learning.mfscreener.repository.UserTransactionDetailsEntityRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Loggable
public class CalculatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorService.class);
    private static final double TOLERANCE = 0.001; // tolerance for Newton's method

    private final UserFolioDetailsEntityRepository userFolioDetailsEntityRepository;
    private final UserTransactionDetailsEntityRepository userTransactionDetailsEntityRepository;
    private final NavService navService;

    public CalculatorService(
            UserFolioDetailsEntityRepository userFolioDetailsEntityRepository,
            UserTransactionDetailsEntityRepository userTransactionDetailsEntityRepository,
            NavService navService) {
        this.userFolioDetailsEntityRepository = userFolioDetailsEntityRepository;
        this.userTransactionDetailsEntityRepository = userTransactionDetailsEntityRepository;
        this.navService = navService;
    }

    // method to calculate the total XIRR for a given PAN number
    public Map<Long, Double> calculateTotalXIRRByPan(String pan) {
        // get the map of fund id and XIRR value for all funds
        // TODO : return schemeName as well
        Map<Long, Double> xirrMap = calculateXIRRForAllFundsByPAN(pan);
        // initialize the total XIRR to zero
        //        double totalXirr = 0.0;
        //        // loop through the map
        //        for (Map.Entry<Long, Double> entry : xirrMap.entrySet()) {
        //            // get the fund id and XIRR value
        //            Long fundId = entry.getKey();
        //            Double xirr = entry.getValue();
        //
        //            // add the XIRR value to the total XIRR
        //            totalXirr += xirr;
        //        }
        //        // return the total XIRR
        //        return totalXirr;
        return xirrMap;
    }

    // method to calculate XIRR for all funds
    Map<Long, Double> calculateXIRRForAllFundsByPAN(String pan) {
        // get all the funds
        List<UserFolioDetailsProjection> funds = userFolioDetailsEntityRepository.findByPan(pan);
        //        Iterable<Fund> funds = getFunds();
        // create a map to store the fund id and XIRR value
        Map<Long, Double> xirrMap = new HashMap<>();
        // loop through the funds
        for (UserFolioDetailsProjection fund : funds) {
            // get the fund id
            Long fundId = fund.getSchemeEntities().get(0).getAmfi();
            if (fundId == null) {
                LOGGER.error("FundID not available for fund :{} hence skipping", fund);
                continue;
            }
            // TODO calculate individually and at overall level as well
            Long schemeIdInDb = fund.getSchemeEntities().get(0).getId();
            // calculate the XIRR for the fund
            Double xirr = calculateXIRR(fundId, schemeIdInDb);
            LOGGER.debug("adding XIRR for schemeId : {}", fundId);
            // put the fund id and XIRR value in the map
            xirrMap.put(fundId, xirr * 100);
        }
        // return the map
        return xirrMap;
    }

    Double calculateXIRR(Long fundId, Long schemeIdInDb) {
        LOGGER.debug("Calculating XIRR for fund ID : {} & schemeIdInDB :{}", fundId, schemeIdInDb);
        List<UserTransactionDetailsProjection> byUserSchemeDetailsEntityId =
                userTransactionDetailsEntityRepository
                        .findByUserSchemeDetailsEntity_IdAndTypeNotInOrderByTransactionDateAsc(
                                schemeIdInDb, List.of("STT_TAX", "STAMP_DUTY_TAX", "MISC"));

        int arraySize = byUserSchemeDetailsEntityId.size() + 1;
        double[] payments = new double[arraySize];
        LocalDate[] dates = new LocalDate[arraySize];

        for (int i = 0; i < byUserSchemeDetailsEntityId.size(); i++) {
            UserTransactionDetailsProjection userTransactionDetailsProjection = byUserSchemeDetailsEntityId.get(i);
            payments[i] = -(userTransactionDetailsProjection.getAmount());
            dates[i] = userTransactionDetailsProjection.getTransactionDate();
        }

        // Add current Value and current date
        payments[arraySize - 1] = getCurrentValuation(fundId, getBalance(byUserSchemeDetailsEntityId));
        dates[arraySize - 1] = LocalDate.now();
        return calculateXIRR(payments, dates);
    }

    // ensures that balance will never be null
    Double getBalance(List<UserTransactionDetailsProjection> byUserSchemeDetailsEntityId) {
        Double balance = byUserSchemeDetailsEntityId
                .get(byUserSchemeDetailsEntityId.size())
                .getBalance();
        if (balance == null) {
            LOGGER.debug("Balance units Not found hence, attempting for 2nd last row");
            balance = byUserSchemeDetailsEntityId
                    .get(byUserSchemeDetailsEntityId.size() - 1)
                    .getBalance();
        }
        return balance;
    }

    double getCurrentValuation(Long fundId, Double balance) {
        MFSchemeDTO scheme =
                navService.getNavByDateWithRetry(fundId, LocalDateUtility.getAdjustedDate(LocalDate.now()));
        return balance * Double.parseDouble(scheme.nav());
    }

    // method to calculate XIRR for a given set of payments and dates
    double calculateXIRR(double[] payments, LocalDate[] dates) {
        // use Newton's method with an initial guess of 0.1
        return newtonsMethod(0.1, payments, dates);
    }

    // method to implement Newton's method to find the root of the polynomial equation for XIRR
    double newtonsMethod(double guess, double[] payments, LocalDate[] days) {
        double x0 = guess;
        double x1 = 0.0;
        double err = 1e+100;

        while (err > TOLERANCE) {
            x1 = x0 - total_f_xirr(payments, days, x0) / total_df_xirr(payments, days, x0);
            err = Math.abs(x1 - x0);
            x0 = x1;
        }

        return x0;
    }

    // helper method to calculate the sum of the derivative of the polynomial equation for XIRR
    double total_df_xirr(double[] payments, LocalDate[] days, double x) {
        double resf = 0.0;
        for (int i = 0; i < payments.length; i++) {
            resf = resf + df_xirr(payments[i], days[i], days[0], x);
        }
        return resf;
    }

    // helper method to calculate the derivative of the polynomial equation for XIRR
    double df_xirr(double payment, LocalDate day, LocalDate day1, double x) {
        return (1.0 / 365.0)
                * dateDiff(day, day1)
                * payment
                * Math.pow((x + 1.0), ((dateDiff(day, day1) / 365.0) - 1.0));
    }

    // helper method to calculate the sum of the polynomial equation for XIRR
    double total_f_xirr(double[] payments, LocalDate[] days, double x) {
        double resf = 0.0;
        for (int i = 0; i < payments.length; i++) {
            resf = resf + f_xirr(payments[i], days[i], days[0], x);
        }
        return resf;
    }

    // helper method to calculate the value of the polynomial equation for XIRR
    double f_xirr(double payment, LocalDate day, LocalDate day1, double x) {
        return payment * Math.pow((1.0 + x), (dateDiff(day, day1) / 365.0));
    }

    // helper method to calculate the difference between two dates in days
    double dateDiff(LocalDate day, LocalDate day1) {
        return ChronoUnit.DAYS.between(day, day1);
    }
}
