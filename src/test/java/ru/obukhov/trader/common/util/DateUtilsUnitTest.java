package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.MockedStatic;
import org.springframework.scheduling.support.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Stream;

class DateUtilsUnitTest {

    private final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);

    @Test
    void now() {
        final OffsetDateTime mockedNow = OffsetDateTime.of(
                2023, 10, 11,
                12, 13, 14, 15,
                ZoneOffset.ofHours(16)
        );
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final OffsetDateTime result = DateUtils.now();
            Assertions.assertEquals(DateUtils.DEFAULT_OFFSET, result.getOffset());
            Assertions.assertTrue(mockedNow.isEqual(result));
        }
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
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1, 10, ZoneOffset.UTC);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2, 11, ZoneOffset.UTC);

        final Interval result = DateUtils.getIntervalWithDefaultOffsets(from, to);

        final OffsetDateTime expectedFrom = DateTimeTestData.newDateTime(2021, 1, 1, 13, DateUtils.DEFAULT_OFFSET);
        final OffsetDateTime expectedTo = DateTimeTestData.newDateTime(2021, 1, 2, 14, DateUtils.DEFAULT_OFFSET);
        Assertions.assertEquals(expectedFrom, result.getFrom());
        Assertions.assertEquals(expectedTo, result.getTo());
    }

    // endregion

    // region isWorkDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsWorkDay() {
        return Stream.of(
                Arguments.of(DateTimeTestData.newDateTime(2020, 8, 24), true), // monday
                Arguments.of(DateTimeTestData.newDateTime(2020, 8, 28), true), // friday
                Arguments.of(DateTimeTestData.newDateTime(2020, 8, 22), false), // saturday
                Arguments.of(DateTimeTestData.newDateTime(2020, 8, 23), false) // sunday
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
                        DateTimeTestData.newDateTime(2020, 10, 12),  // monday
                        DateTimeTestData.newDateTime(2020, 10, 13)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 16), // friday
                        DateTimeTestData.newDateTime(2020, 10, 19)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 17), // saturday
                        DateTimeTestData.newDateTime(2020, 10, 19)
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
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 1);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsFirst_whenSecondIsNull() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = null;

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getLatestDateTime_returnsSecond_whenFirstIsNull() {
        final OffsetDateTime dateTime1 = null;
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 1);

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
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 2);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime2, result);
    }

    @Test
    void getLatestDateTime_returnsFirst_whenFirstIsLater() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 2);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 1);

        final OffsetDateTime result = DateUtils.getLatestDateTime(dateTime1, dateTime2);

        Assertions.assertSame(dateTime1, result);
    }

    // endregion

    // region getAverage tests

    @Test
    void getAverage_returnsEqualsDate_whenDatesAreEqual() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 1, 10);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 1, 10);

        final OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        Assertions.assertEquals(dateTime1, result);
    }

    @Test
    void getAverage_returnsAverageDate_whenDatesAreNotEqual() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 1, 10);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 2, 10, 30);

        final OffsetDateTime result = DateUtils.getAverage(dateTime1, dateTime2);

        final OffsetDateTime expected = DateTimeTestData.newDateTime(2020, 1, 1, 22, 15);
        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region getEarliestDateTime tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetEarliestDateTime() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        null,
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        null,
                        null,
                        null
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 1)
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

    @Test
    void setTime_setsTime() {
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2020, 10, 10, 10, 10, 10);
        final OffsetTime time = DateTimeTestData.newTime(5, 30, 0);
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
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2021, 1, 5, 10, ZoneOffset.UTC);

        final OffsetDateTime result = DateUtils.setDefaultOffsetSameInstant(dateTime);

        final OffsetDateTime expected = DateTimeTestData.newDateTime(2021, 1, 5, 13, DateUtils.DEFAULT_OFFSET);

        Assertions.assertEquals(expected, result);
    }

    // region isAfter tests

    @Test
    @SuppressWarnings("ConstantConditions")
    void isAfter_returnsTrue_whenDateTime2IsNull() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 5);

        final boolean result = DateUtils.isAfter(dateTime1, null);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsTrue_whenDateTime1IsAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 5);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 1);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertTrue(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1EqualsAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 1);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    @Test
    void isAfter_returnsFalse_whenDateTime1isBeforeAfterDateTime2() {
        final OffsetDateTime dateTime1 = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime dateTime2 = DateTimeTestData.newDateTime(2020, 1, 5);

        final boolean result = DateUtils.isAfter(dateTime1, dateTime2);

        Assertions.assertFalse(result);
    }

    // region toStartOfDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfDay() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 5)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfDay")
    void toStartOfDay(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toStartOfDay(dateTime);


        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfDay tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfDay() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfDay")
    void toEndOfDay(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toEndOfDay(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toStartOf2Days tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOf2Days() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 4)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOf2Days")
    void toStartOf2Days(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toStartOf2Days(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOf2Days tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOf2Days() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOf2Days")
    void toEndOf2Days(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toEndOf2Days(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toStartOfWeek tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfWeek() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 5)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfWeek")
    void toStartOfWeek(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toStartOfWeek(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfWeek tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfWeek() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfWeek")
    void toEndOfWeek(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toEndOfWeek(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toStartOfMonth tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfMonth() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        DateTimeTestData.newDateTime(2020, 10, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfMonth")
    void toStartOfMonth(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toStartOfMonth(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfMonth tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfMonth() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfMonth")
    void toEndOfMonth(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toEndOfMonth(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toStartOfYear tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfYear() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfYear")
    void toStartOfYear(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toStartOfYear(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfYear tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfYear() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfYear")
    void toEndOfYear(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toEndOfYear(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toStartOf2Years tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOf2Years() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOf2Years")
    void toStartOf2Years(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toStartOf2Years(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOf2Years tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOf2Years() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 1, 1),
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOf2Years")
    void toEndOf2Years(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toEndOf2Years(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toStartOfDecade tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToStartOfDecade() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2025, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2029, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToStartOfDecade")
    void toStartOfDecade(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toStartOfDecade(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region toEndOfDecade tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToEndOfDecade() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2025, 7, 8, 10, 11, 12, 13),
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToEndOfDecade")
    void toEndOfDecade(final OffsetDateTime dateTime, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.toEndOfDecade(dateTime);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region assertDateTimeNotFuture tests

    @Test
    void assertDateTimeNotFuture_throwsException_whenDateTimeIsInFuture() {
        final OffsetDateTime now = DateTimeTestData.newDateTime(2021, 1, 1, 10);
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2021, 1, 1, 10, 0, 1);

        final Executable executable = () -> DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
        final String expectedMessage = "'name' (2021-01-01T10:00:01+03:00) can't be in future. Now is 2021-01-01T10:00+03:00";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsInPast() {
        final OffsetDateTime now = DateTimeTestData.newDateTime(2021, 1, 1, 10, 0, 1);
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2021, 1, 1, 10);

        DateUtils.assertDateTimeNotFuture(dateTime, now, "name");
    }

    @Test
    void assertDateTimeNotFuture_notThrowsException_whenDateTimeIsEqualsToNow() {
        final OffsetDateTime now = DateTimeTestData.newDateTime(2021, 1, 1, 10);
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2021, 1, 1, 10);

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

        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(year, month, dayOfMonth, hour, minute, second);

        final Date date = DateUtils.toDate(dateTime);

        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.setTimeZone(DateUtils.DEFAULT_TIME_ZONE);
        Assertions.assertEquals(year, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(month, calendar.get(Calendar.MONTH) + 1);
        Assertions.assertEquals(dayOfMonth, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(hour, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(minute, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(second, calendar.get(Calendar.SECOND));
    }

    // endregion

    // region fromDate tests

    @Test
    void fromDate_returnsEqualDate() {
        final int year = 2021;
        final int month = Calendar.OCTOBER;
        final int dayOfMonth = 11;
        final int hour = 12;
        final int minute = 13;
        final int second = 14;

        final Calendar calendar = new GregorianCalendar();
        calendar.set(year, Calendar.OCTOBER, dayOfMonth, hour, minute, second);
        calendar.setTimeZone(DateUtils.DEFAULT_TIME_ZONE);
        final Date date = calendar.getTime();

        final OffsetDateTime dateTime = DateUtils.fromDate(date);

        Assertions.assertEquals(year, dateTime.getYear());
        Assertions.assertEquals(month + 1, dateTime.getMonthValue());
        Assertions.assertEquals(dayOfMonth, dateTime.getDayOfMonth());
        Assertions.assertEquals(hour, dateTime.getHour());
        Assertions.assertEquals(minute, dateTime.getMinute());
        Assertions.assertEquals(second, dateTime.getSecond());
    }

    // endregion

    // region getCronHitsCountBetweenDates tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCronHitsBetweenDates_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 10, 11),
                        DateTimeTestData.newDateTime(2021, 10, 11)
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 10, 11, 0, 0, 0, 1),
                        DateTimeTestData.newDateTime(2021, 10, 11)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCronHitsBetweenDates_throwsIllegalArgumentException")
    void getCronHitsBetweenDates_throwsIllegalArgumentException(final String expression, final OffsetDateTime from, final OffsetDateTime to) {

        final CronExpression cronExpression = CronExpression.parse(expression);

        final Executable executable = () -> DateUtils.getCronHitsBetweenDates(cronExpression, from, to);
        final String expectedMessage = String.format("from [%s] must be before to [%s]", from, to);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCronHitsBetweenDates() {
        return Stream.of(
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 10, 1),
                        DateTimeTestData.newDateTime(2021, 10, 2),
                        List.of(DateTimeTestData.newDateTime(2021, 10, 1))
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 10, 1),
                        DateTimeTestData.newDateTime(2021, 12, 2),
                        List.of(
                                DateTimeTestData.newDateTime(2021, 10, 1),
                                DateTimeTestData.newDateTime(2021, 11, 1),
                                DateTimeTestData.newDateTime(2021, 12, 1)
                        )
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 10, 2),
                        DateTimeTestData.newDateTime(2021, 12, 2),
                        List.of(
                                DateTimeTestData.newDateTime(2021, 11, 1),
                                DateTimeTestData.newDateTime(2021, 12, 1)
                        )
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 10, 2),
                        DateTimeTestData.newDateTime(2022, 1, 1),
                        List.of(
                                DateTimeTestData.newDateTime(2021, 11, 1),
                                DateTimeTestData.newDateTime(2021, 12, 1)
                        )
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 12, 2),
                        DateTimeTestData.newDateTime(2022, 1, 1),
                        List.of()
                ),
                Arguments.of(
                        "0 0 0 1 * ?",
                        DateTimeTestData.newDateTime(2021, 1, 2),
                        DateTimeTestData.newDateTime(2021, 1, 30),
                        List.of()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCronHitsBetweenDates")
    void getCronHitsBetweenDates(
            final String expression,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final List<OffsetDateTime> expectedHits
    ) {

        final CronExpression cronExpression = CronExpression.parse(expression);

        final List<OffsetDateTime> hits = DateUtils.getCronHitsBetweenDates(cronExpression, from, to);

        AssertUtils.assertEquals(expectedHits, hits);
    }

    @Test
    void getCronHitsBetweenDates_returnsEmptyList_whenCronExpressionIsNull() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 1, 1);

        final List<OffsetDateTime> hits = DateUtils.getCronHitsBetweenDates(null, from, to);

        Assertions.assertTrue(hits.isEmpty());
    }

    // endregion

    // region timestampIsInInterval tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forTimestampIsInInterval_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 14),
                        DateTimeTestData.newDateTime(2020, 10, 5, 14)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 15),
                        DateTimeTestData.newDateTime(2020, 10, 5, 14)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forTimestampIsInInterval_throwsIllegalArgumentException")
    void timestampIsInInterval_throwsIllegalArgumentException(final OffsetDateTime from, final OffsetDateTime to) {
        final Timestamp timestamp = dateTimeMapper.offsetDateTimeToTimestamp(from);
        final Instant fromInstant = from.toInstant();
        final Instant toInstant = to.toInstant();

        final Executable executable = () -> DateUtils.timestampIsInInterval(timestamp, fromInstant, toInstant);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "From must be before to");
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forTimestampIsInInterval() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 14),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 12),
                        DateTimeTestData.newDateTime(2020, 10, 5, 14),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 11),
                        DateTimeTestData.newDateTime(2020, 10, 5, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 14),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 14),
                        DateTimeTestData.newDateTime(2020, 10, 5, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 14),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 15),
                        DateTimeTestData.newDateTime(2020, 10, 5, 13),
                        DateTimeTestData.newDateTime(2020, 10, 5, 14),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forTimestampIsInInterval")
    void timestampIsInInterval(final OffsetDateTime dateTime, final OffsetDateTime from, final OffsetDateTime to, final boolean expectedResult) {
        final Timestamp timestamp = dateTimeMapper.offsetDateTimeToTimestamp(dateTime);

        final boolean result = DateUtils.timestampIsInInterval(timestamp, from.toInstant(), to.toInstant());

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region toSameDayInstant tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToSameDayInstant() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1, 1, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1, 3, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1, 5, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 2, 10, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 2, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 2, 10, 1, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 2, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 2, 10, 3, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 2, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 2, 10, 5, ZoneOffset.ofHours(3)),
                        OffsetDateTime.of(2020, 2, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToSameDayInstant")
    void toSameDayInstant(final OffsetDateTime dateTime, final Instant expected) {
        final Instant instant = DateUtils.toSameDayInstant(dateTime);

        Assertions.assertEquals(expected, instant);
    }

    // endregion

    // region equalDates tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCandleEndTime() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 3),
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 10),
                        CandleInterval.CANDLE_INTERVAL_2_MIN,
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 3),
                        CandleInterval.CANDLE_INTERVAL_3_MIN,
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 6)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 5),
                        CandleInterval.CANDLE_INTERVAL_5_MIN,
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 10)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 30),
                        CandleInterval.CANDLE_INTERVAL_10_MIN,
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 40)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 30),
                        CandleInterval.CANDLE_INTERVAL_15_MIN,
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 45)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10, 30),
                        CandleInterval.CANDLE_INTERVAL_30_MIN,
                        DateTimeTestData.newDateTime(2024, 6, 10, 11)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10),
                        CandleInterval.CANDLE_INTERVAL_HOUR,
                        DateTimeTestData.newDateTime(2024, 6, 10, 11)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10),
                        CandleInterval.CANDLE_INTERVAL_2_HOUR,
                        DateTimeTestData.newDateTime(2024, 6, 10, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10, 10),
                        CandleInterval.CANDLE_INTERVAL_4_HOUR,
                        DateTimeTestData.newDateTime(2024, 6, 10, 14)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10),
                        CandleInterval.CANDLE_INTERVAL_DAY,
                        DateTimeTestData.newDateTime(2024, 6, 11)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 6, 10),
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        DateTimeTestData.newDateTime(2024, 6, 17)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2024, 5, 1),
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(2024, 6, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCandleEndTime")
    void getCandleEndTime(final OffsetDateTime candleTime, final CandleInterval candleInterval, final OffsetDateTime expectedResult) {
        final OffsetDateTime actualResult = DateUtils.getCandleEndTime(candleTime, candleInterval);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @EnumSource(names = {"CANDLE_INTERVAL_UNSPECIFIED", "UNRECOGNIZED"})
    void getCandleEndTime_throwsIllegalArgumentException_whenCandlesIntervalIsUnexpected(final CandleInterval candleInterval) {
        final OffsetDateTime candleTime = DateTimeTestData.newDateTime(2024, 5, 1);
        final Executable executable = () -> DateUtils.getCandleEndTime(candleTime, candleInterval);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Unexpected candle interval " + candleInterval);
    }

    // endregion

    @Test
    void toLocalDateTime() {
        final OffsetDateTime offsetDateTime = DateTimeTestData.newDateTime(2024, 5, 1, 10);
        final LocalDateTime actualResult = DateUtils.toLocalDateTime(offsetDateTime);
        final ZoneOffset systemOffset = OffsetDateTime.now().getOffset();
        Assertions.assertEquals(offsetDateTime.toInstant(), actualResult.toInstant(systemOffset));
    }

}