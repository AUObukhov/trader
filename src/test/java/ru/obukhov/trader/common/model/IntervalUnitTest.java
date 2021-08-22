package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

class IntervalUnitTest {

    // region of tests

    @Test
    void of_throwsIllegalArgumentException_whenFromIsAfterTo() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5);

        AssertUtils.assertThrowsWithMessage(
                () -> Interval.of(from, to),
                IllegalArgumentException.class,
                "from can't be after to"
        );
    }

    @Test
    void of_throwsIllegalArgumentException_whenOffsetsAreDifferent() {
        final OffsetDateTime from = OffsetDateTime.of(2020, 10, 5, 0, 0, 0, 0, ZoneOffset.ofHours(1));
        final OffsetDateTime to = OffsetDateTime.of(2020, 10, 10, 0, 0, 0, 0, ZoneOffset.ofHours(2));

        AssertUtils.assertThrowsWithMessage(
                () -> Interval.of(from, to),
                IllegalArgumentException.class,
                "offsets of from and to must be equal"
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
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 10);

        final Interval interval = Interval.of(null, to);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void of_returnsInterval_whenToIsNull() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);

        final Interval interval = Interval.of(from, null);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_returnsInterval_whenFromAndToAreNotNull() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 10);

        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    // endregion

    // region createIntervalOfDay tests

    @Test
    void ofDay_withYearMonthDayOfMonth_returnsProperInterval() {
        final Interval interval = DateTimeTestData.createIntervalOfDay(2020, 10, 10);

        final OffsetDateTime expectedFrom = DateTimeTestData.createDateTime(2020, 10, 10);
        final OffsetDateTime expectedToo = DateUtils.atEndOfDay(expectedFrom);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedToo, interval.getTo());
    }

    @Test
    void ofDay_withDateTime_returnsProperInterval() {
        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2020, 10, 10, 11, 12, 13);
        final Interval interval = DateTimeTestData.createIntervalOfDay(dateTime);

        final OffsetDateTime expectedFrom = DateUtils.atStartOfDay(dateTime);
        final OffsetDateTime expectedToo = DateUtils.atEndOfDay(dateTime);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedToo, interval.getTo());
    }

    // endregion

    // region limitByNowIfNull tests

    @Test
    void limitByNowIfNull_setToToNow_whenToIsNull() {
        final OffsetDateTime now = OffsetDateTime.now();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
        final OffsetDateTime to = null;

        final Interval interval = Interval.of(from, to);
        final Interval newInterval = interval.limitByNowIfNull(now);

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertNotNull(newInterval.getTo());
    }

    @Test
    void limitByNowIfNull_notChangesTo_whenToIsNotNull() {
        final OffsetDateTime now = OffsetDateTime.now();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 10);

        final Interval interval = Interval.of(from, to);
        final Interval newInterval = interval.limitByNowIfNull(now);

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertEquals(interval.getTo(), newInterval.getTo());
    }

    // endregion

    // region extendToDay tests

    @Test
    void extendToDay_throwsIllegalArgumentException_whenNotEqualDates() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 6, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        AssertUtils.assertThrowsWithMessage(
                interval::extendToDay,
                IllegalArgumentException.class,
                "'from' and 'to' must be at same day"
        );
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_throwsIllegalArgumentException_whenFromIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = mockedNow.plusHours(1);
        final OffsetDateTime to = from.plusMinutes(10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            AssertUtils.assertThrowsWithMessage(
                    interval::extendToDay,
                    IllegalArgumentException.class,
                    "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00"
            );
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_throwsIllegalArgumentException_whenToIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = mockedNow.minusMinutes(10);
        final OffsetDateTime to = mockedNow.plusMinutes(10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            AssertUtils.assertThrowsWithMessage(
                    interval::extendToDay,
                    IllegalArgumentException.class,
                    "'to' (2020-09-23T10:21:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00"
            );
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_extendsToWholeDay_whenEqualsDatesBeforeToday() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2021, 9, 23, 10, 11, 12);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToDay();

            final OffsetDateTime expectedFrom = DateTimeTestData.createDateTime(2020, 10, 5);
            final OffsetDateTime expectedTo = DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(expectedTo, extendedInterval.getTo());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_extendsToPartOfDayTillNow_whenDatesAreToday() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 9, 23, 3, 11, 12);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 9, 23, 6, 11, 12);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToDay();

            final OffsetDateTime expectedFrom = DateTimeTestData.createDateTime(2020, 9, 23);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(mockedNow, extendedInterval.getTo());
        }
    }

    // endregion

    // region extendToYear tests

    @Test
    void extendToYear_throwsIllegalArgumentException_whenNotEqualYears() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 10, 6, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        AssertUtils.assertThrowsWithMessage(
                interval::extendToYear,
                IllegalArgumentException.class,
                "'from' and 'to' must be at same year"
        );
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_throwsIllegalArgumentException_whenFromIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = mockedNow.plusHours(1);
        final OffsetDateTime to = from.plusMinutes(10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            AssertUtils.assertThrowsWithMessage(
                    interval::extendToYear,
                    IllegalArgumentException.class,
                    "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00"
            );
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_throwsIllegalArgumentException_whenToIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = mockedNow.minusHours(1);
        final OffsetDateTime to = mockedNow.plusMinutes(10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            AssertUtils.assertThrowsWithMessage(
                    interval::extendToYear,
                    IllegalArgumentException.class,
                    "'to' (2020-09-23T10:21:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00"
            );
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_extendsToWholeYear_whenEqualsYearsBeforeCurrentYear() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2021, 9, 23, 10, 11, 12);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToYear();

            final OffsetDateTime expectedFrom = DateTimeTestData.createDateTime(2020, 1, 1);
            final OffsetDateTime expectedTo = DateTimeTestData.createDateTime(2020, 12, 31, 23, 59, 59, 999999999);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(expectedTo, extendedInterval.getTo());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_extendsToPartOfDayTillNow_whenYearsAreCurrentYear() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 1, 23, 10, 11, 12);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 4, 23, 10, 11, 12);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToYear();

            final OffsetDateTime expectedFrom = DateTimeTestData.createDateTime(2020, 1, 1);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(mockedNow, extendedInterval.getTo());
        }
    }

    // endregion

    // region hashCode tests

    @Test
    void hashCode_returnsEqualValuesForEqualIntervals() {
        final Interval interval1 = Interval.of(
                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
        );
        final Interval interval2 = Interval.of(
                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
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
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 41),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        false
                ),

                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2019, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2019, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                DateTimeTestData.createDateTime(2021, 10, 5, 10, 20, 30, 40),
                                DateTimeTestData.createDateTime(2021, 10, 5, 11, 30, 40, 50)
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
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5);
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusDays(1));
    }

    @Test
    void minusDays_throwNullPointerException_whenToIsNull() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
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
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -2,
                        DateTimeTestData.createDateTime(2020, 10, 7, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 7, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -1,
                        DateTimeTestData.createDateTime(2020, 10, 6, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 6, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        0,
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        1,
                        DateTimeTestData.createDateTime(2020, 10, 4, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 4, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        2,
                        DateTimeTestData.createDateTime(2020, 10, 3, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 3, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        10,
                        DateTimeTestData.createDateTime(2020, 9, 25, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 9, 25, 11, 30, 40, 50)
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
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5);
        final Interval interval = Interval.of(from, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusYears(1));
    }

    @Test
    void minusYears_throwNullPointerException_whenToIsNull() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
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
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -2,
                        DateTimeTestData.createDateTime(2022, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2022, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        -1,
                        DateTimeTestData.createDateTime(2021, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2021, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        0,
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        1,
                        DateTimeTestData.createDateTime(2019, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2019, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        2,
                        DateTimeTestData.createDateTime(2018, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2018, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        10,
                        DateTimeTestData.createDateTime(2010, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2010, 10, 5, 11, 30, 40, 50)
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

    // region withDefaultOffsetSameInstant tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forWithDefaultOffsetSameInstant() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(
                        null,
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                ),
                Arguments.of(
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        null
                ),
                Arguments.of(
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        OffsetDateTime.of(2020, 10, 5, 10, 0, 0, 0, ZoneOffset.UTC),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forWithDefaultOffsetSameInstant")
    void withDefaultOffsetSameInstant(OffsetDateTime from, OffsetDateTime to, OffsetDateTime expectedFrom, OffsetDateTime expectedTo) {
        final Interval result = Interval.of(from, to).withDefaultOffsetSameInstant();

        Assertions.assertEquals(expectedFrom, result.getFrom());
        Assertions.assertEquals(expectedTo, result.getTo());
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
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5),
                        DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999),
                        true
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        null,
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5).minusNanos(1),
                        DateTimeTestData.createDateTime(2020, 10, 5),
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

    // region equalYears tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forEqualYears() {
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        true
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30, 40),
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        true
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        DateTimeTestData.createDateTime(2020, 12, 31, 23, 59, 59, 999999999),
                        true
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 11, 30, 40, 50),
                        null,
                        false
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 1, 1).minusNanos(1),
                        DateTimeTestData.createDateTime(2020, 1, 1),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEqualYears")
    void equalYears(OffsetDateTime from, OffsetDateTime to, boolean expected) {
        final boolean result = Interval.of(from, to).equalYears();

        Assertions.assertEquals(expected, result);
    }

    // endregion


    // region contains tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forContains() {
        return Stream.of(
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 5),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.createDateTime(2020, 10, 1, 12, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 10),
                                DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 15),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                null,
                                DateTimeTestData.createDateTime(2020, 10, 2, 10, 5, 10)
                        ),
                        DateTimeTestData.createDateTime(2020, 10, 1, 12, 5, 10),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                DateTimeTestData.createDateTime(2020, 10, 1, 10, 5, 10),
                                null
                        ),
                        DateTimeTestData.createDateTime(2020, 10, 1, 12, 5, 10),
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

    // region splitIntoDailyIntervals tests

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToAreEqual() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneDay() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneWholeDay() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToDiffersInOneDay() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 6, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final OffsetDateTime expectedRight0 = DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateTimeTestData.createDateTime(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtStartOfNeighbourDays() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 6);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final OffsetDateTime expectedRight0 = DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateTimeTestData.createDateTime(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourDays() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 6, 23, 59, 59, 999999999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(from, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateTimeTestData.createDateTime(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsThreePairs_whenFromAndToDiffersInTwoDay() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 7, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(3, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final OffsetDateTime expectedRight0 = DateTimeTestData.createDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final OffsetDateTime expectedLeft1 = DateTimeTestData.createDateTime(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        final OffsetDateTime expectedRight1 = DateTimeTestData.createDateTime(2020, 10, 6, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight1, intervals.get(1).getTo());

        final OffsetDateTime expectedLeft2 = DateTimeTestData.createDateTime(2020, 10, 7);
        Assertions.assertEquals(expectedLeft2, intervals.get(2).getFrom());
        Assertions.assertEquals(to, intervals.get(2).getTo());
    }

    // endregion

    // region toDuration tests

    @Test
    void toDuration_returnsProperDuration() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 7, 12, 20, 30);

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
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30),
                        null,
                        "05.10.2020 10:20:30 — ∞"
                ),
                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 7, 12, 20, 30),
                        "-∞ — 07.10.2020 12:20:30"
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.createDateTime(2020, 10, 7, 12, 20, 30),
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
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.createDateTime(2020, 10, 15, 10, 20, 30),
                        10
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.createDateTime(2020, 10, 6, 10, 20, 30),
                        1
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30),
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, 30, 30),
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