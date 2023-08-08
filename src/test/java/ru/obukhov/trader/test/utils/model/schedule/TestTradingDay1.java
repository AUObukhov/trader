package ru.obukhov.trader.test.utils.model.schedule;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.TradingDay;

public class TestTradingDay1 {

    public static final Timestamp DATE = TimestampUtils.newTimestamp(2022, 10, 3, 3);
    public static final boolean IS_TRADING_DAY = true;
    public static final Timestamp START_TIME = TimestampUtils.newTimestamp(2022, 10, 4, 1);
    public static final Timestamp END_TIME = TimestampUtils.newTimestamp(2022, 10, 5);
    public static final Timestamp OPENING_AUCTION_START_TIME = TimestampUtils.newTimestamp(2022, 10, 4, 9, 50);
    public static final Timestamp CLOSING_AUCTION_END_TIME = TimestampUtils.newTimestamp(2022, 10, 4, 18, 50);
    public static final Timestamp EVENING_OPENING_AUCTION_START_TIME = TimestampUtils.newTimestamp(2022, 10, 3, 19);
    public static final Timestamp EVENING_START_TIME = TimestampUtils.newTimestamp(2022, 10, 3, 19, 5);
    public static final Timestamp EVENING_END_TIME = TimestampUtils.newTimestamp(2022, 10, 3, 23, 50);
    public static final Timestamp CLEARING_START_TIME = TimestampUtils.newTimestamp(2022, 10, 4);
    public static final Timestamp CLEARING_END_TIME = TimestampUtils.newTimestamp(2022, 10, 4, 1);
    public static final Timestamp PREMARKET_START_TIME = TimestampUtils.newTimestamp(2022, 10, 3, 7);
    public static final Timestamp PREMARKET_END_TIME = TimestampUtils.newTimestamp(2022, 10, 3, 9, 50);

    public static final TradingDay TRADING_DAY = TradingDay.newBuilder()
            .setDate(DATE)
            .setIsTradingDay(IS_TRADING_DAY)
            .setStartTime(START_TIME)
            .setEndTime(END_TIME)
            .setOpeningAuctionStartTime(OPENING_AUCTION_START_TIME)
            .setClosingAuctionEndTime(CLOSING_AUCTION_END_TIME)
            .setEveningOpeningAuctionStartTime(EVENING_OPENING_AUCTION_START_TIME)
            .setEveningStartTime(EVENING_START_TIME)
            .setEveningEndTime(EVENING_END_TIME)
            .setClearingStartTime(CLEARING_START_TIME)
            .setClearingEndTime(CLEARING_END_TIME)
            .setPremarketStartTime(PREMARKET_START_TIME)
            .setPremarketEndTime(PREMARKET_END_TIME)
            .build();

    public static final String JSON_STRING = "{\"date\":{\"seconds\":1664755200,\"nanos\":0}," +
            "\"isTradingDay\":true," +
            "\"startTime\":{\"seconds\":1664834400,\"nanos\":0}," +
            "\"endTime\":{\"seconds\":1664917200,\"nanos\":0}," +
            "\"openingAuctionStartTime\":{\"seconds\":1664866200,\"nanos\":0}," +
            "\"closingAuctionEndTime\":{\"seconds\":1664898600,\"nanos\":0}," +
            "\"eveningOpeningAuctionStartTime\":{\"seconds\":1664812800,\"nanos\":0}," +
            "\"eveningStartTime\":{\"seconds\":1664813100,\"nanos\":0}," +
            "\"eveningEndTime\":{\"seconds\":1664830200,\"nanos\":0}," +
            "\"clearingStartTime\":{\"seconds\":1664830800,\"nanos\":0}," +
            "\"clearingEndTime\":{\"seconds\":1664834400,\"nanos\":0}," +
            "\"premarketStartTime\":{\"seconds\":1664769600,\"nanos\":0}," +
            "\"premarketEndTime\":{\"seconds\":1664779800,\"nanos\":0}}";

    public static final String PRETTY_STRING = "date=2022-10-03T03:00:00+03:00, " +
            "isTradingDay=true, " +
            "startTime=2022-10-04T01:00:00+03:00, " +
            "endTime=2022-10-05T00:00:00+03:00, " +
            "openingAuctionStartTime=2022-10-04T09:50:00+03:00, " +
            "closingAuctionEndTime=2022-10-04T18:50:00+03:00, " +
            "eveningOpeningAuctionStartTime=2022-10-03T19:00:00+03:00, " +
            "eveningStartTime=2022-10-03T19:05:00+03:00, " +
            "eveningEndTime=2022-10-03T23:50:00+03:00, " +
            "clearingStartTime=2022-10-04T00:00:00+03:00, " +
            "clearingEndTime=2022-10-04T01:00:00+03:00, " +
            "premarketStartTime=2022-10-03T07:00:00+03:00, " +
            "premarketEndTime=2022-10-03T09:50:00+03:00";

}