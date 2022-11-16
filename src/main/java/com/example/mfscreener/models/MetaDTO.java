/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MetaDTO {

    @JsonProperty("fund_house")
    private String fundHouse;

    @JsonProperty("scheme_type")
    private String schemeType;

    @JsonProperty("scheme_category")
    private String schemeCategory;

    @JsonProperty("scheme_code")
    private String schemeCode;

    @JsonProperty("scheme_name")
    private String schemeName;
}
