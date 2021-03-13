package ru.obukhov.trader.common.model;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Interval.class)
public class IntervalTest {

    // region of tests

    @Test
    public void of_throwsIllegalArgumentException_whenFromIsAfterTo() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 10);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 5);

        AssertUtils.assertThrowsWithMessage(() -> Interval.of(from, to),
                IllegalArgumentException.class,
                "from can't be after to");
    }

    @Test
    public void of_returnsInterval_whenFromAndToAreNull() {
        Interval interval = Interval.of(null, null);

        Assert.assertNull(interval.getFrom());
        Assert.assertNull(interval.getTo());
    }

    @Test
    public void of_returnsInterval_whenFromIsNull() {
        OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        Interval interval = Interval.of(null, to);

        Assert.assertNull(interval.getFrom());
        Assert.assertEquals(to, interval.getTo());
    }

    @Test
    public void of_returnsInterval_whenToIsNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);

        Interval interval = Interval.of(from, null);

        Assert.assertEquals(from, interval.getFrom());
        Assert.assertNull(interval.getTo());
    }

    @Test
    public void of_returnsInterval_whenFromAndToAreNotNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        Interval interval = Interval.of(from, to);

        Assert.assertEquals(from, interval.getFrom());
        Assert.assertEquals(to, interval.getTo());
    }

    // endregion

    // region ofDay tests

    @Test
    public void ofDay_withYearMonthDayOfMonth_returnsProperInterval() {
        Interval interval = Interval.ofDay(2020, 10, 10);

        OffsetDateTime expectedFrom = DateUtils.getDate(2020, 10, 10);
        OffsetDateTime expectedToo = DateUtils.atEndOfDay(expectedFrom);
        Assert.assertEquals(expectedFrom, interval.getFrom());
        Assert.assertEquals(expectedToo, interval.getTo());

    }

    @Test
    public void ofDay_withDateTime_returnsProperInterval() {
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 10, 11, 12, 13);

        Interval interval = Interval.ofDay(dateTime);

        OffsetDateTime expectedFrom = DateUtils.atStartOfDay(dateTime);
        OffsetDateTime expectedToo = DateUtils.atEndOfDay(dateTime);
        Assert.assertEquals(expectedFrom, interval.getFrom());
        Assert.assertEquals(expectedToo, interval.getTo());

    }

    // endregion

    // region limitByNowIfNull tests

    @Test
    public void limitByNowIfNull_setToToNow_whenToIsNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = null;

        Interval interval = Interval.of(from, to);
        Interval newInterval = interval.limitByNowIfNull();

        Assert.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assert.assertNotNull(newInterval.getTo());
    }

    @Test
    public void limitByNowIfNull_notChangesTo_whenToIsNotNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        Interval interval = Interval.of(from, to);
        Interval newInterval = interval.limitByNowIfNull();

        Assert.assertEquals(interval.getFrom(), newInterval.getFrom());
        Assert.assertEquals(interval.getTo(), newInterval.getTo());
    }


    // endregion

    // region extendToWholeDay tests

    @Test
    public void extendToWholeDay_throwsIllegalArgumentException_whenNotEqualsDates() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 11, 30, 40);
        Interval interval = Interval.of(from, to);

        AssertUtils.assertThrowsWithMessage(() -> interval.extendToWholeDay(false),
                IllegalArgumentException.class,
                "'from' and 'to' must be at same day");
    }

    @Test
    public void extendToWholeDay_throwsIllegalArgumentException_whenNotFutureIsTrueAndFromIsInFuture() {

        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 10, 11, 12);

        OffsetDateTime from = mockedNow.plusHours(1);
        OffsetDateTime to = from.plusMinutes(10);
        Interval interval = Interval.of(from, to);

        PowerMockito.mockStatic(OffsetDateTime.class);
        when(OffsetDateTime.now()).thenReturn(mockedNow);

        String expectedMessage =
                "'from' (2020-09-23T11:11:12+03:00) can't be in future. Now is 2020-09-23T10:11:12+03:00";
        AssertUtils.assertThrowsWithMessage(() -> interval.extendToWholeDay(true),
                IllegalArgumentException.class,
                expectedMessage);
    }

    @Test
    public void extendToWholeDay_extendsToWholeDay_whenEqualsDates_andNotFuture() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 11, 30, 40);
        Interval interval = Interval.of(from, to);

        Interval extendedInterval = interval.extendToWholeDay(false);

        OffsetDateTime expectedFrom = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime expectedTo = DateUtils.atEndOfDay(expectedFrom);
        Assert.assertEquals(expectedFrom, extendedInterval.getFrom());
        Assert.assertEquals(expectedTo, extendedInterval.getTo());

    }

    @Test
    public void extendToWholeDay_extendsToWholeDay_whenEqualsDates_andFuture() {

        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = from.plusHours(1);
        Interval interval = Interval.of(from, to);

        Interval extendedInterval = interval.extendToWholeDay(true);

        Assert.assertEquals(DateUtils.atStartOfDay(from), extendedInterval.getFrom());
        Assert.assertTrue(to.isAfter(extendedInterval.getTo()));

    }

    // endregion

    // region equalDates tests

    @Test
    public void equalDates_returnsTrue_whenDatesAreNull() {

        OffsetDateTime from = null;
        OffsetDateTime to = null;

        boolean result = Interval.of(from, to).equalDates();

        Assert.assertTrue(result);

    }

    @Test
    public void equalDates_returnsTrue_whenDatesAreEqual1() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30, 40);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50);

        boolean result = Interval.of(from, to).equalDates();

        Assert.assertTrue(result);

    }

    @Test
    public void equalDates_returnsTrue_whenDatesAreEqua2() {

        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        boolean result = Interval.of(from, to).equalDates();

        Assert.assertTrue(result);

    }

    @Test
    public void equalDates_returnsFalse_whenFromIsNullAndToIsNot() {

        OffsetDateTime from = null;
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50);

        boolean result = Interval.of(from, to).equalDates();

        Assert.assertFalse(result);

    }

    @Test
    public void equalDates_returnsFalse_whenFromIsNotNullAndToIsNull() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 11, 30, 40, 50);
        OffsetDateTime to = null;

        boolean result = Interval.of(from, to).equalDates();

        Assert.assertFalse(result);

    }

    @Test
    public void equalDates_returnsFalse_whenDatesAreNotEqual() {

        OffsetDateTime to = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime from = to.minusNanos(1);

        boolean result = Interval.of(from, to).equalDates();

        Assert.assertFalse(result);

    }

    // endregion

    // region contains tests

    @Test
    public void contains_returnsFalse_whenDateTimeBeforeFrom() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 10, 5, 5);

        boolean result = Interval.of(from, to).contains(dateTime);

        Assert.assertFalse(result);

    }

    @Test
    public void contains_returnsTrue_whenDateTimeEqualsFrom() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);

        boolean result = Interval.of(from, to).contains(dateTime);

        Assert.assertTrue(result);

    }

    @Test
    public void contains_returnsTrue_whenDateTimeBetweenFromAndTo() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 12, 5, 10);

        boolean result = Interval.of(from, to).contains(dateTime);

        Assert.assertTrue(result);

    }

    @Test
    public void contains_returnsTrue_whenDateTimeEqualsTo() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);

        boolean result = Interval.of(from, to).contains(dateTime);

        Assert.assertTrue(result);

    }

    @Test
    public void contains_returnsFalse_whenDateTimeAfterTo() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 2, 10, 5, 15);

        boolean result = Interval.of(from, to).contains(dateTime);

        Assert.assertFalse(result);

    }

    @Test
    public void contains_returnsFalse_whenFromIsNull() {

        OffsetDateTime from = null;
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 12, 5, 10);

        boolean result = Interval.of(from, to).contains(dateTime);

        Assert.assertFalse(result);

    }

    @Test
    public void contains_returnsFalse_whenToIsNull() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime to = null;
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 12, 5, 10);

        boolean result = Interval.of(from, to).contains(dateTime);

        Assert.assertFalse(result);

    }

    // endregion

    // region getDefault tests

    @Test
    public void getDefault_returnsSameValues_whenValuesAreNotNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 10);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 11);

        OffsetDateTime defaultFrom = DateUtils.getDate(2020, 11, 10);
        OffsetDateTime defaultTo = DateUtils.getDate(2020, 11, 11);

        Interval interval = Interval.of(from, to).getDefault(defaultFrom, defaultTo);

        Assert.assertEquals(from, interval.getFrom());
        Assert.assertEquals(to, interval.getTo());
    }

    @Test
    public void getDefault_returnsDefaultValues_whenValuesAreNull() {
        OffsetDateTime from = null;
        OffsetDateTime to = null;

        OffsetDateTime defaultFrom = DateUtils.getDate(2020, 11, 10);
        OffsetDateTime defaultTo = DateUtils.getDate(2020, 11, 11);

        Interval interval = Interval.of(from, to).getDefault(defaultFrom, defaultTo);

        Assert.assertEquals(defaultFrom, interval.getFrom());
        Assert.assertEquals(defaultTo, interval.getTo());
    }

    // endregion

    // region splitIntoDailyIntervals tests

    @Test
    public void splitIntoDailyIntervals_returnsOnePair_whenFromAndToAreEqual() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assert.assertEquals(1, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getFrom());
        Assert.assertEquals(to, intervals.get(0).getTo());

    }

    @Test
    public void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneDay() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 12, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assert.assertEquals(1, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getFrom());
        Assert.assertEquals(to, intervals.get(0).getTo());

    }

    @Test
    public void splitIntoDailyIntervals_returnsOnePair_whenFromAndToInOneWholeDay() {

        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assert.assertEquals(1, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getFrom());
        Assert.assertEquals(to, intervals.get(0).getTo());

    }

    @Test
    public void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToDiffersInOneDay() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 12, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assert.assertEquals(2, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getFrom());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight0, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assert.assertEquals(to, intervals.get(1).getTo());

    }

    @Test
    public void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtStartOfNeighbourDays() {

        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 6);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assert.assertEquals(2, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getFrom());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight0, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assert.assertEquals(to, intervals.get(1).getTo());

    }

    @Test
    public void splitIntoDailyIntervals_returnsTwoPairs_whenFromAndToAreAtEndOfNeighbourDays() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assert.assertEquals(2, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getFrom());
        Assert.assertEquals(from, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        Assert.assertEquals(to, intervals.get(1).getTo());

    }

    @Test
    public void splitIntoDailyIntervals_returnsThreePairs_whenFromAndToDiffersInTwoDay() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        List<Interval> intervals = Interval.of(from, to).splitIntoDailyIntervals();

        Assert.assertEquals(3, intervals.size());

        Assert.assertEquals(from, intervals.get(0).getFrom());
        OffsetDateTime expectedRight0 = DateUtils.getDateTime(2020, 10, 5, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight0, intervals.get(0).getTo());

        OffsetDateTime expectedLeft1 = DateUtils.getDate(2020, 10, 6);
        Assert.assertEquals(expectedLeft1, intervals.get(1).getFrom());
        OffsetDateTime expectedRight1 = DateUtils.getDateTime(2020, 10, 6, 23, 59, 59, 999999999);
        Assert.assertEquals(expectedRight1, intervals.get(1).getTo());

        OffsetDateTime expectedLeft2 = DateUtils.getDate(2020, 10, 7);
        Assert.assertEquals(expectedLeft2, intervals.get(2).getFrom());
        Assert.assertEquals(to, intervals.get(2).getTo());

    }

    // endregion

    // region toDuration tests

    @Test
    public void toDuration_returnsProperDuration() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        Duration duration = Interval.of(from, to).toDuration();

        Duration expectedDuration = Duration.between(from, to);
        Assert.assertEquals(expectedDuration, duration);
    }

    // endregion

    // region toPrettyString tests

    @Test
    public void toPrettyString_whenFromAndToAreNull() {
        String prettyString = Interval.of(null, null).toPrettyString();

        Assert.assertEquals("-∞ — ∞", prettyString);
    }

    @Test
    public void toPrettyString_whenFromIsNotNull_andToIsNull() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);

        String prettyString = Interval.of(from, null).toPrettyString();

        Assert.assertEquals("2020.10.05 10:20:30 — ∞", prettyString);
    }

    @Test
    public void toPrettyString_whenFromIsNull_andToIsNotNull() {
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        String prettyString = Interval.of(null, to).toPrettyString();

        Assert.assertEquals("-∞ — 2020.10.07 12:20:30", prettyString);
    }

    @Test
    public void toPrettyString_whenFromAndToAreNotNull() {
        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 7, 12, 20, 30);

        String prettyString = Interval.of(from, to).toPrettyString();

        Assert.assertEquals("2020.10.05 10:20:30 — 2020.10.07 12:20:30", prettyString);
    }

    // endregion

}