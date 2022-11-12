/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StatementPeriodDTO {

    String from;

    @JsonProperty("to")
    String myto;
}
