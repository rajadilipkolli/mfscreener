/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record UserTransactionDTO(
        String date,
        String description,
        String amount,
        String units,
        String nav,
        String balance,
        String type,
        @JsonProperty("dividend_rate") String dividendRate)
        implements Serializable {}
