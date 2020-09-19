package ru.obukhov.investor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
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
        DayOfWeek dayOfWeek = date.toLocalDate().getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
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
     * @return {@link ChronoUnit#DAYS} when {@code candleInterval) is less than day, or else {@link ChronoUnit#YEARS}
     */
    public static ChronoUnit getPeriodByCandleInterval(CandleInterval candleInterval) {
        boolean intervalIsInDay = candleInterval == CandleInterval.ONE_MIN
                || candleInterval == CandleInterval.TWO_MIN
                || candleInterval == CandleInterval.THREE_MIN
                || candleInterval == CandleInterval.FIVE_MIN
                || candleInterval == CandleInterval.TEN_MIN
                || candleInterval == CandleInterval.QUARTER_HOUR
                || candleInterval == CandleInterval.HALF_HOUR
                || candleInterval == CandleInterval.HOUR
                || candleInterval == CandleInterval.TWO_HOURS
                || candleInterval == CandleInterval.FOUR_HOURS;
        return intervalIsInDay ? ChronoUnit.DAYS : ChronoUnit.YEARS;
    }

    /**
     * Null safe extention of {@link OffsetDateTime#isAfter}
     *
     * @return true, if {@code dateTime2} is null, or else result of {@link OffsetDateTime#isAfter}
     */
    public static boolean isAfter(OffsetDateTime dateTime1, OffsetDateTime dateTime2) {
        return dateTime2 == null || dateTime1.isAfter(dateTime2);
    }

}