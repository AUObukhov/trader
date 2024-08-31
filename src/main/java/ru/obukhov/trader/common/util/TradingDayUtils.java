package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.market.model.TradingDay;

import java.time.OffsetDateTime;
import java.util.List;

@UtilityClass
public class TradingDayUtils {

    public static boolean includes(final TradingDay tradingDay, final OffsetDateTime dateTime) {
        return !dateTime.isBefore(tradingDay.startTime()) && dateTime.isBefore(tradingDay.endTime());
    }

    public static OffsetDateTime nextScheduleMinute(
            final List<TradingDay> tradingSchedule,
            final OffsetDateTime dateTime
    ) {
        return ceilingScheduleMinute(tradingSchedule, dateTime.plusMinutes(1));
    }

    public static OffsetDateTime ceilingScheduleMinute(
            final List<TradingDay> tradingSchedule,
            final OffsetDateTime dateTime
    ) {
        for (final TradingDay tradingDay : tradingSchedule) {
            if (tradingDay.isTradingDay()) {
                if (tradingDay.startTime().isAfter(dateTime)) {
                    return tradingDay.startTime();
                } else if (dateTime.isBefore(tradingDay.endTime())) {
                    return dateTime;
                }
            }
        }

        return null;
    }

}