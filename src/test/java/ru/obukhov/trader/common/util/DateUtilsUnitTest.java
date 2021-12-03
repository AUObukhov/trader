package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.test.utils.DateTimeTestData;
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

    // region getIntervalWithDefaultOffsets tests

    @Test
    void getIntervalWithDefaultOffsets_returnsIntervalWithNulls_whenArgumentsAreNull() {
        final Interval result = DateUtils.getIntervalWithDefaultOffsets(null, null);

        Assertions.assertNull(result.getFrom());
        Assertions.assertNull(result.getTo());
    }

    @Test
    void getIntervalWithDefaultOffsets_changesOffsets_whenOffsetsAreNotDefault() {
        final OffsetDateTime from = OffsetDateTime.of(2021, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime to = OffsetDateTime.of(2021, 1, 2, 11, 0, 0, 0, ZoneOffset.UTC);

        final Interval result = DateUtils.getIntervalWithDefaultOffsets(from, to);

        final OffsetDateTime expectedFrom = OffsetDateTime.of(
                2021, 1, 1,
                13, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET
        );
        final OffsetDateTime expectedTo = OffsetDateTime.of(
                2021, 1, 2,
                14, 0, 0, 0,
                DateUtils.DEFAULT_OFFSET
        );
        Assertions.assertEquals(expectedFrom, result.getFrom());
        Assertions.assertEquals(expectedTo, result.getTo());
    }

    // endregion

    // region isWorkDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsWorkDay() {
        return Stream.of(
                Arguments.of(DateTimeTestData.createDateTime(2020, 8, 24), true), // monday
                Arguments.of(DateTimeTestData.createDateTime(2020, 8, 28), true), // friday
                Arguments.of(DateTimeTestData.createDateTime(2020, 8, 22), false), // saturday
                Arguments.of(DateTimeTestData.createDateTime(2020, 8, 23), false) // sunday
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
                        DateTimeTestData.createDateTime(2020, 10, 12),  // monday
                        DateTimeTestData.createDateTime(2020, 10, 13)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 16), // friday
                        DateTimeTestData.createDateTime(2020, 10, 19)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 17), // saturday
                        DateTimeTestData.createDateTime(2020, 10, 19)
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

    // region getLatestDateTime tests

    @Test
    void getLatestDateTime_returnsFirst_whenEquals() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 1);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsFirst_whenSecondIsNull() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = null;

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsSecond_whenFirstIsNull() {
        final OffsetDateTime dateTime1 = null;
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 1);

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
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 2);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    @Test
    void getLatestDateTime_returnsFirst_whenFirstIsLater() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 2);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 1);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    // endregion

    // region getAverage tests

    @Test
    void getAverage_returnsEqualsDate_whenDatesAreEqual() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 1, 10);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 1, 10);

        final OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        Assertions.assertEquals(dateTime1, result);
    }

    @Test
    void getAverage_returnsAverageDate_whenDatesAreNotEqual() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 1, 10);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 2, 10, 30);

        final OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        final OffsetDateTime expected = DateTimeTestData.createDateTime(2020, 1, 1, 22, 15);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region getEarliestDateTime tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetEarliestDateTime() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        DateTimeTestData.createDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        null,
                        DateTimeTestData.createDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        DateTimeTestData.createDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        DateTimeTestData.createDateTime(2020, 1, 2),
                        DateTimeTestData.createDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 1, 2),
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        DateTimeTestData.createDateTime(2020, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetEarliestDateTime")
    void getEarliestDateTime(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2, final OffsetDateTime expected) {
        final OffsetDateTime result = DateUtils.getEarliestDateTime(dateTime1, dateTime2);

        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region plusLimited tests

    @Test
    void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsAfterThanIncrementedDateTime() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime maxDateTime = DateTimeTestData.createDateTime(2020, 2, 1);

        final OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, maxDateTime);

        Assertions.assertNotSame(maxDateTime, result);
        Assertions.assertTrue(dateTime.isBefore(result));
        Assertions.assertTrue(maxDateTime.isAfter(result));
    }

    @Test
    void plusLimited_returnsIncrementedDateTime_whenMaxDateTimeIsNull() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 1, 1);

        final OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assertions.assertTrue(dateTime.isBefore(result));
    }

    @Test
    void plusLimited_returnsMaxDateTime_whenMaxDateTimeIsBeforeThanIncrementedDateTime() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime maxDateTime = DateTimeTestData.createDateTime(2020, 2, 1);

        final OffsetDateTime result = DateUtils.plusLimited(dateTime, 2, ChronoUnit.MONTHS, maxDateTime);

        Assertions.assertSame(maxDateTime, result);
    }

    // endregion

    // region minusLimited tests

    @Test
    void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsBeforeThanDecrementedDateTime() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 1, 5);
        final OffsetDateTime minDateTime = DateTimeTestData.createDateTime(2020, 1, 2);

        final OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, minDateTime);

        Assertions.assertNotSame(minDateTime, result);
        Assertions.assertTrue(dateTime.isAfter(result));
        Assertions.assertTrue(minDateTime.isBefore(result));
    }

    @Test
    void minusLimited_returnsDecrementedDateTime_whenMinDateTimeIsNull() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 1, 5);

        final OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.DAYS, null);

        Assertions.assertTrue(dateTime.isAfter(result));
    }

    @Test
    void minusLimited_returnsMinDateTime_whenMinDateTimeIsAfterThanDecrementedDateTime() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 1, 5);
        final OffsetDateTime minDateTime = DateTimeTestData.createDateTime(2020, 1, 4);

        final OffsetDateTime result = DateUtils.minusLimited(dateTime, 2, ChronoUnit.MONTHS, minDateTime);

        Assertions.assertSame(minDateTime, result);
    }

    // endregion

    @Test
    void setTime_setsTime() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 10, 10, 10, 10);
        final OffsetTime time = DateTimeTestData.createTime(5, 30, 0);
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
        final OffsetDateTime dateTime = OffsetDateTime.of(
                2021, 1, 5,
                10, 0, 0, 0,
                ZoneOffset.UTC
        );

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
    void getPeriodUnitByCandleResolution_returnsDays_whenResolutionIsHour() {
        final TemporalUnit unit = DateUtils.getPeriodByCandleResolution(CandleResolution.HOUR);

        Assertions.assertEquals(ChronoUnit.DAYS, unit);
    }

    @Test
    void getPeriodUnitByCandleResolution_returnsYears_whenResolutionIsDay() {
        final TemporalUnit unit = DateUtils.getPeriodByCandleResolution(CandleResolution.DAY);

        Assertions.assertEquals(ChronoUnit.YEARS, unit);
    }

    // endregion

    // region isAfter tests

    @Test
    @SuppressWarnings("ConstantConditions")
    void isAfter_returnsTrue_whenDateTime2IsNull() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 5);

        final boolean result = DateUtils.isAfter(dateTime1, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsTrue_whenDateTime1IsAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 5);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 1);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1EqualsAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 1);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1isBeforeAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.createDateTime(2020, 1, 5);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    // endregion

    // region roundUpToDay tests

    @Test
    void roundUpToDay_doesNotChangesDateTime_whenDateTimeIsStartOfDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2019, 1, 1);

        final OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        final OffsetDateTime expected = DateTimeTestData.createDateTime(2019, 1, 1);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void roundUpToDay_movesDateTimeToNextDay_whenDateTimeIsAfterStartOfDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 5, 5, 4, 6, 7);

        final OffsetDateTime result = DateUtils.roundUpToDay(dateTime);

        final OffsetDateTime expected = DateTimeTestData.createDateTime(2020, 5, 6);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region roundDownToYear tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forRoundDownToYear() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2019, 1, 1),
                        DateTimeTestData.createDateTime(2019, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2019, 5, 5, 1),
                        DateTimeTestData.createDateTime(2019, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2019, 1, 5, 0, 0, 1),
                        DateTimeTestData.createDateTime(2019, 1, 1)
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
                        DateTimeTestData.createDateTime(2019, 1, 1),
                        DateTimeTestData.createDateTime(2019, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2019, 5, 5),
                        DateTimeTestData.createDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2019, 1, 5, 0, 0, 1),
                        DateTimeTestData.createDateTime(2020, 1, 1)
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
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(-1);

        final Executable executable = () -> DateUtils.isWorkTime(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "workTimeDuration must be positive");
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsZero() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(0);

        final Executable executable = () -> DateUtils.isWorkTime(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "workTimeDuration must be positive");
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsOneDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofDays(1);

        final Executable executable = () -> DateUtils.isWorkTime(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "workTimeDuration must be less than 1 day");
    }

    @Test
    void isWorkTime_throwsIllegalArgumentException_whenDurationIsMoreThanOneDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(25);

        final Executable executable = () -> DateUtils.isWorkTime(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "workTimeDuration must be less than 1 day");
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_isWorkTime() {
        return Stream.of(
                // dateTime is work time
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        true
                ),
                // dateTime is before midnight and end work time is after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 23),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is after midnight and end work time is after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 1),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is start time
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        true
                ),
                // dateTime is start time and end work time are after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is saturday and before end of work time and both are after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 1),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        true
                ),
                // dateTime is weekend and work time
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 3, 12),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is weekend and not work time
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 3, 4),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is work day and not work time
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 8, 4),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is work day and end of work time
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 8, 19),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is work day and end of work time and both after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 8, 2),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is after end of work time and both are after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 8, 5),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is saturday and after end of work time and both are after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 4),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is saturday and before end of work time and both are before midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 13),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                ),
                // saturday and equals end of work time and both are before midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 19),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is sunday and before end of work time and both are after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 11, 1),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is sunday and after end of work time and both are after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 11, 4),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is sunday and time equals end of work time and both are after midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 11, 2),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(16),
                        false
                ),
                // dateTime is sunday and before end of work time and both are before midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 11, 13),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                ),
                // dateTime is sunday and equals end of work time and both are before midnight
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 11, 19),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_isWorkTime")
    void isWorkTime(final OffsetDateTime dateTime, final OffsetTime startTime, final Duration duration, final boolean expectedResult) {
        final boolean result = DateUtils.isWorkTime(dateTime, startTime, duration);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region getCeilingWorkTime tests

    @Test
    void getCeilingWorkTime_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(-1);

        final Executable executable = () -> DateUtils.getCeilingWorkTime(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "workTimeDuration must be positive");
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_getCeilingWorkTime_orGetNextWorkMinute_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(0),
                        "workTimeDuration must be positive"
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(24),
                        "workTimeDuration must be less than 1 day"
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(25),
                        "workTimeDuration must be less than 1 day"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_getCeilingWorkTime_orGetNextWorkMinute_throwsIllegalArgumentException")
    void getCeilingWorkTime_throwsIllegalArgumentException(
            final OffsetDateTime dateTime,
            final OffsetTime startTime,
            final Duration duration,
            final String expectedErrorMessage
    ) {
        final Executable executable = () -> DateUtils.getCeilingWorkTime(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedErrorMessage);
    }

    @Test
    void getCeilingWorkTime_returnsCurrentMinute_whenMiddleOfWorkDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getCeilingWorkTime(dateTime, startTime, duration);

        Assertions.assertEquals(dateTime, nextWorkMinute);
    }

    @Test
    void getCeilingWorkTime_returnsStartOfNextDay_whenAtEndOfWorkDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 19);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getCeilingWorkTime(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getCeilingWorkTime_returnsStartOfNextDay_whenAfterEndOfWorkDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 19, 20);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getCeilingWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(1), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getCeilingWorkTime_returnsStartOfNextWeek_whenEndOfWorkWeek() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 19);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getCeilingWorkTime(dateTime, startTime, duration);

        OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(3), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getCeilingWorkTime_returnsStartOfNextWeek_whenAtWeekend() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 10, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getCeilingWorkTime(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime.plusDays(2), startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    @Test
    void getCeilingWorkTime_returnsStartOfTodayWorkDay_whenBeforeStartOfTodayWorkDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 9, 9);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(9);

        final OffsetDateTime nextWorkMinute = DateUtils.getCeilingWorkTime(dateTime, startTime, duration);

        final OffsetDateTime expected = DateUtils.setTime(dateTime, startTime);
        Assertions.assertEquals(expected, nextWorkMinute);
    }

    // endregion

    // region getNextWorkMinute tests

    @Test
    void getNextWorkMinute_throwsIllegalArgumentException_whenWorkTimeDurationIsNegative() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 12);
        final OffsetTime startTime = DateTimeTestData.createTime(10, 0, 0);
        final Duration duration = Duration.ofHours(-1);

        final Executable executable = () -> DateUtils.getNextWorkMinute(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "workTimeDuration must be positive");
    }

    @ParameterizedTest
    @MethodSource("getData_getNearestWorkTime_orGetNextWorkMinute_throwsIllegalArgumentException")
    void getNextWorkMinute_throwsIllegalArgumentException(
            final OffsetDateTime dateTime,
            final OffsetTime startTime,
            final Duration duration,
            final String expectedErrorMessage
    ) {
        final Executable executable = () -> DateUtils.getNextWorkMinute(dateTime, startTime, duration);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedErrorMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetNextWorkMinute() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 12),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        DateTimeTestData.createDateTime(2020, 10, 5, 12, 1)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 19, 20),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 19), // friday
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 10, 12), // saturday
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        DateTimeTestData.createDateTime(2020, 10, 12, 10) // monday
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 9, 9),
                        DateTimeTestData.createTime(10, 0, 0),
                        Duration.ofHours(9),
                        DateTimeTestData.createDateTime(2020, 10, 9, 10)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetNextWorkMinute")
    void getNextWorkMinute(final OffsetDateTime dateTime, final OffsetTime startTime, final Duration duration, final OffsetDateTime expectedResult) {
        final OffsetDateTime result = DateUtils.getNextWorkMinute(dateTime, startTime, duration);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region atStartOfDay tests

    @Test
    void atStartOfDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40);

        final OffsetDateTime startOfDay = DateUtils.atStartOfDay(dateTime);

        final OffsetDateTime expected = DateTimeTestData.createDateTime(2020, 10, 5);

        Assertions.assertEquals(expected, startOfDay);
    }

    // endregion

    // region atEndOfDay tests

    @Test
    void atEndOfDay() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40);

        final OffsetDateTime endOfDay = DateUtils.atEndOfDay(dateTime);

        final OffsetDateTime expected = DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        Assertions.assertEquals(expected, endOfDay);
    }

    // endregion

    // region atStartOfYear tests

    @Test
    void atStartOfYear() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40);

        final OffsetDateTime startOfDay = DateUtils.atStartOfYear(dateTime);

        final OffsetDateTime expected = DateTimeTestData.createDateTime(2020, 1, 1);

        Assertions.assertEquals(expected, startOfDay);
    }

    // endregion

    // region atEndOfYear tests

    @Test
    void atEndOfYear() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40);

        final OffsetDateTime startOfDay = DateUtils.atEndOfYear(dateTime);

        final OffsetDateTime expected = DateTimeTestData.createDateTime(2020, 12, 31, 23, 59, 59, 999999999);

        Assertions.assertEquals(expected, startOfDay);
    }

    // endregion

    // region withDefaultOffset tests

    @Test
    void withDefaultOffset() {
        final ZoneOffset testOffset = DateTimeTestData.getNotDefaultOffset();

        final OffsetDateTime dateTimeWithTestOffset = OffsetDateTime.now().withOffsetSameInstant(testOffset);
        final OffsetDateTime dateTimeWithDefaultOffset = DateUtils.withDefaultOffset(dateTimeWithTestOffset);

        Assertions.assertEquals(DateUtils.DEFAULT_OFFSET, dateTimeWithDefaultOffset.getOffset());
    }

    // endregion

    // region assertDateTimeNotFuture tests

    @Test
    void assertDateTimeNotFuture_throwsException_whenDateTimeIsInFuture() {
        final OffsetDateTime now = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2021, 1, 1, 10, 0, 1);

        final Executable executable = () -> DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
        final String expectedMessage = "'name' (2021-01-01T10:00:01+03:00) can't be in future. Now is 2021-01-01T10:00+03:00";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsInPast() {
        final OffsetDateTime now = DateTimeTestData.createDateTime(2021, 1, 1, 10, 0, 1);
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2021, 1, 1, 10);

        DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsEqualsToNow() {
        final OffsetDateTime now = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2021, 1, 1, 10);

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

        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(year, month, dayOfMonth, hour, minute, second);

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

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCronHitsBetweenDates_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 10, 11),
                        DateTimeTestData.createDateTime(2021, 10, 11),
                        "from must be before to"
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 10, 11, 0, 0, 0, 1),
                        DateTimeTestData.createDateTime(2021, 10, 11),
                        "from must be before to"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCronHitsBetweenDates_throwsIllegalArgumentException")
    void getCronHitsBetweenDates_throwsIllegalArgumentException(
            final String expression,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final String expectedMessage
    ) throws ParseException {

        final CronExpression cronExpression = new CronExpression(expression);

        final Executable executable = () -> DateUtils.getCronHitsBetweenDates(cronExpression, from, to);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCronHitsBetweenDates() {
        return Stream.of(
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 10, 1),
                        DateTimeTestData.createDateTime(2021, 10, 2),
                        1
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 10, 1),
                        DateTimeTestData.createDateTime(2021, 12, 2),
                        3
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 10, 2),
                        DateTimeTestData.createDateTime(2021, 12, 2),
                        2
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 10, 2),
                        DateTimeTestData.createDateTime(2022, 1, 1),
                        2
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 12, 2),
                        DateTimeTestData.createDateTime(2022, 1, 1),
                        0
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.createDateTime(2021, 1, 2),
                        DateTimeTestData.createDateTime(2021, 1, 30),
                        0
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCronHitsBetweenDates")
    void getCronHitsBetweenDates(final String expression, final OffsetDateTime from, final OffsetDateTime to, final int expectedCount)
            throws ParseException {

        final CronExpression cronExpression = new CronExpression(expression);

        final int count = DateUtils.getCronHitsBetweenDates(cronExpression, from, to);

        Assertions.assertEquals(expectedCount, count);
    }

    // endregion

}