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
        OffsetDateTime from = DateUtils.getDate(2020, 10, 10);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 5);

        AssertUtils.assertThrowsWithMessage(
                () -> Interval.of(from, to),
                IllegalArgumentException.class,
                "from can't be after to"
        );
    }

    @Test
    void of_returnsInterval_whenFromAndToAreNull() {
        Interval interval = Interval.of(null, null);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_returnsInterval_whenFromIsNull() {
        OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        Interval interval = Interval.of(null, to);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void of_returnsInterval_whenToIsNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);

        Interval interval = Interval.of(from, null);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_returnsInterval_whenFromAndToAreNotNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        Interval interval = Interval.of(from, to);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    // endregion

    // region ofDay tests

    @Test
    void ofDay_withYearMonthDayOfMonth_returnsProperInterval() {
        Interval interval = Interval.ofDay(2020, 10, 10);

        OffsetDateTime expectedFrom = DateUtils.getDate(2020, 10, 10);
        OffsetDateTime expectedToo = DateUtils.atEndOfDay(expectedFrom);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedToo, interval.getTo());
    }

    @Test
    void ofDay_withDateTime_returnsProperInterval() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 11, 12, 13);

        Interval interval = Interval.ofDay(dateTime);

        OffsetDateTime expectedFrom = DateUtils.atStartOfDay(dateTime);
        OffsetDateTime expectedToo = DateUtils.atEndOfDay(dateTime);
        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedToo, interval.getTo());
    }

    // endregion

    // region limitByNowIfNull tests

    @Test
    void limitByNowIfNull_setToToNow_whenToIsNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = null;

        Interval interval = Interval.of(from, to);
        Interval newInterval = interval.limitByNowIfNull();

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertNotNull(newInterval.getTo());
    }

    @Test
    void limitByNowIfNull_notChangesTo_whenToIsNotNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        Interval interval = Interval.of(from, to);
        Interval newInterval = interval.limitByNowIfNull();

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertEquals(interval.getTo(), newInterval.getTo());
    }

    // endregion

    // region extendToWholeDay tests

    @Test
    void extendToWholeDay_throwsIllegalArgumentException_whenNotEqualsDates() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 11, 30, 40);
        Interval interval = Interval.of(from, to);

        AssertUtils.assertThrowsWithMessage(
                () -> interval.extendToWholeDay(false),
                IllegalArgumentException.class,
                "'from' and 'to' must be at same day"
        );
    }

    @Test
    void extendToWholeDay_throwsIllegalArgumentException_whenNotFutureIsTrueAndFromIsInFuture() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 10, 11, 12);

        OffsetDateTime from = mockedNow.plusHours(1);
        OffsetDateTime to = from.plusMinutes(10);
        Interval interval = Interval.of(from, to);

        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            String expectedMessage =
                    "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(
                    () -> interval.extendToWholeDay(true),
                    IllegalArgumentException.class,
                    expectedMessage
            );
        }
    }

    @Test
    void extendToWholeDay_extendsToWholeDay_whenEqualsDates_andNotFuture() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 11, 30, 40);
        Interval interval = Interval.of(from, to);

        Interval extendedInterval = interval.extendToWholeDay(false);

        OffsetDateTime expectedFrom = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime expectedTo = DateUtils.atEndOfDay(expectedFrom);
        Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
        Assertions.assertEquals(expectedTo, extendedInterval.getTo());
    }

    @Test
    void extendToWholeDay_extendsToWholeDay_whenEqualsDates_andFuture() {
        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = from.plusHours(1);
        Interval interval = Interval.of(from, to);

        Interval extendedInterval = interval.extendToWholeDay(true);

        Assertions.assertEquals(DateUtils.atStartOfDay(from), extendedInterval.getFrom());
        Assertions.assertTrue(to.isAfter(extendedInterval.getTo()));
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
        boolean result = Interval.of(from, to).equalDates();

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
        OffsetDateTime from = DateUtils.getDate(2020, 10, 10);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 11);

        OffsetDateTime defaultFrom = DateUtils.getDate(2020, 11, 10);
        OffsetDateTime defaultTo = DateUtils.getDate(2020, 11, 11);

        Interval interval = Interval.of(from, to).getDefault(defaultFrom, defaultTo);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void getDefault_returnsDefaultValues_whenValuesAreNull() {
        OffsetDateTime from = null;
        OffsetDateTime to = null;

        OffsetDateTime defaultFrom = DateUtils.getDate(2020, 11, 10);
        OffsetDateTime defaultTo = DateUtils.getDate(2020, 11, 11);

        Interval interval = Interval.of(from, to).getDefault(defaultFrom, defaultTo);

        Assertions.assertEquals(defaultFrom, interval.getFrom());
        Assertions.assertEquals(defaultTo, interval.getTo());
    }

    // endregion

    // region splitIntoDailyIntervals tests

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToAreEqual() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneDay() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 12, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneWholeDay() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToDiffersInOneDay() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 12, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtStartOfNeighbourDays() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 6);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourDays() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(from, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsThreePairs_whenFromAndToDiffersInTwoDay() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(3, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        OffsetDateTime expectedRight1 = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);
        Assertions.assertEquals(expectedRight1, intervals.get(1).getTo());

        OffsetDateTime expectedLeft2 = DateUtils.getDate(2020, 10, 7);
        Assertions.assertEquals(expectedLeft2, intervals.get(2).getFrom());
        Assertions.assertEquals(to, intervals.get(2).getTo());
    }

    // endregion

    // region toDuration tests

    @Test
    void toDuration_returnsProperDuration() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        Duration duration = Interval.of(from, to).toDuration();

        Duration expectedDuration = Duration.between(from, to);
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
                        "2020.10.05 10:20:30 — ∞"
                ),
                Arguments.of(
                        null,
                        DateUtils.getDateTime(2020, 10, 7, 12, 20, 30),
                        "-∞ — 2020.10.07 12:20:30"
                ),
                Arguments.of(
                        DateUtils.getDateTime(2020, 10, 5, 10, 20, 30),
                        DateUtils.getDateTime(2020, 10, 7, 12, 20, 30),
                        "2020.10.05 10:20:30 — 2020.10.07 12:20:30"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToPrettyString")
    void toPrettyString(OffsetDateTime from, OffsetDateTime to, String expected) {
        Interval interval = Interval.of(from, to);

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
        Interval interval = Interval.of(from, to);

        AssertUtils.assertEquals(expected, interval.toDays());
    }

    // endregion

}