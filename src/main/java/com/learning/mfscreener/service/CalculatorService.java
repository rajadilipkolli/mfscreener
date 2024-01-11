package com.learning.mfscreener.service;

import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import com.learning.mfscreener.repository.UserFolioDetailsEntityRepository;
import com.learning.mfscreener.repository.UserTransactionDetailsEntityRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalculatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorService.class);

    private final UserFolioDetailsEntityRepository userFolioDetailsEntityRepository;
    private final UserTransactionDetailsEntityRepository userTransactionDetailsEntityRepository;

    public CalculatorService(
            UserFolioDetailsEntityRepository userFolioDetailsEntityRepository,
            UserTransactionDetailsEntityRepository userTransactionDetailsEntityRepository) {
        this.userFolioDetailsEntityRepository = userFolioDetailsEntityRepository;
        this.userTransactionDetailsEntityRepository = userTransactionDetailsEntityRepository;
    }

    // method to calculate the total XIRR for a given PAN number
    public double calculateTotalXIRRByPan(String pan) {
        // get the map of fund id and XIRR value for all funds
        Map<Long, Double> xirrMap = calculateXIRRForAllFundsByPAN(pan);
        // initialize the total XIRR to zero
        double totalXirr = 0.0;
        // loop through the map
        for (Map.Entry<Long, Double> entry : xirrMap.entrySet()) {
            // get the fund id and XIRR value
            Long fundId = entry.getKey();
            Double xirr = entry.getValue();

            // add the XIRR value to the total XIRR
            totalXirr += xirr;
        }
        // return the total XIRR
        return totalXirr;
    }

    // method to calculate XIRR for all funds
    private Map<Long, Double> calculateXIRRForAllFundsByPAN(String pan) {
        // get all the funds
        List<UserFolioDetailsProjection> funds = userFolioDetailsEntityRepository.findByPan(pan);
        //        Iterable<Fund> funds = getFunds();
        // create a map to store the fund id and XIRR value
        Map<Long, Double> xirrMap = new HashMap<>();
        // loop through the funds
        for (UserFolioDetailsProjection fund : funds) {
            // get the fund id
            Long fundId = fund.getSchemeEntities().getFirst().getAmfi();
            Long schemeIdInDb = fund.getSchemeEntities().getFirst().getId();
            // calculate the XIRR for the fund
            Double xirr = calculateXIRR(fundId, schemeIdInDb);
            // put the fund id and XIRR value in the map
            xirrMap.put(fundId, xirr);
        }
        // return the map
        return xirrMap;
    }

    private Double calculateXIRR(Long fundId, Long schemeIdInDb) {
        List<UserTransactionDetailsProjection> byUserSchemeDetailsEntityId =
                userTransactionDetailsEntityRepository.findByUserSchemeDetailsEntity_IdAndTypeNotIn(
                        schemeIdInDb, List.of("STT_TAX", "STAMP_DUTY_TAX"));

        double[] payments = new double[byUserSchemeDetailsEntityId.size()];
        LocalDate[] dates = new LocalDate[byUserSchemeDetailsEntityId.size()];

        for (int i = 0; i < byUserSchemeDetailsEntityId.size(); i++) {
            UserTransactionDetailsProjection userTransactionDetailsProjection = byUserSchemeDetailsEntityId.get(i);
            payments[i] = userTransactionDetailsProjection.getAmount();
            dates[i] = userTransactionDetailsProjection.getTransactionDate();
        }
        return calculateXIRR(payments, dates);
    }

    // method to calculate XIRR for a given set of payments and dates
    private double calculateXIRR(double[] payments, LocalDate[] dates) {
        // use Newton's method with an initial guess of 0.1
        return newtonsMethod(0.1, payments, dates);
    }

    private double newtonsMethod(double d, double[] payments, LocalDate[] dates) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newtonsMethod'");
    }
}
