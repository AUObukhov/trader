package ru.obukhov.trader.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.time.Period;
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
     * @return new Interval where {@code from} is at start of unit of given {@code period} containing current {@code from} and
     * {@code to} is earliest timestamp between end of unit of given {@code period} containing current {@code to} and now
     * @throws IllegalArgumentException when {@code from} and {@code to} are not at the same Period
     * @throws IllegalArgumentException when {@code from} or {@code to}  is in future
     */
    public Interval extendTo(final Period period) {
        if (!isSamePeriods(period)) {
            final String message = String.format("'from' (%s) and 'to' (%s) must be at the same period %s", from, to, period);
            throw new IllegalArgumentException(message);
        }

        final OffsetDateTime now = DateUtils.now();
        DateUtils.assertDateTimeNotFuture(from, now, "from");
        DateUtils.assertDateTimeNotFuture(to, now, "to");

        final OffsetDateTime extendedFrom = Periods.toStartOfPeriod(from, period);
        OffsetDateTime extendedTo = extendedFrom.plus(period);
        extendedTo = DateUtils.getEarliestDateTime(extendedTo, now);

        return Interval.of(extendedFrom, extendedTo);
    }

    /**
     * @return true, if dates of instants of {@code from} and {@code to} are equal, or else false
     */
    private boolean isSamePeriods(final Period period) {
        final OffsetDateTime startOfPeriod = Periods.toStartOfPeriod(from, period);
        final OffsetDateTime startOfNextPeriod = startOfPeriod.plus(period);
        return !to.isAfter(startOfNextPeriod);
    }

    /**
     * @return true if this Interval matches any value of {@link Period}
     */
    @JsonIgnore
    public boolean isAnyPeriod() {
        if (from == null || to == null) {
            return false;
        }

        return isPeriod(Periods.DAY)
                || isPeriod(Periods.TWO_DAYS)
                || isPeriod(Periods.WEEK)
                || isPeriod(Periods.MONTH)
                || isPeriod(Periods.YEAR)
                || isPeriod(Periods.TWO_YEARS)
                || isPeriod(Periods.DECADE);
    }

    private boolean isPeriod(final Period period) {
        final OffsetDateTime startOfPeriod = Periods.toStartOfPeriod(from, period);
        final OffsetDateTime startOfNextPeriod = startOfPeriod.plus(period);
        return startOfPeriod.equals(from) && startOfNextPeriod.equals(to);
    }

    /**
     * @return true if given {@code dateTime} is in current interval including {@code from} and excluding {@code to}
     */
    public boolean contains(final OffsetDateTime dateTime) {
        return from != null && to != null && !dateTime.isBefore(from) && dateTime.isBefore(to);
    }

    /**
     * @return list of consecutive intervals starting with {@code from} inclusive and ending with {@code to} exclusive.
     * Every interval is in one Period.
     * <br/>
     * {@code from} of first interval equals {@code from} of current interval.
     * <br/>
     * {@code from} of other intervals are on start of given {@code period}.
     * <br/>
     * {@code to} of last interval equals {@code to} of current interval.
     * <br/>
     * {@code to} of other intervals are on end of given {@code period}.
     */
    public List<Interval> splitIntoIntervals(final Period period) {
        final List<Interval> result = new ArrayList<>();

        OffsetDateTime currentFrom = from;
        OffsetDateTime currentTo = Periods.toStartOfPeriod(from, period).plus(period);

        while (currentTo.isBefore(to)) {
            result.add(Interval.of(currentFrom, currentTo));

            currentFrom = currentTo;
            currentTo = currentTo.plus(period);
        }
        if (!currentFrom.isEqual(to)) {
            result.add(Interval.of(currentFrom, to));
        }

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
        return splitIntoIntervals(Periods.DAY)
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