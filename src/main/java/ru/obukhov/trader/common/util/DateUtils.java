package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
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

    public static OffsetDateTime now() {
        return OffsetDateTime.now().withOffsetSameInstant(DEFAULT_OFFSET);
    }

    public static Interval getIntervalWithDefaultOffsets(@Nullable final OffsetDateTime from, @Nullable final OffsetDateTime to) {
        final OffsetDateTime innerFrom = from == null ? null : DateUtils.setDefaultOffsetSameInstant(from);
        final OffsetDateTime innerTo = to == null ? null : DateUtils.setDefaultOffsetSameInstant(to);

        return Interval.of(innerFrom, innerTo);
    }

    public static boolean isWorkDay(final OffsetDateTime date) {
        final DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    public static OffsetDateTime getNextWorkDay(final OffsetDateTime dateTime) {
        final DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        final int daysAfterLastWorkDay = dayOfWeek.getValue() - DayOfWeek.FRIDAY.getValue();
        final int adjustment = daysAfterLastWorkDay < 0
                ? 1
                : 3 - daysAfterLastWorkDay;
        return dateTime.plusDays(adjustment);
    }

    public static OffsetDateTime getEarliestDateTime(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        if (dateTime1 == null) {
            return dateTime2;
        } else if (dateTime2 == null) {
            return dateTime1;
        } else {
            return dateTime1.isAfter(dateTime2) ? dateTime2 : dateTime1;
        }
    }

    public static OffsetDateTime getLatestDateTime(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        if (dateTime1 == null) {
            return dateTime2;
        } else if (dateTime2 == null) {
            return dateTime1;
        } else {
            return dateTime1.isBefore(dateTime2) ? dateTime2 : dateTime1;
        }
    }

    public static OffsetDateTime getAverage(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        final Duration halfOfDuration = Duration.between(dateTime1, dateTime2).dividedBy(2);
        return dateTime1.plus(halfOfDuration);
    }

    public static OffsetDateTime setTime(final OffsetDateTime dateTime, final OffsetTime time) {
        return dateTime.withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(time.getSecond())
                .withNano(time.getNano());
    }

    public static OffsetDateTime setDefaultOffsetSameInstant(final OffsetDateTime dateTime) {
        return dateTime.withOffsetSameInstant(DEFAULT_OFFSET);
    }

    public static boolean isAfter(final OffsetDateTime dateTime1, final OffsetDateTime dateTime2) {
        return dateTime2 == null || dateTime1.isAfter(dateTime2);
    }

    public static OffsetDateTime toStartOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    public static OffsetDateTime toEndOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

    public static OffsetDateTime toStartOf2Days(final OffsetDateTime dateTime) {
        final OffsetDateTime start = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
        final long daysCount = Duration.between(start, dateTime).toDays();
        final OffsetDateTime startOfDay = toStartOfDay(dateTime);
        return (daysCount & 1) == 1 ? startOfDay : startOfDay.minusDays(1);
    }

    public static OffsetDateTime toEndOf2Days(final OffsetDateTime dateTime) {
        final OffsetDateTime start = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, DateUtils.DEFAULT_OFFSET);
        final long daysCount = Duration.between(start, dateTime).toDays();
        final OffsetDateTime endOfDay = toEndOfDay(dateTime);
        return (daysCount & 1) == 1 ? endOfDay.plusDays(1) : endOfDay;
    }

    public static OffsetDateTime toStartOfWeek(final OffsetDateTime dateTime) {
        return toStartOfDay(dateTime).minusDays(dateTime.getDayOfWeek().getValue() - 1L);
    }

    public static OffsetDateTime toEndOfWeek(final OffsetDateTime dateTime) {
        return toEndOfDay(dateTime).plusDays(7L - dateTime.getDayOfWeek().getValue());
    }

    public static OffsetDateTime toStartOfMonth(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), 1,
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    public static OffsetDateTime toEndOfMonth(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), dateTime.getMonthValue(), YearMonth.from(dateTime).lengthOfMonth(),
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

    public static OffsetDateTime toStartOfYear(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), 1, 1,
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    public static OffsetDateTime toEndOfYear(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(
                dateTime.getYear(), 12, 31,
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

    public static OffsetDateTime toStartOf2Years(final OffsetDateTime dateTime) {
        final int year = dateTime.getYear() - 1 + dateTime.getYear() % 2;
        return OffsetDateTime.of(
                year, 1, 1,
                0, 0, 0,
                0, dateTime.getOffset()
        );
    }

    public static OffsetDateTime toEndOf2Years(final OffsetDateTime dateTime) {
        final int year = dateTime.getYear() + dateTime.getYear() % 2;
        return OffsetDateTime.of(
                year, 12, 31,
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

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

    public static OffsetDateTime toEndOfDecade(final OffsetDateTime dateTime) {
        final int lastDigit = dateTime.getYear() % 10;
        final int year = dateTime.getYear() + (lastDigit == 0 ? 0 : 10 - lastDigit);
        return OffsetDateTime.of(
                year, 12, 31,
                23, 59, 59,
                999_999_999, dateTime.getOffset()
        );
    }

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

    public static boolean timestampIsInInterval(final Timestamp timestamp, final Instant from, final Instant to) {
        Assert.isTrue(from.isBefore(to), "From must be before to");

        final Instant time = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return !time.isBefore(from) && time.isBefore(to);
    }

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