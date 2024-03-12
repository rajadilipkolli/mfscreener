package com.learning.mfscreener.utils;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class AppConstants {
    public static final String PROFILE_PROD = "prod";
    public static final String PROFILE_NOT_PROD = "!" + PROFILE_PROD;
    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_NOT_TEST = "!" + PROFILE_TEST;
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    public static final String AMFI_WEBSITE_LINK = "https://www.amfiindia.com/spages/NAVAll.txt";
    public static final String SEPARATOR = ";";
    private static final String DATE_PATTERN_DD_MMM_YYYY = "dd-MMM-yyyy";
    public static final DateTimeFormatter FORMATTER_DD_MMM_YYYY =
            DateTimeFormatter.ofPattern(DATE_PATTERN_DD_MMM_YYYY, Locale.ENGLISH);
    public static final String MFAPI_WEBSITE_BASE_URL = "https://api.mfapi.in/mf/";
}
