package com.example.mfscreener.model;

import java.io.Serializable;

public record Scheme(String schemeCode, String payout, String schemeName, String nav, String date)
        implements Serializable {}
