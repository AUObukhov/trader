package ru.obukhov.investor.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateUtils {

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

}