package ru.obukhov.investor.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class DateUtilsTest {

    // region getDate tests

    @Test
    public void getDateTime() {
        int year = 2020;
        int month = 5;
        int dayOfMonth = 10;
        int hour = 12;
        int minute = 30;
        int second = 15;

        OffsetDateTime dateTime = DateUtils.getDateTime(year, month, dayOfMonth, hour, minute, second);

        assertEquals(year, dateTime.getYear());
        assertEquals(month, dateTime.getMonth().getValue());
        assertEquals(dayOfMonth, dateTime.getDayOfMonth());
        assertEquals(hour, dateTime.getHour());
        assertEquals(minute, dateTime.getMinute());
        assertEquals(second, dateTime.getSecond());
        assertEquals(0, dateTime.getNano());
    }

    @Test
    public void getDate_fillsDateOnly() {
        int year = 2020;
        int month = 5;
        int dayOfMonth = 10;

        OffsetDateTime dateTime = DateUtils.getDate(year, month, dayOfMonth);

        assertEquals(year, dateTime.getYear());
        assertEquals(month, dateTime.getMonth().getValue());
        assertEquals(dayOfMonth, dateTime.getDayOfMonth());
        assertEquals(0, dateTime.getHour());
        assertEquals(0, dateTime.getMinute());
        assertEquals(0, dateTime.getSecond());
        assertEquals(0, dateTime.getNano());
    }

    // endregion

    @Test
    public void getTime_fillsTimeOnly() {
        int hour = 12;
        int minute = 30;
        int second = 15;

        OffsetDateTime dateTime = DateUtils.getTime(hour, minute, second);

        assertEquals(0, dateTime.getYear());
        assertEquals(1, dateTime.getMonth().getValue());
        assertEquals(1, dateTime.getDayOfMonth());
        assertEquals(hour, dateTime.getHour());
        assertEquals(minute, dateTime.getMinute());
        assertEquals(second, dateTime.getSecond());
        assertEquals(0, dateTime.getNano());
    }

    // region getDefaultFromIfNull tests

    @Test
    public void getDefaultFromIfNull_returnsValue_whenNotNull() {
        OffsetDateTime from = OffsetDateTime.now();

        OffsetDateTime result = DateUtils.getDefaultFromIfNull(from);

        assertEquals(from, result);
    }

    @Test
    public void getDefaultFromIfNull_returnsStartDate_whenNull() {
        OffsetDateTime result = DateUtils.getDefaultFromIfNull(null);

        assertEquals(DateUtils.START_DATE, result);
    }

    // endregion

    // region getDefaultToIfNull tests

    @Test
    public void getDefaultToIfNull_returnsValue_whenNotNull() {
        OffsetDateTime to = OffsetDateTime.now().minusYears(1);

        OffsetDateTime result = DateUtils.getDefaultToIfNull(to);

        assertEquals(to, result);
    }

    @Test
    public void getDefaultToIfNull_returnsNow_whenNull() {
        OffsetDateTime start = OffsetDateTime.now();

        OffsetDateTime result = DateUtils.getDefaultToIfNull(null);

        OffsetDateTime end = OffsetDateTime.now();

        assertTrue(!start.isAfter(result) && !end.isBefore(result));
    }

    // endregion

    // region isWorkDay tests

    @Test
    public void isWorkDay_returnsTrue_whenMonday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 24);

        boolean result = DateUtils.isWorkDay(date);

        assertTrue(result);
    }

    @Test
    public void isWorkDay_returnsTrue_whenFriday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 28);

        boolean result = DateUtils.isWorkDay(date);

        assertTrue(result);
    }

    @Test
    public void isWorkDay_returnsFalse_whenSaturday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 22);

        boolean result = DateUtils.isWorkDay(date);

        assertFalse(result);
    }

    @Test
    public void isWorkDay_returnsFalse_whenSunday() {
        OffsetDateTime date = DateUtils.getDate(2020, 8, 23);

        boolean result = DateUtils.isWorkDay(date);

        assertFalse(result);
    }

    // endregion

    // region getNextWorkDay tests

    @Test
    public void getNextWorkDay_returnsNextDay_whenMonday() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 10, 12);
        OffsetDateTime nextWorkDay = DateUtils.getNextWorkDay(dateTime);

        Assert.assertEquals(dateTime.plusDays(1), nextWorkDay);
    }

    @Test
    public void getNextWorkDay_returnsNextMonday_whenFriday() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 10, 16);
        OffsetDateTime nextWorkDay = DateUtils.getNextWorkDay(dateTime);

        Assert.assertEquals(dateTime.plusDays(3), nextWorkDay);
    }

    @Test
    public void getNextWorkDay_returnsNextMonday_whenSaturday() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 10, 17);
        OffsetDateTime nextWorkDay = DateUtils.getNextWorkDay(dateTime);

        Assert.assertEquals(dateTime.plusDays(2), nextWorkDay);
    }

    // endregion

    // region getLastWorkDay without arguments tests

    @Test
    public void getLastWorkDay_returnsNow_whenTodayIsWorkDay() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 23); // wednesday
        PowerMockito.mockStatic(OffsetDateTime.class);
        when(OffsetDateTime.now()).thenReturn(mockedNow);

        OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay();

        assertEquals(mockedNow, lastWorkDay);
    }

    @Test
    public void getLastWorkDay_returnsNow_whenTodayIsWeekend() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 27); // sunday
        OffsetDateTime expected = DateUtils.getDate(2020, 9, 25); // friday
        PowerMockito.mockStatic(OffsetDateTime.class);
        when(OffsetDateTime.now()).thenReturn(mockedNow);

        OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay();

        assertEquals(expected, lastWorkDay);
    }

    // endregion

    // region getLastWorkDay with dateTime argument tests

    @Test
    public void getLastWorkDayDateTime_returnsNow_whenWorkDay() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 23); // wednesday

        OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay(mockedNow);

        assertEquals(mockedNow, lastWorkDay);
    }

    @Test
    public void getLastWorkDayDateTime_returnsNow_whenWeekend() {
        OffsetDateTime mockedNow = DateUtils.getDate(2020, 9, 27); // sunday
        OffsetDateTime expected = DateUtils.getDate(2020, 9, 25); // friday

        OffsetDateTime lastWorkDay = DateUtils.getLastWorkDay(mockedNow);

        assertEquals(expected, lastWorkDay);
    }

    // endregion

    // region getLatestDateTime tests

    @Test
    public void getLatestDateTime_returnsFirst_whenEquals() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime1, result);
    }

    @Test
    public void getLatestDateTime_returnsFirst_whenSecondIsNull() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime1, result);
    }

    @Test
    public void getLatestDateTime_returnsSecond_whenFirstIsNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime2, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getLatestDateTime_returnsNull_whenBothAreNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assert.assertNull(result);
    }

    @Test
    public void getLatestDateTime_returnsSecond_whenSecondIsLater() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 2);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime2, result);
    }

    @Test
    public void getLatestDateTime_returnsFirst_whenFirstIsLater() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 2);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime1, result);
    }

    // endregion

    // region getEarliestDateTime tests

    @Test
    public void getEarliestDateTime_returnsFirst_whenEquals() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime1, result);
    }

    @Test
    public void getEarliestDateTime_returnsFirst_whenSecondIsNull() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime1, result);
    }

    @Test
    public void getEarliestDateTime_returnsSecond_whenFirstIsNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime2, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getEarliestDateTime_returnsNull_whenBothAreNull() {
        OffsetDateTime dateTime1 = null;
        OffsetDateTime dateTime2 = null;

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assert.assertNull(result);
    }

    @Test
    public void getEarliestDateTime_returnsFirst_whenFirstIsEarlier() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 2);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime1, result);
    }

    @Test
    public void getEarliestDateTime_returnsSecond_whenFirstIsLater() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 2);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assert.assertSame(dateTime2, result);
    }

    // endregion

    // region plusLimited tests

    @Test
    public void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsAfterThanIncrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime maxDateTime = DateUtils.getDate(2020, 2, 1);

        OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, maxDateTime);

        Assert.assertNotSame(maxDateTime, result);
        Assert.assertTrue(dateTime.isBefore(result));
        Assert.assertTrue(maxDateTime.isAfter(result));
    }

    @Test
    public void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsNull() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);

        OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assert.assertTrue(dateTime.isBefore(result));
    }

    @Test
    public void plusLimited_returnsMaxDateTime_whenMaxDateTimeIsBeforeThanIncrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime maxDateTime = DateUtils.getDate(2020, 2, 1);

        OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.MONTHS, maxDateTime);

        Assert.assertSame(maxDateTime, result);
    }

    // endregion

    // region minusLimited tests

    @Test
    public void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsBeforeThanDecrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);
        OffsetDateTime minDateTime = DateUtils.getDate(2020, 1, 2);

        OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, minDateTime);

        Assert.assertNotSame(minDateTime, result);
        Assert.assertTrue(dateTime.isAfter(result));
        Assert.assertTrue(minDateTime.isBefore(result));
    }

    @Test
    public void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsNull() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);

        OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assert.assertTrue(dateTime.isAfter(result));
    }

    @Test
    public void minusLimited_returnsMinDateTime_whenMinDateTimeIsAfterThanDecrementedDateTime() {
        OffsetDateTime dateTime = DateUtils.getDate(2020, 1, 5);
        OffsetDateTime minDateTime = DateUtils.getDate(2020, 1, 4);

        OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.MONTHS, minDateTime);

        Assert.assertSame(minDateTime, result);
    }

    // endregion

    @Test
    public void setTime_setsTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 10, 10, 10);
        OffsetTime time = DateUtils.getTime(5, 30, 0).toOffsetTime();
        OffsetDateTime result = DateUtils.setTime(dateTime, time);

        assertEquals(dateTime.getYear(), result.getYear());
        assertEquals(dateTime.getMonth(), result.getMonth());
        assertEquals(dateTime.getDayOfMonth(), result.getDayOfMonth());
        assertEquals(time.getHour(), result.getHour());
        assertEquals(time.getMinute(), result.getMinute());
        assertEquals(time.getSecond(), result.getSecond());
        assertEquals(time.getNano(), result.getNano());
    }

    // region getPeriodUnitByCandleInterval tests

    @Test
    public void getPeriodUnitByCandleInterval_returnsDays_whenIntervalIsHour() {

        TemporalUnit unit = DateUtils.getPeriodByCandleInterval(CandleInterval.HOUR);

        Assert.assertEquals(ChronoUnit.DAYS, unit);
    }

    @Test
    public void getPeriodUnitByCandleInterval_returnsYears_whenIntervalIsDay() {

        TemporalUnit unit = DateUtils.getPeriodByCandleInterval(CandleInterval.DAY);

        Assert.assertEquals(ChronoUnit.YEARS, unit);
    }

    // endregion

    // region isAfter tests

    @Test
    @SuppressWarnings("ConstantConditions")
    public void isAfter_returnsTrue_whenDateTime2IsNull() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 5);

        boolean result = DateUtils.isAfter(dateTime1, null);

        Assert.assertTrue(result);
    }

    @Test
    public void isAfter_returnsTrue_whenDateTime1IsAfterDateTime2() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 5);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assert.assertTrue(result);
    }

    @Test
    public void isAfter_returnsFalse_whenDateTime1EqualsAfterDateTime2() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 1);

        boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assert.assertFalse(result);
    }

    @Test
    public void isAfter_returnsFalse_whenDateTime1isBeforeAfterDateTime2() {
        OffsetDateTime dateTime1 = DateUtils.getDate(2020, 1, 1);
        OffsetDateTime dateTime2 = DateUtils.getDate(2020, 1, 5);

        boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assert.assertFalse(result);
    }

    // endregion

    // region isBetween tests

    @Test(expected = IllegalArgumentException.class)
    public void isBetween_throwsIllegalArgumentException_whenLeftIsAfterRight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime left = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime right = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);

        boolean result = DateUtils.isBetween(dateTime, left, right);
    }

    @Test
    public void isBetween_returnsFalse_whenDateTimeBeforeLeft() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 10, 5, 5);
        OffsetDateTime left = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime right = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);

        boolean result = DateUtils.isBetween(dateTime, left, right);

        Assert.assertFalse(result);
    }

    @Test
    public void isBetween_returnsTrue_whenDateTimeEqualsLeft() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime left = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime right = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);

        boolean result = DateUtils.isBetween(dateTime, left, right);

        Assert.assertTrue(result);
    }

    @Test
    public void isBetween_returnsTrue_whenDateTimeBetweenLeftAndRight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 12, 5, 10);
        OffsetDateTime left = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime right = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);

        boolean result = DateUtils.isBetween(dateTime, left, right);

        Assert.assertTrue(result);
    }

    @Test
    public void isBetween_returnsTrue_whenDateTimeEqualsRight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime left = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime right = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);

        boolean result = DateUtils.isBetween(dateTime, left, right);

        Assert.assertTrue(result);
    }

    @Test
    public void isBetween_returnsFalse_whenDateTimeAfterRight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 2, 10, 5, 15);
        OffsetDateTime left = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime right = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);

        boolean result = DateUtils.isBetween(dateTime, left, right);

        Assert.assertFalse(result);
    }

    // endregion

    // region roundUpToDay tests

    @Test
    public void roundUpToDay_doesNotChangesDateTime_whenDateTimeIsStartOfDay() {
        OffsetDateTime dateTime = DateUtils.getDate(2019, 1, 1);

        OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2019, 1, 1);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void roundUpToDay_movesDateTimeToNextDay_whenDateTimeIsAfterStartOfDay() {
        OffsetDateTime dateTime =
                DateUtils.getDateTime(2020, 5, 5, 4, 6, 7);

        OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 5, 6);
        Assert.assertEquals(expected, result);
    }

    // endregion

    // region roundUpToYear tests

    @Test
    public void roundUpToYear_doesNotChangesDateTime_whenDateTimeIsStartOfYear() {
        OffsetDateTime dateTime = DateUtils.getDate(2019, 1, 1);

        OffsetDateTime result = DateUtils.roundUpToYear(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2019, 1, 1);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void roundUpToYear_movesDateTimeToNextYear_whenDateTimeIsAfterStartOfYear() {
        OffsetDateTime dateTime = DateUtils.getDate(2019, 5, 5);

        OffsetDateTime result = DateUtils.roundUpToYear(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 1, 1);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void roundUpToYear_movesDateTimeToNextYear_whenDateTimeIsAfterStartOfYearForFewTime() {
        OffsetDateTime dateTime =
                DateUtils.getDateTime(2019, 1, 5, 0, 0, 1);

        OffsetDateTime result = DateUtils.roundUpToYear(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 1, 1);
        Assert.assertEquals(expected, result);
    }

    // endregion

    // region isWorkTime tests

    @Test(expected = IllegalArgumentException.class)
    public void isWorkTime_throwsIllegalArgumentException_whenDurationIsNegative() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(-1);

        DateUtils.isWorkTime(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isWorkTime_throwsIllegalArgumentException_whenDurationIsZero() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(0);

        DateUtils.isWorkTime(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isWorkTime_throwsIllegalArgumentException_whenDurationIsOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofDays(1);

        DateUtils.isWorkTime(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isWorkTime_throwsIllegalArgumentException_whenDurationIsMoreThanOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(25);

        DateUtils.isWorkTime(dateTime, startTime, duration);
    }

    @Test
    public void isWorkTime_returnsTrue_whenWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertTrue(result);
    }

    @Test
    public void isWorkTime_returnsTrue_whenTimeIsBeforeMidnight_andEndWorkTimeIsAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 23, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertTrue(result);
    }

    @Test
    public void isWorkTime_returnsTrue_whenTimeIsAfterMidnight_andEndWorkTimeIsAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 1, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertTrue(result);
    }

    @Test
    public void isWorkTime_returnsTrue_whenStartTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertTrue(result);
    }

    @Test
    public void isWorkTime_returnsTrue_whenStartTime_andEndWorkTimeAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertTrue(result);
    }

    @Test
    public void isWorkTime_returnsTrue_whenSaturday_andBeforeEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 1, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertTrue(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenWeekend_andWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 3, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenWeekend_andNotWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 3, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenWorkDay_andNotWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenWorkDay_andEndOfWorkTime() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenWorkDayAndEndOfWorkTime_andBothAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 2, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenAfterEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 8, 5, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSaturday_andAfterEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSaturday_andBeforeEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 13, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSaturday_andEqualsEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSunday_andBeforeEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 1, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSunday_andAfterEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 4, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSunday_andTimeEqualsEndOfWorkTime_andBothAreAfterMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 2, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(16);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSunday_andBeforeEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 13, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    @Test
    public void isWorkTime_returnsFalse_whenSunday_andEqualsEndOfWorkTime_andBothAreBeforeMidnight() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 11, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        assertFalse(result);
    }

    // endregion

    // region getNearestWorkTime tests

    @Test(expected = IllegalArgumentException.class)
    public void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsZero() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(0);

        DateUtils.getNearestWorkTime(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(-1);

        DateUtils.getNearestWorkTime(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(24);

        DateUtils.getNearestWorkTime(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNearestWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsMoreThanOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(25);

        DateUtils.getNearestWorkTime(dateTime, startTime, duration);
    }

    @Test
    public void getNearestWorkTime_returnsCurrentMinute_whenMiddleOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        Assert.assertEquals(dateTime, nextWorkMinute);
    }

    @Test
    public void getNearestWorkTime_returnsStartOfNextDay_whenAtEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNearestWorkTime_returnsStartOfNextDay_whenAfterEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 20, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNearestWorkTime_returnsStartOfNextWeek_whenEndOfWorkWeek() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNearestWorkTime_returnsStartOfNextWeek_whenAtWeekend() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNearestWorkTime_returnsStartOfTodayWorkDay_whenBeforeStartOfTodayWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 9, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNearestWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime, startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    // endregion

    // region getNextWorkMinute tests

    @Test(expected = IllegalArgumentException.class)
    public void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsZero() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(0);

        DateUtils.getNextWorkMinute(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(-1);

        DateUtils.getNextWorkMinute(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(24);

        DateUtils.getNextWorkMinute(dateTime, startTime, duration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsMoreThanOneDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(25);

        DateUtils.getNextWorkMinute(dateTime, startTime, duration);
    }

    @Test
    public void getNextWorkMinute_returnsNextMinute_whenMiddleOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = dateTime.plusMinutes(1);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNextWorkMinute_returnsStartOfNextDay_whenAtEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNextWorkMinute_returnsStartOfNextDay_whenAfterEndOfWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 19, 20, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNextWorkMinute_returnsStartOfNextWeek_whenEndOfWorkWeek() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 19, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNextWorkMinute_returnsStartOfNextWeek_whenAtWeekend() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 12, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    @Test
    public void getNextWorkMinute_returnsStartOfTodayWorkDay_whenBeforeStartOfTodayWorkDay() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 9, 9, 0, 0);
        OffsetTime startTime = DateUtils.getTime(10, 0, 0).toOffsetTime();
        Duration duration = Duration.ofHours(9);

        OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime, startTime);
        Assert.assertEquals(expected, nextWorkMinute);
    }

    // endregion

    // region atStartOfDay tests

    @Test
    public void atStartOfDay() {

        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40);

        OffsetDateTime startOfDay = DateUtils.atStartOfDay(dateTime);

        OffsetDateTime expected = DateUtils.getDate(2020, 10, 5);

        Assert.assertEquals(expected, startOfDay);
    }

    // endregion

    // region atEndOfDay tests

    @Test
    public void atEndOfDay() {

        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40);

        OffsetDateTime endOfDay = DateUtils.atEndOfDay(dateTime);

        OffsetDateTime expected = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        Assert.assertEquals(expected, endOfDay);
    }

    // endregion

    // region splitIntervalIntoDays tests

    @Test(expected = IllegalArgumentException.class)
    public void splitIntervalIntoDays_throwsIllegalArgumentException_whenFromIsAfterTo() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 30, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);

        DateUtils.splitIntervalIntoDays(from, to);

    }

    @Test
    public void splitIntervalIntoDays_returnsOnePair_whenFromAndToAreEqual() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(from, to);

        Assert.assertEquals(1, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getLeft());
        Assert.assertEquals(to, intervals.get(0).getRight());

    }

    @Test
    public void splitIntervalIntoDays_returnsOnePair_whenFromAndToInOneDay() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 12, 20, 30);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(from, to);

        Assert.assertEquals(1, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getLeft());
        Assert.assertEquals(to, intervals.get(0).getRight());

    }

    @Test
    public void splitIntervalIntoDays_returnsOnePair_whenFromAndToInOneWholeDay() {

        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(from, to);

        Assert.assertEquals(1, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getLeft());
        Assert.assertEquals(to, intervals.get(0).getRight());

    }

    @Test
    public void splitIntervalIntoDays_returnsTwoPairs_whenFromAndToDiffersInOneDay() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 12, 20, 30);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(from, to);

        Assert.assertEquals(2, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getLeft());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight0, intervals.get(0).getRight());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getLeft());
        Assert.assertEquals(to, intervals.get(1).getRight());

    }

    @Test
    public void splitIntervalIntoDays_returnsTwoPairs_whenFromAndToAreAtStartOfNeighbourDays() {

        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 6);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(from, to);

        Assert.assertEquals(2, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getLeft());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight0, intervals.get(0).getRight());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getLeft());
        Assert.assertEquals(to, intervals.get(1).getRight());

    }

    @Test
    public void splitIntervalIntoDays_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourDays() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(from, to);

        Assert.assertEquals(2, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getLeft());
        Assert.assertEquals(from, intervals.get(0).getRight());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getLeft());
        Assert.assertEquals(to, intervals.get(1).getRight());

    }

    @Test
    public void splitIntervalIntoDays_returnsThreePairs_whenFromAndToDiffersInTwoDay() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(from, to);

        Assert.assertEquals(3, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getLeft());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight0, intervals.get(0).getRight());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getLeft());
        OffsetDateTime expectedRight1 = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight1, intervals.get(1).getRight());

        OffsetDateTime expectedLeft2 = DateUtils.getDate(2020, 10, 7);
        Assert.assertEquals(expectedLeft2, intervals.get(2).getLeft());
        Assert.assertEquals(to, intervals.get(2).getRight());

    }

    // endregion
}