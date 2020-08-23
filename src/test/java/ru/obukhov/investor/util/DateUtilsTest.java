package ru.obukhov.investor.util;

import org.junit.Test;

import java.time.OffsetDateTime;

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

}