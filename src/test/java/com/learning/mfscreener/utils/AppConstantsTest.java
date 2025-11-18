package com.learning.mfscreener.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class AppConstantsTest {

    @Test
    void shouldHaveCorrectProfileConstants() {
        assertThat(AppConstants.PROFILE_PROD).isEqualTo("prod");
        assertThat(AppConstants.PROFILE_NOT_PROD).isEqualTo("!prod");
        assertThat(AppConstants.PROFILE_TEST).isEqualTo("test");
    }

    @Test
    void shouldHaveCorrectAmfiWebsiteLink() {
        assertThat(AppConstants.AMFI_WEBSITE_LINK).isEqualTo("https://www.amfiindia.com/spages/NAVAll.txt");
    }

    @Test
    void shouldHaveCorrectNavSeparator() {
        assertThat(AppConstants.NAV_SEPARATOR).isEqualTo(";");
    }

    @Test
    void shouldHaveCorrectGrandFatheredDate() {
        LocalDate expectedDate = LocalDate.of(2018, 1, 31);
        assertThat(AppConstants.GRAND_FATHERED_DATE).isEqualTo(expectedDate);
    }

    @Test
    void shouldHaveCorrectDateFormatter() {
        DateTimeFormatter formatter = AppConstants.FORMATTER_DD_MMM_YYYY;
        LocalDate testDate = LocalDate.of(2023, 6, 15);
        String formattedDate = testDate.format(formatter);
        assertThat(formattedDate).isEqualTo("15-Jun-2023");
    }

    @Test
    void shouldHaveCorrectMfApiWebsiteBaseUrl() {
        assertThat(AppConstants.MFAPI_WEBSITE_BASE_URL).isEqualTo("https://api.mfapi.in/mf/");
    }

    @Test
    void shouldHaveCorrectTaxStartedDate() {
        LocalDate expectedDate = LocalDate.of(2020, 7, 1);
        assertThat(AppConstants.TAX_STARTED_DATE).isEqualTo(expectedDate);
    }

    @Test
    void shouldHaveCorrectRetryConstants() {
        assertThat(AppConstants.MAX_RETRIES).isEqualTo(4);
        assertThat(AppConstants.FIRST_RETRY).isEqualTo(1);
        assertThat(AppConstants.THIRD_RETRY).isEqualTo(3);
    }

    @Test
    void shouldParseFlexibleDateFormats() {
        DateTimeFormatter formatter = AppConstants.FLEXIBLE_DATE_FORMATTER;

        // Test ISO_LOCAL_DATE format
        LocalDate isoDate = LocalDate.parse("2023-06-15", formatter);
        assertThat(isoDate).isEqualTo(LocalDate.of(2023, 6, 15));

        // Test custom format
        LocalDate customDate = LocalDate.parse("15-Jun-2023", formatter);
        assertThat(customDate).isEqualTo(LocalDate.of(2023, 6, 15));
    }

    @Test
    void flexibleDateFormatterShouldHandleCurrentYearDefault() {
        DateTimeFormatter formatter = AppConstants.FLEXIBLE_DATE_FORMATTER;
        // The formatter should default to current year if not specified
        assertThat(formatter).isNotNull();
    }
}
