package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class DateTestUtils {

    /**
     * @return last work day not after given {@code dateTime}
     */
    public static OffsetDateTime getLastWorkDay(final OffsetDateTime dateTime) {
        OffsetDateTime date = dateTime.truncatedTo(ChronoUnit.DAYS);
        while (!ru.obukhov.trader.common.util.DateUtils.isWorkDay(date)) {
            date = date.minusDays(1);
        }
        return date;
    }

}