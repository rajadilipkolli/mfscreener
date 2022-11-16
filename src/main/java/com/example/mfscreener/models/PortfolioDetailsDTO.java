/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import java.io.Serializable;

public record PortfolioDetailsDTO(Float totalValue, String schemeName, String folioNumber)
        implements Serializable {}
