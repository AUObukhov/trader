package ru.obukhov.trader.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.web.model.validation.constraint.NotAllNull;

import java.beans.ConstructorProperties;
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
@NotAllNull(message = "from and to can't be both null")
public class Interval {

    private static final double NANOSECONDS_IN_DAY = 24.0 * 60 * 60 * 1000_000_000;

    @JsonFormat(pattern = DateUtils.OFFSET_DATE_TIME_FORMAT)
    private final OffsetDateTime from;

    @JsonFormat(pattern = DateUtils.OFFSET_DATE_TIME_FORMAT)
    private final OffsetDateTime to;

    @ConstructorProperties({"from", "to"})
    public Interval(
            @Nullable
            @JsonProperty("from")
            @JsonFormat(pattern = DateUtils.OFFSET_DATE_TIME_FORMAT) final OffsetDateTime from,

            @Nullable
            @JsonProperty("to")
            @JsonFormat(pattern = DateUtils.OFFSET_DATE_TIME_FORMAT) final OffsetDateTime to
    ) {
        if (from != null && to != null) {
            Assert.isTrue(!from.isAfter(to), "from can't be after to");
        }

        this.from = from == null ? null : DateUtils.setDefaultOffsetSameInstant(from);
        this.to = to == null ? null : DateUtils.setDefaultOffsetSameInstant(to);
    }

    /**
     * @return new Interval with given {@code from} and {@code to}
     * @throws IllegalArgumentException if {@code from} is after {@code to} or if they have different offsets
     */
    public static Interval of(final OffsetDateTime from, final OffsetDateTime to) {
        return new Interval(from, to);
    }

    /**
     * @param now timestamp which is handled as now
     * @return new Interval where {@code from} is equals to current {@code from} and
     * {@code to} is equal to:<br/>
     * {@code now} if current {@code to} is null;<br/>
     * equals to current {@code to} otherwise;
     */
    public Interval limitByNowIfNull(final OffsetDateTime now) {
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

        final OffsetDateTime now = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(from, now, "from");
        DateUtils.assertDateTimeNotFuture(to, now, "to");

        final OffsetDateTime extendedFrom = DateUtils.toStartOfDay(from);
        final OffsetDateTime extendedTo = DateUtils.getEarliestDateTime(DateUtils.toEndOfDay(to), now);

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

        final OffsetDateTime now = OffsetDateTime.now();
        DateUtils.assertDateTimeNotFuture(from, now, "from");
        DateUtils.assertDateTimeNotFuture(to, now, "to");

        final OffsetDateTime extendedFrom = DateUtils.atStartOfYear(from);
        OffsetDateTime extendedTo = DateUtils.getEarliestDateTime(DateUtils.atEndOfYear(to), now);

        return new Interval(extendedFrom, extendedTo);
    }

    /**
     * @return a copy of this Interval with the specified number of days subtracted from each side.
     * @throws NullPointerException if from or to is null
     */
    public Interval minusDays(final int days) {
        return new Interval(from.minusDays(days), to.minusDays(days));
    }

    /**
     * @return a copy of this Interval with the specified number of years subtracted from each side.
     * @throws NullPointerException if from or to is null
     */
    public Interval minusYears(final int years) {
        return new Interval(from.minusYears(years), to.minusYears(years));
    }

    /**
     * @return true, if dates of instants of {@code from} and {@code to} are equal, or else false
     */
    public boolean equalDates() {
        return DateUtils.equalDates(from, to);
    }

    /**
     * @return true, if years of {@code from} and {@code to} are equal, or else false
     */
    public boolean equalYears() {
        if (from == null) {
            return to == null;
        }

        return to != null && DateUtils.atStartOfYear(from).equals(DateUtils.atStartOfYear(to));
    }

    /**
     * @return true if {@code dateTime} is in current interval including extreme values
     */
    public boolean contains(final OffsetDateTime dateTime) {
        return from != null && to != null && !dateTime.isBefore(from) && !dateTime.isAfter(to);
    }

    /**
     * @return list of consecutive intervals starting with {@code from} inclusive and ending with {@code to} exclusive.
     * Every interval is in one day.
     * <br/>
     * {@code from} of first interval equals {@code from} of current interval.
     * <br/>
     * {@code from} of other intervals are on start of day.
     * <br/>
     * {@code to} of last interval equals {@code to} of current interval.
     * <br/>
     * {@code to} of other intervals are on end of day.
     */
    public List<Interval> splitIntoDailyIntervals() {
        final List<Interval> result = new ArrayList<>();

        OffsetDateTime currentFrom = from;
        OffsetDateTime endOfDay = DateUtils.toEndOfDay(from);

        while (endOfDay.isBefore(to)) {
            result.add(Interval.of(currentFrom, endOfDay));

            currentFrom = endOfDay.plusNanos(1);
            endOfDay = endOfDay.plusDays(1);
        }
        if (!currentFrom.isEqual(to)) {
            result.add(Interval.of(currentFrom, to));
        }

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

        OffsetDateTime currentFrom = from;
        OffsetDateTime endOfYear = DateUtils.atEndOfYear(from);

        while (endOfYear.isBefore(to)) {
            result.add(Interval.of(currentFrom, endOfYear));

            currentFrom = endOfYear.plusNanos(1);
            endOfYear = endOfYear.plusYears(1);
        }
        result.add(Interval.of(currentFrom, to));

        return result;
    }

    public Interval unite(final Interval other) {
        final OffsetDateTime resultFrom = DateUtils.getEarliestDateTime(this.from, other.from);
        final OffsetDateTime resultTo = DateUtils.getLatestDateTime(this.to, other.to);
        return Interval.of(resultFrom, resultTo);
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

    public List<TradingDay> toTradingDays(final WorkSchedule workSchedule) {
        return splitIntoDailyIntervals()
                .stream()
                .map(interval -> interval.toTradingDay(workSchedule))
                .toList();
    }

    private TradingDay toTradingDay(final WorkSchedule workSchedule) {
        final OffsetDateTime date = DateUtils.toStartOfDay(from);
        final OffsetDateTime startTime = DateUtils.setTime(date, workSchedule.getStartTime());
        final OffsetDateTime endTime = startTime.plus(workSchedule.getDuration());

        return TradingDay.builder()
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .isTradingDay(DateUtils.isWorkDay(date))
                .build();
    }

    /**
     * @return pretty string representation of current interval, where null values are represented as "-∞" or "∞"
     */
    public String toPrettyString() {
        final String fromString = from == null ? "-∞" : DateUtils.DATE_TIME_FORMATTER.format(from);
        final String toString = to == null ? "∞" : DateUtils.DATE_TIME_FORMATTER.format(to);

        return fromString + " — " + toString;
    }

}