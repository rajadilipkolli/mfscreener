package com.learning.mfscreener.utils;

import com.learning.mfscreener.models.portfolio.FundType;
import com.learning.mfscreener.models.portfolio.TransactionType;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import java.util.List;

public class FundTypeUtility {

    /**
     *
     * Detect Fund Type.
     *  - UNKNOWN if there are no redemption transactions
     *  - EQUITY if STT_TAX transactions are present in the portfolio
     *  - DEBT if no STT_TAX transactions are present along with redemptions
     *
     * @param transactions list of transactions for a single fund parsed from the CAS
     * @return type of fund
     */
    public static FundType deriveFundTypeFromTransactions(List<UserTransactionDTO> transactions) {
        boolean valid = transactions.stream()
                .anyMatch(x -> x.units() != null && x.units() < 0 && x.type() != TransactionType.REVERSAL);

        if (!valid) {
            return FundType.UNKNOWN;
        }
        if (transactions.stream().anyMatch(x -> x.type() == TransactionType.STT_TAX)) {
            return FundType.EQUITY;
        } else {
            return FundType.DEBT;
        }
    }
}
