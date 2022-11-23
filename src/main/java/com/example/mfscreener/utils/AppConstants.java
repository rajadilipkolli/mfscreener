/* Licensed under Apache-2.0 2021-2022. */
package com.example.mfscreener.utils;

import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {
    public static final String AMFI_WEBSITE_LINK = "https://www.amfiindia.com/spages/NAVAll.txt";
    public static final String MFAPI_WEBSITE_BASE_URL = "https://api.mfapi.in/mf/";
    public static final String SEPARATOR = ";";
    public static final String DATE_PATTERN_DD_MM_YYYY = "dd-MM-yyyy";

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN_DD_MM_YYYY);

    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_NOT_TEST = "!" + PROFILE_TEST;
}
