package ru.obukhov.trader.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.Timestamp;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.TimestampUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable class of Timestamp interval
 */
@Getter
@ToString
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Interval {

    private static final double NANOSECONDS_IN_DAY = 24.0 * 60 * 60 * 1000_000_000;

    private final Timestamp from;
    private final Timestamp to;

    @JsonProperty("from")
    @ApiModelProperty(value = "start of the interval", position = 1, example = "2021-08-01T00:00:00+03:00")
    public String getOffsetDateTimeStringFrom() {
        return TimestampUtils.toOffsetDateTimeString(from);
    }

    @JsonProperty("to")
    @ApiModelProperty(value = "end of the interval", position = 2, example = "2021-08-01T12:00:00+03:00")
    public String getOffsetDateTimeStringTo() {
        return TimestampUtils.toOffsetDateTimeString(to);
    }

    /**
     * @return new Interval with given {@code from} and {@code to}
     * @throws IllegalArgumentException if {@code from} is after {@code to} or if they have different offsets
     */
    @JsonCreator
    public static Interval of(
            @Nullable
            @JsonProperty("from")
            @JsonFormat final OffsetDateTime from,

            @Nullable
            @JsonProperty("to")
            @JsonFormat final OffsetDateTime to
    ) {
        Timestamp fromTimestamp;
        Timestamp toTimestamp;

        if (from == null) {
            fromTimestamp = null;
            toTimestamp = to == null ? null : TimestampUtils.newTimestamp(to);
        } else {
            fromTimestamp = TimestampUtils.newTimestamp(from);
            if (to == null) {
                toTimestamp = null;
            } else {
                Assert.isTrue(from.getOffset().equals(to.getOffset()), "offsets of from and to must be equal");
                toTimestamp = TimestampUtils.newTimestamp(to);
            }
        }

        return of(fromTimestamp, toTimestamp);
    }

    /**
     * @return new Interval with given {@code from} and {@code to}
     * @throws IllegalArgumentException if {@code from} is after {@code to} or if they have different offsets
     */
    public static Interval of(final Timestamp from, final Timestamp to) {
        if (from != null && to != null) {
            Assert.isTrue(!TimestampUtils.isAfter(from, to), "from can't be after to");
        }

        return new Interval(from, to);
    }

    /**
     * @param now timestamp which is handled as now
     * @return new Interval where {@code from} is equals to current {@code from} and
     * {@code to} is equal to:<br/>
     * {@code now} if current {@code to} is null;<br/>
     * equals to current {@code to} otherwise;
     */
    public Interval limitByNowIfNull(Timestamp now) {
        return Interval.of(from, to == null ? now : to);
    }

    /**
     * @return new Interval where {@code from} is at start of current {@code from} and
     * {@code to} is earliest timestamp between end of day of current {@code to} and now
     * @throws IllegalArgumentException when {@code from} and {@code to} are not at same day
     * @throws IllegalArgumentException when {@code from} is in future
     */
    public Interval extendToDay() {
        Assert.isTrue(equalDates(), "'from' and 'to' must be at same day");

        final Timestamp now = TimestampUtils.now();
        TimestampUtils.assertNotFuture(from, now, "from");
        TimestampUtils.assertNotFuture(to, now, "to");

        final Timestamp extendedFrom = TimestampUtils.toStartOfDay(from);
        final Timestamp extendedTo = TimestampUtils.getEarliest(TimestampUtils.toEndOfDay(to), now);

        return new Interval(extendedFrom, extendedTo);
    }

    /**
     * @return new Interval where {@code from} is at start of current {@code from} and
     * {@code to} is earliest timestamp between end of day of current {@code to} and current timestamp
     * @throws IllegalArgumentException when {@code from} and {@code to} are not at same day
     * @throws IllegalArgumentException when {@code from} is in future
     */
    public Interval extendToYear() {
        Assert.isTrue(equalYears(), "'from' and 'to' must be at same year");

        final Timestamp now = TimestampUtils.now();
        TimestampUtils.assertNotFuture(from, now, "from");
        TimestampUtils.assertNotFuture(to, now, "to");

        final Timestamp extendedFrom = TimestampUtils.toStartOfYear(from);
        Timestamp extendedTo = TimestampUtils.getEarliest(TimestampUtils.toEndOfYear(to), now);

        return new Interval(extendedFrom, extendedTo);
    }

    /**
     * @return a copy of this Interval with the specified number of days subtracted from each side.
     * @throws NullPointerException if from or to is null
     */
    public Interval minusDays(final int days) {
        return new Interval(TimestampUtils.plusDays(from, -days), TimestampUtils.plusDays(to, -days));
    }

    /**
     * @return a copy of this Interval with the specified number of years subtracted from each side.
     * @throws NullPointerException if from or to is null
     */
    public Interval minusYears(final int years) {
        return new Interval(TimestampUtils.plusYears(from, -years), TimestampUtils.plusYears(to, -years));
    }

    /**
     * @return true, if dates of instants of {@code from} and {@code to} are equal, or else false
     */
    public boolean equalDates() {
        return TimestampUtils.equalDates(from, to);
    }

    /**
     * @return true, if years of {@code from} and {@code to} are equal, or else false
     */
    public boolean equalYears() {
        if (from == null) {
            return to == null;
        }

        return to != null && TimestampUtils.toStartOfYear(from).equals(TimestampUtils.toStartOfYear(to));
    }

    /**
     * @return true if {@code timestamp} is in current interval including extreme values
     */
    public boolean contains(final Timestamp timestamp) {
        return from != null && to != null && !TimestampUtils.isBefore(timestamp, from) && !TimestampUtils.isAfter(timestamp, to);
    }

    /**
     * @return list of consecutive intervals starting with {@code from} and ending with {@code to}.
     * Every interval is in one day.
     * <br/>
     * {@code from} of first interval equals {@code from} of current interval.
     * <br/>
     * {@code from} of other intervals are at start of day.
     * <br/>
     * {@code to} of last interval equals {@code to} of current interval.
     * <br/>
     * {@code to} of other intervals are at end of day.
     */
    public List<Interval> splitIntoDailyIntervals() {
        final List<Interval> result = new ArrayList<>();

        Timestamp currentFrom = from;
        Timestamp endOfDay = TimestampUtils.toEndOfDay(from);

        while (TimestampUtils.isBefore(endOfDay, to)) {
            result.add(Interval.of(currentFrom, endOfDay));

            currentFrom = TimestampUtils.plusNanos(endOfDay, 1);
            endOfDay = TimestampUtils.plusDays(endOfDay, 1);
        }
        result.add(Interval.of(currentFrom, to));

        return result;
    }

    /**
     * @return list of consecutive intervals starting with {@code from} and ending with {@code to}.
     * Every interval is in one year.
     * <br/>
     * {@code from} of first interval equals {@code from} of current interval.
     * <br/>
     * {@code from} of other intervals are at start of day.
     * <br/>
     * {@code to} of last interval equals {@code to} of current interval.
     * <br/>
     * {@code to} of other intervals are at end of day.
     */
    public List<Interval> splitIntoYearlyIntervals() {
        final List<Interval> result = new ArrayList<>();

        Timestamp currentFrom = from;
        Timestamp endOfYear = TimestampUtils.toEndOfYear(from);

        while (TimestampUtils.isBefore(endOfYear, to)) {
            result.add(Interval.of(currentFrom, endOfYear));

            currentFrom = TimestampUtils.plusNanos(endOfYear, 1);
            endOfYear = TimestampUtils.plusYears(endOfYear, 1);
        }
        result.add(Interval.of(currentFrom, to));

        return result;
    }

    /**
     * @return duration of current interval
     */
    public Duration toDuration() {
        return TimestampUtils.toDuration(from, to);
    }

    /**
     * @return double count of days in current interval
     */
    public double toDays() {
        return toDuration().toNanos() / NANOSECONDS_IN_DAY;
    }

    /**
     * @return pretty string representation of current interval, where null values are represented as "-∞" or "∞"
     */
    public String toPrettyString() {
        final String fromString = from == null ? "-∞" : DateUtils.DATE_TIME_FORMATTER.format(TimestampUtils.toOffsetDateTime(from));
        final String toString = to == null ? "∞" : DateUtils.DATE_TIME_FORMATTER.format(TimestampUtils.toOffsetDateTime(to));

        return fromString + " — " + toString;
    }

}