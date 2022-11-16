/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.models;

import java.io.Serializable;

public record MFSchemeDTO(String schemeCode, String payout, String schemeName, String nav, String date)
        implements Serializable {}
