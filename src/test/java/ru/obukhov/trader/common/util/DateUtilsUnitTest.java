package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.text.ParseException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.stream.Stream;

class DateUtilsUnitTest {

    @Test
    void getDateTime() {
        final int year = 2020;
        final int month = 5;
        final int dayOfMonth = 10;
        final int hour = 12;
        final int minute = 30;
        final int second = 15;

        final OffsetDateTime dateTime = DateUtils.getDateTime(year, month, dayOfMonth, hour, minute, second);

        Assertions.assertEquals(year, dateTime.getYear());
        Assertions.assertEquals(month, dateTime.getMonth().getValue());
        Assertions.assertEquals(dayOfMonth, dateTime.getDayOfMonth());
        Assertions.assertEquals(hour, dateTime.getHour());
        Assertions.assertEquals(minute, dateTime.getMinute());
        Assertions.assertEquals(second, dateTime.getSecond());
        Assertions.assertEquals(0, dateTime.getNano());
    }

    @Test
    void getDate_fillsDateOnly() {
        final int year = 2020;
        final int month = 5;
        final int dayOfMonth = 10;

        final OffsetDateTime dateTime = DateUtils.getDate(year, month, dayOfMonth);

        Assertions.assertEquals(year, dateTime.getYear());
        Assertions.assertEquals(month, dateTime.getMonth().getValue());
        Assertions.assertEquals(dayOfMonth, dateTime.getDayOfMonth());
        Assertions.assertEquals(0, dateTime.getHour());
        Assertions.assertEquals(0, dateTime.getMinute());
        Assertions.assertEquals(0, dateTime.getSecond());
        Assertions.assertEquals(0, dateTime.getNano());
    }

    @Test
    void getTime_fillsTimeOnly() {
        final int hour = 12;
        final int minute = 30;
        final int second = 15;

        final OffsetDateTime dateTime = DateUtils.getTime(hour, minute, second);

        Assertions.assertEquals(0, dateTime.getYear());
        Assertions.assertEquals(1, dateTime.getMonth().getValue());
        Assertions.assertEquals(1, dateTime.getDayOfMonth());
        Assertions.assertEquals(hour, dateTime.getHour());
        Assertions.assertEquals(minute, dateTime.getMinute());
        Assertions.assertEquals(second, dateTime.getSecond());
        Assertions.assertEquals(0, dateTime.getNano());
    }

    // region getIntervalWithDefaultOffsets tests

    @Test
    void getIntervalWithDefaultOffsets_returnsIntervalWithNulls_whenArgumentsAreNull() {
        final Interval result = DateUtils.getIntervalWithDefaultOffsets(null, null);

        Assertions.assertNull(result.getFrom());
        Assertions.assertNull(result.getTo());
    }

    @Test
    void getIntervalWithDefaultOffsets_changesOffsets_whenOffsetsAreNotDefault() {
        final OffsetDateTime from = OffsetDateTime.of(2021, 1, 1,
                10, 0, 0, 0,
                ZoneOffset.UTC);
        final OffsetDateTime to = OffsetDateTime.of(2021, 1, 2,
                11, 0, 0, 0,
                ZoneOffset.UTC);

        final Interval result = DateUtils.getIntervalWithDefaultOffsets(from, to);

        final OffsetDateTime expectedFrom = OffsetDateTime.of(2021, 1, 1,
                13, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET);
        final OffsetDateTime expectedTo = OffsetDateTime.of(2021, 1, 2,
                14, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET);
        Assertions.assertEquals(expectedFrom, result.getFrom());
        Assertions.assertEquals(expectedTo, result.getTo());
    }

    // endregion

    // region getDefaultToIfNull tests

    @Test
    void getDefaultToIfNull_returnsValue_whenNotNull() {
        final OffsetDateTime to = OffsetDateTime.now().minusYears(1);

        final OffsetDateTime result = DateUtils.getDefaultToIfNull(to);

        Assertions.assertEquals(to, result);
    }

    @Test
    void getDefaultToIfNull_returnsNow_whenNull() {
        final OffsetDateTime start = OffsetDateTime.now();

        final OffsetDateTime result = DateUtils.getDefaultToIfNull(null);

        final OffsetDateTime end = OffsetDateTime.now();

        Assertions.assertTrue(!start.isAfter(result) && !end.isBefore(result));
    }

    // endregion

    // region isWorkDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsWorkDay() {
        return Stream.of(
                Arguments.of(DateUtils.getDate(2020, 8, 24), true), // monday
                Arguments.of(DateUtils.getDate(2020, 8, 28), true), // friday
                Arguments.of(DateUtils.getDate(2020, 8, 22), false), // saturday
                Arguments.of(DateUtils.getDate(2020, 8, 23), false) // sunday
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsWorkDay")
    void isWorkDay(final OffsetDateTime date, final boolean expectedResult) {
        final boolean result = DateUtils.isWorkDay(date);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region getNextWorkDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetNextWorkDay_returnsNextDay() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDate(2020, 10, 12),  // monday
                        DateUtils.getDate(2020, 10, 13)
                ),
                Arguments.of(
                        DateUtils.getDate(2020, 10, 16), // friday
                        DateUtils.getDate(2020, 10, 19)
                ),
                Arguments.of(
                        DateUtils.getDate(2020, 10, 17), // saturday
                        DateUtils.getDate(2020, 10, 19)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetNextWorkDay_returnsNextDay")
    void getNextWorkDay(final OffsetDateTime dateTime, final OffsetDateTime expectedNextWorkDay) {
        final OffsetDateTime nextWorkDay = DateUtils.getNextWorkDay(dateTime);

        Assertions.assertEquals(expectedNextWorkDay, nextWorkDay);
    }

    // endregion

    // region getLastWorkDay without arguments tests

    @Test
    @SuppressWarnings("unused")
    void getLastWorkDay_returnsNow_whenTodayIsWorkDay() {
        final OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 23); // wednesday
        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay();

            Assertions.assertEquals(mockedNow, lastWorkDay);
        }
    }

    @Test
    @SuppressWarnings("unused")
    void getLastWorkDay_returnsNow_whenTodayIsWeekend() {
        final OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 27); // sunday
        final OffsetDateTime expected = DateUtils.getDate(2020, 9, 25); // friday
        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay();

            Assertions.assertEquals(expected, lastWorkDay);
        }
    }

    // endregion

    // region getLastWorkDay with dateTime argument tests

    @Test
    void getLastWorkDayDateTime_returnsNow_whenWorkDay() {
        final OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 23); // wednesday

        final OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay(mockedNow);

        Assertions.assertEquals(mockedNow, lastWorkDay);
    }

    @Test
    void getLastWorkDayDateTime_returnsNow_whenWeekend() {
        final OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 27); // sunday
        final OffsetDateTime expected = DateUtils.getDate(2020, 9, 25); // friday

        final OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay(mockedNow);

        Assertions.assertEquals(expected, lastWorkDay);
    }

    // endregion

    // region getLatestDateTime tests

    @Test
    void getLatestDateTime_returnsFirst_whenEquals() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsFirst_whenSecondIsNull() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime dateTime2 = null;

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsSecond_whenFirstIsNull() {
        final OffsetDateTime dateTime1 = null;
        final OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsNull_whenBothAreNull() {
        final OffsetDateTime dateTime1 = null;
        final OffsetDateTime dateTime2 = null;

        final OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertNull(result);
    }

    @Test
    void getLatestDateTime_returnsSecond_whenSecondIsLater() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 2);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    @Test
    void getLatestDateTime_returnsFirst_whenFirstIsLater() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 2);
        final OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    // endregion

    // region getAverage tests

    @Test
    void getAverage_returnsEqualsDate_whenDatesAreEqual() {
        final OffsetDateTime dateTime1 = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);
        final OffsetDateTime dateTime2 = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);

        final OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        Assertions.assertEquals(dateTime1, result);
    }

    @Test
    void getAverage_returnsAverageDate_whenDatesAreNotEqual() {
        final OffsetDateTime dateTime1 = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);
        final OffsetDateTime dateTime2 = DateUtils.getDateTime(2020, 1, 2, 10, 30, 0);

        final OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        final OffsetDateTime expected = DateUtils.getDateTime(2020, 1, 1, 22, 15, 0);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region getEarliestDateTime tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetEarliestDateTime() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDate(2020, 1, 1),
                        DateUtils.getDate(2020, 1, 1),
                        DateUtils.getDate(2020, 1, 1)
                ),
                Arguments.of(
                        DateUtils.getDate(2020, 1, 1),
                        null,
                        DateUtils.getDate(2020, 1, 1)
                ),
                Arguments.of(
                        null,
                        DateUtils.getDate(2020, 1, 1),
                        DateUtils.getDate(2020, 1, 1)
                ),
                Arguments.of(
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        DateUtils.getDate(2020, 1, 1),
                        DateUtils.getDate(2020, 1, 2),
                        DateUtils.getDate(2020, 1, 1)
                ),
                Arguments.of(
                        DateUtils.getDate(2020, 1, 2),
                        DateUtils.getDate(2020, 1, 1),
                        DateUtils.getDate(2020, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetEarliestDateTime")
    void getEarliestDateTime(
            final OffsetDateTime dateTime1,
            final OffsetDateTime dateTime2,
            final OffsetDateTime expected
    ) {
        final OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region plusLimited tests

    @Test
    void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsAfterThanIncrementedDateTime() {
        final OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime maxDateTime = DateUtils.getDate(2020, 2, 1);

        final OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, maxDateTime);

        Assertions.assertNotSame(maxDateTime, result);
        Assertions.assertTrue(dateTime.isBefore(result));
        Assertions.assertTrue(maxDateTime.isAfter(result));
    }

    @Test
    void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsNull() {
        final OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);

        final OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assertions.assertTrue(dateTime.isBefore(result));
    }

    @Test
    void plusLimited_returnsMaxDateTime_whenMaxDateTimeIsBeforeThanIncrementedDateTime() {
        final OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime maxDateTime = DateUtils.getDate(2020, 2, 1);

        final OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.MONTHS, maxDateTime);

        Assertions.assertSame(maxDateTime, result);
    }

    // endregion

    // region minusLimited tests

    @Test
    void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsBeforeThanDecrementedDateTime() {
        final OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);
        final OffsetDateTime minDateTime = DateUtils.getDate(2020, 1, 2);

        final OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, minDateTime);

        Assertions.assertNotSame(minDateTime, result);
        Assertions.assertTrue(dateTime.isAfter(result));
        Assertions.assertTrue(minDateTime.isBefore(result));
    }

    @Test
    void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsNull() {
        final OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);

        final OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assertions.assertTrue(dateTime.isAfter(result));
    }

    @Test
    void minusLimited_returnsMinDateTime_whenMinDateTimeIsAfterThanDecrementedDateTime() {
        final OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);
        final OffsetDateTime minDateTime = DateUtils.getDate(2020, 1, 4);

        final OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.MONTHS, minDateTime);

        Assertions.assertSame(minDateTime, result);
    }

    // endregion

    @Test
    void setTime_setsTime() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 10, 10, 10);
        final OffsetTime time = DateUtils.getTime(5, 30, 0).toOffsetTime();
        final OffsetDateTime result = DateUtils.setTime(dateTime, time);

        Assertions.assertEquals(dateTime.getYear(), result.getYear());
        Assertions.assertEquals(dateTime.getMonth(), result.getMonth());
        Assertions.assertEquals(dateTime.getDayOfMonth(), result.getDayOfMonth());
        Assertions.assertEquals(time.getHour(), result.getHour());
        Assertions.assertEquals(time.getMinute(), result.getMinute());
        Assertions.assertEquals(time.getSecond(), result.getSecond());
        Assertions.assertEquals(time.getNano(), result.getNano());
    }

    @Test
    void setDefaultOffsetSameInstant() {
        final OffsetDateTime dateTime = OffsetDateTime.of(2021, 1, 5,
                10, 0, 0, 0,
                ZoneOffset.UTC);

        final OffsetDateTime result = DateUtils.setDefaultOffsetSameInstant(dateTime);

        final OffsetDateTime expected = OffsetDateTime.of(
                2021, 1, 5,
                13, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET
        );

        Assertions.assertEquals(expected, result);
    }

    // region getPeriodUnitByCandleInterval tests

    @Test
    void getPeriodUnitByCandleInterval_returnsDays_whenIntervalIsHour() {
        final TemporalUnit unit = DateUtils.getPeriodByCandleInterval(CandleResolution.HOUR);

        Assertions.assertEquals(ChronoUnit.DAYS, unit);
    }

    @Test
    void getPeriodUnitByCandleInterval_returnsYears_whenIntervalIsDay() {
        final TemporalUnit unit = DateUtils.getPeriodByCandleInterval(CandleResolution.DAY);

        Assertions.assertEquals(ChronoUnit.YEARS, unit);
    }

    // endregion

    // region isAfter tests

    @Test
    @SuppressWarnings("ConstantConditions")
    void isAfter_returnsTrue_whenDateTime2IsNull() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 5);

        final boolean result = DateUtils.isAfter(dateTime1, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsTrue_whenDateTime1IsAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 5);
        final OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1EqualsAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1isBeforeAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 5);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    // endregion

    // region roundUpToDay tests

    @Test
    void roundUpToDay_doesNotChangesDateTime_whenDateTimeIsStartOfDay() {
        final OffsetDateTime dateTime = DateUtils.getDate(2019, 1, 1);

        final OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        final OffsetDateTime expected = DateUtils.getDate(2019, 1, 1);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void roundUpToDay_movesDateTimeToNextDay_whenDateTimeIsAfterStartOfDay() {
        final OffsetDateTime dateTime =
                DateUtils.getDateTime(2020, 5, 5, 4, 6, 7);

        final OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        final OffsetDateTime expected = DateUtils.getDate(2020, 5, 6);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region roundDownToYear tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forRoundDownToYear() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDate(2019, 1, 1),
                        DateUtils.getDate(2019, 1, 1)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2019, 5, 5, 1, 0, 0),
                        DateUtils.getDate(2019, 1, 1)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2019, 1, 5, 0, 0, 1),
                        DateUtils.getDate(2019, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forRoundDownToYear")
    void roundDownToYear(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime result = DateUtils.roundDownToYear(dateTime);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region roundUpToYear tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forRoundUpToYear() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDate(2019, 1, 1),
                        DateUtils.getDate(2019, 1, 1)
                ),
                Arguments.of(
                        DateUtils.getDate(2019, 5, 5),
                        DateUtils.getDate(2020, 1, 1)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2019, 1, 5, 0, 0, 1),
                        DateUtils.getDate(2020, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forRoundUpToYear")
    void roundUpToYear(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime result = DateUtils.roundUpToYear(dateTime);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region isWorkTime tests

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsNegative() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(-1);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive"
        );
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsZero() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(0);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive"
        );
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsOneDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofDays(1);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day"
        );
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsMoreThanOneDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(25);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day"
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_isWorkTime() {
        return Stream.of(
                // dateTime is work time
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 12, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        true
                ),
                // dateTime is before midnight and end work time is after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 23, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is after midnight and end work time is after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 1, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is start time
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        true
                ),
                // dateTime is start time and end work time are after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is saturday and before end of work time and both are after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 10, 1, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is weekend and work time
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 3, 12, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is weekend and not work time
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 3, 4, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is work day and not work time
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 8, 4, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is work day and end of work time
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 8, 19, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is work day and end of work time and both after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 8, 2, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is after end of work time and both are after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 8, 5, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is saturday and after end of work time and both are after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 10, 4, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is saturday and before end of work time and both are before midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 10, 13, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                ),
                // saturday and equals end of work time and both are before midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 10, 19, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is sunday and before end of work time and both are after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 11, 1, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is sunday and after end of work time and both are after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 11, 4, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is sunday and time equals end of work time and both are after midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 11, 2, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is sunday and before end of work time and both are before midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 11, 13, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is sunday and equals end of work time and both are before midnight
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 11, 19, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(9),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_isWorkTime")
    void isWorkTime(
            final OffsetDateTime dateTime,
            final OffsetTime startTime,
            final Duration duration,
            final boolean expectedResult
    ) {
        final boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region getNearestWorkTime tests

    @Test
    void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(-1);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.getNearestWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive"
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_getNearestWorkTime_orGetNextWorkMinute_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 12, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(0),
                        "workTimeDuration must be positive"
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 12, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(24),
                        "workTimeDuration must be less than 1 day"
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 12, 0, 0),
                        DateUtils.getTime(10, 0, 0).toOffsetTime(),
                        Duration.ofHours(25),
                        "workTimeDuration must be less than 1 day"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_getNearestWorkTime_orGetNextWorkMinute_throwsIllegalArgumentException")
    void getNearestWorkTime_throwsIllegalArgumentException(
            final OffsetDateTime dateTime,
            final OffsetTime startTime,
            final Duration duration,
            final String expectedErrorMessage
    ) {
        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.getNearestWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                expectedErrorMessage
        );
    }

    @Test
    void getNearestWorkTime_returnsCurrentMinute_whenMiddleOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        Assertions.assertEquals(dateTime, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextDay_whenAtEndOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextDay_whenAfterEndOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 20, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextWeek_whenEndOfWorkWeek() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 19, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextWeek_whenAtWeekend() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfTodayWorkDay_whenBeforeStartOfTodayWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 9, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime, startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    // endregion

    // region getNextWorkMinute tests

    @Test
    void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(-1);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.getNextWorkMinute(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive"
        );
    }

    @ParameterizedTest
    @MethodSource("getData_getNearestWorkTime_orGetNextWorkMinute_throwsIllegalArgumentException")
    void getNextWorkMinute_throwsIllegalArgumentException(
            final OffsetDateTime dateTime,
            final OffsetTime startTime,
            final Duration duration,
            final String expectedErrorMessage
    ) {
        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.getNextWorkMinute(dateTime, startTime, duration),
                IllegalArgumentException.class,
                expectedErrorMessage);
    }

    @Test
    void getNextWorkMinute_returnsNextMinute_whenMiddleOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        final OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextDay_whenAtEndOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextDay_whenAfterEndOfWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 20, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextWeek_whenEndOfWorkWeek() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 19, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextWeek_whenAtWeekend() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 12, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfTodayWorkDay_whenBeforeStartOfTodayWorkDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 9, 0, 0);
        final OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime, startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    // endregion

    // region atStartOfDay tests

    @Test
    void atStartOfDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40);

        final OffsetDateTime startOfDay = DateUtils.atStartOfDay(dateTime);

        final OffsetDateTime expected = DateUtils.getDate(2020, 10, 5);

        Assertions.assertEquals(expected, startOfDay);
    }

    // endregion

    // region atEndOfDay tests

    @Test
    void atEndOfDay() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40);

        final OffsetDateTime endOfDay = DateUtils.atEndOfDay(dateTime);

        final OffsetDateTime expected = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        Assertions.assertEquals(expected, endOfDay);
    }

    // endregion

    // region withDefaultOffset tests

    @Test
    void withDefaultOffset() {
        final ZoneOffset testOffset = TestDataHelper.getNotDefaultOffset();

        final OffsetDateTime dateTimeWithTestOffset = OffsetDateTime.now().withOffsetSameInstant(testOffset);
        final OffsetDateTime dateTimeWithDefaultOffset = DateUtils.withDefaultOffset(dateTimeWithTestOffset);

        Assertions.assertEquals(DateUtils.DEFAULT_OFFSET, dateTimeWithDefaultOffset.getOffset());
    }

    // endregion

    // region assertDateTimeNotFuture tests

    @Test
    void assertDateTimeNotFuture_throwsException_whenDateTimeIsInFuture() {
        final OffsetDateTime now = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        final OffsetDateTime dateTime = DateUtils.getDateTime(2021, 1, 1, 10, 0, 1);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.assertDateTimeNotFuture(dateTime, now, "name"),
                IllegalArgumentException.class,
                "'name' (2021-01-01T10:00:01+03:00) can't be in future. Now is 2021-01-01T10:00+03:00"
        );
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsInPast() {
        final OffsetDateTime now = DateUtils.getDateTime(2021, 1, 1, 10, 0, 1);
        final OffsetDateTime dateTime = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);

        DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsEqualsToNow() {
        final OffsetDateTime now = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        final OffsetDateTime dateTime = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);

        DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
    }

    // endregion

    // region toDate tests

    @Test
    @SuppressWarnings("ConstantConditions")
    void toDate_returnsNull_whenDateTimeIsNull() {
        final OffsetDateTime dateTime = null;

        final Date date = DateUtils.toDate(dateTime);

        Assertions.assertNull(date);
    }

    @Test
    void toDate_returnsEqualDate_whenDateTimeIsNotNull() {
        final int year = 2021;
        final int month = 10;
        final int dayOfMonth = 11;
        final int hour = 12;
        final int minute = 13;
        final int second = 14;

        final OffsetDateTime dateTime = DateUtils.getDateTime(year, month, dayOfMonth, hour, minute, second);

        final Date date = DateUtils.toDate(dateTime);

        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        Assertions.assertEquals(year, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(month, calendar.get(Calendar.MONTH) + 1);
        Assertions.assertEquals(dayOfMonth, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(minute, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(second, calendar.get(Calendar.SECOND));
    }

    // endregion

    // region getCronHitsBetweenDates tests

    @Test
    void getCronHitsBetweenDates_throwsIllegalArgumentException_whenFromIsEqualToTo() throws ParseException {
        final CronExpression expression = new CronExpression("0 0 0 1 * ?");
        final OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.getCronHitsBetweenDates(expression, from, to),
                IllegalArgumentException.class,
                "from must be before to"
        );
    }

    @Test
    void getCronHitsBetweenDates_throwsIllegalArgumentException_whenFromIsAfterTo() throws ParseException {
        final CronExpression expression = new CronExpression("0 0 0 1 * ?");
        final OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 15);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);

        AssertUtils.assertThrowsWithMessage(
                () -> DateUtils.getCronHitsBetweenDates(expression, from, to),
                IllegalArgumentException.class,
                "from must be before to"
        );
    }

    @Test
    void getCronHitsBetweenDates_returnsProperCount_whenValidDatesAreBetweenFromAndTo() throws ParseException {
        final CronExpression expression = new CronExpression("0 0 0 1 * ?");
        final OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 12, 11, 12, 13, 14);

        final int count = DateUtils.getCronHitsBetweenDates(expression, from, to);

        Assertions.assertEquals(2, count);
    }

    @Test
    void getCronHitsBetweenDates_returnsProperCount_whenFirstValidDateIsEqualToFrom() throws ParseException {
        final CronExpression expression = new CronExpression("0 0 0 1 * ?");
        final OffsetDateTime from = DateUtils.getDateTime(2021, 10, 1, 0, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 12, 11, 12, 13, 14);

        final int count = DateUtils.getCronHitsBetweenDates(expression, from, to);

        Assertions.assertEquals(2, count);
    }

    @Test
    void getCronHitsBetweenDates_returnsProperCount_whenFirstValidDateIsEqualToTo() throws ParseException {
        final CronExpression expression = new CronExpression("0 0 0 1 * ?");
        final OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);
        final OffsetDateTime to = DateUtils.getDateTime(2022, 1, 1, 0, 0, 0);

        final int count = DateUtils.getCronHitsBetweenDates(expression, from, to);

        Assertions.assertEquals(3, count);
    }

    // endregion

}