package com.learning.mfscreener.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.learning.mfscreener.exception.XIRRCalculationException;
import com.learning.mfscreener.models.projection.UserTransactionDetailsProjection;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.decampo.xirr.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class XirrCalculatorHelperTest {

    @Test
    @DisplayName("Constructor should throw UnsupportedOperationException")
    void constructorShouldThrowException() throws NoSuchMethodException {
        Constructor<XirrCalculatorHelper> constructor = XirrCalculatorHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance())
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .satisfies(exception -> {
                    UnsupportedOperationException cause = (UnsupportedOperationException) exception.getCause();
                    assertThat(cause.getMessage()).contains("Constructor can't be initialized");
                });
    }

    @Test
    @DisplayName("Should return balance from last transaction when available")
    void shouldReturnBalanceFromLastTransaction() {
        List<UserTransactionDetailsProjection> projections = new ArrayList<>();

        UserTransactionDetailsProjection firstProjection = mock(UserTransactionDetailsProjection.class);
        when(firstProjection.getBalance()).thenReturn(100.0);

        UserTransactionDetailsProjection lastProjection = mock(UserTransactionDetailsProjection.class);
        when(lastProjection.getBalance()).thenReturn(150.0);

        projections.add(firstProjection);
        projections.add(lastProjection);

        Double result = XirrCalculatorHelper.getBalance(projections);

        assertThat(result).isEqualTo(150.0);
    }

    @Test
    @DisplayName("Should return balance from second last transaction when last is null")
    void shouldReturnBalanceFromSecondLastTransactionWhenLastIsNull() {
        List<UserTransactionDetailsProjection> projections = new ArrayList<>();

        UserTransactionDetailsProjection firstProjection = mock(UserTransactionDetailsProjection.class);
        when(firstProjection.getBalance()).thenReturn(100.0);

        UserTransactionDetailsProjection secondProjection = mock(UserTransactionDetailsProjection.class);
        when(secondProjection.getBalance()).thenReturn(125.0);

        UserTransactionDetailsProjection lastProjection = mock(UserTransactionDetailsProjection.class);
        when(lastProjection.getBalance()).thenReturn(null);

        projections.add(firstProjection);
        projections.add(secondProjection);
        projections.add(lastProjection);

        Double result = XirrCalculatorHelper.getBalance(projections);

        assertThat(result).isEqualTo(125.0);
    }

    @Test
    @DisplayName("Should throw exception when insufficient data and last balance is null")
    void shouldThrowExceptionWhenInsufficientDataAndLastBalanceIsNull() {
        List<UserTransactionDetailsProjection> projections = new ArrayList<>();

        UserTransactionDetailsProjection projection = mock(UserTransactionDetailsProjection.class);
        when(projection.getBalance()).thenReturn(null);

        projections.add(projection);

        assertThatThrownBy(() -> XirrCalculatorHelper.getBalance(projections))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient data to calculate balance.");
    }

    @Test
    @DisplayName("Should throw exception when empty list")
    void shouldThrowExceptionWhenEmptyList() {
        List<UserTransactionDetailsProjection> projections = new ArrayList<>();

        assertThatThrownBy(() -> XirrCalculatorHelper.getBalance(projections))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("Should calculate XIRR value with valid transactions")
    void shouldCalculateXirrValueWithValidTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        // Add investment (negative cash flow)
        transactions.add(new Transaction(-1000.0, LocalDate.of(2023, 1, 1)));
        transactions.add(new Transaction(-500.0, LocalDate.of(2023, 6, 1)));

        // Add return (positive cash flow)
        transactions.add(new Transaction(1600.0, LocalDate.of(2023, 12, 31)));

        double result = XirrCalculatorHelper.calculateXirrValue(transactions, 123L);

        // XIRR should be a reasonable value (between -1 and 1 typically)
        assertThat(result).isGreaterThan(-1.0).isLessThan(1.0);
        assertThat(result).isNotEqualTo(-0.00001); // Should not be the default value
    }

    @Test
    @DisplayName("Should return default value when only one transaction")
    void shouldReturnDefaultValueWhenOnlyOneTransaction() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(-1000.0, LocalDate.of(2023, 1, 1)));

        double result = XirrCalculatorHelper.calculateXirrValue(transactions, 123L);

        assertThat(result).isEqualTo(-0.00001);
    }

    @Test
    @DisplayName("Should return default value when no transactions")
    void shouldReturnDefaultValueWhenNoTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        double result = XirrCalculatorHelper.calculateXirrValue(transactions, 123L);

        assertThat(result).isEqualTo(-0.00001);
    }

    @Test
    @DisplayName("Should throw XIRRCalculationException when calculation fails")
    void shouldThrowXIRRCalculationExceptionWhenCalculationFails() {
        List<Transaction> transactions = new ArrayList<>();

        // Add invalid transactions that would cause XIRR calculation to fail
        // All positive cash flows (no investments)
        transactions.add(new Transaction(1000.0, LocalDate.of(2023, 1, 1)));
        transactions.add(new Transaction(500.0, LocalDate.of(2023, 6, 1)));

        Long fundId = 456L;

        assertThatThrownBy(() -> XirrCalculatorHelper.calculateXirrValue(transactions, fundId))
                .isInstanceOf(XIRRCalculationException.class)
                .hasMessage("Unable to calculate XIRR for fundId " + fundId);
    }

    @Test
    @DisplayName("Should handle edge case with very small transactions")
    void shouldHandleEdgeCaseWithVerySmallTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        // Add very small transactions
        transactions.add(new Transaction(-0.01, LocalDate.of(2023, 1, 1)));
        transactions.add(new Transaction(0.02, LocalDate.of(2023, 12, 31)));

        double result = XirrCalculatorHelper.calculateXirrValue(transactions, 789L);

        // Should either calculate a valid XIRR or return default value, but not throw exception
        assertThat(result).isInstanceOf(Double.class);
    }

    @Test
    @DisplayName("Should handle transactions with same dates")
    void shouldHandleTransactionsWithSameDates() {
        List<Transaction> transactions = new ArrayList<>();

        LocalDate sameDate = LocalDate.of(2023, 6, 15);
        transactions.add(new Transaction(-1000.0, sameDate));
        transactions.add(new Transaction(1100.0, sameDate));

        // This should either calculate or throw an exception, but not crash
        try {
            double result = XirrCalculatorHelper.calculateXirrValue(transactions, 999L);
            assertThat(result).isInstanceOf(Double.class);
        } catch (XIRRCalculationException e) {
            assertThat(e.getMessage()).contains("Unable to calculate XIRR for fundId 999");
        }
    }
}
