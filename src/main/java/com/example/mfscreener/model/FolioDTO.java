/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class FolioDTO {

    @JsonProperty("folio")
    String folio;

    @JsonProperty("amc")
    String amc;

    @JsonProperty("PAN")
    String pAN;

    @JsonProperty("KYC")
    String kYC;

    @JsonProperty("PANKYC")
    String pANKYC;

    @JsonProperty("schemes")
    List<SchemeDTO> schemes;
}
