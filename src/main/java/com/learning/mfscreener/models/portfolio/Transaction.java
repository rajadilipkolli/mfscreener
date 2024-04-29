package com.learning.mfscreener.models.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(LocalDate txnDate, BigDecimal units, BigDecimal nav, BigDecimal tax) {}
