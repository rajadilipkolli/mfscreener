/* Licensed under Apache-2.0 2023. */
package com.example.mfscreener.models;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Transaction {

    private LocalDate date;
    private double amount;
    private TransactionType type;
}
