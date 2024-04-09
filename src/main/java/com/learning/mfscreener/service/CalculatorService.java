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
        // get all the funds
        List<UserFolioDetailsProjection> funds = userFolioDetailsEntityRepository.findByPan(pan);
        //        Iterable<Fund> funds = getFunds();
        // create a map to store the fund id and XIRR value
        List<XIRRResponse> xirrResponseList = new ArrayList<>();
        // loop through the funds
        for (UserFolioDetailsProjection fund : funds) {
            // get the fund id
            Long amfiId = fund.getSchemeEntities().get(0).getAmfi();
            if (amfiId == null) {
                LOGGER.error("FundID not available for fund :{} hence skipping", fund);
                continue;
            }
            // TODO calculate individually and at overall level as well
            Long schemeIdInDb = fund.getSchemeEntities().get(0).getId();
            // calculate the XIRR for the fund
            double xirr = calculateXIRR(amfiId, schemeIdInDb);
            LOGGER.debug("adding XIRR for schemeId : {}", amfiId);

            // put the fund id and XIRR value in the map
            xirrResponseList.add(new XIRRResponse(
                    fund.getFolio(), amfiId, fund.getSchemeEntities().get(0).getScheme(), xirr * 100));
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

        double currentBalance = getBalance(byUserSchemeDetailsEntityId);
        List<Transaction> transactionList = new ArrayList<>(byUserSchemeDetailsEntityId.stream()
                .map(userTransactionDetailsProjection -> new Transaction(
                        -(userTransactionDetailsProjection.getAmount()),
                        userTransactionDetailsProjection.getTransactionDate()))
                .toList());
        // Add current Value and current date
        transactionList.add(new Transaction(getCurrentValuation(fundId, currentBalance), LocalDate.now()));
        return new Xirr(transactionList).xirr();
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
