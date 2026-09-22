package com.learning.mfscreener.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

class ColumnParsingUtilityTest {

    /** Verifies field extraction from the six-column AMFI layout. */
    @Test
    void test6ColumnAmfi() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/amfi_6_column.txt"));
        ColumnParsingUtility utility = new ColumnParsingUtility(lines.get(0), ";");
        String[] tokenize = lines.get(4).split(";");

        assertThat(utility.extractFieldValue(tokenize, AppConstants.SCHEME_CODE))
                .isEqualTo("120465");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.SCHEME_NAME))
                .isEqualTo("Aditya Birla Sun Life Flexi Cap Fund - Dividend");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.NET_ASSET_VALUE))
                .isEqualTo("139.81");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.DATE)).isEqualTo("22-Sep-2026");
    }

    /** Verifies field extraction from the eight-column AMFI layout. */
    @Test
    void test8ColumnAmfi() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/amfi_8_column.txt"));
        ColumnParsingUtility utility = new ColumnParsingUtility(lines.get(0), ";");
        String[] tokenize = lines.get(4).split(";");

        assertThat(utility.extractFieldValue(tokenize, AppConstants.SCHEME_CODE))
                .isEqualTo("120465");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.SCHEME_NAME))
                .isEqualTo("Aditya Birla Sun Life Flexi Cap Fund");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.PLAN)).isEqualTo("Direct");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.OPTION)).isEqualTo("Dividend");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.NET_ASSET_VALUE))
                .isEqualTo("139.81");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.DATE)).isEqualTo("22-Sep-2026");
    }

    /** Verifies field extraction from a historical AMFI report. */
    @Test
    void testHistoricalReport() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/historical_report.txt"));
        ColumnParsingUtility utility = new ColumnParsingUtility(lines.get(0), ";");
        String[] tokenize = lines.get(4).split(";");

        assertThat(utility.extractFieldValue(tokenize, AppConstants.SCHEME_CODE))
                .isEqualTo("120465");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.SCHEME_NAME))
                .isEqualTo("Aditya Birla Sun Life Flexi Cap Fund - Dividend");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.NET_ASSET_VALUE))
                .isEqualTo("139.81");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.DATE)).isEqualTo("22-Sep-2026");
    }

    /** Verifies field extraction from the bundled NAV data. */
    @Test
    void testBundledNavData() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/nav/31Jan2018Navdata.csv"));
        ColumnParsingUtility utility = new ColumnParsingUtility(lines.get(0), ",");
        String[] tokenize = lines.get(1).split(",");

        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_NAV)).isEqualTo("10.0996");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_NAV_DATE))
                .isEqualTo("1/31/2018");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_SCHEME_ID))
                .isEqualTo("121721");
    }

    /** Verifies field extraction from the bundled closed-scheme NAV data. */
    @Test
    void testBundledNavDataDump() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("src/test/resources/nav/31Jan2018Navdatadump.csv"));
        ColumnParsingUtility utility = new ColumnParsingUtility(lines.get(0), ",");
        String[] tokenize = lines.get(1).split(",");

        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_NAV)).isEqualTo("37.7972");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_NAV_DATE))
                .isEqualTo("1/31/2018");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_SCHEME_ID))
                .isEqualTo("125301");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_FUND_HOUSE))
                .isEqualTo("HDFC Mutual Fund");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_SCHEME_NAME))
                .isEqualTo("HDFC Equity Savings Fund");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_PAY_OUT))
                .isEqualTo("INF179K01WH8");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_TYPE)).isEqualTo("Open Ended Schemes");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_CATEGORY))
                .isEqualTo("Equity Scheme");
        assertThat(utility.extractFieldValue(tokenize, AppConstants.CSV_SUB_CATEGORY))
                .isEqualTo("Equity Savings");
    }
}
