/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransactionDTO {

    String date;
    String description;
    String amount;
    String units;
    String nav;
    String balance;
    String type;

    @JsonProperty("dividend_rate")
    String dividendRate;
}
