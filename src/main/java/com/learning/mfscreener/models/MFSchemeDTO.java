/* Licensed under Apache-2.0 2021-2022. */
package com.learning.mfscreener.models;

import java.io.Serializable;

public record MFSchemeDTO(Long schemeCode, String payout, String schemeName, String nav, String date)
        implements Serializable {
    public MFSchemeDTO withNavAndDate(String navValue, String navDate) {
        return new MFSchemeDTO(schemeCode(), payout(), schemeName(), navValue, navDate);
    }
}
