/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SchemeDTO(
        String scheme,
        String isin,
        Long amfi,
        String advisor,
        @JsonProperty("rta_code") String rtaCode,
        String type,
        String rta,
        @JsonProperty("open") String myopen,
        String close,
        @JsonProperty("close_calculated") String closeCalculated,
        @JsonProperty("valuation") ValuationDTO valuation,
        @JsonProperty("transactions") List<TransactionDTO> transactions) {}
