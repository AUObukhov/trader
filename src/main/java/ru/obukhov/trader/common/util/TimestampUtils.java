package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.quartz.CronExpression;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utils for {@link Timestamp}
 */
@UtilityClass
public class TimestampUtils {

    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("+03:00");

    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_DAY = HOURS_PER_DAY * 60;
    public static final int SECONDS_PER_DAY = MINUTES_PER_DAY * 60;

    public static final int NANOS_PER_SECOND = 1000_000_000;
    public static final long NANOS_PER_MINUTE = 60L * NANOS_PER_SECOND;
    public static final long NANOS_PER_HOUR = 60 * NANOS_PER_MINUTE;

    private static final long SECONDS_MIN_VALUE = -62135596800L;
    private static final long SECONDS_MAX_VALUE = 253402300799L;
    private static final int NANOS_MIN_VALUE = 0;
    private static final int NANOS_MAX_VALUE = 999_999_999;

    // region newTimestamp

    public static Timestamp newTimestamp(final long seconds) {
        return newTimestamp(seconds, 0);
    }

    public static Timestamp newTimestamp(final long seconds, final int nanos) {
        if (seconds < SECONDS_MIN_VALUE || seconds > SECONDS_MAX_VALUE) {
            throw new IllegalArgumentException(
                    "seconds must be from 0001-01-01T00:00:00Z (" + SECONDS_MIN_VALUE +
                            ") to 9999-12-31T23:59:59Z (" + SECONDS_MAX_VALUE + ") inclusive"
            );
        }
        if (nanos < NANOS_MIN_VALUE || nanos > NANOS_MAX_VALUE) {
            throw new IllegalArgumentException("nanos must be from " + NANOS_MIN_VALUE + " to " + NANOS_MAX_VALUE + " inclusive");
        }

        return Timestamp.newBuilder().setSeconds(seconds).setNanos(nanos).build();
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth) {
        return newTimestamp(year, month, dayOfMonth, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final int hour) {
        return newTimestamp(year, month, dayOfMonth, hour, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final int hour, final int minute) {
        return newTimestamp(year, month, dayOfMonth, hour, minute, 0, 0, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final int hour, final ZoneOffset offset) {
        return newTimestamp(year, month, dayOfMonth, hour, 0, 0, 0, offset);
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final ZoneOffset offset) {
        return newTimestamp(year, month, dayOfMonth, 0, 0, 0, 0, offset);
    }

    public static Timestamp newTimestamp(final int year, final int month, final int dayOfMonth, final int hour, final int minute, final int second) {
        return newTimestamp(year, month, dayOfMonth, hour, minute, second, 0, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second,
            final int nanoOfSecond
    ) {
        return newTimestamp(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, DateUtils.DEFAULT_OFFSET);
    }

    public static Timestamp newTimestamp(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second,
            final int nanoOfSecond,
            final ZoneOffset offset
    ) {
        final OffsetDateTime dateTime = OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond, offset);
        return newTimestamp(dateTime);
    }

    public static Timestamp newTimestamp(final OffsetDateTime dateTime) {
        final Instant instant = dateTime.toInstant();
        return newTimestamp(instant.getEpochSecond(), instant.getNano());
    }

    public static Timestamp newTimestamp(final Date date) {
        final Instant instant = date.toInstant();
        return newTimestamp(instant.getEpochSecond(), instant.getNano());
    }

    public static Timestamp newTimestamp(final String string) {
        final TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(string);
        return newTimestamp(OffsetDateTime.from(temporalAccessor));
    }

    public static Timestamp now() {
        return newTimestamp(OffsetDateTime.now());
    }

    public static Timestamp nowIfNull(final Timestamp timestamp) {
        return timestamp == null ? TimestampUtils.now() : timestamp;
    }

    // endregion

    // region conversion

    public static Instant toInstant(final Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    /**
     * @return Instant with same day of year as given @{code timestamp}, but with zero time
     */
    public static Instant toStartOfDayInstant(final Timestamp timestamp) {
        return toInstant(toStartOfDay(timestamp));
    }

    public static OffsetDateTime toOffsetDateTime(final Timestamp timestamp) {
        final Instant instant = toInstant(timestamp);
        return OffsetDateTime.ofInstant(instant, DEFAULT_OFFSET);
    }

    public static Date toDate(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return Date.from(toInstant(timestamp));
    }

    public static Duration toDuration(final Timestamp from, final Timestamp to) {
        final long nanosDiff = (to.getSeconds() - from.getSeconds()) * NANOS_PER_SECOND + to.getNanos() - from.getNanos();
        return Duration.ofNanos(nanosDiff);
    }

    public static String toOffsetDateTimeString(final Timestamp timestamp) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(toOffsetDateTime(timestamp));
    }

    // endregion

    // region comparison

    public static int compare(final Timestamp left, final Timestamp right) {
        return left.getSeconds() == right.getSeconds()
                ? Integer.compare(left.getNanos(), right.getNanos())
                : Long.compare(left.getSeconds(), right.getSeconds());
    }

    public static boolean isBefore(final Timestamp left, final Timestamp right) {
        return left.getSeconds() < right.getSeconds() || left.getSeconds() == right.getSeconds() && left.getNanos() < right.getNanos();
    }

    public static boolean isAfter(final Timestamp left, final Timestamp right) {
        return left.getSeconds() > right.getSeconds() || left.getSeconds() == right.getSeconds() && left.getNanos() > right.getNanos();
    }

    /**
     * @throws IllegalArgumentException when given {@code timestamp} is not null and after given {@code now}
     */
    public static void assertNotFuture(@Nullable final Timestamp timestamp, final Timestamp now, final String name) {
        if (timestamp != null && isAfter(timestamp, now)) {
            final OffsetDateTime dateTime = toOffsetDateTime(timestamp);
            final OffsetDateTime nowDateTime = toOffsetDateTime(now);
            final String message = String.format("'%s' (%s) can't be in future. Now is %s", name, dateTime, nowDateTime);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @throws IllegalArgumentException when given {@code timestamp} is not null and after given now
     */
    public static void assertNotFuture(@Nullable final Timestamp timestamp, final String name) {
        final Timestamp now = now();
        if (timestamp != null && isAfter(timestamp, now)) {
            final String timestampString = toOffsetDateTimeString(timestamp);
            final String nowString = toOffsetDateTimeString(now);
            final String message = String.format("'%s' (%s) can't be in future. Now is %s", name, timestampString, nowString);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @return timestamps which match given {@code expression} and between given {@code from} inclusively and {@code to} exclusively
     */
    public static List<Timestamp> getCronHitsBetweenDates(final CronExpression expression, final Timestamp from, final Timestamp to) {
        final Supplier<String> messageSupplier = () ->
                "from [" + TimestampUtils.toOffsetDateTimeString(from) + "] must be before to [" + TimestampUtils.toOffsetDateTimeString(to) + "]";
        Assert.isTrue(isBefore(from, to), messageSupplier);

        final Date dateFrom = toDate(from);
        final Date dateTo = toDate(to);

        final List<Timestamp> hits = new ArrayList<>();
        if (expression.isSatisfiedBy(dateFrom)) {
            hits.add(newTimestamp(dateFrom));
        }

        Date currentValidDate = expression.getNextValidTimeAfter(dateFrom);
        while (currentValidDate.before(dateTo)) {
            hits.add(newTimestamp(currentValidDate));
            currentValidDate = expression.getNextValidTimeAfter(currentValidDate);
        }

        return hits;
    }

    /**
     * @return true if given {@code timestamp} is in interval {@code [from; to)}
     */
    public static boolean isInInterval(final Timestamp timestamp, final Instant from, final Instant to) {
        Assert.isTrue(from.isBefore(to), "From must be before to");

        final Instant instant = toInstant(timestamp);
        return !instant.isBefore(from) && instant.isBefore(to);
    }

    /**
     * @return true, if dates of instants of given {@code timestamp1} and {@code timestamp2} are equal, or else false
     */
    public static boolean equalDates(final Timestamp timestamp1, final Timestamp timestamp2) {
        if (timestamp1 == null) {
            return timestamp2 == null;
        } else if (timestamp2 == null) {
            return false;
        }

        return getStartOfDaySeconds(timestamp1) == getStartOfDaySeconds(timestamp2);
    }

    /**
     * @return earliest timestamp of parameters.
     * <br/>
     * If parameters are equal, returns {@code timestamp1}.
     * <br/>
     * If one of parameters is null, returns another.
     */
    public static Timestamp getEarliest(final Timestamp timestamp1, final Timestamp timestamp2) {
        if (timestamp1 == null) {
            return timestamp2;
        } else if (timestamp2 == null) {
            return timestamp1;
        } else {
            return isAfter(timestamp1, timestamp2) ? timestamp2 : timestamp1;
        }
    }

    /**
     * @return latest timestamp of parameters. If parameters are equal, returns {@code timestamp1}.
     * If one of parameters is null, returns another.
     */
    public static Timestamp getLatest(final Timestamp timestamp1, final Timestamp timestamp2) {
        if (timestamp1 == null) {
            return timestamp2;
        } else if (timestamp2 == null) {
            return timestamp1;
        } else {
            return isBefore(timestamp1, timestamp2) ? timestamp2 : timestamp1;
        }
    }

    /**
     * @return average timestamp between given {@code timestamp1} and {@code timestamp2}
     */
    public static Timestamp getAverage(final Timestamp timestamp1, final Timestamp timestamp2) {
        final long seconds = (timestamp1.getSeconds() + timestamp2.getSeconds()) / 2;
        final int nanos = (timestamp1.getNanos() + timestamp2.getNanos()) / 2;
        return newTimestamp(seconds, nanos);
    }

    // endregion

    /**
     * @return true if passed date is for Monday, Tuesday, Wednesday, Thursday or Friday, or else false
     */
    public static boolean isWorkDay(final Timestamp timestamp) {
        final DayOfWeek dayOfWeek = toOffsetDateTime(timestamp).getDayOfWeek();
        return dayOfWeek.compareTo(DayOfWeek.SATURDAY) < 0;
    }

    // region time move

    public static Timestamp plus(final Timestamp timestamp, final Duration duration) {
        return plusNanos(timestamp, duration.toNanos());
    }

    public static Timestamp plusNanos(final Timestamp timestamp, final long amountToAdd) {
        long nanosSum = timestamp.getNanos() + amountToAdd;
        long seconds = timestamp.getSeconds() + nanosSum / NANOS_PER_SECOND;
        if (nanosSum < 0) {
            nanosSum = nanosSum % NANOS_PER_SECOND + NANOS_PER_SECOND;
            seconds--;
        }
        final int nanos = (int) (nanosSum % NANOS_PER_SECOND);
        return newTimestamp(seconds, nanos);
    }

    public static Timestamp plusMinutes(final Timestamp timestamp, final long amountToAdd) {
        return newTimestamp(timestamp.getSeconds() + amountToAdd * 60, timestamp.getNanos());
    }

    public static Timestamp plusHours(final Timestamp timestamp, final long amountToAdd) {
        return newTimestamp(timestamp.getSeconds() + amountToAdd * 3600, timestamp.getNanos());
    }

    /**
     * @return a copy of this Timestamp with the specified number of days added.<br/>
     * Works similar to {@link OffsetDateTime#plusDays}
     */
    public static Timestamp plusDays(final Timestamp timestamp, final int amountToAdd) {
        return newTimestamp(timestamp.getSeconds() + (long) amountToAdd * SECONDS_PER_DAY, timestamp.getNanos());
    }

    public static Timestamp plusWeeks(final Timestamp timestamp, final int amountToAdd) {
        return plusDays(timestamp, 7 * amountToAdd);
    }

    /**
     * @return a copy of this Timestamp with the specified number of years added.<br/>
     * Works similar to {@link OffsetDateTime#plusYears}
     */
    public static Timestamp plusYears(final Timestamp timestamp, final int amountToAdd) {
        final OffsetDateTime dateTime = toOffsetDateTime(timestamp).plusYears(amountToAdd);
        return newTimestamp(dateTime);
    }

    /**
     * Changes time of given {@code timestamp} to given {@code time}.<br/>
     * Date of the result remains the same considering {@link DateUtils#DEFAULT_OFFSET}.
     *
     * @return timestamp with updated time
     */
    public static Timestamp setTime(final Timestamp timestamp, final OffsetTime time) {
        final long totalNanos = time.getHour() * NANOS_PER_HOUR
                + time.getMinute() * NANOS_PER_MINUTE
                + ((long) time.getSecond() - time.getOffset().getTotalSeconds()) * NANOS_PER_SECOND
                + time.getNano();
        final long timestampSeconds = timestamp.getSeconds() + DateUtils.DEFAULT_OFFSET.getTotalSeconds();
        final long seconds = timestampSeconds - timestampSeconds % SECONDS_PER_DAY + totalNanos / NANOS_PER_SECOND;
        final int nanos = (int) (totalNanos % NANOS_PER_SECOND);
        return newTimestamp(seconds, nanos);
    }

    /**
     * @return yyyy.01.01 00:00:00.000000000 with {@link DateUtils#DEFAULT_OFFSET} where:<br/>
     * yyyy - year of given {@code timestamp}
     */
    public static Timestamp toStartOfDay(final Timestamp timestamp) {
        return newTimestamp(getStartOfDaySeconds(timestamp), 0);
    }

    /**
     * @return yyyy.MM.DD 23:59:59.{@value NANOS_MAX_VALUE} with {@link DateUtils#DEFAULT_OFFSET} where:<br/>
     * yyyy - year of given {@code timestamp}<br/>
     * MM - month of given {@code timestamp}<br/>
     * DD - month of given {@code timestamp}<br/>
     */
    public static Timestamp toEndOfDay(final Timestamp timestamp) {
        final long seconds = getStartOfDaySeconds(timestamp) + SECONDS_PER_DAY - 1;
        return newTimestamp(seconds, NANOS_MAX_VALUE);
    }

    private long getStartOfDaySeconds(final Timestamp timestamp) {
        long seconds = timestamp.getSeconds() + DateUtils.DEFAULT_OFFSET.getTotalSeconds();
        return seconds - seconds % SECONDS_PER_DAY
                - DateUtils.DEFAULT_OFFSET.getTotalSeconds();
    }

    /**
     * @return yyyy.01.01 00:00:00.000000000 with {@link DateUtils#DEFAULT_OFFSET} where:<br/>
     * yyyy - year of given {@code timestamp}
     */
    public static Timestamp toStartOfYear(final Timestamp timestamp) {
        OffsetDateTime dateTime = TimestampUtils.toOffsetDateTime(timestamp);
        dateTime = OffsetDateTime.of(dateTime.getYear(), 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
        return newTimestamp(dateTime);
    }

    /**
     * @return yyyy.12.31 23:59:59.{@value NANOS_MAX_VALUE} with {@link DateUtils#DEFAULT_OFFSET} where:<br/>
     * yyyy - year of given {@code timestamp}
     */
    public static Timestamp toEndOfYear(final Timestamp timestamp) {
        OffsetDateTime dateTime = TimestampUtils.toOffsetDateTime(timestamp);
        dateTime = OffsetDateTime.of(dateTime.getYear(), 12, 31, 23, 59, 59, NANOS_MAX_VALUE, DateUtils.DEFAULT_OFFSET);
        return newTimestamp(dateTime);
    }

    // endregion

}