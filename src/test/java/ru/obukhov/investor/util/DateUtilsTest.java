package ru.obukhov.investor.util;

import org.junit.Assert;
import org.junit.Test;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ru.obukhov.investor.util.DateUtils.START_DATE;

public class DateUtilsTest {

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

        assertEquals(START_DATE, result);
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
}