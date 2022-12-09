/* Licensed under Apache-2.0 2022. */
package com.example.mfscreener.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocalDateUtility {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.DATE_PATTERN_DD_MM_YYYY);

    public LocalDate getAdjustedDate(LocalDate adjustedDate) {
        if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjustedDate = adjustedDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        return adjustedDate;
    }

    public LocalDate getAdjustedDateForNAV(String inputDate) {
        LocalDate adjustedDate = LocalDate.parse(inputDate, FORMATTER);
        return getAdjustedDate(adjustedDate);
    }
}
