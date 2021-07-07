package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

class IntervalUnitTest {

    // region of tests

    @Test
    void of_throwsIllegalArgumentException_whenFromIsAfterTo() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 10);
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 5);

        AssertUtils.assertThrowsWithMessage(
                () -> Interval.of(from, to),
                IllegalArgumentException.class,
                "from can't be after to"
        );
    }

    @Test
    void of_returnsInterval_whenFromAndToAreNull() {
        final Interval interval = Interval.of(null, null);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_returnsInterval_whenFromIsNull() {
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        final Interval interval = Interval.of(null, to);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void of_returnsInterval_whenToIsNull() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);

        final Interval interval = Interval.of(from, null);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_returnsInterval_whenFromAndToAreNotNull() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    // endregion

    // region ofDay tests

    @Test
    void ofDay_withYearMonthDayOfMonth_returnsProperInterval() {
        final Interval interval = Interval.ofDay(2020, 10, 10);

        final OffsetDateTime expectedFrom = DateUtils.getDate(2020, 10, 10);
        final OffsetDateTime expectedToo = DateUtils.atEndOfDay(expectedFrom);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedToo, interval.getTo());
    }

    @Test
    void ofDay_withDateTime_returnsProperInterval() {
        final OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 11, 12, 13);
        final Interval interval = Interval.ofDay(dateTime);

        final OffsetDateTime expectedFrom = DateUtils.atStartOfDay(dateTime);
        final OffsetDateTime expectedToo = DateUtils.atEndOfDay(dateTime);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedToo, interval.getTo());
    }

    // endregion

    // region limitByNowIfNull tests

    @Test
    void limitByNowIfNull_setToToNow_whenToIsNull() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        final OffsetDateTime to = null;

        final Interval interval = Interval.of(from, to);
        final Interval newInterval = interval.limitByNowIfNull();

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertNotNull(newInterval.getTo());
    }

    @Test
    void limitByNowIfNull_notChangesTo_whenToIsNotNull() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        final Interval interval = Interval.of(from, to);
        final Interval newInterval = interval.limitByNowIfNull();

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertEquals(interval.getTo(), newInterval.getTo());
    }

    // endregion

    // region extendToWholeDay tests

    @Test
    void extendToWholeDay_throwsIllegalArgumentException_whenNotEqualDates() {
        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        AssertUtils.assertThrowsWithMessage(
                () -> interval.extendToWholeDay(false),
                IllegalArgumentException.class,
                "'from' and 'to' must be at same day"
        );
    }

    @Test
    @SuppressWarnings("unused")
    void extendToWholeDay_throwsIllegalArgumentException_whenAllowFutureIsFalseAndFromIsInFuture() {
        final OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = mockedNow.plusHours(1);
        final OffsetDateTime to = from.plusMinutes(10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final String expectedMessage =
                    "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(
                    () -> interval.extendToWholeDay(false),
                    IllegalArgumentException.class,
                    expectedMessage
            );
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToWholeDay_extendsToWholeDay_whenEqualsDatesBeforeToday_andAllowFutureIsFalse() {
        final OffsetDateTime mockedNow = DateUtils.getDateTime(2021, 9, 23, 10, 11, 12);

        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToWholeDay(false);

            final OffsetDateTime expectedFrom = DateUtils.getDate(2020, 10, 5);
            final OffsetDateTime expectedTo = DateUtils.atEndOfDay(expectedFrom);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(expectedTo, extendedInterval.getTo());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToWholeDay_extendsToWholeDay_whenEqualsDatesBeforeToday_andAllowFutureIsTrue() {
        final OffsetDateTime mockedNow = DateUtils.getDateTime(2021, 9, 23, 10, 11, 12);

        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToWholeDay(true);

            final OffsetDateTime expectedFrom = DateUtils.getDate(2020, 10, 5);
            final OffsetDateTime expectedTo = DateUtils.atEndOfDay(expectedFrom);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(expectedTo, extendedInterval.getTo());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToWholeDay_extendsToPartOfDayTillNow_whenDatesAreToday_andAllowFutureIsFalse() {
        final OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime to = mockedNow.plusHours(1);
        final Interval interval = Interval.of(mockedNow, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToWholeDay(false);

            Assertions.assertEquals(DateUtils.atStartOfDay(mockedNow), extendedInterval.getFrom());
            Assertions.assertEquals(mockedNow, extendedInterval.getTo());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToWholeDay_extendsToWholeDay_whenDatesAreToday_andAllowFutureIsTrue() {
        final OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime to = mockedNow.plusHours(1);
        final Interval interval = Interval.of(mockedNow, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToWholeDay(true);

            Assertions.assertEquals(DateUtils.atStartOfDay(mockedNow), extendedInterval.getFrom());
            Assertions.assertEquals(DateUtils.atEndOfDay(mockedNow), extendedInterval.getTo());
        }
    }

    // endregion

    // region hashCode tests

    @Test
    void hashCode_returnsEqualValuesForEqualIntervals() {
        final Interval interval1 = Interval.of(
                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
        );
        final Interval interval2 = Interval.of(
                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
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
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        false
                ),

                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2019, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateUtils.getDateTime(2021, 10, 5, 10, 20, 30, 40),
                                DateUtils.getDateTime(2021, 10, 5, 11, 30, 40, 50)
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

    // region minusDays tests

    @Test
    void minusDays_throwNullPointerException_whenFromIsNull() {
        final OffsetDateTime from = null;
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 5);
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusDays(1));
    }

    @Test
    void minusDays_throwNullPointerException_whenToIsNull() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        final OffsetDateTime to = null;
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusDays(1));
    }

    @Test
    void minusDays_throwNullPointerException_whenFromAndToAreNull() {
        final OffsetDateTime from = null;
        final OffsetDateTime to = null;
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusDays(1));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forMinusDays() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -2,
                        DateUtils.getDateTime(2020, 10, 7, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 7, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -1,
                        DateUtils.getDateTime(2020, 10, 6, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 6, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        0,
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        1,
                        DateUtils.getDateTime(2020, 10, 4, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 4, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        2,
                        DateUtils.getDateTime(2020, 10, 3, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 3, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        10,
                        DateUtils.getDateTime(2020, 9, 25, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 9, 25, 11, 30, 40, 50)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forMinusDays")
    void minusDays(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final long days,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) {
        final Interval interval = Interval.of(from, to).minusDays(days);

        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
    }

    // endregion

    // region minusYears tests

    @Test
    void minusYears_throwNullPointerException_whenFromIsNull() {
        final OffsetDateTime from = null;
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 5);
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusYears(1));
    }

    @Test
    void minusYears_throwNullPointerException_whenToIsNull() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        final OffsetDateTime to = null;
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusYears(1));
    }

    @Test
    void minusYears_throwNullPointerException_whenFromAndToAreNull() {
        final OffsetDateTime from = null;
        final OffsetDateTime to = null;
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusYears(1));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forMinusYears() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -2,
                        DateUtils.getDateTime(2022, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2022, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -1,
                        DateUtils.getDateTime(2021, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2021, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        0,
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        1,
                        DateUtils.getDateTime(2019, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2019, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        2,
                        DateUtils.getDateTime(2018, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2018, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        10,
                        DateUtils.getDateTime(2010, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2010, 10, 5, 11, 30, 40, 50)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forMinusYears")
    void minusYears(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final long years,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) {
        final Interval interval = Interval.of(from, to).minusYears(years);

        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
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
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        true
                ),
                Arguments.of(
                        DateUtils.getDate(2020, 10, 5),
                        DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999),
                        true
                ),
                Arguments.of(
                        null,
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        false
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50),
                        null,
                        false
                ),
                Arguments.of(
                        DateUtils.getDate(2020, 10, 5).minusNanos(1),
                        DateUtils.getDate(2020, 10, 5),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEqualDates")
    void equalDates(OffsetDateTime from, OffsetDateTime to, boolean expected) {
        final boolean result = Interval.of(from, to).equalDates();

        Assertions.assertEquals(expected, result);
    }

    // endregion

    // region contains tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forContains() {
        return Stream.of(
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 1, 10, 5, 10),
                                DateUtils.getDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateUtils.getDateTime(2020, 10, 1, 10, 5, 5),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 1, 10, 5, 10),
                                DateUtils.getDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateUtils.getDateTime(2020, 10, 1, 10, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 1, 10, 5, 10),
                                DateUtils.getDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateUtils.getDateTime(2020, 10, 1, 12, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 1, 10, 5, 10),
                                DateUtils.getDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateUtils.getDateTime(2020, 10, 2, 10, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 1, 10, 5, 10),
                                DateUtils.getDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateUtils.getDateTime(2020, 10, 2, 10, 5, 15),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                null,
                                DateUtils.getDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateUtils.getDateTime(2020, 10, 1, 12, 5, 10),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateUtils.getDateTime(2020, 10, 1, 10, 5, 10),
                                null
                        ),
                        DateUtils.getDateTime(2020, 10, 1, 12, 5, 10),
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

    // region getDefault tests

    @Test
    void getDefault_returnsSameValues_whenValuesAreNotNull() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 10);
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 11);

        final OffsetDateTime defaultFrom = DateUtils.getDate(2020, 11, 10);
        final OffsetDateTime defaultTo = DateUtils.getDate(2020, 11, 11);

        final Interval interval = Interval.of(from, to).getDefault(defaultFrom, defaultTo);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void getDefault_returnsDefaultValues_whenValuesAreNull() {
        final OffsetDateTime from = null;
        final OffsetDateTime to = null;

        final OffsetDateTime defaultFrom = DateUtils.getDate(2020, 11, 10);
        final OffsetDateTime defaultTo = DateUtils.getDate(2020, 11, 11);

        final Interval interval = Interval.of(from, to).getDefault(defaultFrom, defaultTo);

        Assertions.assertEquals(defaultFrom, interval.getFrom());
        Assertions.assertEquals(defaultTo, interval.getTo());
    }

    // endregion

    // region splitIntoDailyIntervals tests

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToAreEqual() {
        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneDay() {
        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneWholeDay() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToDiffersInOneDay() {
        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtStartOfNeighbourDays() {
        final OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        final OffsetDateTime to = DateUtils.getDate(2020, 10, 6);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourDays() {
        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(from, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsThreePairs_whenFromAndToDiffersInTwoDay() {
        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(3, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        final OffsetDateTime expectedRight1 = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight1, intervals.get(1).getTo());

        final OffsetDateTime expectedLeft2 = DateUtils.getDate(2020, 10, 7);
        Assertions.assertEquals(expectedLeft2, intervals.get(2).getFrom());
        Assertions.assertEquals(to, intervals.get(2).getTo());
    }

    // endregion

    // region toDuration tests

    @Test
    void toDuration_returnsProperDuration() {
        final OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

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
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30),
                        null,
                        "05.10.2020 10:20:30 — ∞"
                ),
                Arguments.of(
                        null,
                        DateUtils.getDateTime(2020, 10, 7, 12, 20, 30),
                        "-∞ — 07.10.2020 12:20:30"
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30),
                        DateUtils.getDateTime(2020, 10, 7, 12, 20, 30),
                        "05.10.2020 10:20:30 — 07.10.2020 12:20:30"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToPrettyString")
    void toPrettyString(OffsetDateTime from, OffsetDateTime to, String expected) {
        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(expected, interval.toPrettyString());
    }

    // endregion

    // region toDays tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToDays() {
        return Stream.of(
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30),
                        DateUtils.getDateTime(2020, 10, 15, 10, 20, 30),
                        10
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30),
                        DateUtils.getDateTime(2020, 10, 6, 10, 20, 30),
                        1
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30),
                        DateUtils.getDateTime(2020, 10, 5, 10, 30, 30),
                        0.006944444444444444
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToDays")
    void toDays(OffsetDateTime from, OffsetDateTime to, double expected) {
        final Interval interval = Interval.of(from, to);

        AssertUtils.assertEquals(expected, interval.toDays());
    }

    // endregion

}