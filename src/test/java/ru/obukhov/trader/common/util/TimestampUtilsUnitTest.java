package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class TimestampUtilsUnitTest {

    // region newTimestamp tests

    @ParameterizedTest
    @CsvSource(value = {
            "-62135596800, 0",
            "0, 0",
            "0, 10",
            "11, 0",
            "12, 13",
            "253402300799, 999999999"
    })
    void newTimestamp_fromSecondsAndNanos(final long seconds, final int nanos) {
        final Timestamp actualResult = TimestampUtils.newTimestamp(seconds, nanos);
        Assertions.assertEquals(seconds, actualResult.getSeconds());
        Assertions.assertEquals(nanos, actualResult.getNanos());
    }

    @ParameterizedTest
    @ValueSource(longs = {-62135596801L, 253402300800L})
    void newTimestamp_fromSecondsAndNanos_throwsIllegalArgumentException_whenSecondsOutOfRange(final long seconds) {
        final Executable executable = () -> TimestampUtils.newTimestamp(seconds, 1);
        final String expectedMessage = "seconds must be from 0001-01-01T00:00:00Z (-62135596800) to 9999-12-31T23:59:59Z (253402300799) inclusive";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1000_000_000})
    void newTimestamp_fromSecondsAndNanos_throwsIllegalArgumentException_whenNanosOutOfRange(final int nanos) {
        final Executable executable = () -> TimestampUtils.newTimestamp(1, nanos);
        final String expectedMessage = "nanos must be from 0 to 999999999 inclusive";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @ParameterizedTest
    @ValueSource(longs = {-62135596800L, 0L, 167L, 253402300799L})
    void newTimestamp_fromSeconds(final long seconds) {
        final Timestamp actualResult = TimestampUtils.newTimestamp(seconds);
        Assertions.assertEquals(seconds, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @ParameterizedTest
    @ValueSource(longs = {-62135596801L, 253402300800L})
    void newTimestamp_fromSeconds_throwsIllegalArgumentException_whenSecondsOutOfRange(final long seconds) {
        final Executable executable = () -> TimestampUtils.newTimestamp(seconds);
        final String expectedMessage = "seconds must be from 0001-01-01T00:00:00Z (-62135596800) to 9999-12-31T23:59:59Z (253402300799) inclusive";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void newTimestamp_fromYearMonthDayOfMonthHour() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 4);

        Assertions.assertEquals(1651539600L, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromYearMonthDayOfMonthHourMinute() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 4, 5);

        Assertions.assertEquals(1651539900L, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromYearMonthDayOfMonthHourMinuteSecond() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 4, 5, 6);

        Assertions.assertEquals(1651539906L, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromYearMonthDayOfMonthHourMinuteSecondNanoOfSecond() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 10, 20, 30, 123);

        Assertions.assertEquals(1651562430L, actualResult.getSeconds());
        Assertions.assertEquals(123, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromDateTimeUnits() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 10, 20, 30, 123, ZoneOffset.ofHours(6));

        Assertions.assertEquals(1651551630L, actualResult.getSeconds());
        Assertions.assertEquals(123, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromOffsetDateTime() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2022, 5, 3, 10, 20, 30, 123);

        final Timestamp actualResult = TimestampUtils.newTimestamp(dateTime);

        Assertions.assertEquals(1651562430L, actualResult.getSeconds());
        Assertions.assertEquals(123, actualResult.getNanos());
    }

    // endregion

    @Test
    void toInstant() {
        final Timestamp timestamp = TimestampUtils.newTimestamp(1651562430, 123);

        final Instant instant = TimestampUtils.toInstant(timestamp);

        final Instant expectedInstant = DateTimeTestData.createDateTime(2022, 5, 3, 10, 20, 30, 123)
                .toInstant();
        Assertions.assertEquals(expectedInstant, instant);
    }

    @Test
    void toOffsetDateTime() {
        final Timestamp timestamp = TimestampUtils.newTimestamp(1651562430, 123);

        final OffsetDateTime dateTime = TimestampUtils.toOffsetDateTime(timestamp);

        final OffsetDateTime expectedDateTime = DateTimeTestData.createDateTime(2022, 5, 3, 10, 20, 30, 123);
        Assertions.assertEquals(expectedDateTime, dateTime);
    }

    // region toDate tests

    @Test
    @SuppressWarnings("ConstantConditions")
    void toDate_returnsNull_whenDateTimeIsNull() {
        final Timestamp timestamp = null;

        final Date date = TimestampUtils.toDate(timestamp);

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

        final Timestamp timestamp = TimestampUtils.newTimestamp(year, month, dayOfMonth, hour, minute, second);

        final Date date = TimestampUtils.toDate(timestamp);

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

}