package com.learning.mfscreener.models.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GainEntry(
        String finYear,
        Fund fund,
        FundType fundType,
        LocalDate purchaseDate,
        BigDecimal purchaseNav,
        BigDecimal purchaseValue,
        BigDecimal stampDuty,
        LocalDate sellDate,
        BigDecimal nav,
        BigDecimal saleValue,
        BigDecimal stt,
        BigDecimal gainUnits) {}
