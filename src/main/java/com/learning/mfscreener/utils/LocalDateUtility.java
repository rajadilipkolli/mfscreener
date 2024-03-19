package com.learning.mfscreener.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class LocalDateUtility {

    public static LocalDate getAdjustedDate(LocalDate adjustedDate) {
        if (adjustedDate.getDayOfWeek() == DayOfWeek.SATURDAY || adjustedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            adjustedDate = adjustedDate.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
        }
        return adjustedDate;
    }
}
