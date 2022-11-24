/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record MetaDTO(
        @JsonProperty("fund_house") String fundHouse,
        @JsonProperty("scheme_type") String schemeType,
        @JsonProperty("scheme_category") String schemeCategory,
        @JsonProperty("scheme_code") String schemeCode,
        @JsonProperty("scheme_name") String schemeName)
        implements Serializable {}
