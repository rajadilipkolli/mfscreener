package com.learning.mfscreener.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class AppConstants {
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_NOT_PROD = "!" + PROFILE_PROD;
    public static final String PROFILE_TEST = "test";

    public static final String AMFI_WEBSITE_LINK = "https://www.amfiindia.com/spages/NAVAll.txt";
    public static final String NAV_SEPARATOR = ";";
    public static final LocalDate GRAND_FATHERTED_DATE = LocalDate.of(2018, 1, 31);
    private static final String DATE_PATTERN_DD_MMM_YYYY = "dd-MMM-yyyy";
    public static final DateTimeFormatter FORMATTER_DD_MMM_YYYY =
            DateTimeFormatter.ofPattern(DATE_PATTERN_DD_MMM_YYYY, Locale.ENGLISH);
    public static final String MFAPI_WEBSITE_BASE_URL = "https://api.mfapi.in/mf/";
}
