package com.learning.mfscreener.utils;

import java.util.HashMap;
import java.util.Map;

public class ColumnParsingUtility {
    private final Map<String, Integer> headerMap = new HashMap<>();

    public ColumnParsingUtility(String headerLine, String delimiter) {
        if (headerLine != null && !headerLine.trim().isEmpty()) {
            String[] headerNames = headerLine.split(delimiter, -1);
            for (int i = 0; i < headerNames.length; i++) {
                String colName = normalize(headerNames[i]);
                headerMap.put(colName, i);
            }
        }
    }

    private String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public String extractFieldValue(String[] row, String... aliases) {
        for (String alias : aliases) {
            String normAlias = normalize(alias);
            Integer index = headerMap.get(normAlias);
            if (index != null && index < row.length) {
                String val = row[index];
                if (val != null) {
                    return val.trim();
                }
            }
        }
        return null;
    }
}
