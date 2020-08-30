package ru.obukhov.investor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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

}