package ru.obukhov.trader.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.quartz.CronExpression;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    public static final double DAYS_IN_YEAR = 365.25;

    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("+03:00");

    public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss");

    /**
     * @return OffsetDateTime with by params, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime getDateTime(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second
    ) {
        return OffsetDateTime.of(
                year,
                month,
                dayOfMonth,
                hour,
                minute,
                second,
                0,
                DEFAULT_OFFSET
        );
    }

    /**
     * @return OffsetDateTime with by params and UTC zone
     */
    public static OffsetDateTime getDateTime(
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int minute,
            final int second,
            final int nanoOfSecond
    ) {
        return OffsetDateTime.of(
                year,
                month,
                dayOfMonth,
                hour,
                minute,
                second,
                nanoOfSecond,
                DEFAULT_OFFSET
        );
    }

    /**
     * @return OffsetDateTime with by params, 0 hours, 0 minutes, 0 seconds, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime getDate(final int year, final int month, final int dayOfMonth) {
        return getDateTime(year, month, dayOfMonth, 0, 0, 0);
    }

    /**
     * @return OffsetDateTime with by params, 0 year, 1 month, 1 day of month, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime getTime(final int hour, final int minute, final int second) {
        return getDateTime(0, 1, 1, hour, minute, second);
    }

    /**
     * @return Interval with given {@code from} and {@code to}, but with offset {@link DateUtils#DEFAULT_OFFSET}
     */
    public static Interval getIntervalWithDefaultOffsets(
            @Nullable final OffsetDateTime from,
            @Nullable final OffsetDateTime to
    ) {
        final OffsetDateTime innerFrom = from == null ? null : DateUtils.setDefaultOffsetSameInstant(from);
        final OffsetDateTime innerTo = to == null ? null : DateUtils.setDefaultOffsetSameInstant(to);

        return Interval.of(innerFrom, innerTo);
    }

    /**
     * @return passed value if it is not null or current dateTime otherways
     */
    public static OffsetDateTime getDefaultToIfNull(final OffsetDateTime to) {
        return ObjectUtils.defaultIfNull(to, OffsetDateTime.now());
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
     * @return last work day not after today
     */
    public static OffsetDateTime getLastWorkDay() {
        return getLastWorkDay(OffsetDateTime.now());
    }

    /**
     * @return last work day not after given {@code dateTime}
     */
    public static OffsetDateTime getLastWorkDay(final OffsetDateTime dateTime) {
        OffsetDateTime date = dateTime.truncatedTo(ChronoUnit.DAYS);
        while (!isWorkDay(date)) {
            date = date.minusDays(1);
        }
        return date;
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
        Duration halfOfDuration = Duration.between(dateTime1, dateTime2).dividedBy(2);
        return dateTime1.plus(halfOfDuration);
    }

    /**
     * Same as {@link OffsetDateTime#plus}, but if result is after {@code maxDateTime}, then returns {@code maxDateTime}
     */
    public static OffsetDateTime plusLimited(
            final OffsetDateTime dateTime,
            final long amountToAdd,
            final TemporalUnit temporalUnit,
            final @Nullable OffsetDateTime maxDateTime
    ) {
        return DateUtils.getEarliestDateTime(dateTime.plus(amountToAdd, temporalUnit), maxDateTime);
    }

    /**
     * Same as {@link OffsetDateTime#minus}, but if result is before {@code minDateTime}, then returns {@code minDateTime}
     */
    public static OffsetDateTime minusLimited(
            final OffsetDateTime dateTime,
            final long amountToSubtract,
            final TemporalUnit temporalUnit,
            final @Nullable OffsetDateTime minDateTime
    ) {
        return DateUtils.getLatestDateTime(dateTime.minus(amountToSubtract, temporalUnit), minDateTime);
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
     * @return {@link ChronoUnit#DAYS} when {@code candleResolution) is less than day, or else {@link ChronoUnit#YEARS}
     */
    public static ChronoUnit getPeriodByCandleResolution(final CandleResolution candleResolution) {
        return candleResolution == CandleResolution.DAY
                || candleResolution == CandleResolution.WEEK
                || candleResolution == CandleResolution.MONTH
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

    public static OffsetDateTime roundUpToDay(final OffsetDateTime dateTime) {
        OffsetDateTime date = dateTime.truncatedTo(ChronoUnit.DAYS);
        if (!date.equals(dateTime)) {
            date = date.plusDays(1);
        }

        return date;
    }

    /**
     * Moves given {@code dateTime} to start of it's year.
     * If given {@code dateTime} is already start of year, not changes it
     *
     * @return moved date
     */
    public static OffsetDateTime roundDownToYear(final OffsetDateTime dateTime) {
        return dateTime.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Moves given {@code dateTime} to start of next year.
     * If given {@code dateTime} is already start of year, not changes it
     *
     * @return moved date
     */
    public static OffsetDateTime roundUpToYear(final OffsetDateTime dateTime) {
        OffsetDateTime date = roundDownToYear(dateTime);
        if (!date.equals(dateTime)) {
            date = date.plusYears(1);
        }

        return date;
    }

    /**
     * @param startTime start time of work
     * @param duration  duration or work period
     * @return true if today is work day and current time is between {@code startTime} and {@code startTime + duration}
     */
    public static boolean isWorkTimeNow(final OffsetTime startTime, final Duration duration) {
        return isWorkTime(OffsetDateTime.now(), startTime, duration);
    }

    /**
     * Checks if given {@code dateTime} is work time, which means than it is between {@code workStartTime} included and
     * {@code workStartTime + workTimeDuration} and not at weekend except Saturday, when
     * {@code workStartTime + workTimeDuration} is after midnight
     *
     * @param dateTime         checked dateTime
     * @param workStartTime    start time of work
     * @param workTimeDuration duration of work period, must be positive and less than 1 day
     * @return true if given {@code dateTime} is work time, or else false
     * @throws IllegalArgumentException when {@code workTimeDuration} is not positive or when it 1 day or longer
     */
    public static boolean isWorkTime(
            final OffsetDateTime dateTime,
            final OffsetTime workStartTime,
            final Duration workTimeDuration
    ) {
        validateWorkTimeDuration(workTimeDuration);

        final OffsetTime workEndTime = workStartTime.plus(workTimeDuration);
        final boolean livingAfterMidnight = workStartTime.isAfter(workEndTime);
        final OffsetTime time = dateTime.toOffsetTime();

        if (!isWorkDay(dateTime)) {
            return livingAfterMidnight
                    && DayOfWeek.SATURDAY == dateTime.getDayOfWeek()
                    && time.isBefore(workEndTime);
        }

        if (livingAfterMidnight) {
            return !time.isBefore(workStartTime) || time.isBefore(workEndTime);
        } else {
            return !time.isBefore(workStartTime) && time.isBefore(workEndTime);
        }
    }

    /**
     * @param workStartTime    start time of work
     * @param workTimeDuration duration of work period
     * @return first minute of work time not before {@code dateTime}
     */
    public static OffsetDateTime getNearestWorkTime(
            final OffsetDateTime dateTime,
            final OffsetTime workStartTime,
            final Duration workTimeDuration
    ) {
        validateWorkTimeDuration(workTimeDuration);

        if (isWorkTime(dateTime, workStartTime, workTimeDuration)) {
            return dateTime;
        }

        return toWorkStartTime(dateTime, workStartTime);
    }

    /**
     * @param workStartTime    start time of work
     * @param workTimeDuration duration of work period
     * @return next minute of work time after {@code dateTime}
     */
    public static OffsetDateTime getNextWorkMinute(
            final OffsetDateTime dateTime,
            final OffsetTime workStartTime,
            final Duration workTimeDuration
    ) {
        validateWorkTimeDuration(workTimeDuration);

        if (isWorkTime(dateTime, workStartTime, workTimeDuration)) {
            final OffsetDateTime nextDateTime = dateTime.plusMinutes(1);
            if (isWorkTime(nextDateTime, workStartTime, workTimeDuration)) {
                return nextDateTime;
            }
        }

        return toWorkStartTime(dateTime, workStartTime);
    }

    private static OffsetDateTime toWorkStartTime(final OffsetDateTime dateTime, final OffsetTime workStartTime) {
        if (dateTime.toOffsetTime().isBefore(workStartTime)) {
            return setTime(dateTime, workStartTime);
        }

        return setTime(getNextWorkDay(dateTime), workStartTime);
    }

    private static void validateWorkTimeDuration(final Duration workTimeDuration) {
        Assert.isTrue(workTimeDuration.toNanos() > 0, "workTimeDuration must be positive");
        Assert.isTrue(Duration.ofDays(1).compareTo(workTimeDuration) > 0,
                "workTimeDuration must be less than 1 day");
    }

    /**
     * @return value of given {@code dateTime} with minimum hours, minutes, seconds and nanos of this date
     */
    public static OffsetDateTime atStartOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                OffsetTime.MIN.getHour(), OffsetTime.MIN.getMinute(), OffsetTime.MIN.getSecond(),
                OffsetTime.MIN.getNano(), dateTime.getOffset());
    }

    /**
     * @return value of given {@code dateTime} with maximum hours, minutes, seconds and nanos of this date
     */
    public static OffsetDateTime atEndOfDay(final OffsetDateTime dateTime) {
        return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                OffsetTime.MAX.getHour(), OffsetTime.MAX.getMinute(), OffsetTime.MAX.getSecond(),
                OffsetTime.MAX.getNano(), dateTime.getOffset());
    }

    /**
     * @return dateTime equals to given {@code dateTime}, but with system default offset
     */
    public static OffsetDateTime withDefaultOffset(final OffsetDateTime dateTime) {
        return dateTime.withOffsetSameInstant(DateUtils.DEFAULT_OFFSET);
    }

    /**
     * @throws IllegalArgumentException when given {@code dateTime} is not null and after given {@code now}
     */
    public static void assertDateTimeNotFuture(
            @Nullable final OffsetDateTime dateTime,
            final OffsetDateTime now,
            final String name
    ) {
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

    /**
     * @return number of dates which match given {@code expression} and between given {@code from} exclusively and {@code to}
     * inclusively
     */
    public static int getCronHitsBetweenDates(
            final CronExpression expression,
            final OffsetDateTime from,
            final OffsetDateTime to
    ) {
        Assert.isTrue(from.isBefore(to), "from must be before to");

        final Date dateFrom = DateUtils.toDate(from);
        final Date dateTo = DateUtils.toDate(to);
        final Date finalValidDate = expression.getNextValidTimeAfter(dateTo);

        int count = 0;
        Date currentValidDate = expression.getNextValidTimeAfter(dateFrom);
        while (!currentValidDate.equals(finalValidDate)) {
            count++;
            currentValidDate = expression.getNextValidTimeAfter(currentValidDate);
        }

        return count;
    }

}