/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.mfscreener.models.Transaction;
import com.example.mfscreener.models.TransactionType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class XIRRServiceTest {

    private final XIRRService xirrService = new XIRRService();

    @Test
    void calculateXIRR() {
        List<Transaction> cashflows = new ArrayList<>();
        cashflows.add(new Transaction(LocalDate.of(2022, 1, 1), 10000.0, TransactionType.BUY));
        cashflows.add(new Transaction(LocalDate.of(2022, 3, 1), 5000.0, TransactionType.BUY));
        cashflows.add(new Transaction(LocalDate.of(2022, 5, 1), 15000.0, TransactionType.REDEEM));

        double xirr = xirrService.calculateXIRR(cashflows);

        // The expected result has been calculated manually using a financial calculator
        // or an online XIRR calculator
        assertEquals(0.6187, xirr, 0.0001);
    }
}
