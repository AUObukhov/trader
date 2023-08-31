package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.market.model.TradingDay;

import java.time.OffsetDateTime;
import java.util.List;

@UtilityClass
public class TradingDayUtils {

    /**
     * @return result of {@code tradingDay.startTime <= dateTime < tradingDay.endTime}
     */
    public static boolean includes(final TradingDay tradingDay, final OffsetDateTime dateTime) {
        return !dateTime.isBefore(tradingDay.startTime()) && dateTime.isBefore(tradingDay.endTime());
    }

    /**
     * @param tradingSchedule list of trading days, items must not have intersections, must be sorted is ascending order
     * @param dateTime        initial dateTime
     * @return - {@code nextMinute} if it is inside any tradingDays<br/>
     * - the first minute of first tradingDays after {@code nextMinute} if it is not inside any of tradingDays<br/>
     * - null if all tradingDays are before {@code nextMinute}
     * @Terms: nextMinute – {@code dateTime} + 1 minute<br/>
     * tradingDay – item of {@code tradingSchedule} with {@code isTradingDay=true}<br/>
     */
    public static OffsetDateTime nextScheduleMinute(final List<TradingDay> tradingSchedule, final OffsetDateTime dateTime) {
        return ceilingScheduleMinute(tradingSchedule, dateTime.plusMinutes(1));
    }

    /**
     * @param tradingSchedule list of trading days, items must not have intersections, must be sorted is ascending order
     * @param dateTime        initial dateTime
     * @return - {@code dateTime} if it is inside any tradingDays<br/>
     * - the first minute of first tradingDays after {@code dateTime} if it is not inside any of tradingDays<br/>
     * - null if all tradingDays are before {@code dateTime}
     * @Terms: tradingDay – item of {@code tradingSchedule} with {@code isTradingDay=true}<br/>
     */
    public static OffsetDateTime ceilingScheduleMinute(final List<TradingDay> tradingSchedule, final OffsetDateTime dateTime) {
        for (TradingDay tradingDay : tradingSchedule) {
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