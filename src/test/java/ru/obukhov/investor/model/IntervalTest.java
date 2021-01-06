package ru.obukhov.investor.model;

import org.junit.Assert;
import org.junit.Test;
import ru.obukhov.investor.util.DateUtils;

import java.time.OffsetDateTime;
import java.util.List;

public class IntervalTest {

    // region of tests

    @Test(expected = IllegalArgumentException.class)
    public void of_throwsIllegalArgumentException_whenFromIsAfterTo() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 10);
        OffsetDateTime to = DateUtils.getDate(2020, 10, 5);

        Interval.of(from, to);
    }

    public void of_returnsInterval_whenFromAndToAreNull() {
        Interval interval = Interval.of(null, null);

        Assert.assertNull(interval.getFrom());
        Assert.assertNull(interval.getTo());
    }

    public void of_returnsInterval_whenFromIsNull() {
        OffsetDateTime to = DateUtils.getDate(2020, 10, 10);

        Interval interval = Interval.of(null, to);

        Assert.assertNull(interval.getFrom());
        Assert.assertEquals(to, interval.getTo());
    }

    public void of_returnsInterval_whenToIsNull() {
        OffsetDateTime from = DateUtils.getDate(2020, 10, 5);

        Interval interval = Interval.of(from, null);

        Assert.assertEquals(from, interval.getFrom());
        Assert.assertNull(interval.getTo());
    }

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

    // region extendToWholeDay tests

    @Test(expected = IllegalArgumentException.class)
    public void extendToWholeDay_throwsIllegalArgumentException_whenNotEqualsDates() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 5, 10, 20, 30);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 6, 11, 30, 40);
        Interval interval = Interval.of(from, to);

        interval.extendToWholeDay(false);

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

    @Test(expected = IllegalArgumentException.class)
    public void contains_throwsIllegalArgumentException_whenFromIsAfterTo() {

        OffsetDateTime from = DateUtils.getDateTime(2020, 10, 2, 10, 5, 10);
        OffsetDateTime to = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);
        OffsetDateTime dateTime = DateUtils.getDateTime(2020, 10, 1, 10, 5, 10);

        Interval.of(from, to).contains(dateTime);

    }

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
}