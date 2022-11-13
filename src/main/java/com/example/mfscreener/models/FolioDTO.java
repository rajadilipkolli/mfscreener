/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FolioDTO(
        String folio,
        String amc,
        @JsonProperty("PAN") String pan,
        @JsonProperty("KYC") String kyc,
        @JsonProperty("PANKYC") String panKyc,
        List<SchemeDTO> schemes) {}
