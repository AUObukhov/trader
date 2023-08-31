package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.quartz.CronExpression;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@UtilityClass
public class DateUtils {

    public static final double DAYS_IN_YEAR = 365.25;

    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("+03:00");

    public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String OFFSET_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");

    /**
     * @return Interval with given {@code from} and {@code to}, but with offset {@link DateUtils#DEFAULT_OFFSET}
     */
    public static Interval getIntervalWithDefaultOffsets(@Nullable final OffsetDateTime from, @Nullable final OffsetDateTime to) {
        final OffsetDateTime innerFrom = from == null ? null : DateUtils.setDefaultOffsetSameInstant(from);
        final OffsetDateTime innerTo = to == null ? null : DateUtils.setDefaultOffsetSameInstant(to);

        return Interval.of(innerFrom, innerTo);
    }

    /**
     * @return true if passed date is for Monday, Tuesday, Wednesday, Thursday or Friday, or else false
     */
    public static boolean isWorkDay(final OffsetDateTime date) {
        final DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * @return next work day after given {@code dateTime}
     */
    public static OffsetDateTime getNextWorkDay(final OffsetDateTime dateTime) {
        final DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        final int daysAfterLastWorkDay = dayOfWeek.getValue() - DayOfWeek.FRIDAY.getValue();
        final int adjustment = daysAfterLastWorkDay < 0
                ? 1
                : 3 - daysAfterLastWorkDay;
        return dateTime.plusDays(adjustment);
    }


    /**
     * @return earliest dateTime of parameters. If parameters are equal, returns {@code dateTime1}.
     * If one of parameters is null, returns another.
     */
    public static OffsetDateTime getEarliestDateTime(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        if (dateTime1 == null) {
            return dateTime2;
        } else if (dateTime2 == null) {
            return dateTime1;
        } else {
            return dateTime1.isAfter(dateTime2) ? dateTime2 : dateTime1;
        }
    }

    /**
     * @return latest dateTime of parameters. If parameters are equal, returns {@code dateTime1}.
     * If one of parameters is null, returns another.
     */
    public static OffsetDateTime getLatestDateTime(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        if (dateTime1 == null) {
            return dateTime2;
        } else if (dateTime2 == null) {
            return dateTime1;
        } else {
            return dateTime1.isBefore(dateTime2) ? dateTime2 : dateTime1;
        }
    }

    /**
     * @return average dateTime between given {@code dateTime1} and {@code dateTime2}
     */
    public static OffsetDateTime getAverage(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        final Duration halfOfDuration = Duration.between(dateTime1, dateTime2).dividedBy(2);
        return dateTime1.plus(halfOfDuration);
    }

    /**
     * Sets values of time fields of given {@code time} to time fields of given {@code dateTime}
     *
     * @return dateTime with updated time fields values
     */
    public static OffsetDateTime setTime(final OffsetDateTime dateTime, final OffsetTime time) {
        return dateTime.withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(time.getSecond())
                .withNano(time.getNano());
    }

    /**
     * @return dateTime with same instant as in given {@code dateTime},
     * but with offset equals to {@link DateUtils#DEFAULT_OFFSET}
     */
    public static OffsetDateTime setDefaultOffsetSameInstant(final OffsetDateTime dateTime) {
        return dateTime.withOffsetSameInstant(DEFAULT_OFFSET);
    }

    /**
     * @return {@link ChronoUnit#DAYS} when {@code candleInterval) is less than day, or else {@link ChronoUnit#YEARS}
     */
    public static ChronoUnit getPeriodByCandleInterval(final CandleInterval candleInterval) {
        return candleInterval == CandleInterval.CANDLE_INTERVAL_DAY
                ? ChronoUnit.YEARS
                : ChronoUnit.DAYS;
    }

    /**
     * Null safe extension of {@link OffsetDateTime#isAfter}
     *
     * @return true, if {@code dateTime2} is null, or else result of {@link OffsetDateTime#isAfter}
     */
    public static boolean isAfter(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        return dateTime2 == null || dateTime1.isAfter(dateTime2);
    }

    /**
     * @return value of given {@code dateTime} with maximum hours, minutes, seconds and nanos of this date
     */
    public static OffsetDateTime toEndOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                OffsetTime.MAX.getHour(), OffsetTime.MAX.getMinute(), OffsetTime.MAX.getSecond(),
                OffsetTime.MAX.getNano(), dateTime.getOffset()
        );
    }

    /**
     * @return value of given {@code dateTime} with minimum month, day, hours, minutes, seconds and nanos of this date
     */
    public static OffsetDateTime toStartOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    /**
     * @return value of given {@code dateTime} with minimum month, day, hours, minutes, seconds and nanos of this date
     */
    public static OffsetDateTime atStartOfYear(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(dateTime.getYear(), 1, 1, 0, 0, 0, 0, dateTime.getOffset());
    }

    /**
     * @return value of given {@code dateTime} with maximum month, day, hours, minutes, seconds and nanos of this date
     */
    public static OffsetDateTime atEndOfYear(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), 12, 31,
                23, 59, 59,
                999999999, dateTime.getOffset()
        );
    }

    /**
     * @throws IllegalArgumentException when given {@code dateTime} is not null and after given {@code now}
     */
    public static void assertDateTimeNotFuture(@Nullable final OffsetDateTime dateTime, final OffsetDateTime now, final String name) {
        if (dateTime != null && dateTime.isAfter(now)) {
            final String message = String.format("'%s' (%s) can't be in future. Now is %s", name, dateTime, now);
            throw new IllegalArgumentException(message);
        }
    }

    public static Date toDate(final OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        final ZoneOffset zoneOffset = ZoneOffset.of(dateTime.getOffset().getId());
        final Instant instant = dateTime.toLocalDateTime().toInstant(zoneOffset);
        return Date.from(instant);
    }

    public static OffsetDateTime fromDate(final Date date) {
        return date.toInstant().atOffset(DEFAULT_OFFSET);
    }

    /**
     * @return dates which match given {@code expression} and between given {@code from} inclusively and {@code to} exclusively
     */
    public static List<OffsetDateTime> getCronHitsBetweenDates(final CronExpression expression, final OffsetDateTime from, final OffsetDateTime to) {
        Assert.isTrue(from.isBefore(to), () -> "from [" + from + "] must be before to [" + to + "]");

        final Date dateFrom = DateUtils.toDate(from);
        final Date dateTo = DateUtils.toDate(to);

        final List<OffsetDateTime> hits = new ArrayList<>();
        if (expression.isSatisfiedBy(dateFrom)) {
            hits.add(fromDate(dateFrom));
        }

        Date currentValidDate = expression.getNextValidTimeAfter(dateFrom);
        while (currentValidDate.before(dateTo)) {
            hits.add(fromDate(currentValidDate));
            currentValidDate = expression.getNextValidTimeAfter(currentValidDate);
        }

        return hits;
    }

    /**
     * @return true if given {@code timestamp} is in interval {@code [from; to)}
     */
    public static boolean timestampIsInInterval(final Timestamp timestamp, final Instant from, final Instant to) {
        Assert.isTrue(from.isBefore(to), "From must be before to");

        final Instant time = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return !time.isBefore(from) && time.isBefore(to);
    }

    /**
     * @return Instant with same day of year as given @{code dateTime}, but with zero time and offset
     */
    public static Instant toSameDayInstant(final OffsetDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.DAYS)
                .withOffsetSameLocal(ZoneOffset.UTC)
                .toInstant();
    }

    /**
     * @return true, if dates of instants of given {@code dateTime1} and {@code dateTime2} are equal, or else false
     */
    public static boolean equalDates(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        if (dateTime1 == null) {
            return dateTime2 == null;
        } else if (dateTime2 == null) {
            return false;
        }

        final OffsetDateTime adjustedDateTime1 = dateTime1.withOffsetSameInstant(DateUtils.DEFAULT_OFFSET);
        final OffsetDateTime adjustedDateTime2 = dateTime2.withOffsetSameInstant(DateUtils.DEFAULT_OFFSET);

        return adjustedDateTime1.getYear() == adjustedDateTime2.getYear()
                && adjustedDateTime1.getDayOfYear() == adjustedDateTime2.getDayOfYear();
    }

}