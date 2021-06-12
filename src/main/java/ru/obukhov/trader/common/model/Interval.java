package ru.obukhov.trader.common.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable class of OffsetDateTime interval
 */
@Getter
@ToString
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Interval {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    private static final double NANOSECONDS_IN_DAY = 24.0 * 60 * 60 * 1000_000_000;

    private final OffsetDateTime from;

    private final OffsetDateTime to;

    /**
     * @return new Interval with given {@code from} and {@code to}
     * @throws IllegalArgumentException if {@code from} is after {@code to}
     */
    public static Interval of(@Nullable final OffsetDateTime from, @Nullable final OffsetDateTime to) {
        Assert.isTrue(from == null || to == null || !from.isAfter(to), "from can't be after to");

        return new Interval(from, to);
    }

    /**
     * @param year       year of given date
     * @param month      month of given date
     * @param dayOfMonth day of month of given date
     * @return new Interval with {@code from} is start of given date and {@code to} is end of given date
     */
    public static Interval ofDay(final int year, final int month, final int dayOfMonth) {
        final OffsetDateTime from = DateUtils.getDate(year, month, dayOfMonth);
        final OffsetDateTime to = DateUtils.atEndOfDay(from);
        return new Interval(from, to);
    }

    /**
     * @return new Interval with {@code from} is start of given date and {@code to} is end of given date
     */
    public static Interval ofDay(@NotNull final OffsetDateTime dateTime) {
        final OffsetDateTime from = DateUtils.atStartOfDay(dateTime);
        final OffsetDateTime to = DateUtils.atEndOfDay(from);
        return new Interval(from, to);
    }

    /**
     * @return new Interval where {@code from} is equals to current {@code from} and
     * {@code to} is equal to:<br/>
     * {@code now} if current {@code to} is null;<br/>
     * equals to current {@code to} otherwise;
     */
    public Interval limitByNowIfNull() {
        return Interval.of(from, to == null ? OffsetDateTime.now() : to);
    }

    /**
     * @param allowFuture if false then to of returned value is set to now
     * @return new Interval where {@code from} is at start of current {@code from} and
     * {@code to} is earliest dateTime between end of day of current {@code to} and current dateTime
     * @throws IllegalArgumentException when {@code from} and {@code to} are not at same day
     * @throws IllegalArgumentException when {@code from} is in future and {@code allowFuture} is false
     */
    public Interval extendToWholeDay(final boolean allowFuture) {
        Assert.isTrue(equalDates(), "'from' and 'to' must be at same day");

        final OffsetDateTime extendedFrom = DateUtils.atStartOfDay(from);
        OffsetDateTime extendedTo = DateUtils.atEndOfDay(to);
        if (!allowFuture) {
            final OffsetDateTime now = OffsetDateTime.now();
            DateUtils.assertDateTimeNotFuture(from, now, "from");
            extendedTo = DateUtils.getEarliestDateTime(extendedTo, now);
        }

        return new Interval(extendedFrom, extendedTo);
    }

    /**
     * @return true, if days of year of {@code from} and {@code to} are equal, or else false
     */
    public boolean equalDates() {
        if (from == null) {
            return to == null;
        }

        return to != null && DateUtils.atStartOfDay(from).equals(DateUtils.atStartOfDay(to));
    }

    /**
     * @return true if {@code dateTime} is in current interval including extreme values
     */
    public boolean contains(final OffsetDateTime dateTime) {
        return from != null && to != null
                && !dateTime.isBefore(from) && !dateTime.isAfter(to);
    }

    /**
     * @return new Interval with
     * same value of current {@code from} or given {@code defaultFrom} if current {@code from} is null
     * and
     * same value of current {@code to} or given {@code defaultTo} if current {@code to} is null
     */
    public Interval getDefault(final OffsetDateTime defaultFrom, final OffsetDateTime defaultTo) {
        return new Interval(from == null ? defaultFrom : from,
                to == null ? defaultTo : to);
    }

    /**
     * @return list of consecutive intervals starting with {@code from} and ending with {@code to}.
     * Every interval is in one day.
     * <br/>
     * {@code from} of first interval equals {@code from} of current interval.
     * {@code from} of other intervals are at start of day.
     * <br/>
     * {@code to} of last interval equals {@code to} of current interval.
     * {@code to} of other intervals are at end of day.
     */
    public List<Interval> splitIntoDailyIntervals() {
        final List<Interval> result = new ArrayList<>();

        OffsetDateTime currentFrom = from;
        OffsetDateTime endOfDay = DateUtils.atEndOfDay(from);

        while (endOfDay.isBefore(to)) {
            result.add(Interval.of(currentFrom, endOfDay));

            currentFrom = endOfDay.plusNanos(1);
            endOfDay = endOfDay.plusDays(1);
        }
        result.add(Interval.of(currentFrom, to));

        return result;
    }

    /**
     * @return duration of current interval
     */
    public Duration toDuration() {
        return Duration.between(from, to);
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
        final String fromString = from == null ? "-∞" : DATE_TIME_FORMATTER.format(from);
        final String toString = to == null ? "∞" : DATE_TIME_FORMATTER.format(to);

        return fromString + " — " + toString;
    }

}