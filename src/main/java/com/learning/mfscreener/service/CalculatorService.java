package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import com.learning.mfscreener.models.response.XIRRResponse;
import com.learning.mfscreener.repository.UserFolioDetailsEntityRepository;
import com.learning.mfscreener.repository.UserTransactionDetailsEntityRepository;
import com.learning.mfscreener.utils.LocalDateUtility;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.decampo.xirr.NewtonRaphson;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;
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
    public List<XIRRResponse> calculateTotalXIRRByPan(String pan) {
        // get the map of fund id and XIRR value for all funds
        // TODO : return schemeName as well
        List<XIRRResponse> xirrResponseList = calculateXIRRForAllFundsByPAN(pan);
        // initialize the total XIRR to zero
        //        double totalXirr = 0.0;
        //        // loop through the map
        //        for (Map.Entry<Long, Double> entry : xirrResponseList.entrySet()) {
        //            // get the fund id and XIRR value
        //            Long fundId = entry.getKey();
        //            Double xirr = entry.getValue();
        //
        //            // add the XIRR value to the total XIRR
        //            totalXirr += xirr;
        //        }
        //        // return the total XIRR
        //        return totalXirr;
        return xirrResponseList;
    }

    // method to calculate XIRR for all funds
    List<XIRRResponse> calculateXIRRForAllFundsByPAN(String pan) {
        // get all the userFolioDetailsProjections
        List<UserFolioDetailsProjection> userFolioDetailsProjections = userFolioDetailsEntityRepository.findByPan(pan);

        // create a map to store the fund id and XIRR value
        List<XIRRResponse> xirrResponseList = new ArrayList<>();
        // loop through the userFolioDetailsProjections
        for (UserFolioDetailsProjection folioDetailsProjection : userFolioDetailsProjections) {

            folioDetailsProjection.getSchemeEntities().forEach(userSchemeDetailsEntityInfo -> {
                Long schemeIdInDb = userSchemeDetailsEntityInfo.getId();
                Long amfiId = userSchemeDetailsEntityInfo.getAmfi();
                // calculate the XIRR for the folioDetailsProjection
                double xirr = calculateXIRR(amfiId, schemeIdInDb);

                if (xirr != 0.0d) {
                    LOGGER.debug("adding XIRR for schemeId : {}", amfiId);
                    // put the folioDetailsProjection id and XIRR value in the map
                    xirrResponseList.add(new XIRRResponse(
                            folioDetailsProjection.getFolio(),
                            amfiId,
                            userSchemeDetailsEntityInfo.getScheme(),
                            xirr * 100));
                } else {
                    LOGGER.info("Consolidated portfolio");
                }
            });
        }
        // return the map
        return xirrResponseList;
    }

    double calculateXIRR(Long fundId, Long schemeIdInDb) {
        LOGGER.debug("Calculating XIRR for fund ID : {} & schemeIdInDB :{}", fundId, schemeIdInDb);
        List<UserTransactionDetailsProjection> byUserSchemeDetailsEntityId =
                userTransactionDetailsEntityRepository
                        .findByUserSchemeDetailsEntity_IdAndTypeNotInOrderByTransactionDateAsc(
                                schemeIdInDb, List.of("STT_TAX", "STAMP_DUTY_TAX", "MISC"));

        double xirrValue = 0.0d;
        if (!byUserSchemeDetailsEntityId.isEmpty()) {
            double currentBalance = getBalance(byUserSchemeDetailsEntityId);
            // in case if Additional Allotment is done then amount will be null
            List<Transaction> transactionList = new ArrayList<>(byUserSchemeDetailsEntityId.stream()
                    .filter(userTransactionDetailsProjection -> userTransactionDetailsProjection.getAmount() != null)
                    .map(userTransactionDetailsProjection -> new Transaction(
                            -(userTransactionDetailsProjection.getAmount()),
                            userTransactionDetailsProjection.getTransactionDate()))
                    .toList());
            if (currentBalance != 0.0) {
                // Add current Value and current date
                transactionList.add(new Transaction(getCurrentValuation(fundId, currentBalance), LocalDate.now()));
            }
            xirrValue = Xirr.builder()
                    .withTransactions(transactionList)
                    .withGuess(0.01)
                    .withNewtonRaphsonBuilder(NewtonRaphson.builder()
                            .withFunction(x -> x)
                            .withIterations(1000)
                            .withTolerance(TOLERANCE))
                    .xirr();
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

    double getCurrentValuation(Long fundId, Double balance) {
        MFSchemeDTO scheme = navService.getNavByDateWithRetry(fundId, LocalDateUtility.getAdjustedDate());
        return balance * Double.parseDouble(scheme.nav());
    }
}
