package ru.obukhov.trader.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

class IntervalUnitTest {

    // region constructor tests

    @Test
    void constructor_throwsIllegalArgumentException_whenFromIsAfterTo() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 5);

        final Executable executable = () -> new Interval(from, to);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "from can't be after to");
    }

    @Test
    void constructor_returnsInterval_whenFromAndToAreNull() {
        final Interval interval = new Interval(null, null);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void constructor_returnsInterval_whenFromIsNull() {
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 10);

        final Interval interval = new Interval(null, to);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void constructor_returnsInterval_whenToIsNull() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5);

        final Interval interval = new Interval(from, null);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void constructor_returnsInterval_whenFromAndToAreNotNull() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 10);

        final Interval interval = new Interval(from, to);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void constructor_adjustsOffsets() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5, 4, ZoneOffset.UTC);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 10, 7, ZoneOffset.UTC);

        final Interval interval = new Interval(from, to);

        final OffsetDateTime expectedFrom = DateTimeTestData.newDateTime(2020, 10, 5, 7);
        final OffsetDateTime expectedTo = DateTimeTestData.newDateTime(2020, 10, 10, 10);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
    }

    // endregion

    // region of tests

    @Test
    void of_throwsIllegalArgumentException_whenFromIsAfterTo() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 5);

        final Executable executable = () -> Interval.of(from, to);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "from can't be after to");
    }

    @Test
    void of_returnsInterval_whenFromAndToAreNull() {
        final Interval interval = Interval.of(null, null);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_returnsInterval_whenFromIsNull() {
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 10);

        final Interval interval = Interval.of(null, to);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void of_returnsInterval_whenToIsNull() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5);

        final Interval interval = Interval.of(from, null);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_returnsInterval_whenFromAndToAreNotNull() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 10);

        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void of_adjustsOffsets() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5, 4, ZoneOffset.UTC);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 10, 7, ZoneOffset.UTC);

        final Interval interval = Interval.of(from, to);

        final OffsetDateTime expectedFrom = DateTimeTestData.newDateTime(2020, 10, 5, 7);
        final OffsetDateTime expectedTo = DateTimeTestData.newDateTime(2020, 10, 10, 10);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
    }

    // endregion

    // region limitByNowIfNull tests

    @Test
    void limitByNowIfNull_setToToNow_whenToIsNull() {
        final OffsetDateTime now = DateUtils.now();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5);

        final Interval interval = Interval.of(from, null);
        final Interval newInterval = interval.limitByNowIfNull(now);

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertNotNull(newInterval.getTo());
    }

    @Test
    void limitByNowIfNull_notChangesTo_whenToIsNotNull() {
        final OffsetDateTime now = DateUtils.now();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 10);

        final Interval interval = Interval.of(from, to);
        final Interval newInterval = interval.limitByNowIfNull(now);

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertEquals(interval.getTo(), newInterval.getTo());
    }

    // endregion

    // region extendTo tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forExtendTo_throwsIllegalArgumentException_whenNotSamePeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10),
                        DateTimeTestData.newDateTime(2020, 10, 6, 11),
                        Periods.DAY
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 6, 0, 0, 0, 1),
                        Periods.DAY
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10),
                        DateTimeTestData.newDateTime(2020, 10, 6, 11),
                        Periods.TWO_DAYS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 6, 0, 0, 0, 1),
                        Periods.TWO_DAYS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10),
                        DateTimeTestData.newDateTime(2020, 10, 12, 11),
                        Periods.WEEK
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 12, 0, 0, 0, 1),
                        Periods.WEEK
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        DateTimeTestData.newDateTime(2020, 11, 2),
                        Periods.MONTH
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 11, 1, 0, 0, 0, 1),
                        Periods.MONTH
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2021, 10, 6),
                        Periods.YEAR
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2021, 1, 1, 0, 0, 0, 1),
                        Periods.YEAR
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 5, 1),
                        DateTimeTestData.newDateTime(2022, 1, 2),
                        Periods.TWO_YEARS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2022, 1, 1, 0, 0, 0, 1),
                        Periods.TWO_YEARS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 5, 1),
                        DateTimeTestData.newDateTime(2032, 1, 2),
                        Periods.DECADE
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2030, 1, 1, 0, 0, 0, 1),
                        Periods.DECADE
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forExtendTo_throwsIllegalArgumentException_whenNotSamePeriod")
    void extendTo_throwsIllegalArgumentException_whenNotSamePeriod(final OffsetDateTime from, final OffsetDateTime to, final Period period) {
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> interval.extendTo(period);
        final String expectedMessage = String.format("'from' (%s) and 'to' (%s) must be at the same period %s", from, to, period);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void extendTo_throwsIllegalArgumentException_whenFromIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = mockedNow.plusHours(1);
        final OffsetDateTime to = from.plusMinutes(10);
        final Interval interval = Interval.of(from, to);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Executable executable = () -> interval.extendTo(Periods.DAY);
            final String expectedMessage = "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
        }
    }

    @Test
    void extendTo_throwsIllegalArgumentException_whenToIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = mockedNow.plusMinutes(-10);
        final OffsetDateTime to = mockedNow.plusMinutes(10);
        final Interval interval = Interval.of(from, to);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> timestampStaticMock = Mocker.mockNow(mockedNow)) {
            final Executable executable = () -> interval.extendTo(Periods.DAY);
            final String expectedMessage = "'to' (2020-09-23T10:21:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forExtendTo_extendsToWholePeriod_whenFromAndToInSamePeriod_andBeforeCurrentPeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 6)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 4),
                        DateTimeTestData.newDateTime(2020, 10, 6)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 7, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 8, 11, 30, 40),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 2, 1, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 2, 25, 11, 30, 40),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 2, 1),
                        DateTimeTestData.newDateTime(2020, 3, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 7, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 8, 11, 30, 40),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        DateTimeTestData.newDateTime(2020, 11, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2021, 10, 5, 11, 30, 40),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2023, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2012, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2014, 10, 5, 11, 30, 40),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2011, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forExtendTo_extendsToWholePeriod_whenFromAndToInSamePeriod_andBeforeCurrentPeriod")
    void extendTo_extendsToWholePeriod_whenFromAndToInSamePeriod_andBeforeCurrentPeriod(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final Period period,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) {
        final OffsetDateTime mockedNow = to.plus(period);
        final Interval interval = Interval.of(from, to);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendTo(period);

            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(expectedTo, extendedInterval.getTo());
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forExtendTo_extendsToNow_whenFromAndToAreInCurrentPeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23, 3, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 23, 6, 11, 12),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 9, 23, 12, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 23)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 22, 3, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 23, 6, 11, 12),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 9, 23, 12, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 22)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23, 3, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 24, 6, 11, 12),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 9, 25, 12, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 21)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 13, 3, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 14, 6, 11, 12),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 9, 20, 6, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 6, 23, 3, 11, 12),
                        DateTimeTestData.newDateTime(2020, 6, 24, 6, 11, 12),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2020, 9, 2, 6, 11, 12),
                        DateTimeTestData.newDateTime(2020, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 2, 10, 6, 11, 12),
                        DateTimeTestData.newDateTime(2020, 9, 10, 6, 11, 12),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2019, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 9, 23, 3, 11, 12),
                        DateTimeTestData.newDateTime(2024, 9, 24, 6, 11, 12),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2026, 4, 15, 6, 11, 12),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forExtendTo_extendsToNow_whenFromAndToAreInCurrentPeriod")
    void extendTo_extendsToNow_whenFromAndToAreInCurrentPeriod(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final Period period,
            final OffsetDateTime now,
            final OffsetDateTime expectedFrom
    ) {
        final Interval interval = Interval.of(from, to);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(now)) {
            final Interval extendedInterval = interval.extendTo(period);

            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(now, extendedInterval.getTo());
        }
    }

    // endregion

    // region isAnyPeriod tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forIsAnyPeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        null,
                        false
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        false
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        DateTimeTestData.newDateTime(2020, 9, 24),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        DateTimeTestData.newDateTime(2020, 9, 23, 23, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        DateTimeTestData.newDateTime(2020, 9, 24, 0, 0, 0, 1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23, 10),
                        DateTimeTestData.newDateTime(2020, 9, 24, 10),
                        false
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 22),
                        DateTimeTestData.newDateTime(2020, 9, 24),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 22),
                        DateTimeTestData.newDateTime(2020, 9, 23, 23, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 22),
                        DateTimeTestData.newDateTime(2020, 9, 24, 0, 0, 0, 1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        DateTimeTestData.newDateTime(2020, 9, 25),
                        false
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 21),
                        DateTimeTestData.newDateTime(2020, 9, 28),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 21),
                        DateTimeTestData.newDateTime(2020, 9, 27, 23, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 21),
                        DateTimeTestData.newDateTime(2020, 9, 28, 0, 0, 0, 1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 23),
                        DateTimeTestData.newDateTime(2020, 9, 30),
                        false
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 1),
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 1),
                        DateTimeTestData.newDateTime(2020, 9, 30, 23, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 1),
                        DateTimeTestData.newDateTime(2020, 10, 1, 0, 0, 0, 1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 9, 10),
                        DateTimeTestData.newDateTime(2020, 10, 10),
                        false
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1, 0, 0, 0, 1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 4, 1),
                        DateTimeTestData.newDateTime(2021, 4, 1),
                        false
                ),

                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2031, 1, 1),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2031, 1, 1, 0, 0, 0, 1),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2025, 1, 1),
                        DateTimeTestData.newDateTime(2034, 1, 1),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forIsAnyPeriod")
    void isAnyPeriod(final OffsetDateTime from, final OffsetDateTime to, final boolean expectedResult) {
        final Interval interval = Interval.of(from, to);

        final boolean actualResult = interval.isAnyPeriod();

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region hashCode tests

    @Test
    void hashCode_returnsEqualValuesForEqualIntervals() {
        final Interval interval1 = Interval.of(
                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
        );
        final Interval interval2 = Interval.of(
                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
        );

        Assertions.assertEquals(interval1.hashCode(), interval2.hashCode());
    }

    // endregion

    // region equals tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forEquals() {
        return Stream.of(
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        false
                ),

                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2019, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.newDateTime(2021, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.newDateTime(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEquals")
    void equals(Interval interval1, Interval interval2, boolean expected) {
        Assertions.assertEquals(expected, interval1.equals(interval2));
    }

    // endregion

    // region contains tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forContains() {
        return Stream.of(
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 9, 999_999_999),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.newDateTime(2020, 10, 1, 12, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 9, 999_999_999),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 10),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                null,
                                DateTimeTestData.newDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.newDateTime(2020, 10, 1, 12, 5, 10),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.newDateTime(2020, 10, 1, 10, 5, 10),
                                null
                        ),
                        DateTimeTestData.newDateTime(2020, 10, 1, 12, 5, 10),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forContains")
    void contains(Interval interval, OffsetDateTime dateTime, boolean expectedResult) {
        Assertions.assertEquals(expectedResult, interval.contains(dateTime));
    }

    // endregion

    // region splitIntoIntervals tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSplitIntoIntervals_returnsEmptyList_whenFromAndToAreEqual() {
        return Stream.of(
                Arguments.of(Periods.DAY),
                Arguments.of(Periods.TWO_DAYS),
                Arguments.of(Periods.WEEK),
                Arguments.of(Periods.MONTH),
                Arguments.of(Periods.YEAR),
                Arguments.of(Periods.TWO_YEARS),
                Arguments.of(Periods.DECADE)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSplitIntoIntervals_returnsEmptyList_whenFromAndToAreEqual")
    void splitIntoIntervals_returnsEmptyList_whenFromAndToAreEqual(final Period period) {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoIntervals(period);

        Assertions.assertTrue(intervals.isEmpty());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSplitIntoIntervals_returnsOnePair_whenFromAndToInOnePeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 5, 12, 20, 30),
                        Periods.DAY
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 1),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        Periods.DAY
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 5, 12, 20, 30),
                        Periods.TWO_DAYS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 1),
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        Periods.TWO_DAYS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 10, 12, 20, 30),
                        Periods.WEEK
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 1),
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999),
                        Periods.WEEK
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 21, 12, 20, 30),
                        Periods.MONTH
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 1, 1),
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999),
                        Periods.MONTH
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 21, 12, 20, 30),
                        Periods.YEAR
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1, 1),
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.YEAR
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2021, 10, 21, 12, 20, 30),
                        Periods.TWO_YEARS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1, 1),
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.TWO_YEARS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2026, 10, 21, 12, 20, 30),
                        Periods.DECADE
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1, 1),
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.DECADE
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSplitIntoIntervals_returnsOnePair_whenFromAndToInOnePeriod")
    void splitIntoIntervals_returnsOnePair_whenFromAndToInOnePeriod(final OffsetDateTime from, final OffsetDateTime to, final Period period) {
        final List<Interval> intervals = Interval.of(from, to).splitIntoIntervals(period);

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.getFirst().getFrom());
        Assertions.assertEquals(to, intervals.getFirst().getTo());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSplitIntoIntervals_returnsTwoPairs_whenFromAndToDiffersInOnePeriod() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 6, 12, 20, 30),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 6)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 6, 12, 20, 30),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 6)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 12, 12, 20, 30),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 11, 12, 12, 20, 30),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 11, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2021, 10, 5),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 10, 5),
                        DateTimeTestData.newDateTime(2023, 10, 5),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2023, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 10, 5),
                        DateTimeTestData.newDateTime(2032, 10, 5),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2031, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSplitIntoIntervals_returnsTwoPairs_whenFromAndToDiffersInOnePeriod")
    void splitIntoIntervals_returnsTwoPairs_whenFromAndToDiffersInOnePeriod(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final Period period,
            final OffsetDateTime expectedTo0AndFrom1
    ) {
        final List<Interval> intervals = Interval.of(from, to).splitIntoIntervals(period);

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.getFirst().getFrom());
        Assertions.assertEquals(expectedTo0AndFrom1, intervals.getFirst().getTo());
        Assertions.assertEquals(expectedTo0AndFrom1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSplitIntoIntervals_returnsOnePair_whenFromAndToAreAtStartOfNeighbourPeriods() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 6),
                        Periods.DAY
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 4),
                        DateTimeTestData.newDateTime(2020, 10, 6),
                        Periods.TWO_DAYS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2020, 10, 12),
                        Periods.WEEK
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 1),
                        DateTimeTestData.newDateTime(2020, 11, 1),
                        Periods.MONTH
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        Periods.YEAR
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2023, 1, 1),
                        Periods.TWO_YEARS
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2031, 1, 1),
                        Periods.DECADE
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSplitIntoIntervals_returnsOnePair_whenFromAndToAreAtStartOfNeighbourPeriods")
    void splitIntoIntervals_returnsOnePair_whenFromAndToAreAtStartOfNeighbourPeriods(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final Period period
    ) {
        final List<Interval> intervals = Interval.of(from, to).splitIntoIntervals(period);

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.getFirst().getFrom());
        Assertions.assertEquals(to, intervals.getFirst().getTo());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSplitIntoIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourPeriods() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 6, 23, 59, 59, 999_999_999),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 6)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 7, 23, 59, 59, 999_999_999),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 6)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 11, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 10, 18, 23, 59, 59, 999_999_999),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 12)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2020, 11, 30, 23, 59, 59, 999_999_999),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 11, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2021, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2022, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2024, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2023, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 12, 31, 23, 59, 59, 999_999_999),
                        DateTimeTestData.newDateTime(2030, 12, 31, 23, 59, 59, 999_999_999),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2021, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSplitIntoIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourPeriods")
    void splitIntoIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourPeriods(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final Period period,
            final OffsetDateTime expectedTo0AndFrom1
    ) {
        final List<Interval> intervals = Interval.of(from, to).splitIntoIntervals(period);

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.getFirst().getFrom());
        Assertions.assertEquals(expectedTo0AndFrom1, intervals.getFirst().getTo());

        Assertions.assertEquals(expectedTo0AndFrom1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSplitIntoIntervals_returnsThreePairs_whenFromAndToDiffersByTwoPeriods() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 7, 12, 20, 30),
                        Periods.DAY,
                        DateTimeTestData.newDateTime(2020, 10, 6),
                        DateTimeTestData.newDateTime(2020, 10, 7)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 8, 12, 20, 30),
                        Periods.TWO_DAYS,
                        DateTimeTestData.newDateTime(2020, 10, 6),
                        DateTimeTestData.newDateTime(2020, 10, 8)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 19, 12, 20, 30),
                        Periods.WEEK,
                        DateTimeTestData.newDateTime(2020, 10, 12),
                        DateTimeTestData.newDateTime(2020, 10, 19)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 12, 19, 12, 20, 30),
                        Periods.MONTH,
                        DateTimeTestData.newDateTime(2020, 11, 1),
                        DateTimeTestData.newDateTime(2020, 12, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2022, 10, 5),
                        Periods.YEAR,
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2022, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5),
                        DateTimeTestData.newDateTime(2023, 10, 5),
                        Periods.TWO_YEARS,
                        DateTimeTestData.newDateTime(2021, 1, 1),
                        DateTimeTestData.newDateTime(2023, 1, 1)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2001, 10, 5),
                        DateTimeTestData.newDateTime(2023, 10, 5),
                        Periods.DECADE,
                        DateTimeTestData.newDateTime(2011, 1, 1),
                        DateTimeTestData.newDateTime(2021, 1, 1)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSplitIntoIntervals_returnsThreePairs_whenFromAndToDiffersByTwoPeriods")
    void splitIntoIntervals_returnsThreePairs_whenFromAndToDiffersByTwoPeriods(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final Period period,
            final OffsetDateTime expectedTo0AndFrom1,
            final OffsetDateTime expectedTo1AndFrom2
    ) {
        final List<Interval> intervals = Interval.of(from, to).splitIntoIntervals(period);

        Assertions.assertEquals(3, intervals.size());

        Assertions.assertEquals(from, intervals.getFirst().getFrom());
        Assertions.assertEquals(expectedTo0AndFrom1, intervals.getFirst().getTo());

        Assertions.assertEquals(expectedTo0AndFrom1, intervals.get(1).getFrom());
        Assertions.assertEquals(expectedTo1AndFrom2, intervals.get(1).getTo());

        Assertions.assertEquals(expectedTo1AndFrom2, intervals.get(2).getFrom());
        Assertions.assertEquals(to, intervals.get(2).getTo());
    }

    // endregion

    // region unite tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forUnite() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 2)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 4),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 3),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 3),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 4),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 4)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 3),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 3)
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 2),
                        DateTimeTestData.newDateTime(2020, 1, 3),
                        DateTimeTestData.newDateTime(2020, 1, 4),
                        DateTimeTestData.newDateTime(2020, 1, 1),
                        DateTimeTestData.newDateTime(2020, 1, 4)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forUnite")
    void unite(
            final OffsetDateTime from1,
            final OffsetDateTime to1,
            final OffsetDateTime from2,
            final OffsetDateTime to2,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) {
        final Interval interval1 = Interval.of(from1, to1);
        final Interval interval2 = Interval.of(from2, to2);
        final Interval expectedResult = Interval.of(expectedFrom, expectedTo);

        final Interval actualResult1 = interval1.unite(interval2);
        final Interval actualResult2 = interval2.unite(interval1);

        Assertions.assertEquals(expectedResult, actualResult1);
        Assertions.assertEquals(expectedResult, actualResult2);
    }

    // endregion

    // region toDuration tests

    @Test
    void toDuration() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 10, 7, 12, 20, 30);

        final Duration duration = Interval.of(from, to).toDuration();

        final Duration expectedDuration = Duration.between(from, to);
        Assertions.assertEquals(expectedDuration, duration);
    }

    // endregion

    // region toPrettyString tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToPrettyString() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        "-∞ — ∞"
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        null,
                        "05.10.2020 10:20:30 — ∞"
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.newDateTime(2020, 10, 7, 12, 20, 30),
                        "-∞ — 07.10.2020 12:20:30"
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 7, 12, 20, 30),
                        "05.10.2020 10:20:30 — 07.10.2020 12:20:30"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToPrettyString")
    void toPrettyString(final OffsetDateTime from, final OffsetDateTime to, String expected) {
        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(expected, interval.toPrettyString());
    }

    // endregion

    // region toDays tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToDays() {
        return Stream.of(
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 15, 10, 20, 30),
                        10
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 6, 10, 20, 30),
                        1
                ),
                Arguments.of(
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.newDateTime(2020, 10, 5, 10, 30, 30),
                        0.006944444444444444
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToDays")
    void toDays(final OffsetDateTime from, final OffsetDateTime to, double expected) {
        final Interval interval = Interval.of(from, to);

        AssertUtils.assertEquals(expected, interval.toDays());
    }

    // endregion

    // region toTradingDays tests

    @Test
    void toTradingDays_tradingIntervalWithinOneDay_fromBeforeTradingSchedule() {
        final int year = 2023;
        final int month = 8;
        final int hour = 7;
        final int durationHours = 12;

        final OffsetDateTime from = DateTimeTestData.newDateTime(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);
        final WorkSchedule workSchedule = TestData.newWorkSchedule(hour, durationHours);

        final List<TradingDay> actualResult = interval.toTradingDays(workSchedule);

        final List<TradingDay> expectedResult = List.of(
                TestData.newTradingDay(true, year, month, 18, hour, durationHours),
                TestData.newTradingDay(false, year, month, 19, hour, durationHours),
                TestData.newTradingDay(false, year, month, 20, hour, durationHours),
                TestData.newTradingDay(true, year, month, 21, hour, durationHours),
                TestData.newTradingDay(true, year, month, 22, hour, durationHours),
                TestData.newTradingDay(true, year, month, 23, hour, durationHours),
                TestData.newTradingDay(true, year, month, 24, hour, durationHours),
                TestData.newTradingDay(true, year, month, 25, hour, durationHours),
                TestData.newTradingDay(false, year, month, 26, hour, durationHours)
        );

        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void toTradingDays_tradingIntervalWithinOneDay_fromAfterTradingSchedule() {
        final int year = 2023;
        final int month = 8;
        final int hour = 7;
        final int durationHours = 12;

        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);
        final WorkSchedule workSchedule = TestData.newWorkSchedule(hour, durationHours);

        final List<TradingDay> actualResult = interval.toTradingDays(workSchedule);

        final List<TradingDay> expectedResult = List.of(
                TestData.newTradingDay(true, year, month, 18, hour, durationHours),
                TestData.newTradingDay(false, year, month, 19, hour, durationHours),
                TestData.newTradingDay(false, year, month, 20, hour, durationHours),
                TestData.newTradingDay(true, year, month, 21, hour, durationHours),
                TestData.newTradingDay(true, year, month, 22, hour, durationHours),
                TestData.newTradingDay(true, year, month, 23, hour, durationHours),
                TestData.newTradingDay(true, year, month, 24, hour, durationHours),
                TestData.newTradingDay(true, year, month, 25, hour, durationHours),
                TestData.newTradingDay(false, year, month, 26, hour, durationHours)
        );

        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void toTradingDays_tradingIntervalWithinTwoDays() {
        final int year = 2023;
        final int month = 8;
        final int hour = 7;
        final int durationHours = 19;

        final OffsetDateTime from = DateTimeTestData.newDateTime(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);
        final WorkSchedule workSchedule = TestData.newWorkSchedule(hour, durationHours);

        final List<TradingDay> actualResult = interval.toTradingDays(workSchedule);

        final List<TradingDay> expectedResult = List.of(
                TestData.newTradingDay(true, year, month, 18, hour, durationHours),
                TestData.newTradingDay(false, year, month, 19, hour, durationHours),
                TestData.newTradingDay(false, year, month, 20, hour, durationHours),
                TestData.newTradingDay(true, year, month, 21, hour, durationHours),
                TestData.newTradingDay(true, year, month, 22, hour, durationHours),
                TestData.newTradingDay(true, year, month, 23, hour, durationHours),
                TestData.newTradingDay(true, year, month, 24, hour, durationHours),
                TestData.newTradingDay(true, year, month, 25, hour, durationHours),
                TestData.newTradingDay(false, year, month, 26, hour, durationHours)
        );

        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region jsonMapping tests

    @Test
    void jsonMapping_mapsValue() throws JsonProcessingException {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 1, 10, 11, 12);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 11, 2, 15, 16, 17);
        final Interval interval = Interval.of(from, to);

        final String json = TestUtils.OBJECT_MAPPER.writeValueAsString(interval);

        final String expectedJson = "{\"from\":\"2022-10-01T10:11:12+03:00\",\"to\":\"2022-11-02T15:16:17+03:00\"}";
        Assertions.assertEquals(expectedJson, json);
    }

    @Test
    void jsonMapping_createsFromValue_throwsValueInstantiationException_whenFromIsAfterTo() {
        final String json = """
                {
                    "from": "2022-11-02T15:16:17+03:00",
                    "to": "2022-10-01T10:11:12+03:00"
                }
                """;

        final Executable executable = () -> TestUtils.OBJECT_MAPPER.readValue(json, Interval.class);
        AssertUtils.assertThrowsWithMessageSubStrings(ValueInstantiationException.class, executable, "from can't be after to");
    }

    @Test
    void jsonMapping_createsFromValue_returnsInterval_whenFromAndToAreNull() throws JsonProcessingException {
        final String json = "{}";

        final Interval interval = TestUtils.OBJECT_MAPPER.readValue(json, Interval.class);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void jsonMapping_createsFromValue_returnsInterval_whenFromIsNull() throws JsonProcessingException {
        final String json = """
                {
                    "to": "2022-11-02T15:16:17+03:00"
                }
                """;

        final Interval interval = TestUtils.OBJECT_MAPPER.readValue(json, Interval.class);

        final OffsetDateTime expectedTo = DateTimeTestData.newDateTime(2022, 11, 2, 15, 16, 17);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
    }

    @Test
    void jsonMapping_createsFromValue_returnsInterval_whenToIsNull() throws JsonProcessingException {
        final String json = """
                {
                    "from": "2022-10-01T10:11:12+03:00"
                }
                """;

        final Interval interval = TestUtils.OBJECT_MAPPER.readValue(json, Interval.class);

        final OffsetDateTime expectedFrom = DateTimeTestData.newDateTime(2022, 10, 1, 10, 11, 12);

        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void jsonMapping_createsFromValue_whenFromAndToAreNotNull() throws JsonProcessingException {
        final String json = """
                {
                    "from": "2022-10-01T10:11:12+03:00",
                    "to": "2022-11-02T15:16:17+03:00"
                }
                """;

        final Interval interval = TestUtils.OBJECT_MAPPER.readValue(json, Interval.class);

        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 1, 10, 11, 12);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 11, 2, 15, 16, 17);
        final Interval expectedInterval = Interval.of(from, to);

        Assertions.assertEquals(expectedInterval, interval);
    }

    // endregion

}