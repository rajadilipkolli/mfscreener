package com.learning.mfscreener.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

public final class AppConstants {
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_NOT_PROD = "!" + PROFILE_PROD;
    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_LOCAL = "prod";
    public static final String PROFILE_NOT_LOCAL = "!" + PROFILE_LOCAL;

    public static final String AMFI_WEBSITE_LINK = "https://www.amfiindia.com/spages/NAVAll.txt";
    public static final String NAV_SEPARATOR = ";";
    public static final LocalDate GRAND_FATHERED_DATE = LocalDate.of(2018, 1, 31);
    private static final String DATE_PATTERN_DD_MMM_YYYY = "dd-MMM-yyyy";
    public static final DateTimeFormatter FORMATTER_DD_MMM_YYYY =
            DateTimeFormatter.ofPattern(DATE_PATTERN_DD_MMM_YYYY, Locale.ENGLISH);
    public static final String MFAPI_WEBSITE_BASE_URL = "https://api.mfapi.in/mf/";
    public static final LocalDate TAX_STARTED_DATE = LocalDate.of(2020, 7, 1);
    public static final int MAX_RETRIES = 4;
    public static final int FIRST_RETRY = 1;
    public static final int THIRD_RETRY = 3;
    public static final DateTimeFormatter FLEXIBLE_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("[yyyy-MM-dd]") // ISO_LOCAL_DATE
            .appendPattern("[dd-MMM-yyyy]") // Custom format
            .parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()) // Default year to current year
            .toFormatter(Locale.ENGLISH); // Ensure English locale for month names
}
