package com.learning.mfscreener.models.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProcessCasResponse(
        Map<String, Map<String, Object>> summaryByFY,
        BigDecimal investedAmount,
        Double currentValue,
        List<String> errors,
        String importSummary) {

    public ProcessCasResponse withImportSummary(String response) {
        return new ProcessCasResponse(summaryByFY, investedAmount, currentValue, errors, response);
    }
}
