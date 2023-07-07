/* Licensed under Apache-2.0 2022. */
package com.learning.mfscreener.models.response;

import com.learning.mfscreener.models.PortfolioDetailsDTO;
import java.io.Serializable;
import java.util.List;

public record PortfolioResponse(Double totalPortfolioValue, List<PortfolioDetailsDTO> portfolioDetailsDTOS)
        implements Serializable {}
