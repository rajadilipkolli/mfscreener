package com.learning.mfscreener.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.learning.mfscreener.models.portfolio.FundType;
import com.learning.mfscreener.models.portfolio.TransactionType;
import com.learning.mfscreener.models.portfolio.UserTransactionDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FundTypeUtilityTest {

    @Test
    @DisplayName("Should return UNKNOWN when no redemption transactions present")
    void shouldReturnUnknownWhenNoRedemptionTransactions() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add only purchase transactions (positive units)
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));
        transactions.add(createTransaction(TransactionType.PURCHASE_SIP, 50.0));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.UNKNOWN);
    }

    @Test
    @DisplayName("Should return UNKNOWN when transactions list is empty")
    void shouldReturnUnknownWhenTransactionsListIsEmpty() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.UNKNOWN);
    }

    @Test
    @DisplayName("Should return UNKNOWN when only null units transactions")
    void shouldReturnUnknownWhenOnlyNullUnitsTransactions() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add transactions with null units (like tax transactions)
        transactions.add(createTransactionWithNullUnits(TransactionType.STT_TAX));
        transactions.add(createTransactionWithNullUnits(TransactionType.STAMP_DUTY_TAX));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.UNKNOWN);
    }

    @Test
    @DisplayName("Should return UNKNOWN when only reversal transactions with negative units")
    void shouldReturnUnknownWhenOnlyReversalTransactionsWithNegativeUnits() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add reversal transactions (should be ignored)
        transactions.add(createTransaction(TransactionType.REVERSAL, -50.0));
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.UNKNOWN);
    }

    @Test
    @DisplayName("Should return EQUITY when STT_TAX transaction is present with redemptions")
    void shouldReturnEquityWhenSTTTaxTransactionPresent() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add purchase transaction
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));

        // Add redemption transaction (negative units)
        transactions.add(createTransaction(TransactionType.REDEMPTION, -50.0));

        // Add STT tax transaction
        transactions.add(createTransactionWithNullUnits(TransactionType.STT_TAX));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.EQUITY);
    }

    @Test
    @DisplayName("Should return EQUITY when STT_TAX transaction is present with switch out")
    void shouldReturnEquityWhenSTTTaxTransactionPresentWithSwitchOut() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add purchase transaction
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));

        // Add switch out transaction (negative units)
        transactions.add(createTransaction(TransactionType.SWITCH_OUT, -30.0));

        // Add STT tax transaction
        transactions.add(createTransactionWithNullUnits(TransactionType.STT_TAX));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.EQUITY);
    }

    @Test
    @DisplayName("Should return DEBT when no STT_TAX but has redemption transactions")
    void shouldReturnDebtWhenNoSTTTaxButHasRedemptionTransactions() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add purchase transaction
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));

        // Add redemption transaction (negative units)
        transactions.add(createTransaction(TransactionType.REDEMPTION, -50.0));

        // Add stamp duty tax (not STT)
        transactions.add(createTransactionWithNullUnits(TransactionType.STAMP_DUTY_TAX));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.DEBT);
    }

    @Test
    @DisplayName("Should return DEBT when no STT_TAX but has switch out transactions")
    void shouldReturnDebtWhenNoSTTTaxButHasSwitchOutTransactions() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add purchase transaction
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));

        // Add switch out transaction (negative units)
        transactions.add(createTransaction(TransactionType.SWITCH_OUT, -25.0));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.DEBT);
    }

    @Test
    @DisplayName("Should return EQUITY even with multiple transaction types when STT_TAX is present")
    void shouldReturnEquityWithMultipleTransactionTypesWhenSTTTaxPresent() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add various transaction types
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));
        transactions.add(createTransaction(TransactionType.PURCHASE_SIP, 50.0));
        transactions.add(createTransaction(TransactionType.SWITCH_IN, 25.0));
        transactions.add(createTransaction(TransactionType.REDEMPTION, -30.0));
        transactions.add(createTransaction(TransactionType.SWITCH_OUT, -20.0));

        // Add various tax transactions
        transactions.add(createTransactionWithNullUnits(TransactionType.STAMP_DUTY_TAX));
        transactions.add(createTransactionWithNullUnits(TransactionType.STT_TAX));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.EQUITY);
    }

    @Test
    @DisplayName("Should return DEBT with multiple transaction types when no STT_TAX")
    void shouldReturnDebtWithMultipleTransactionTypesWhenNoSTTTax() {
        List<UserTransactionDTO> transactions = new ArrayList<>();

        // Add various transaction types
        transactions.add(createTransaction(TransactionType.PURCHASE, 100.0));
        transactions.add(createTransaction(TransactionType.PURCHASE_SIP, 50.0));
        transactions.add(createTransaction(TransactionType.SWITCH_IN, 25.0));
        transactions.add(createTransaction(TransactionType.REDEMPTION, -30.0));
        transactions.add(createTransaction(TransactionType.SWITCH_OUT, -20.0));

        // Add only stamp duty tax (no STT)
        transactions.add(createTransactionWithNullUnits(TransactionType.STAMP_DUTY_TAX));

        FundType result = FundTypeUtility.deriveFundTypeFromTransactions(transactions);

        assertThat(result).isEqualTo(FundType.DEBT);
    }

    private UserTransactionDTO createTransaction(TransactionType type, Double units) {
        return new UserTransactionDTO(
                LocalDate.of(2023, 6, 15), "Test Transaction", 1000.0, units, 100.0, units, type, null);
    }

    private UserTransactionDTO createTransactionWithNullUnits(TransactionType type) {
        return new UserTransactionDTO(
                LocalDate.of(2023, 6, 15), "Test Tax Transaction", 0.1, null, null, null, type, null);
    }
}
