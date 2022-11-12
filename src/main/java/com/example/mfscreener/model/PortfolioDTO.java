package com.example.mfscreener.model;

import java.util.List;

public record PortfolioDTO(
        Float totalPortfolioValue, List<PortfolioDetailsDTO> portfolioDetailsDTOS) {}
