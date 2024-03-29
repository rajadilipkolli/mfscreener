/* Licensed under Apache-2.0 2021-2024. */
package com.learning.mfscreener.models;

import java.io.Serializable;

public record MFSchemeDTO(
        String amc, Long schemeCode, String payout, String schemeName, String nav, String date, String schemeType)
        implements Serializable {

    public MFSchemeDTO withNavAndDateAndSchemeType(String schemeType, String navValue, String navDate) {
        return new MFSchemeDTO(amc(), schemeCode(), payout(), schemeName(), navValue, navDate, schemeType);
    }
}
