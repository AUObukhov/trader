package ru.obukhov.investor.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    static final OffsetDateTime START_DATE = getDate(2000, 1, 1);

    public static OffsetDateTime getDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return OffsetDateTime.of(year, month, dayOfMonth,
                hour, minute, second, 0,
                ZoneOffset.UTC);
    }

    public static OffsetDateTime getDate(int year, int month, int dayOfMonth) {
        return getDateTime(year, month, dayOfMonth, 0, 0, 0);
    }

    public static OffsetDateTime getTime(int hour, int minute, int second) {
        return getDateTime(0, 1, 1, hour, minute, second);
    }

    public static OffsetDateTime getDefaultFromIfNull(OffsetDateTime from) {
        return ObjectUtils.defaultIfNull(from, START_DATE);
    }

    public static OffsetDateTime getDefaultToIfNull(OffsetDateTime to) {
        return ObjectUtils.defaultIfNull(to, OffsetDateTime.now());
    }

    public static boolean isWorkDay(OffsetDateTime date) {
        DayOfWeek dayOfWeek = date.toLocalDate().getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

}