package ru.obukhov.investor.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateUtils {

    private static final OffsetDateTime START_DATE = getDate(2000, 1, 1);

    public static OffsetDateTime getDate(int year, int month, int dayOfMonth) {
        return OffsetDateTime.of(year, month, dayOfMonth,
                0, 0, 0, 0,
                ZoneOffset.UTC);
    }

    public static OffsetDateTime getTime(int hour, int minute, int second) {
        return OffsetDateTime.of(0, 1, 1,
                hour, minute, second, 0,
                ZoneOffset.UTC);
    }

    public static OffsetDateTime adjustFrom(OffsetDateTime from) {
        return from == null ? START_DATE : from;
    }

    public static OffsetDateTime adjustTo(OffsetDateTime to) {
        return to == null ? OffsetDateTime.now() : to;
    }

}