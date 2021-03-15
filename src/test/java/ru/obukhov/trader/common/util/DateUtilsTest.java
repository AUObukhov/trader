package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.text.ParseException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

class DateUtilsTest {

    // region getDate tests

    @Test
    void getDateTime() {
        int year = 2020;
        int month = 5;
        int dayOfMonth = 10;
        int hour = 12;
        int minute = 30;
        int second = 15;

        OffsetDateTime dateTime = DateUtils.getDateTime(year, month, dayOfMonth, hour, minute, second);

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
        int year = 2020;
        int month = 5;
        int dayOfMonth = 10;

        OffsetDateTime dateTime = DateUtils.getDate(year, month, dayOfMonth);

        Assertions.assertEquals(year, dateTime.getYear());
        Assertions.assertEquals(month, dateTime.getMonth().getValue());
        Assertions.assertEquals(dayOfMonth, dateTime.getDayOfMonth());
        Assertions.assertEquals(0, dateTime.getHour());
        Assertions.assertEquals(0, dateTime.getMinute());
        Assertions.assertEquals(0, dateTime.getSecond());
        Assertions.assertEquals(0, dateTime.getNano());
    }

    // endregion

    @Test
    void getTime_fillsTimeOnly() {
        int hour = 12;
        int minute = 30;
        int second = 15;

        OffsetDateTime dateTime = DateUtils.getTime(hour, minute, second);

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
        Interval result = DateUtils.getIntervalWithDefaultOffsets(null, null);

        Assertions.assertNull(result.getFrom());
        Assertions.assertNull(result.getTo());
    }

    @Test
    void getIntervalWithDefaultOffsets_changesOffsets_whenOffsetsAreNotDefault() {
        OffsetDateTime from = OffsetDateTime.of(2021, 1, 1,
                10, 0, 0, 0,
                ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2021, 1, 2,
                11, 0, 0, 0,
                ZoneOffset.UTC);

        Interval result = DateUtils.getIntervalWithDefaultOffsets(from, to);

        OffsetDateTime expectedFrom = OffsetDateTime.of(2021, 1, 1,
                13, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET);
        OffsetDateTime expectedTo = OffsetDateTime.of(2021, 1, 2,
                14, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET);
        Assertions.assertEquals(expectedFrom, result.getFrom());
        Assertions.assertEquals(expectedTo, result.getTo());
    }

    // endregion

    // region getDefaultToIfNull tests

    @Test
    void getDefaultToIfNull_returnsValue_whenNotNull() {
        OffsetDateTime to = OffsetDateTime.now().minusYears(1);

        OffsetDateTime result = DateUtils.getDefaultToIfNull(to);

        Assertions.assertEquals(to, result);
    }

    @Test
    void getDefaultToIfNull_returnsNow_whenNull() {
        OffsetDateTime start = OffsetDateTime.now();

        OffsetDateTime result = DateUtils.getDefaultToIfNull(null);

        OffsetDateTime end = OffsetDateTime.now();

        Assertions.assertTrue(!start.isAfter(result) && !end.isBefore(result));
    }

    // endregion

    // region isWorkDay tests

    @Test
    void isWorkDay_returnsTrue_whenMonday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 24);

        boolean result = DateUtils.isWorkDay(date);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkDay_returnsTrue_whenFriday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 28);

        boolean result = DateUtils.isWorkDay(date);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkDay_returnsFalse_whenSaturday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 22);

        boolean result = DateUtils.isWorkDay(date);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkDay_returnsFalse_whenSunday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 23);

        boolean result = DateUtils.isWorkDay(date);

        Assertions.assertFalse(result);
    }

    // endregion

    // region getNextWorkDay tests

    @Test
    void getNextWorkDay_returnsNextDay_whenMonday() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 10, 12);
        OffsetDateTime nextWorkDay = DateUtils.getNextWorkDay(dateTime);

        Assertions.assertEquals(dateTime.plusDays(1), nextWorkDay);
    }

    @Test
    void getNextWorkDay_returnsNextMonday_whenFriday() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 10, 16);
        OffsetDateTime nextWorkDay = DateUtils.getNextWorkDay(dateTime);

        Assertions.assertEquals(dateTime.plusDays(3), nextWorkDay);
    }

    @Test
    void getNextWorkDay_returnsNextMonday_whenSaturday() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 10, 17);
        OffsetDateTime nextWorkDay = DateUtils.getNextWorkDay(dateTime);

        Assertions.assertEquals(dateTime.plusDays(2), nextWorkDay);
    }

    // endregion

    // region getLastWorkDay without arguments tests

    @Test
    void getLastWorkDay_returnsNow_whenTodayIsWorkDay() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 23); // wednesday
        try (MockedStatic<OffsetDateTime> OffsetDateTimeStaticMock =
                     Mockito.mockStatic(OffsetDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            OffsetDateTimeStaticMock.when(OffsetDateTime::now).thenReturn(mockedNow);

            OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay();

            Assertions.assertEquals(mockedNow, lastWorkDay);
        }
    }

    @Test
    void getLastWorkDay_returnsNow_whenTodayIsWeekend() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 27); // sunday
        OffsetDateTime expected = DateUtils.getDate(2020, 9, 25); // friday
        try (MockedStatic<OffsetDateTime> OffsetDateTimeStaticMock =
                     Mockito.mockStatic(OffsetDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            OffsetDateTimeStaticMock.when(OffsetDateTime::now).thenReturn(mockedNow);

            OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay();

            Assertions.assertEquals(expected, lastWorkDay);
        }
    }

    // endregion

    // region getLastWorkDay with dateTime argument tests

    @Test
    void getLastWorkDayDateTime_returnsNow_whenWorkDay() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 23); // wednesday

        OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay(mockedNow);

        Assertions.assertEquals(mockedNow, lastWorkDay);
    }

    @Test
    void getLastWorkDayDateTime_returnsNow_whenWeekend() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 27); // sunday
        OffsetDateTime expected = DateUtils.getDate(2020, 9, 25); // friday

        OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay(mockedNow);

        Assertions.assertEquals(expected, lastWorkDay);
    }

    // endregion

    // region getLatestDateTime tests

    @Test
    void getLatestDateTime_returnsFirst_whenEquals() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    void getLatestDateTime_returnsFirst_whenSecondIsNull() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    void getLatestDateTime_returnsSecond_whenFirstIsNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsNull_whenBothAreNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertNull(result);
    }

    @Test
    void getLatestDateTime_returnsSecond_whenSecondIsLater() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 2);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    @Test
    void getLatestDateTime_returnsFirst_whenFirstIsLater() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 2);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    // endregion

    // region getAverage tests

    @Test
    void getAverage_returnsEqualsDate_whenDatesAreEqual() {
        OffsetDateTime dateTime1 = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);
        OffsetDateTime dateTime2 = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);

        OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        Assertions.assertEquals(dateTime1, result);
    }

    @Test
    void getAverage_returnsAverageDate_whenDatesAreNotEqual() {
        OffsetDateTime dateTime1 = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);
        OffsetDateTime dateTime2 = DateUtils.getDateTime(2020, 1, 2, 10, 30, 0);

        OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        OffsetDateTime expected = DateUtils.getDateTime(2020, 1, 1, 22, 15, 0);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region getEarliestDateTime tests

    @Test
    void getEarliestDateTime_returnsFirst_whenEquals() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    void getEarliestDateTime_returnsFirst_whenSecondIsNull() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    void getEarliestDateTime_returnsSecond_whenFirstIsNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getEarliestDateTime_returnsNull_whenBothAreNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertNull(result);
    }

    @Test
    void getEarliestDateTime_returnsFirst_whenFirstIsEarlier() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 2);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    void getEarliestDateTime_returnsSecond_whenFirstIsLater() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 2);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    // endregion

    // region plusLimited tests

    @Test
    void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsAfterThanIncrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime maxDateTime = DateUtils.getDate(2020, 2, 1);

        OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, maxDateTime);

        Assertions.assertNotSame(maxDateTime, result);
        Assertions.assertTrue(dateTime.isBefore(result));
        Assertions.assertTrue(maxDateTime.isAfter(result));
    }

    @Test
    void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsNull() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assertions.assertTrue(dateTime.isBefore(result));
    }

    @Test
    void plusLimited_returnsMaxDateTime_whenMaxDateTimeIsBeforeThanIncrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime maxDateTime = DateUtils.getDate(2020, 2, 1);

        OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.MONTHS, maxDateTime);

        Assertions.assertSame(maxDateTime, result);
    }

    // endregion

    // region minusLimited tests

    @Test
    void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsBeforeThanDecrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);
        OffsetDateTime minDateTime = DateUtils.getDate(2020, 1, 2);

        OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, minDateTime);

        Assertions.assertNotSame(minDateTime, result);
        Assertions.assertTrue(dateTime.isAfter(result));
        Assertions.assertTrue(minDateTime.isBefore(result));
    }

    @Test
    void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsNull() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);

        OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assertions.assertTrue(dateTime.isAfter(result));
    }

    @Test
    void minusLimited_returnsMinDateTime_whenMinDateTimeIsAfterThanDecrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);
        OffsetDateTime minDateTime = DateUtils.getDate(2020, 1, 4);

        OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.MONTHS, minDateTime);

        Assertions.assertSame(minDateTime, result);
    }

    // endregion

    @Test
    void setTime_setsTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 10, 10, 10);
        OffsetTime time = DateUtils.getTime(5, 30, 0).toOffsetTime();
        OffsetDateTime result = DateUtils.setTime(dateTime, time);

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
        OffsetDateTime dateTime = OffsetDateTime.of(2021, 1, 5,
                10, 0, 0, 0,
                ZoneOffset.UTC);

        OffsetDateTime result = DateUtils.setDefaultOffsetSameInstant(dateTime);

        OffsetDateTime expected = OffsetDateTime.of(2021, 1, 5,
                13, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET);

        Assertions.assertEquals(expected, result);
    }

    // region getPeriodUnitByCandleInterval tests

    @Test
    void getPeriodUnitByCandleInterval_returnsDays_whenIntervalIsHour() {

        TemporalUnit unit = DateUtils.getPeriodByCandleInterval(CandleInterval.HOUR);

        Assertions.assertEquals(ChronoUnit.DAYS, unit);
    }

    @Test
    void getPeriodUnitByCandleInterval_returnsYears_whenIntervalIsDay() {

        TemporalUnit unit = DateUtils.getPeriodByCandleInterval(CandleInterval.DAY);

        Assertions.assertEquals(ChronoUnit.YEARS, unit);
    }

    // endregion

    // region isAfter tests

    @Test
    @SuppressWarnings("ConstantConditions")
    void isAfter_returnsTrue_whenDateTime2IsNull() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 5);

        boolean result = DateUtils.isAfter(dateTime1, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsTrue_whenDateTime1IsAfterDateTime2() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 5);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1EqualsAfterDateTime2() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1isBeforeAfterDateTime2() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 5);

        boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    // endregion

    // region roundUpToDay tests

    @Test
    void roundUpToDay_doesNotChangesDateTime_whenDateTimeIsStartOfDay() {
        OffsetDateTime dateTime = DateUtils.getDate(2019, 1, 1);

        OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2019, 1, 1);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void roundUpToDay_movesDateTimeToNextDay_whenDateTimeIsAfterStartOfDay() {
        OffsetDateTime dateTime =
                DateUtils.getDateTime(2020, 5, 5, 4, 6, 7);

        OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 5, 6);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region roundUpToYear tests

    @Test
    void roundUpToYear_doesNotChangesDateTime_whenDateTimeIsStartOfYear() {
        OffsetDateTime dateTime = DateUtils.getDate(2019, 1, 1);

        OffsetDateTime result = DateUtils.roundUpToYear(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2019, 1, 1);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void roundUpToYear_movesDateTimeToNextYear_whenDateTimeIsAfterStartOfYear() {
        OffsetDateTime dateTime = DateUtils.getDate(2019, 5, 5);

        OffsetDateTime result = DateUtils.roundUpToYear(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 1, 1);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void roundUpToYear_movesDateTimeToNextYear_whenDateTimeIsAfterStartOfYearForFewTime() {
        OffsetDateTime dateTime =
                DateUtils.getDateTime(2019, 1, 5, 0, 0, 1);

        OffsetDateTime result = DateUtils.roundUpToYear(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 1, 1);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region isWorkTime tests

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsNegative() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(-1);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive");
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsZero() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(0);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive");
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofDays(1);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day");
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsMoreThanOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(25);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.isWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day");
    }

    @Test
    void isWorkTime_returnsTrue_whenWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkTime_returnsTrue_whenTimeIsBeforeMidnight_andEndWorkTimeIsAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 23, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkTime_returnsTrue_whenTimeIsAfterMidnight_andEndWorkTimeIsAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 1, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkTime_returnsTrue_whenStartTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkTime_returnsTrue_whenStartTime_andEndWorkTimeAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkTime_returnsTrue_whenSaturday_andBeforeEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 1, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertTrue(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenWeekend_andWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 3, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenWeekend_andNotWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 3, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenWorkDay_andNotWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenWorkDay_andEndOfWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenWorkDayAndEndOfWorkTime_andBothAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 2, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenAfterEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 5, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSaturday_andAfterEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSaturday_andBeforeEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 13, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSaturday_andEqualsEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSunday_andBeforeEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 1, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSunday_andAfterEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSunday_andTimeEqualsEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 2, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSunday_andBeforeEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 13, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    @Test
    void isWorkTime_returnsFalse_whenSunday_andEqualsEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertFalse(result);
    }

    // endregion

    // region getNearestWorkTime tests

    @Test
    void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(-1);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNearestWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive");
    }

    @Test
    void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsZero() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(0);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNearestWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive");
    }

    @Test
    void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(24);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNearestWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day");
    }

    @Test
    void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsMoreThanOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(25);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNearestWorkTime(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day");
    }

    @Test
    void getNearestWorkTime_returnsCurrentMinute_whenMiddleOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        Assertions.assertEquals(dateTime, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextDay_whenAtEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextDay_whenAfterEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 20, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextWeek_whenEndOfWorkWeek() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfNextWeek_whenAtWeekend() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNearestWorkTime_returnsStartOfTodayWorkDay_whenBeforeStartOfTodayWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 9, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime, startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    // endregion

    // region getNextWorkMinute tests

    @Test
    void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(-1);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNextWorkMinute(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive");
    }

    @Test
    void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsZero() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(0);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNextWorkMinute(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be positive");
    }

    @Test
    void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(24);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNextWorkMinute(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day");
    }

    @Test
    void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsMoreThanOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(25);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getNextWorkMinute(dateTime, startTime, duration),
                IllegalArgumentException.class,
                "workTimeDuration must be less than 1 day");
    }

    @Test
    void getNextWorkMinute_returnsNextMinute_whenMiddleOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = dateTime.plusMinutes(1);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextDay_whenAtEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextDay_whenAfterEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 20, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextWeek_whenEndOfWorkWeek() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfNextWeek_whenAtWeekend() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getNextWorkMinute_returnsStartOfTodayWorkDay_whenBeforeStartOfTodayWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 9, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime, startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    // endregion

    // region atStartOfDay tests

    @Test
    void atStartOfDay() {

        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40);

        OffsetDateTime startOfDay = DateUtils.atStartOfDay(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 10, 5);

        Assertions.assertEquals(expected, startOfDay);
    }

    // endregion

    // region atEndOfDay tests

    @Test
    void atEndOfDay() {

        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40);

        OffsetDateTime endOfDay = DateUtils.atEndOfDay(dateTime);

        OffsetDateTime expected = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        Assertions.assertEquals(expected, endOfDay);
    }

    // endregion

    // region withDefaultOffset tests

    @Test
    void withDefaultOffset() {
        ZoneOffset testOffset = TestDataHelper.getNotDefaultOffset();

        OffsetDateTime dateTimeWithTestOffset = OffsetDateTime.now().withOffsetSameInstant(testOffset);
        OffsetDateTime dateTimeWithDefaultOffset = DateUtils.withDefaultOffset(dateTimeWithTestOffset);

        Assertions.assertEquals(DateUtils.DEFAULT_OFFSET, dateTimeWithDefaultOffset.getOffset());
    }

    // endregion

    // region assertDateTimeNotFuture tests

    @Test
    void assertDateTimeNotFuture_throwsException_whenDateTimeIsInFuture() {
        OffsetDateTime now = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        OffsetDateTime dateTime = DateUtils.getDateTime(2021, 1, 1, 10, 0, 1);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.assertDateTimeNotFuture(dateTime, now, "name"),
                IllegalArgumentException.class,
                "'name' (2021-01-01T10:00:01+03:00) can't be in future. Now is 2021-01-01T10:00+03:00");
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsInPast() {
        OffsetDateTime now = DateUtils.getDateTime(2021, 1, 1, 10, 0, 1);
        OffsetDateTime dateTime = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);

        DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsEqualsToNow() {
        OffsetDateTime now = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        OffsetDateTime dateTime = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);

        DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
    }

    // endregion

    // region toDate tests

    @Test
    void toDate_returnsNull_whenDateTimeIsNull() {
        OffsetDateTime dateTime = null;

        Date date = DateUtils.toDate(dateTime);

        Assertions.assertNull(date);
    }

    @Test
    void toDate_returnsEqualDate_whenDateTimeIsNotNull() {
        final int FIRST_YEAR = 1900;

        final int year = 2021;
        final int month = 10;
        final int dayOfMonth = 11;
        final int hour = 12;
        final int minute = 13;
        final int second = 14;

        OffsetDateTime dateTime = DateUtils.getDateTime(year, month, dayOfMonth, hour, minute, second);

        Date date = DateUtils.toDate(dateTime);

        Assertions.assertEquals(year, date.getYear() + FIRST_YEAR);
        Assertions.assertEquals(month, date.getMonth() + 1);
        Assertions.assertEquals(dayOfMonth, date.getDate());
        Assertions.assertEquals(hour, date.getHours());
        Assertions.assertEquals(minute, date.getMinutes());
        Assertions.assertEquals(second, date.getSeconds());
    }

    // endregion

    // region getCronHitsBetweenDates tests

    @Test
    void getCronHitsBetweenDates_throwsIllegalArgumentException_whenFromIsEqualToTo() throws ParseException {
        CronExpression expression = new CronExpression("0 0 0 1 * ?");
        OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);
        OffsetDateTime to = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getCronHitsBetweenDates(expression, from, to),
                IllegalArgumentException.class,
                "from must be before to");
    }

    @Test
    void getCronHitsBetweenDates_throwsIllegalArgumentException_whenFromIsAfterTo() throws ParseException {
        CronExpression expression = new CronExpression("0 0 0 1 * ?");
        OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 15);
        OffsetDateTime to = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);

        AssertUtils.assertThrowsWithMessage(() -> DateUtils.getCronHitsBetweenDates(expression, from, to),
                IllegalArgumentException.class,
                "from must be before to");
    }

    @Test
    void getCronHitsBetweenDates_returnsProperCount_whenValidDatesAreBetweenFromAndTo() throws ParseException {
        CronExpression expression = new CronExpression("0 0 0 1 * ?");
        OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);
        OffsetDateTime to = DateUtils.getDateTime(2021, 12, 11, 12, 13, 14);

        int count = DateUtils.getCronHitsBetweenDates(expression, from, to);

        Assertions.assertEquals(2, count);
    }

    @Test
    void getCronHitsBetweenDates_returnsProperCount_whenFirstValidDateIsEqualToFrom() throws ParseException {
        CronExpression expression = new CronExpression("0 0 0 1 * ?");
        OffsetDateTime from = DateUtils.getDateTime(2021, 10, 1, 0, 0, 0);
        OffsetDateTime to = DateUtils.getDateTime(2021, 12, 11, 12, 13, 14);

        int count = DateUtils.getCronHitsBetweenDates(expression, from, to);

        Assertions.assertEquals(2, count);
    }

    @Test
    void getCronHitsBetweenDates_returnsProperCount_whenFirstValidDateIsEqualToTo() throws ParseException {
        CronExpression expression = new CronExpression("0 0 0 1 * ?");
        OffsetDateTime from = DateUtils.getDateTime(2021, 10, 11, 12, 13, 14);
        OffsetDateTime to = DateUtils.getDateTime(2022, 1, 1, 0, 0, 0);

        int count = DateUtils.getCronHitsBetweenDates(expression, from, to);

        Assertions.assertEquals(3, count);
    }

    // endregion

}