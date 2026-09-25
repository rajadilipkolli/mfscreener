/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.models.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

public record UserFolioDTO(
        String folio,
        String amc,
        @JsonProperty("PAN") String pan,
        @JsonProperty("KYC") String kyc,
        @JsonProperty("PANKYC") String panKyc,
        List<UserSchemeDTO> schemes)
        implements Serializable {}
