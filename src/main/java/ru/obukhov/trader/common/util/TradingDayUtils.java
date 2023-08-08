package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
import ru.tinkoff.piapi.contract.v1.TradingDay;

import java.util.List;

@UtilityClass
public class TradingDayUtils {

    /**
     * @return result of {@code tradingDay.startTime <= timestamp < tradingDay.endTime}
     */
    public static boolean includes(final TradingDay tradingDay, final Timestamp timestamp) {
        return !TimestampUtils.isBefore(timestamp, tradingDay.getStartTime()) && TimestampUtils.isBefore(timestamp, tradingDay.getEndTime());
    }

    /**
     * @param tradingSchedule list of trading days, items must not have intersections, must be sorted is ascending order
     * @param timestamp       initial timestamp
     * @return - {@code nextMinute} if it is inside any tradingDays<br/>
     * - the first minute of first tradingDays after {@code nextMinute} if it is not inside any of tradingDays<br/>
     * - null if all tradingDays are before {@code nextMinute}
     * @Terms: nextMinute – {@code timestamp} + 1 minute<br/>
     * tradingDay – item of {@code tradingSchedule} with {@code isTradingDay=true}<br/>
     */
    public static Timestamp nextScheduleMinute(final List<TradingDay> tradingSchedule, final Timestamp timestamp) {
        return ceilingScheduleMinute(tradingSchedule, TimestampUtils.plusMinutes(timestamp, 1));
    }

    /**
     * @param tradingSchedule list of trading days, items must not have intersections, must be sorted is ascending order
     * @param timestamp       initial timestamp
     * @return - {@code timestamp} if it is inside any tradingDays<br/>
     * - the first minute of first tradingDays after {@code timestamp} if it is not inside any of tradingDays<br/>
     * - null if all tradingDays are before {@code timestamp}
     * @Terms: tradingDay – item of {@code tradingSchedule} with {@code isTradingDay=true}<br/>
     */
    public static Timestamp ceilingScheduleMinute(final List<TradingDay> tradingSchedule, final Timestamp timestamp) {
        for (TradingDay tradingDay : tradingSchedule) {
            if (tradingDay.getIsTradingDay()) {
                if (TimestampUtils.isAfter(tradingDay.getStartTime(), timestamp)) {
                    return tradingDay.getStartTime();
                } else if (TimestampUtils.isBefore(timestamp, tradingDay.getEndTime())) {
                    return timestamp;
                }
            }
        }

        return null;
    }

    public static String toDateTimesString(final TradingDay tradingDay) {
        return "date=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getDate()) + ", "
                + "isTradingDay=" + tradingDay.getIsTradingDay() + ", "
                + "startTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getStartTime()) + ", "
                + "endTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getEndTime()) + ", "
                + "openingAuctionStartTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getOpeningAuctionStartTime()) + ", "
                + "closingAuctionEndTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getClosingAuctionEndTime()) + ", "
                + "eveningOpeningAuctionStartTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getEveningOpeningAuctionStartTime()) + ", "
                + "eveningStartTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getEveningStartTime()) + ", "
                + "eveningEndTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getEveningEndTime()) + ", "
                + "clearingStartTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getClearingStartTime()) + ", "
                + "clearingEndTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getClearingEndTime()) + ", "
                + "premarketStartTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getPremarketStartTime()) + ", "
                + "premarketEndTime=" + TimestampUtils.toOffsetDateTimeString(tradingDay.getPremarketEndTime());
    }

}