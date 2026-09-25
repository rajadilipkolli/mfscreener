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
            .appendPattern("[M/d/yyyy]")
            .appendPattern("[d/M/yyyy]")
            .parseDefaulting(ChronoField.YEAR_OF_ERA, LocalDate.now().getYear()) // Default year to current year
            .toFormatter(Locale.ENGLISH); // Ensure English locale for month names

    public static final String SCHEME_CODE = "Scheme Code";
    public static final String ISIN_DIV_PAYOUT_GROWTH = "ISIN Div Payout/ ISIN Growth";
    public static final String ISIN_DIV_REINVESTMENT = "ISIN Div Reinvestment";
    public static final String SCHEME_NAME = "Scheme Name";
    public static final String NAV_NAME = "NAV Name";
    public static final String PLAN = "Plan";
    public static final String OPTION = "Option";
    public static final String NET_ASSET_VALUE = "Net Asset Value";
    public static final String REPURCHASE_PRICE = "Repurchase Price";
    public static final String SALE_PRICE = "Sale Price";
    public static final String DATE = "Date";

    // CAS Formats
    public static final String SCHEME = "Scheme";
    public static final String FOLIO = "Folio";
    public static final String AMOUNT = "Amount";
    public static final String UNITS = "Units";
    public static final String PRICE = "Price";
    public static final String BALANCE = "Balance";
    public static final String TYPE = "Type";
    public static final String KYC = "KYC";
    public static final String PAN = "PAN";
    public static final String DESCRIPTION = "Description";
    public static final String ADVISOR = "Advisor";
    public static final String MYOPEN = "MyOpen";
    public static final String CLOSE = "Close";

    public static final String CSV_NAV = "nav";
    public static final String CSV_NAV_DATE = "nav_date";
    public static final String CSV_SCHEME_ID = "scheme_id";
    public static final String CSV_FUND_HOUSE = "fund_house";
    public static final String CSV_SCHEME_NAME = "scheme_name";
    public static final String CSV_PAY_OUT = "pay_out";
    public static final String CSV_TYPE = "type";
    public static final String CSV_CATEGORY = "category";
    public static final String CSV_SUB_CATEGORY = "sub_category";
}
