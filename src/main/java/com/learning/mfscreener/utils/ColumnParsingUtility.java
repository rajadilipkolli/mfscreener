package com.learning.mfscreener.utils;

import java.util.HashMap;
import java.util.Map;

public class ColumnParsingUtility {
    private final Map<String, Integer> headerMap = new HashMap<>();

    /**
     * Creates a column lookup from a delimited header row.
     *
     * @param headerLine delimited header row
     * @param delimiter regular expression used to split the header
     */
    public ColumnParsingUtility(String headerLine, String delimiter) {
        if (headerLine != null && !headerLine.trim().isEmpty()) {
            String[] headerNames = headerLine.split(delimiter, -1);
            for (int i = 0; i < headerNames.length; i++) {
                String colName = normalize(headerNames[i]);
                headerMap.put(colName, i);
            }
        }
    }

    /**
     * Normalizes a column name for case- and whitespace-insensitive matching.
     *
     * @param name column name
     * @return normalized column name, or an empty string for {@code null}
     */
    private String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("^\"+|\"+$", "")
                .trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*/\\s*", "/")
                .toLowerCase();
    }

    /**
     * Extracts a value using the first alias present in the header.
     *
     * @param row values from a data row
     * @param aliases accepted column names in priority order
     * @return the trimmed field value, or {@code null} when no alias maps to the row
     */
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
