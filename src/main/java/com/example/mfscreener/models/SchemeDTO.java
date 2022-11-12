/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class SchemeDTO {

    String scheme;
    String isin;
    String amfi;
    String advisor;

    @JsonProperty("rta_code")
    String rtaCode;

    String rta;

    @JsonProperty("open")
    String myopen;

    String close;

    @JsonProperty("close_calculated")
    String closeCalculated;

    @JsonProperty("valuation")
    ValuationDTO valuation;

    @JsonProperty("transactions")
    List<TransactionDTO> transactions;
}
