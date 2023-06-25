package com.learning.mfscreener.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocalDateUtility {

    public LocalDate getAdjustedDate(LocalDate adjustedDate) {
        if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY || adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjustedDate = adjustedDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        return adjustedDate;
    }

    public LocalDate getAdjustedDateForNAV(String inputDate) {
        LocalDate adjustedDate = LocalDate.parse(inputDate, AppConstants.FORMATTER);
        return getAdjustedDate(adjustedDate);
    }
}
