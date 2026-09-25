/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.models.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UserTransactionDTO(
        LocalDate date,
        String description,
        BigDecimal amount,
        BigDecimal units,
        BigDecimal nav,
        BigDecimal balance,
        TransactionType type,
        @JsonProperty("dividend_rate") String dividendRate)
        implements Serializable {}
