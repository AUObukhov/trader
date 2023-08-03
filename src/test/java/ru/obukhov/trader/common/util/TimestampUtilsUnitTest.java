package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.quartz.CronExpression;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Stream;

class TimestampUtilsUnitTest {

    // region newTimestamp tests

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0",
            "0, 10",
            "11, 0",
            "12, 13",
    })
    void newTimestamp_fromSecondsAndNanos(final long seconds, final int nanos) {
        final Timestamp actualResult = TimestampUtils.newTimestamp(seconds, nanos);
        Assertions.assertEquals(seconds, actualResult.getSeconds());
        Assertions.assertEquals(nanos, actualResult.getNanos());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 167L})
    void newTimestamp_fromSeconds(final long seconds) {
        final Timestamp actualResult = TimestampUtils.newTimestamp(seconds);
        Assertions.assertEquals(seconds, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromYearMonthDayOfMonth() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3);

        Assertions.assertEquals(1651525200L, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromYearMonthDayOfMonthHour() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 4);

        Assertions.assertEquals(1651539600L, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromYearMonthDayOfMonthHourMinuteSecond() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 4, 5, 6);

        Assertions.assertEquals(1651539906L, actualResult.getSeconds());
        Assertions.assertEquals(0, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromDateTimeUnits() {
        final Timestamp actualResult = TimestampUtils.newTimestamp(2022, 5, 3, 10, 20, 30, 123);

        Assertions.assertEquals(1651562430L, actualResult.getSeconds());
        Assertions.assertEquals(123, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromOffsetDateTime() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2022, 5, 3, 10, 20, 30, 123);

        final Timestamp actualResult = TimestampUtils.newTimestamp(dateTime);

        Assertions.assertEquals(1651562430, actualResult.getSeconds());
        Assertions.assertEquals(123, actualResult.getNanos());
    }

    @Test
    void newTimestamp_fromDate() {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(2021, Calendar.OCTOBER, 11, 12, 13, 14);
        final Date date = calendar.getTime();

        final Timestamp timestamp = TimestampUtils.newTimestamp(date);

        Assertions.assertEquals(1633943594L, timestamp.getSeconds());
    }

    // endregion

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

    // region isBefore tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsBefore() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 45),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 46),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234568),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(0, 0),
                        TimestampUtils.newTimestamp(0, 0),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234566),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 29),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsBefore")
    void isBefore(final Timestamp left, final Timestamp right, final boolean expectedResult) {
        final boolean actualResult = TimestampUtils.isBefore(left, right);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region isAfter tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsAfter() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 46),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 45),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234568),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(0, 0),
                        TimestampUtils.newTimestamp(0, 0),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234566),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30, 1234567),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 29),
                        TimestampUtils.newTimestamp(2020, 10, 5, 1, 10, 30),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsAfter")
    void isAfter(final Timestamp left, final Timestamp right, final boolean expectedResult) {
        final boolean actualResult = TimestampUtils.isAfter(left, right);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region assertNotFuture tests

    @Test
    void assertNotFuture_throwsException_whenTimestampIsInFuture() {
        final Timestamp timestamp = TimestampUtils.newTimestamp(2021, 1, 1, 10, 0, 1);
        final Timestamp now = TimestampUtils.newTimestamp(2021, 1, 1, 10);

        final Executable executable = () -> TimestampUtils.assertNotFuture(timestamp, now, "name");
        final String expectedMessage = "'name' (2021-01-01T10:00:01+03:00) can't be in future. Now is 2021-01-01T10:00+03:00";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void assertNotFuture_notThrowsException_whenTimestampIsInPast() {
        final Timestamp timestamp = TimestampUtils.newTimestamp(2021, 1, 1, 10);
        final Timestamp now = TimestampUtils.newTimestamp(2021, 1, 1, 10, 0, 1);

        TimestampUtils.assertNotFuture(timestamp, now, "name");
    }

    @Test
    void assertNotFuture_notThrowsException_whenTimestampIsEqualToNow() {
        final Timestamp timestamp = TimestampUtils.newTimestamp(2021, 1, 1, 10);
        final Timestamp now = TimestampUtils.newTimestamp(2021, 1, 1, 10);

        TimestampUtils.assertNotFuture(timestamp, now, "name");
    }

    // endregion

    // region getCronHitsCountBetweenDates tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCronHitsBetweenDates_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 10, 11),
                        TimestampUtils.newTimestamp(2021, 10, 11)
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 10, 11, 0, 0, 0, 1),
                        TimestampUtils.newTimestamp(2021, 10, 11)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCronHitsBetweenDates_throwsIllegalArgumentException")
    void getCronHitsBetweenDates_throwsIllegalArgumentException(final String expression, final Timestamp from, final Timestamp to)
            throws ParseException {

        final CronExpression cronExpression = new CronExpression(expression);

        final Executable executable = () -> TimestampUtils.getCronHitsBetweenDates(cronExpression, from, to);
        final String expectedMessage = String.format("from [%s] must be before to [%s]", from, to);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCronHitsBetweenDates() {
        return Stream.of(
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 10, 1),
                        TimestampUtils.newTimestamp(2021, 10, 2),
                        List.of(TimestampUtils.newTimestamp(2021, 10, 1))
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 10, 1),
                        TimestampUtils.newTimestamp(2021, 12, 2),
                        List.of(
                                TimestampUtils.newTimestamp(2021, 10, 1),
                                TimestampUtils.newTimestamp(2021, 11, 1),
                                TimestampUtils.newTimestamp(2021, 12, 1)
                        )
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 10, 2),
                        TimestampUtils.newTimestamp(2021, 12, 2),
                        List.of(
                                TimestampUtils.newTimestamp(2021, 11, 1),
                                TimestampUtils.newTimestamp(2021, 12, 1)
                        )
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 10, 2),
                        TimestampUtils.newTimestamp(2022, 1, 1),
                        List.of(
                                TimestampUtils.newTimestamp(2021, 11, 1),
                                TimestampUtils.newTimestamp(2021, 12, 1)
                        )
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 12, 2),
                        TimestampUtils.newTimestamp(2022, 1, 1),
                        List.of()
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        TimestampUtils.newTimestamp(2021, 1, 2),
                        TimestampUtils.newTimestamp(2021, 1, 30),
                        List.of()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCronHitsBetweenDates")
    void getCronHitsBetweenDates(final String expression, final Timestamp from, final Timestamp to, final List<Timestamp> expectedHits)
            throws ParseException {

        final CronExpression cronExpression = new CronExpression(expression);

        final List<Timestamp> hits = TimestampUtils.getCronHitsBetweenDates(cronExpression, from, to);

        AssertUtils.assertEquals(expectedHits, hits);
    }

    // endregion

    // region isInInterval tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsInInterval_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 14),
                        DateTimeTestData.createDateTime(2020, 10, 5, 14)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 15),
                        DateTimeTestData.createDateTime(2020, 10, 5, 14)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsInInterval_throwsIllegalArgumentException")
    void isInInterval_throwsIllegalArgumentException(final OffsetDateTime from, final OffsetDateTime to) {
        final Timestamp timestamp = TimestampUtils.newTimestamp(from);
        final Instant fromInstant = from.toInstant();
        final Instant toInstant = to.toInstant();

        AssertUtils.assertThrowsWithMessage(
                IllegalArgumentException.class,
                () -> TimestampUtils.isInInterval(timestamp, fromInstant, toInstant),
                "From must be before to"
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsInInterval() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 14),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createDateTime(2020, 10, 5, 14),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 11),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 14),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 14),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 14),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 15),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 14),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsInInterval")
    void isInInterval(final Timestamp timestamp, final OffsetDateTime from, final OffsetDateTime to, final boolean expectedResult) {
        final boolean result = TimestampUtils.isInInterval(timestamp, from.toInstant(), to.toInstant());

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region equalDates tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forEqualDates() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5),
                        TimestampUtils.newTimestamp(2020, 10, 5),
                        true
                ),
                Arguments.of(
                        null,
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        null,
                        false
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 4, 23, 59, 59, 999_999_999),
                        TimestampUtils.newTimestamp(2020, 10, 5),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEqualDates")
    void equalDates(Timestamp timestamp1, Timestamp timestamp2, boolean expected) {
        final boolean result1 = TimestampUtils.equalDates(timestamp1, timestamp2);
        final boolean result2 = TimestampUtils.equalDates(timestamp2, timestamp1);

        Assertions.assertEquals(result1, result2);
        Assertions.assertEquals(expected, result1);
    }

    // endregion

    // region getEarliest tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetEarliest() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        null,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        null,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 2, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 2, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 500),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 500),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetEarliest")
    void getEarliest(final Timestamp timestamp1, final Timestamp timestamp2, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.getEarliest(timestamp1, timestamp2);

        if (expectedResult == null) {
            Assertions.assertNull(actualResult);
        } else {
            Assertions.assertEquals(expectedResult.getSeconds(), actualResult.getSeconds());
            Assertions.assertEquals(expectedResult.getNanos(), actualResult.getNanos());
        }
    }

    // endregion

    // region getLatest tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetLatest() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        null,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        null,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 2, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 2, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 2, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 2, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 401),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 401)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 401),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 401)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLatest")
    void getLatest(final Timestamp timestamp1, final Timestamp timestamp2, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.getLatest(timestamp1, timestamp2);

        if (expectedResult == null) {
            Assertions.assertNull(actualResult);
        } else {
            Assertions.assertEquals(expectedResult.getSeconds(), actualResult.getSeconds());
            Assertions.assertEquals(expectedResult.getNanos(), actualResult.getNanos());
        }
    }

    // endregion

    // region getAverage tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverage() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400), // 2020.1.1 10:30:45.400
                        TimestampUtils.newTimestamp(1577950245, 400), // 2020.1.2 10:30:45.400
                        TimestampUtils.newTimestamp(1577907045, 400) // 2020.1.1 22:30:45.400
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400), // 2020.1.1 10:30:45.400
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 500), // 2020.1.1 10:30:45.500
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 450) // 2020.1.1 10:30:45.450
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverage")
    void getAverage(final Timestamp timestamp1, final Timestamp timestamp2, final Timestamp expectedResult) {
        final Timestamp actualResult1 = TimestampUtils.getAverage(timestamp1, timestamp2);
        final Timestamp actualResult2 = TimestampUtils.getAverage(timestamp1, timestamp2);

        Assertions.assertEquals(expectedResult.getSeconds(), actualResult1.getSeconds());
        Assertions.assertEquals(expectedResult.getNanos(), actualResult1.getNanos());
        Assertions.assertEquals(expectedResult.getSeconds(), actualResult2.getSeconds());
        Assertions.assertEquals(expectedResult.getNanos(), actualResult2.getNanos());
    }

    // endregion

    // region isWorkDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsWorkDay() {
        return Stream.of(
                // monday
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 24), true),
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 24, 23, 59, 59, 999_999_999), true),

                // friday
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 28), true),
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 28, 23, 59, 59, 999_999_999), true),

                // saturday
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 22), false),
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 22, 23, 59, 59, 999_999_999), false),

                // sunday
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 23), false),
                Arguments.of(TimestampUtils.newTimestamp(2020, 8, 23, 23, 59, 59, 999_999_999), false)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsWorkDay")
    void isWorkDay(final Timestamp timestamp, final boolean expectedResult) {
        final boolean actualResult = TimestampUtils.isWorkDay(timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region plus tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forPlus() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        Duration.ZERO,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        Duration.ofNanos(200),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 600)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        Duration.ofSeconds(200),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 34, 5, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400000000),
                        Duration.ofNanos(900000000),
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 46, 300000000)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        Duration.ofMinutes(40),
                        TimestampUtils.newTimestamp(2020, 1, 1, 11, 10, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        Duration.ofHours(40),
                        TimestampUtils.newTimestamp(2020, 1, 3, 2, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        Duration.ofDays(40),
                        TimestampUtils.newTimestamp(2020, 2, 10, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        Duration.ofDays(80),
                        TimestampUtils.newTimestamp(2020, 3, 21, 10, 30, 45, 400)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forPlus")
    void plus(final Timestamp timestamp, final Duration duration, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.plus(timestamp, duration);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region plusNanos tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forPlusNanos() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        0L,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        200L,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 600)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400000000),
                        1800000000L,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 47, 200000000)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 200),
                        -150L,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 150),
                        -200000000200L,
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 27, 24, 999999950)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forPlusNanos")
    void plusNanos(final Timestamp timestamp, final long amountToAdd, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.plusNanos(timestamp, amountToAdd);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region plusMinutes tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forPlusMinutes() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 0, 0, 1),
                        0L,
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 0, 0, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 0, 0, 2),
                        1L,
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 1, 0, 2)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 0, 3),
                        1L,
                        TimestampUtils.newTimestamp(2021, 1, 1, 0, 0, 0, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 0, 0, 4),
                        70L,
                        TimestampUtils.newTimestamp(2020, 8, 24, 1, 10, 0, 4)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 0, 0, 2),
                        -1L,
                        TimestampUtils.newTimestamp(2020, 8, 23, 23, 59, 0, 2)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 0, 0, 0, 3),
                        -1L,
                        TimestampUtils.newTimestamp(2019, 12, 31, 23, 59, 0, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 0, 0, 4),
                        -70L,
                        TimestampUtils.newTimestamp(2020, 8, 23, 22, 50, 0, 4)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forPlusMinutes")
    void plusMinutes(final Timestamp timestamp, final long amountToAdd, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.plusMinutes(timestamp, amountToAdd);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region pluHours tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forPlusHours() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 1, 2, 3),
                        0L,
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 1, 2, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 1, 2, 3),
                        1L,
                        TimestampUtils.newTimestamp(2020, 8, 24, 1, 1, 2, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 1, 2, 3),
                        1L,
                        TimestampUtils.newTimestamp(2021, 1, 1, 0, 1, 2, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 1, 2, 3),
                        70L,
                        TimestampUtils.newTimestamp(2020, 8, 26, 22, 1, 2, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 1, 2, 3),
                        -1L,
                        TimestampUtils.newTimestamp(2020, 8, 23, 23, 1, 2, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 0, 1, 2, 3),
                        -1L,
                        TimestampUtils.newTimestamp(2019, 12, 31, 23, 1, 2, 3)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 0, 1, 2, 3),
                        -70L,
                        TimestampUtils.newTimestamp(2020, 8, 21, 2, 1, 2, 3)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forPlusHours")
    void plusHours(final Timestamp timestamp, final long amountToAdd, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.plusHours(timestamp, amountToAdd);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region plusDays tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forPlusDays() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 24, 1, 2, 3, 4),
                        0L,
                        TimestampUtils.newTimestamp(2020, 1, 24, 1, 2, 3, 4)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 2, 28, 5, 6, 7, 8),
                        1L,
                        TimestampUtils.newTimestamp(2020, 2, 29, 5, 6, 7, 8)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 2, 28, 9, 10, 11, 12),
                        2L,
                        TimestampUtils.newTimestamp(2020, 3, 1, 9, 10, 11, 12)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 24, 13, 14, 15, 16),
                        400L,
                        TimestampUtils.newTimestamp(2021, 2, 27, 13, 14, 15, 16)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 2, 29, 17, 18, 19, 20),
                        -1L,
                        TimestampUtils.newTimestamp(2020, 2, 28, 17, 18, 19, 20)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 3, 1, 21, 22, 23, 24),
                        -2L,
                        TimestampUtils.newTimestamp(2020, 2, 28, 21, 22, 23, 24)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 3, 24, 2, 25, 26, 27),
                        -400L,
                        TimestampUtils.newTimestamp(2019, 2, 18, 2, 25, 26, 27)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forPlusDays")
    void plusDays(final Timestamp timestamp, final long amountToAdd, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.plusDays(timestamp, amountToAdd);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region plusYears tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forPlusYears() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 24, 1, 2, 3, 4),
                        0,
                        TimestampUtils.newTimestamp(2020, 1, 24, 1, 2, 3, 4)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 24, 5, 6, 7, 8),
                        1,
                        TimestampUtils.newTimestamp(2021, 1, 24, 5, 6, 7, 8)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 24, 9, 10, 11, 12),
                        2,
                        TimestampUtils.newTimestamp(2022, 1, 24, 9, 10, 11, 12)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 24, 13, 14, 15, 16),
                        400,
                        TimestampUtils.newTimestamp(2420, 1, 24, 13, 14, 15, 16)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 17, 18, 19, 20),
                        -1,
                        TimestampUtils.newTimestamp(2019, 8, 24, 17, 18, 19, 20)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 21, 22, 23, 24),
                        -2,
                        TimestampUtils.newTimestamp(2018, 8, 24, 21, 22, 23, 24)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 8, 24, 2, 25, 26, 27),
                        -400,
                        TimestampUtils.newTimestamp(1620, 8, 24, 2, 25, 26, 27)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forPlusYears")
    void plusYears(final Timestamp timestamp, final long amountToAdd, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.plusYears(timestamp, amountToAdd);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region setTime tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSetTime() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        OffsetTime.of(11, 12, 13, 14, ZoneOffset.UTC),
                        TimestampUtils.newTimestamp(2020, 1, 1, 14, 12, 13, 14)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 10, 30, 45, 400),
                        OffsetTime.of(11, 12, 13, 14, ZoneOffset.ofHours(3)),
                        TimestampUtils.newTimestamp(2020, 1, 1, 17, 12, 13, 14)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSetTime")
    void setTime(final Timestamp timestamp, final OffsetTime time, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.setTime(timestamp, time);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfDay() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5),
                        TimestampUtils.newTimestamp(2020, 10, 5)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 0, 0, 0, 1),
                        TimestampUtils.newTimestamp(2020, 10, 5)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999),
                        TimestampUtils.newTimestamp(2020, 10, 5)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfDay")
    void toStartOfDay(final Timestamp timestamp, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.toStartOfDay(timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfDay() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999),
                        TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5),
                        TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 1),
                        TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfDay")
    void toEndOfDay(final Timestamp timestamp, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.toEndOfDay(timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toStartOfYear tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfYear() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1),
                        TimestampUtils.newTimestamp(2020, 1, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 1, 2, 3, 4),
                        TimestampUtils.newTimestamp(2020, 1, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 1, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 10, 5),
                        TimestampUtils.newTimestamp(2020, 1, 1)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999),
                        TimestampUtils.newTimestamp(2020, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfYear")
    void toStartOfYear(final Timestamp timestamp, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.toStartOfYear(timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfYear tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfYear() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1),
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1, 1, 2, 3, 4),
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 10, 5),
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999),
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfYear")
    void toEndOfYear(final Timestamp timestamp, final Timestamp expectedResult) {
        final Timestamp actualResult = TimestampUtils.toEndOfYear(timestamp);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

}