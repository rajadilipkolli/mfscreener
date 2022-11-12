/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StatementPeriodDTO(String from, @JsonProperty("to") String myto) {}
