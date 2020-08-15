package ru.obukhov.investor.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateUtils {

    public static OffsetDateTime getDate(int year, int month, int dayOfMonth) {
        return OffsetDateTime.of(year, month, dayOfMonth,
                0, 0, 0, 0,
                ZoneOffset.UTC);
    }

}