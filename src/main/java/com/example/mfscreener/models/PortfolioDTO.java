/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models;

import java.util.List;

public record PortfolioDTO(
        Float totalPortfolioValue, List<PortfolioDetailsDTO> portfolioDetailsDTOS) {}
