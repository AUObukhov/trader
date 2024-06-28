package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@UtilityClass
public class DateUtils {

    public static final double DAYS_IN_YEAR = 365.25;
    public static final long NANOSECONDS_PER_DAY = 24L * 60L * 60L * 1000_000_000L;
    public static final long NANOSECONDS_PER_YEAR = (long) (DAYS_IN_YEAR * NANOSECONDS_PER_DAY);

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Europe/Moscow");
    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.ofTotalSeconds(DEFAULT_TIME_ZONE.getRawOffset() / 1000);

    public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String OFFSET_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(OFFSET_DATE_TIME_FORMAT);
    public static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");

    /**
     * @return current dateTime with offset {@link DateUtils#DEFAULT_OFFSET}
     */
    public static OffsetDateTime now() {
        return OffsetDateTime.now().withOffsetSameInstant(DEFAULT_OFFSET);
    }

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
     * Null safe extension of {@link OffsetDateTime#isAfter}
     *
     * @return true, if {@code dateTime2} is null, or else result of {@link OffsetDateTime#isAfter}
     */
    public static boolean isAfter(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        return dateTime2 == null || dateTime1.isAfter(dateTime2);
    }

    /**
     * @return value of given {@code dateTime} with minimum month, day, hours, minutes, seconds and nanos of it's day
     */
    public static OffsetDateTime toStartOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    /**
     * @return value of given {@code dateTime} with maximum hours, minutes, seconds and nanos of this date
     */
    public static OffsetDateTime toEndOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

    /**
     * @param dateTime must be positive year, otherwise the result will be wrong.
     * @return value of given {@code dateTime} with minimum month, day, hours, minutes, seconds and nanos of it's 2 days.
     * Couple of days are starting since day of 0001-01-01T00:00:00.000000000 with {@link DateUtils#DEFAULT_OFFSET} offset.
     */
    public static OffsetDateTime toStartOf2Days(final OffsetDateTime dateTime) {
        final OffsetDateTime start = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
        final long daysCount = Duration.between(start, dateTime).toDays();
        final OffsetDateTime startOfDay = toStartOfDay(dateTime);
        return (daysCount & 1) == 1 ? startOfDay : startOfDay.minusDays(1);
    }

    /**
     * @param dateTime must be positive year, otherwise the result will be wrong.
     * @return value of given {@code dateTime} with maximum month, day, hours, minutes, seconds and nanos of it's 2 days.
     * Couples of days are starting since 0001-01-01T00:00:00.000000000 with {@link DateUtils#DEFAULT_OFFSET} offset.
     */
    public static OffsetDateTime toEndOf2Days(final OffsetDateTime dateTime) {
        final OffsetDateTime start = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
        final long daysCount = Duration.between(start, dateTime).toDays();
        final OffsetDateTime endOfDay = toEndOfDay(dateTime);
        return (daysCount & 1) == 1 ? endOfDay.plusDays(1) : endOfDay;
    }

    /**
     * @return dateTime in the beginning of Monday of week of given {@code dateTime}
     */
    public static OffsetDateTime toStartOfWeek(@NotNull final OffsetDateTime dateTime) {
        return toStartOfDay(dateTime).minusDays(dateTime.getDayOfWeek().getValue() - 1L);
    }

    /**
     * @return dateTime in the end of week of given {@code dateTime}
     */
    public static OffsetDateTime toEndOfWeek(final OffsetDateTime dateTime) {
        return toEndOfDay(dateTime).plusDays(7L - dateTime.getDayOfWeek().getValue());
    }

    /**
     * @return dateTime in the beginning of month of given {@code dateTime}
     */
    public static OffsetDateTime toStartOfMonth(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), 1,
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    /**
     * @return dateTime in the end of month of {@code dateTime}
     */
    public static OffsetDateTime toEndOfMonth(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), YearMonth.from(dateTime).lengthOfMonth(),
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

    /**
     * @return value of given {@code dateTime} with minimum month, day, hours, minutes, seconds and nanos of this year
     */
    public static OffsetDateTime toStartOfYear(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), 1, 1,
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    /**
     * @return value of given {@code dateTime} with maximum month, day, hours, minutes, seconds and nanos of this year
     */
    public static OffsetDateTime toEndOfYear(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), 12, 31,
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

    /**
     * @param dateTime must be positive, otherwise the result will be wrong.
     * @return value of given {@code dateTime} with minimum month, day, hours, minutes, seconds and nanos of this 2 years.
     * Couple of years is odd year and next even year.
     */
    public static OffsetDateTime toStartOf2Years(final OffsetDateTime dateTime) {
        final int year = dateTime.getYear() - 1 + dateTime.getYear() % 2;
        return OffsetDateTime.of(
                year, 1, 1,
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    /**
     * @param dateTime must be positive, otherwise the result will be wrong.
     * @return value of given {@code dateTime} with maximum month, day, hours, minutes, seconds and nanos of this 2 years.
     * Couple of years is odd year and next even year.
     */
    public static OffsetDateTime toEndOf2Years(final OffsetDateTime dateTime) {
        final int year = dateTime.getYear() + dateTime.getYear() % 2;
        return OffsetDateTime.of(
                year, 12, 31,
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

    /**
     * @param dateTime must be positive, otherwise the result will be wrong.
     * @return value of given {@code dateTime} with minimum month, day, hours, minutes, seconds and nanos of this decade.
     * Decade starts with year ###1.
     */
    public static OffsetDateTime toStartOfDecade(final OffsetDateTime dateTime) {
        final int year = dateTime.getYear() % 10 == 0
                ? dateTime.getYear() - 9
                : (dateTime.getYear() / 10) * 10 + 1;
        return OffsetDateTime.of(
                year, 1, 1,
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    /**
     * @param dateTime must be positive, otherwise the result will be wrong.
     * @return value of given {@code dateTime} with maximum month, day, hours, minutes, seconds and nanos of this decade.
     * Decade starts with year ###1.
     */
    public static OffsetDateTime toEndOfDecade(final OffsetDateTime dateTime) {
        final int lastDigit = dateTime.getYear() % 10;
        final int year = dateTime.getYear() + (lastDigit == 0 ? 0 : 10 - lastDigit);
        return OffsetDateTime.of(
                year, 12, 31,
                23, 59, 59,
                999_999_999, dateTime.getOffset()
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
     * @return dates which match given {@code expression} and between given {@code from} inclusively and {@code to} exclusively.
     * If given {@code expression} is null, returns empty list.
     * @throws IllegalArgumentException if given {@code from} is not before given {@code to}
     */
    public static List<OffsetDateTime> getCronHitsBetweenDates(
            @Nullable final CronExpression expression,
            final OffsetDateTime from,
            final OffsetDateTime to
    ) {
        Assert.isTrue(from.isBefore(to), () -> "from [" + from + "] must be before to [" + to + "]");

        final List<OffsetDateTime> hits = new ArrayList<>();

        if (expression == null) {
            return hits;
        }

        OffsetDateTime currentValidDateTime = expression.next(from.minusNanos(1));
        while (currentValidDateTime != null && currentValidDateTime.isBefore(to)) {
            hits.add(currentValidDateTime);
            currentValidDateTime = expression.next(currentValidDateTime);
        }

        return hits;
    }

    /**
     * @return true if given {@code timestamp} is in interval {@code [from; to)}
     * @throws IllegalArgumentException if given {@code from} is not before given {@code to}
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

    public static OffsetDateTime getCandleEndTime(final OffsetDateTime candleTime, final CandleInterval candleInterval) {
        return switch (candleInterval) {
            case CANDLE_INTERVAL_1_MIN -> candleTime.plusMinutes(1);
            case CANDLE_INTERVAL_2_MIN -> candleTime.plusMinutes(2);
            case CANDLE_INTERVAL_3_MIN -> candleTime.plusMinutes(3);
            case CANDLE_INTERVAL_5_MIN -> candleTime.plusMinutes(5);
            case CANDLE_INTERVAL_10_MIN -> candleTime.plusMinutes(10);
            case CANDLE_INTERVAL_15_MIN -> candleTime.plusMinutes(15);
            case CANDLE_INTERVAL_30_MIN -> candleTime.plusMinutes(30);
            case CANDLE_INTERVAL_HOUR -> candleTime.plusHours(1);
            case CANDLE_INTERVAL_2_HOUR -> candleTime.plusHours(2);
            case CANDLE_INTERVAL_4_HOUR -> candleTime.plusHours(4);
            case CANDLE_INTERVAL_DAY -> candleTime.plusDays(1);
            case CANDLE_INTERVAL_WEEK -> candleTime.plusWeeks(1);
            case CANDLE_INTERVAL_MONTH -> candleTime.plusMonths(1);
            default -> throw new IllegalArgumentException("Unexpected candle interval " + candleInterval);
        };
    }

    public static LocalDateTime toLocalDateTime(final OffsetDateTime dateTime) {
        final int secondsAdjustment = TimeZone.getTimeZone(ZoneId.systemDefault()).getRawOffset() / 1000 - dateTime.getOffset().getTotalSeconds();
        return dateTime.toLocalDateTime().plusSeconds(secondsAdjustment);
    }

}