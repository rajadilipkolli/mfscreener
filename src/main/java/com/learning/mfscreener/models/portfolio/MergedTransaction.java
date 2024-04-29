package com.learning.mfscreener.models.portfolio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MergedTransaction {
    private final LocalDate date;
    private final List<UserTransactionDTO> transactions;

    public MergedTransaction(LocalDate date) {
        this.date = date;
        this.transactions = new ArrayList<>();
    }

    public void add(UserTransactionDTO txn) {
        this.transactions.add(txn);
    }

    public LocalDate getDate() {
        return date;
    }

    public List<UserTransactionDTO> getTransactions() {
        return transactions;
    }
}
