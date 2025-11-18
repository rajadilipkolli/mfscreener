package com.learning.mfscreener.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class LocalDateUtilityTest {

    @Test
    @DisplayName("Constructor should throw UnsupportedOperationException")
    void constructorShouldThrowException() throws NoSuchMethodException {
        Constructor<LocalDateUtility> constructor = LocalDateUtility.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance())
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .satisfies(exception -> {
                    UnsupportedOperationException cause = (UnsupportedOperationException) exception.getCause();
                    assertThat(cause.getMessage()).contains("Constructor can't be initialized");
                });
    }

    @Test
    @DisplayName("Should return same date for weekdays")
    void shouldReturnSameDateForWeekdays() {
        // Monday
        LocalDate monday = LocalDate.of(2023, 6, 12);
        assertThat(LocalDateUtility.getAdjustedDate(monday)).isEqualTo(monday);

        // Tuesday
        LocalDate tuesday = LocalDate.of(2023, 6, 13);
        assertThat(LocalDateUtility.getAdjustedDate(tuesday)).isEqualTo(tuesday);

        // Wednesday
        LocalDate wednesday = LocalDate.of(2023, 6, 14);
        assertThat(LocalDateUtility.getAdjustedDate(wednesday)).isEqualTo(wednesday);

        // Thursday
        LocalDate thursday = LocalDate.of(2023, 6, 15);
        assertThat(LocalDateUtility.getAdjustedDate(thursday)).isEqualTo(thursday);

        // Friday
        LocalDate friday = LocalDate.of(2023, 6, 16);
        assertThat(LocalDateUtility.getAdjustedDate(friday)).isEqualTo(friday);
    }

    @Test
    @DisplayName("Should return previous Friday for Saturday")
    void shouldReturnPreviousFridayForSaturday() {
        LocalDate saturday = LocalDate.of(2023, 6, 17); // Saturday
        LocalDate expectedFriday = LocalDate.of(2023, 6, 16); // Previous Friday

        assertThat(LocalDateUtility.getAdjustedDate(saturday)).isEqualTo(expectedFriday);
    }

    @Test
    @DisplayName("Should return previous Friday for Sunday")
    void shouldReturnPreviousFridayForSunday() {
        LocalDate sunday = LocalDate.of(2023, 6, 18); // Sunday
        LocalDate expectedFriday = LocalDate.of(2023, 6, 16); // Previous Friday

        assertThat(LocalDateUtility.getAdjustedDate(sunday)).isEqualTo(expectedFriday);
    }

    @ParameterizedTest
    @DisplayName("Should adjust weekend dates to previous Friday")
    @MethodSource("provideWeekendDates")
    void shouldAdjustWeekendDatesToPreviousFriday(LocalDate weekendDate, LocalDate expectedFriday) {
        assertThat(LocalDateUtility.getAdjustedDate(weekendDate)).isEqualTo(expectedFriday);
    }

    static Stream<Arguments> provideWeekendDates() {
        return Stream.of(
                Arguments.of(LocalDate.of(2023, 6, 17), LocalDate.of(2023, 6, 16)), // Saturday -> Friday
                Arguments.of(LocalDate.of(2023, 6, 18), LocalDate.of(2023, 6, 16)), // Sunday -> Friday
                Arguments.of(LocalDate.of(2023, 12, 23), LocalDate.of(2023, 12, 22)), // Saturday -> Friday
                Arguments.of(LocalDate.of(2023, 12, 24), LocalDate.of(2023, 12, 22)) // Sunday -> Friday
                );
    }

    @Test
    @DisplayName("Should return adjusted date when called without parameters")
    void shouldReturnAdjustedDateWithoutParameters() {
        LocalDate result = LocalDateUtility.getAdjustedDate();

        // The result should be a weekday (Monday to Friday)
        assertThat(result.getDayOfWeek())
                .isIn(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    }

    @Test
    @DisplayName("Should return provided date when not null")
    void shouldReturnProvidedDateWhenNotNull() {
        LocalDate testDate = LocalDate.of(2023, 6, 15); // Thursday
        LocalDate result = LocalDateUtility.getAdjustedDateOrDefault(testDate);

        assertThat(result).isEqualTo(testDate);
    }

    @Test
    @DisplayName("Should return adjusted current date when null is provided")
    void shouldReturnAdjustedCurrentDateWhenNullProvided() {
        LocalDate result = LocalDateUtility.getAdjustedDateOrDefault(null);

        // The result should be a weekday (Monday to Friday)
        assertThat(result.getDayOfWeek())
                .isIn(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    }

    @Test
    @DisplayName("Should adjust weekend date when provided in getAdjustedDateOrDefault")
    void shouldAdjustWeekendDateWhenProvidedInGetAdjustedDateOrDefault() {
        LocalDate saturday = LocalDate.of(2023, 6, 17);
        LocalDate expectedFriday = LocalDate.of(2023, 6, 16);

        LocalDate result = LocalDateUtility.getAdjustedDateOrDefault(saturday);

        assertThat(result).isEqualTo(expectedFriday);
    }

    @ParameterizedTest
    @DisplayName("Should return correct financial year format")
    @CsvSource({
        "2023-04-01, FY2023-24", // Start of FY
        "2023-03-31, FY2022-23", // End of FY
        "2023-12-15, FY2023-24", // Middle of FY
        "2023-01-15, FY2022-23", // Jan (previous FY)
        "2023-02-28, FY2022-23", // Feb (previous FY)
        "2023-03-15, FY2022-23", // March (previous FY)
        "2099-04-01, FY2099-2100", // Edge case with year 99
        "2099-03-31, FY2098-99" // Edge case transition
    })
    void shouldReturnCorrectFinancialYear(String dateStr, String expectedFinYear) {
        LocalDate sellDate = LocalDate.parse(dateStr);
        String result = LocalDateUtility.getFinYear(sellDate);

        assertThat(result).isEqualTo(expectedFinYear);
    }

    @Test
    @DisplayName("Should handle edge case for year ending in 99")
    void shouldHandleEdgeCaseForYear99() {
        LocalDate date99 = LocalDate.of(2099, 5, 15);
        String result = LocalDateUtility.getFinYear(date99);

        assertThat(result).isEqualTo("FY2099-2100");
    }

    @Test
    @DisplayName("Should handle leap year dates correctly")
    void shouldHandleLeapYearDatesCorrectly() {
        LocalDate leapYearDate = LocalDate.of(2024, 2, 29);
        String result = LocalDateUtility.getFinYear(leapYearDate);

        assertThat(result).isEqualTo("FY2023-24");
    }

    @Test
    @DisplayName("Should handle century transition correctly")
    void shouldHandleCenturyTransitionCorrectly() {
        LocalDate centuryDate1 = LocalDate.of(1999, 5, 15);
        LocalDate centuryDate2 = LocalDate.of(2000, 5, 15);

        String result1 = LocalDateUtility.getFinYear(centuryDate1);
        String result2 = LocalDateUtility.getFinYear(centuryDate2);

        assertThat(result1).isEqualTo("FY1999-2000");
        assertThat(result2).isEqualTo("FY2000-01");
    }
}
