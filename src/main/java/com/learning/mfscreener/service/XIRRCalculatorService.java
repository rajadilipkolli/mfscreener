package com.learning.mfscreener.service;

import com.learning.mfscreener.config.logging.Loggable;
import com.learning.mfscreener.models.MFSchemeDTO;
import com.learning.mfscreener.models.projection.UserFolioDetailsProjection;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import com.learning.mfscreener.models.response.XIRRResponse;
import com.learning.mfscreener.utils.LocalDateUtility;
import com.learning.mfscreener.utils.XirrCalculatorHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.decampo.xirr.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Loggable
@Transactional(readOnly = true)
public class XIRRCalculatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XIRRCalculatorService.class);

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
        LocalDate effectiveDate = LocalDateUtility.getAdjustedDateOrDefault(asOfDate);
        LOGGER.debug("Effective date used for calculation: {}", effectiveDate);
        return calculateXIRRForAllFundsByPAN(pan, effectiveDate);
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
        List<UserTransactionDetailsProjection> transactionDetailsProjectionList =
                userTransactionDetailsService.fetchTransactions(schemeIdInDb, asOfDate);
        return computeXIRR(transactionDetailsProjectionList, fundId, asOfDate);
    }

    double computeXIRR(List<UserTransactionDetailsProjection> transactions, Long fundId, LocalDate asOfDate) {
        LOGGER.debug("Starting XIRR calculation for fundId : {} with as of Date : {}", fundId, asOfDate);
        double xirrValue = 0.0d;
        if (!transactions.isEmpty()) {
            double currentBalance = XirrCalculatorHelper.getBalance(transactions);
            List<Transaction> transactionList = buildTransactionList(transactions, currentBalance, fundId, asOfDate);
            xirrValue = XirrCalculatorHelper.calculateXirrValue(transactionList, fundId);
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
                        && transactionList.getFirst().getWhen().equals(asOfDate))) {
            // Add current Value and current date
            transactionList.add(new Transaction(getCurrentValuation(fundId, currentBalance, asOfDate), asOfDate));
        }
        return transactionList;
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
