package ru.obukhov.trader.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

class IntervalUnitTest {

    // region of with DateTimes tests

    @Test
    void of_withDateTimes_throwsIllegalArgumentException_whenFromIsAfterTo() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 5);

        final Executable executable = () -> Interval.of(from, to);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "from can't be after to");
    }

    @Test
    void of_withDateTimes_throwsIllegalArgumentException_whenOffsetsAreDifferent() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5, ZoneOffset.ofHours(1));
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 10, ZoneOffset.ofHours(2));

        final Executable executable = () -> Interval.of(from, to);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "offsets of from and to must be equal");
    }

    @Test
    void of_withDateTimes_returnsInterval_whenFromAndToAreNull() {
        final Interval interval = Interval.of((OffsetDateTime) null, null);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_withDateTimes_returnsInterval_whenFromIsNull() {
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 10);

        final Interval interval = Interval.of(null, to);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(TimestampUtils.newTimestamp(to), interval.getTo());
    }

    @Test
    void of_withDateTimes_returnsInterval_whenToIsNull() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);

        final Interval interval = Interval.of(from, null);

        Assertions.assertEquals(TimestampUtils.newTimestamp(from), interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_withDateTimes_returnsInterval_whenFromAndToAreNotNull() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 10, 5);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 10, 10);

        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(TimestampUtils.newTimestamp(from), interval.getFrom());
        Assertions.assertEquals(TimestampUtils.newTimestamp(to), interval.getTo());
    }

    // endregion

    // region of with Timestamps tests

    @Test
    void of_withTimestamps_throwsIllegalArgumentException_whenFromIsAfterTo() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 10);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5);

        final Executable executable = () -> Interval.of(from, to);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "from can't be after to");
    }

    @Test
    void of_withTimestamps_returnsInterval_whenFromAndToAreNull() {
        final Interval interval = Interval.of((Timestamp) null, null);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_withTimestamps_returnsInterval_whenFromIsNull() {
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 10);

        final Interval interval = Interval.of(null, to);

        Assertions.assertNull(interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    @Test
    void of_withTimestamps_withTimestamps_returnsInterval_whenToIsNull() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);

        final Interval interval = Interval.of(from, null);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertNull(interval.getTo());
    }

    @Test
    void of_withTimestamps_returnsInterval_whenFromAndToAreNotNull() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 10);

        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(from, interval.getFrom());
        Assertions.assertEquals(to, interval.getTo());
    }

    // endregion

    // region limitByNowIfNull tests

    @Test
    void limitByNowIfNull_setToToNow_whenToIsNull() {
        final Timestamp now = TimestampUtils.now();

        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);

        final Interval interval = Interval.of(from, null);
        final Interval newInterval = interval.limitByNowIfNull(now);

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertNotNull(newInterval.getTo());
    }

    @Test
    void limitByNowIfNull_notChangesTo_whenToIsNotNull() {
        final Timestamp now = TimestampUtils.now();

        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 10);

        final Interval interval = Interval.of(from, to);
        final Interval newInterval = interval.limitByNowIfNull(now);

        Assertions.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assertions.assertEquals(interval.getTo(), newInterval.getTo());
    }

    // endregion

    // region extendToDay tests

    @Test
    void extendToDay_throwsIllegalArgumentException_whenNotEqualDates() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 6, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        final Executable executable = interval::extendToDay;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "'from' and 'to' must be at same day");
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_throwsIllegalArgumentException_whenFromIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.plusHours(now, 1);
        final Timestamp to = TimestampUtils.plusMinutes(from, 10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Executable executable = interval::extendToDay;
            final String expectedMessage = "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_throwsIllegalArgumentException_whenToIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.plusMinutes(now, -10);
        final Timestamp to = TimestampUtils.plusMinutes(now, 10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> timestampStaticMock = Mocker.mockNow(mockedNow)) {
            final Executable executable = interval::extendToDay;
            final String expectedMessage = "'to' (2020-09-23T10:21:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_extendsToWholeDay_whenEqualsDatesBeforeToday() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2021, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToDay();

            final Timestamp expectedFrom = TimestampUtils.newTimestamp(2020, 10, 5);
            final Timestamp expectedTo = TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(expectedTo, extendedInterval.getTo());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToDay_extendsToPartOfDayTillNow_whenDatesAreToday() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.newTimestamp(2020, 9, 23, 3, 11, 12);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 9, 23, 6, 11, 12);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToDay();

            final Timestamp expectedFrom = TimestampUtils.newTimestamp(2020, 9, 23);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(now, extendedInterval.getTo());
        }
    }

    // endregion

    // region extendToYear tests

    @Test
    void extendToYear_throwsIllegalArgumentException_whenNotEqualYears() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 10, 6, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        final Executable executable = interval::extendToYear;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "'from' and 'to' must be at same year");
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_throwsIllegalArgumentException_whenFromIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.plusHours(now, 1);
        final Timestamp to = TimestampUtils.plusMinutes(from, 10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Executable executable = interval::extendToYear;
            final String expectedMessage = "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_throwsIllegalArgumentException_whenToIsInFuture() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.plusHours(now, -1);
        final Timestamp to = TimestampUtils.plusMinutes(now, 10);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Executable executable = interval::extendToYear;
            final String expectedMessage = "'to' (2020-09-23T10:21:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
            AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_extendsToWholeYear_whenEqualsYearsBeforeCurrentYear() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2021, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToYear();

            final Timestamp expectedFrom = TimestampUtils.newTimestamp(2020, 1, 1);
            final Timestamp expectedTo = TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(expectedTo, extendedInterval.getTo());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void extendToYear_extendsToPartOfDayTillNow_whenYearsAreCurrentYear() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10, 11, 12);
        final Timestamp now = TimestampUtils.newTimestamp(mockedNow);

        final Timestamp from = TimestampUtils.newTimestamp(2020, 1, 23, 10, 11, 12);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 4, 23, 10, 11, 12);
        final Interval interval = Interval.of(from, to);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final Interval extendedInterval = interval.extendToYear();

            final Timestamp expectedFrom = TimestampUtils.newTimestamp(2020, 1, 1);
            Assertions.assertEquals(expectedFrom, extendedInterval.getFrom());
            Assertions.assertEquals(now, extendedInterval.getTo());
        }
    }

    // endregion

    // region hashCode tests

    @Test
    void hashCode_returnsEqualValuesForEqualIntervals() {
        final Interval interval1 = Interval.of(
                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
        );
        final Interval interval2 = Interval.of(
                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
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
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 41),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 41),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 41),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 41),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 41),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 51)
                        ),
                        false
                ),

                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2019, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2019, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2021, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2019, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2019, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                        ),
                        Interval.of(
                                TimestampUtils.newTimestamp(2021, 10, 5, 10, 20, 30, 40),
                                TimestampUtils.newTimestamp(2021, 10, 5, 11, 30, 40, 50)
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
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5);
        final Interval interval = Interval.of(null, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusDays(1));
    }

    @Test
    void minusDays_throwNullPointerException_whenToIsNull() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Interval interval = Interval.of(from, null);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusDays(1));
    }

    @Test
    void minusDays_throwNullPointerException_whenFromAndToAreNull() {
        final Interval interval = Interval.of((Timestamp) null, null);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusDays(1));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forMinusDays() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        -2,
                        TimestampUtils.newTimestamp(2020, 10, 7, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 7, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        -1,
                        TimestampUtils.newTimestamp(2020, 10, 6, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 6, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        0,
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        1,
                        TimestampUtils.newTimestamp(2020, 10, 4, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 4, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        2,
                        TimestampUtils.newTimestamp(2020, 10, 3, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 3, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        10,
                        TimestampUtils.newTimestamp(2020, 9, 25, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 9, 25, 11, 30, 40, 50)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forMinusDays")
    void minusDays(
            final Timestamp from,
            final Timestamp to,
            final int days,
            final Timestamp expectedFrom,
            final Timestamp expectedTo
    ) {
        final Interval interval = Interval.of(from, to).minusDays(days);

        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
    }

    // endregion

    // region minusYears tests

    @Test
    void minusYears_throwNullPointerException_whenFromIsNull() {
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5);
        final Interval interval = Interval.of(null, to);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusYears(1));
    }

    @Test
    void minusYears_throwNullPointerException_whenToIsNull() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Interval interval = Interval.of(from, null);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusYears(1));
    }

    @Test
    void minusYears_throwNullPointerException_whenFromAndToAreNull() {
        final Interval interval = Interval.of((Timestamp) null, null);

        Assertions.assertThrows(NullPointerException.class, () -> interval.minusYears(1));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forMinusYears() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        -2,
                        TimestampUtils.newTimestamp(2022, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2022, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        -1,
                        TimestampUtils.newTimestamp(2021, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2021, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        0,
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        1,
                        TimestampUtils.newTimestamp(2019, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2019, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        2,
                        TimestampUtils.newTimestamp(2018, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2018, 10, 5, 11, 30, 40, 50)
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        10,
                        TimestampUtils.newTimestamp(2010, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2010, 10, 5, 11, 30, 40, 50)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forMinusYears")
    void minusYears(
            final Timestamp from,
            final Timestamp to,
            final int years,
            final Timestamp expectedFrom,
            final Timestamp expectedTo
    ) {
        final Interval interval = Interval.of(from, to).minusYears(years);

        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
    }

    // endregion

    // region withDefaultOffsetSameInstant tests

//    @SuppressWarnings("unused")
//    static Stream<Arguments> getData_forWithDefaultOffsetSameInstant() {
//        return Stream.of(
//                Arguments.of(null, null, null, null),
//                Arguments.of(
//                        null,
//                        TimestampUtils.newTimestamp(2020, 10, 5, 10, ZoneOffset.UTC),
//                        null,
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13)
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2020, 10, 5, 10, ZoneOffset.UTC),
//                        null,
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13),
//                        null
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2020, 10, 5, 10, ZoneOffset.UTC),
//                        TimestampUtils.newTimestamp(2020, 10, 5, 10, ZoneOffset.UTC),
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13),
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13)
//                ),
//                Arguments.of(
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13),
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13),
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13),
//                        TimestampUtils.newTimestamp(2020, 10, 5, 13)
//                )
//        );
//    }
//
//    @ParameterizedTest
//    @MethodSource("getData_forWithDefaultOffsetSameInstant")
//    void withDefaultOffsetSameInstant(Timestamp from, Timestamp to, Timestamp expectedFrom, Timestamp expectedTo) {
//        final Interval result = Interval.of(from, to).withDefaultOffsetSameInstant();
//
//        Assertions.assertEquals(expectedFrom, result.getFrom());
//        Assertions.assertEquals(expectedTo, result.getTo());
//    }

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
                        TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999),
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
    void equalDates(Timestamp from, Timestamp to, boolean expected) {
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
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30, 40),
                        TimestampUtils.newTimestamp(2020, 10, 5, 11, 30, 40, 50),
                        true
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 1, 1),
                        TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999),
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
                        TimestampUtils.newTimestamp(2019, 12, 31, 23, 59, 59, 999_999_999),
                        TimestampUtils.newTimestamp(2020, 1, 1),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEqualYears")
    void equalYears(Timestamp from, Timestamp to, boolean expected) {
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
                                TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 10),
                                TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 10)
                        ),
                        TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 5),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 10),
                                TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 10)
                        ),
                        TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 10),
                                TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 10)
                        ),
                        TimestampUtils.newTimestamp(2020, 10, 1, 12, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 10),
                                TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 10)
                        ),
                        TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 10),
                        true
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 10),
                                TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 10)
                        ),
                        TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 15),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                null,
                                TimestampUtils.newTimestamp(2020, 10, 2, 10, 5, 10)
                        ),
                        TimestampUtils.newTimestamp(2020, 10, 1, 12, 5, 10),
                        false
                ),
                Arguments.of(
                        Interval.of(
                                TimestampUtils.newTimestamp(2020, 10, 1, 10, 5, 10),
                                null
                        ),
                        TimestampUtils.newTimestamp(2020, 10, 1, 12, 5, 10),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forContains")
    void contains(Interval interval, Timestamp timestamp, boolean expectedResult) {
        Assertions.assertEquals(expectedResult, interval.contains(timestamp));
    }

    // endregion

    // region splitIntoDailyIntervals tests

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToAreEqual() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneDay() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneWholeDay() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToDiffersInOneDay() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 6, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final Timestamp expectedRight0 = TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtStartOfNeighbourDays() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 6);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final Timestamp expectedRight0 = TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourDays() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 6, 23, 59, 59, 999_999_999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(from, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoDailyIntervals_returnsThreePairs_whenFromAndToDiffersInTwoDay() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 7, 12, 20, 30);

        final List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assertions.assertEquals(3, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final Timestamp expectedRight0 = TimestampUtils.newTimestamp(2020, 10, 5, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2020, 10, 6);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        final Timestamp expectedRight1 = TimestampUtils.newTimestamp(2020, 10, 6, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight1, intervals.get(1).getTo());

        final Timestamp expectedLeft2 = TimestampUtils.newTimestamp(2020, 10, 7);
        Assertions.assertEquals(expectedLeft2, intervals.get(2).getFrom());
        Assertions.assertEquals(to, intervals.get(2).getTo());
    }

    // endregion

    // region splitIntoYearlyIntervals tests

    @Test
    void splitIntoYearlyIntervals_returnsOnePair_whenFromAndToAreEqual() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5);

        final List<Interval> intervals = Interval.of(from, to).splitIntoYearlyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoYearlyIntervals_returnsOnePair_whenFromAndToInOneYear() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 2, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 10, 5);

        final List<Interval> intervals = Interval.of(from, to).splitIntoYearlyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoYearlyIntervals_returnsOnePair_whenFromAndToInOneWholeYear() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoYearlyIntervals();

        Assertions.assertEquals(1, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(to, intervals.get(0).getTo());
    }

    @Test
    void splitIntoYearlyIntervals_returnsTwoPairs_whenFromAndToDiffersInOneYear() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 10, 5);

        final List<Interval> intervals = Interval.of(from, to).splitIntoYearlyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final Timestamp expectedRight0 = TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2021, 1, 1);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoYearlyIntervals_returnsTwoPairs_whenFromAndToAreAtStartOfNeighbourYears() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 1, 1);

        final List<Interval> intervals = Interval.of(from, to).splitIntoYearlyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final Timestamp expectedRight0 = TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2021, 1, 1);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoYearlyIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourYears() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999);
        final Timestamp to = TimestampUtils.newTimestamp(2021, 12, 31, 23, 59, 59, 999_999_999);

        final List<Interval> intervals = Interval.of(from, to).splitIntoYearlyIntervals();

        Assertions.assertEquals(2, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        Assertions.assertEquals(from, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2021, 1, 1);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assertions.assertEquals(to, intervals.get(1).getTo());
    }

    @Test
    void splitIntoYearlyIntervals_returnsThreePairs_whenFromAndToDiffersInTwoYear() {
        final Timestamp from = TimestampUtils.newTimestamp(2020, 10, 5);
        final Timestamp to = TimestampUtils.newTimestamp(2022, 10, 5);

        final List<Interval> intervals = Interval.of(from, to).splitIntoYearlyIntervals();

        Assertions.assertEquals(3, intervals.size());

        Assertions.assertEquals(from, intervals.get(0).getFrom());
        final Timestamp expectedRight0 = TimestampUtils.newTimestamp(2020, 12, 31, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight0, intervals.get(0).getTo());

        final Timestamp expectedLeft1 = TimestampUtils.newTimestamp(2021, 1, 1);
        Assertions.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        final Timestamp expectedRight1 = TimestampUtils.newTimestamp(2021, 12, 31, 23, 59, 59, 999_999_999);
        Assertions.assertEquals(expectedRight1, intervals.get(1).getTo());

        final Timestamp expectedLeft2 = TimestampUtils.newTimestamp(2022, 1, 1);
        Assertions.assertEquals(expectedLeft2, intervals.get(2).getFrom());
        Assertions.assertEquals(to, intervals.get(2).getTo());
    }

    // endregion

    // region toDuration tests

    @Test
    void toDuration() {
        final OffsetDateTime fromDateTime = DateTimeTestData.createDateTime(2020, 10, 5, 10, 20, 30);
        final OffsetDateTime toDateTime = DateTimeTestData.createDateTime(2020, 10, 7, 12, 20, 30);
        final Timestamp from = TimestampUtils.newTimestamp(fromDateTime);
        final Timestamp to = TimestampUtils.newTimestamp(toDateTime);

        final Duration duration = Interval.of(from, to).toDuration();

        final Duration expectedDuration = Duration.between(fromDateTime, toDateTime);
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
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30),
                        null,
                        "05.10.2020 10:20:30 — ∞"
                ),
                Arguments.of(
                        null,
                        TimestampUtils.newTimestamp(2020, 10, 7, 12, 20, 30),
                        "-∞ — 07.10.2020 12:20:30"
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30),
                        TimestampUtils.newTimestamp(2020, 10, 7, 12, 20, 30),
                        "05.10.2020 10:20:30 — 07.10.2020 12:20:30"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToPrettyString")
    void toPrettyString(Timestamp from, Timestamp to, String expected) {
        final Interval interval = Interval.of(from, to);

        Assertions.assertEquals(expected, interval.toPrettyString());
    }

    // endregion

    // region toDays tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToDays() {
        return Stream.of(
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30),
                        TimestampUtils.newTimestamp(2020, 10, 15, 10, 20, 30),
                        10
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30),
                        TimestampUtils.newTimestamp(2020, 10, 6, 10, 20, 30),
                        1
                ),
                Arguments.of(
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 20, 30),
                        TimestampUtils.newTimestamp(2020, 10, 5, 10, 30, 30),
                        0.006944444444444444
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToDays")
    void toDays(Timestamp from, Timestamp to, double expected) {
        final Interval interval = Interval.of(from, to);

        AssertUtils.assertEquals(expected, interval.toDays());
    }

    // endregion

    // region jsonMapping tests

    @Test
    void jsonMapping_mapsValue() throws JsonProcessingException {
        final Timestamp from = TimestampUtils.newTimestamp(2022, 10, 1, 10, 11, 12);
        final Timestamp to = TimestampUtils.newTimestamp(2022, 11, 2, 15, 16, 17);
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

        final Timestamp expectedTo = TimestampUtils.newTimestamp(2022, 11, 2, 15, 16, 17);

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

        final Timestamp expectedFrom = TimestampUtils.newTimestamp(2022, 10, 1, 10, 11, 12);

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

        final Timestamp from = TimestampUtils.newTimestamp(2022, 10, 1, 10, 11, 12);
        final Timestamp to = TimestampUtils.newTimestamp(2022, 11, 2, 15, 16, 17);
        final Interval expectedInterval = Interval.of(from, to);

        Assertions.assertEquals(expectedInterval, interval);
    }

    // endregion

}