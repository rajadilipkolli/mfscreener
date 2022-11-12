/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.model;

import java.io.Serializable;

public record Scheme(String schemeCode, String payout, String schemeName, String nav, String date)
        implements Serializable {}
