package com.example.mfscreener.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Meta {

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
