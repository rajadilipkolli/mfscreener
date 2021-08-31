package com.example.mfscreener.util;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class Constants {
  public static final String AMFI_WEBSITE_LINK = "https://www.amfiindia.com/spages/NAVAll.txt";
  public static final String MFAPI_WEBSITE_BASE_URL = "https://api.mfapi.in/mf/";
  public static final String SEPARATOR = ";";
  public static final String DATE_PATTERN_DD_MM_YYYY = "dd-MM-yyyy";

  public static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern(DATE_PATTERN_DD_MM_YYYY);
}
