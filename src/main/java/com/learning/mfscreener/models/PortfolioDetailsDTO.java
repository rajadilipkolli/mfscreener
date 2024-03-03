/* Licensed under Apache-2.0 2022-2024. */
package com.learning.mfscreener.models;

import java.io.Serializable;

public record PortfolioDetailsDTO(double totalValue, String schemeName, String folioNumber, String date)
        implements Serializable {}
