package ru.obukhov.investor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    /**
     * Earliest date for requesting candles
     */
    static final OffsetDateTime START_DATE = getDate(2000, 1, 1);

    /**
     * @return OffsetDateTime with by params, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime getDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return OffsetDateTime.of(year, month, dayOfMonth,
                hour, minute, second, 0,
                ZoneOffset.UTC);
    }

    /**
     * @return OffsetDateTime with by params, 0 hours, 0 minutes, 0 seconds, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime getDate(int year, int month, int dayOfMonth) {
        return getDateTime(year, month, dayOfMonth, 0, 0, 0);
    }

    /**
     * @return OffsetDateTime with by params, 0 year, 1 month, 1 day of month, 0 nanoseconds and UTC zone
     */
    public static OffsetDateTime getTime(int hour, int minute, int second) {
        return getDateTime(0, 1, 1, hour, minute, second);
    }

    /**
     * @return passed value if it is not null or else the earliest date for requesting candles
     */
    public static OffsetDateTime getDefaultFromIfNull(OffsetDateTime from) {
        return ObjectUtils.defaultIfNull(from, START_DATE);
    }

    /**
     * @return passed value if it is not null or current dateTime otherways
     */
    public static OffsetDateTime getDefaultToIfNull(OffsetDateTime to) {
        return ObjectUtils.defaultIfNull(to, OffsetDateTime.now());
    }

    /**
     * @return true if passed date is for Monday, Tuesday, Wednesday, Thursday or Friday, or else false
     */
    public static boolean isWorkDay(OffsetDateTime date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    /**
     * @return next work day after given {@code dateTime}
     */
    public static OffsetDateTime getNextWorkDay(OffsetDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        int daysAfterLastWorkDay = dayOfWeek.getValue() - DayOfWeek.FRIDAY.getValue();
        int adjustment = daysAfterLastWorkDay < 0
                ? 1
                : 3 - daysAfterLastWorkDay;
        return dateTime.plusDays(adjustment);
    }

    /**
     * @return last work day not after today
     */
    public static OffsetDateTime getLastWorkDay() {
        OffsetDateTime date = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
        while (!isWorkDay(date)) {
            date = date.minusDays(1);
        }
        return date;
    }

    /**
     * @return earliest dateTime of parameters. If parameters are equal, returns {@code dateTime1}.
     * If one of parameters is null, returns another.
     */
    public static OffsetDateTime getEarliestDateTime(OffsetDateTime dateTime1, OffsetDateTime dateTime2) {
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
    public static OffsetDateTime getLatestDateTime(OffsetDateTime dateTime1, OffsetDateTime dateTime2) {
        if (dateTime1 == null) {
            return dateTime2;
        } else if (dateTime2 == null) {
            return dateTime1;
        } else {
            return dateTime1.isBefore(dateTime2) ? dateTime2 : dateTime1;
        }
    }

    /**
     * Same as {@link OffsetDateTime#plus}, but if result is after {@code maxDateTime}, then returns {@code maxDateTime}
     */
    public static OffsetDateTime plusLimited(OffsetDateTime dateTime,
                                             long amountToAdd,
                                             TemporalUnit temporalUnit,
                                             @Nullable OffsetDateTime maxDateTime) {
        return DateUtils.getEarliestDateTime(dateTime.plus(amountToAdd, temporalUnit), maxDateTime);
    }

    /**
     * Same as {@link OffsetDateTime#minus}, but if result is before {@code minDateTime}, then returns {@code minDateTime}
     */
    public static OffsetDateTime minusLimited(OffsetDateTime dateTime,
                                              long amountToSubstract,
                                              TemporalUnit temporalUnit,
                                              @Nullable OffsetDateTime minDateTime) {
        return DateUtils.getLatestDateTime(dateTime.minus(amountToSubstract, temporalUnit), minDateTime);
    }

    /**
     * Sets values of time fields of given {@code time} to time fields of given {@code dateTime}
     *
     * @return dateTime with updated time fields values
     */
    public static OffsetDateTime setTime(OffsetDateTime dateTime, OffsetTime time) {
        return dateTime.withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(time.getSecond())
                .withNano(time.getNano());
    }

    /**
     * @return {@link ChronoUnit#DAYS} when {@code candleInterval) is less than day, or else {@link ChronoUnit#YEARS}
     */
    public static ChronoUnit getPeriodByCandleInterval(CandleInterval candleInterval) {
        return candleInterval == CandleInterval.DAY
                || candleInterval == CandleInterval.WEEK
                || candleInterval == CandleInterval.MONTH
                ? ChronoUnit.YEARS
                : ChronoUnit.DAYS;
    }

    /**
     * Null safe extention of {@link OffsetDateTime#isAfter}
     *
     * @return true, if {@code dateTime2} is null, or else result of {@link OffsetDateTime#isAfter}
     */
    public static boolean isAfter(OffsetDateTime dateTime1, OffsetDateTime dateTime2) {
        return dateTime2 == null || dateTime1.isAfter(dateTime2);
    }

    public static OffsetDateTime roundUpToDay(OffsetDateTime dateTime) {
        OffsetDateTime date = dateTime.truncatedTo(ChronoUnit.DAYS);
        if (!date.equals(dateTime)) {
            date = date.plusDays(1);
        }

        return date;
    }

    /**
     * Moves given {@code dateTime} to start of next year.
     * If given {@code dateTime} is already start of year, not changes it
     *
     * @return moved date
     */
    public static OffsetDateTime roundUpToYear(OffsetDateTime dateTime) {
        OffsetDateTime date = dateTime.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
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
    public static boolean isWorkTimeNow(OffsetTime startTime, Duration duration) {
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
    public static boolean isWorkTime(OffsetDateTime dateTime, OffsetTime workStartTime, Duration workTimeDuration) {
        Assert.isTrue(workTimeDuration.toNanos() > 0, "workTimeDuration must be positive");
        Assert.isTrue(Duration.ofDays(1).compareTo(workTimeDuration) > 0,
                "workTimeDuration must be less than 1 day");

        OffsetTime workEndTime = workStartTime.plus(workTimeDuration);
        boolean livingAfterMidnight = workStartTime.isAfter(workEndTime);
        OffsetTime time = dateTime.toOffsetTime();

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
     * @param dateTime
     * @param workStartTime    start time of work
     * @param workTimeDuration duration of work period
     * @return first minute of work time not before {@code dateTime}
     */
    public static OffsetDateTime getNearestWorkTime(OffsetDateTime dateTime,
                                                    OffsetTime workStartTime,
                                                    Duration workTimeDuration) {

        Assert.isTrue(workTimeDuration.toNanos() > 0, "workTimeDuration must be positive");
        Assert.isTrue(Duration.ofDays(1).compareTo(workTimeDuration) >= 0,
                "workTimeDuration must be less than 1 day");

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
    public static OffsetDateTime getNextWorkMinute(OffsetDateTime dateTime,
                                                   OffsetTime workStartTime,
                                                   Duration workTimeDuration) {
        Assert.isTrue(workTimeDuration.toNanos() > 0, "workTimeDuration must be positive");
        Assert.isTrue(Duration.ofDays(1).compareTo(workTimeDuration) >= 0,
                "workTimeDuration must be less than 1 day");

        if (isWorkTime(dateTime, workStartTime, workTimeDuration)) {
            OffsetDateTime nextDateTime = dateTime.plusMinutes(1);
            if (isWorkTime(nextDateTime, workStartTime, workTimeDuration)) {
                return nextDateTime;
            }
        }

        return toWorkStartTime(dateTime, workStartTime);
    }

    private static OffsetDateTime toWorkStartTime(OffsetDateTime dateTime, OffsetTime workStartTime) {
        if (dateTime.toOffsetTime().isBefore(workStartTime)) {
            return setTime(dateTime, workStartTime);
        }

        return setTime(getNextWorkDay(dateTime), workStartTime);
    }

}