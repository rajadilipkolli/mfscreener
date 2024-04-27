package com.learning.mfscreener.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public class LocalDateUtility {

    public static LocalDate getAdjustedDate(LocalDate adjustedDate) {
        if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY || adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjustedDate = adjustedDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        return adjustedDate;
    }

    public static LocalDate getAdjustedDate() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        // NAVs are refreshed only after 11:30 PM so reduce the day by 1
        if (currentDateTime.toLocalTime().isBefore(LocalTime.of(23, 30))) {
            currentDateTime = currentDateTime.minusDays(1);
        }
        return getAdjustedDate(currentDateTime.toLocalDate());
    }

    public static LocalDate getAdjustedDateOrDefault(LocalDate asOfDate) {
        return asOfDate == null ? getAdjustedDate() : getAdjustedDate(asOfDate);
    }

    public static String getFinYear(LocalDate sellDate) {
        int year1;
        int year2;
        if (sellDate.getMonthValue() > 3) {
            year1 = sellDate.getYear();
            year2 = sellDate.getYear() + 1;
        } else {
            year1 = sellDate.getYear() - 1;
            year2 = sellDate.getYear();
        }

        if (year1 % 100 != 99) {
            year2 %= 100;
        }

        return String.format("FY%d-%02d", year1, year2);
    }

    private LocalDateUtility() {
        throw new UnsupportedOperationException("Constructor can't be initialized");
    }
}
