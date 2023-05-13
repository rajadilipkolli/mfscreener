/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.models.response;

import com.example.mfscreener.models.PortfolioDetailsDTO;
import java.io.Serializable;
import java.util.List;

public record PortfolioResponse(Float totalPortfolioValue, List<PortfolioDetailsDTO> portfolioDetailsDTOS)
        implements Serializable {}
