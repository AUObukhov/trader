package ru.obukhov.trader.test.utils.model.schedule;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.TradingDay;

public class TestTradingDay2 {

    public static final Timestamp DATE = TimestampUtils.plusDays(TestTradingDay1.DATE, 1);
    public static final boolean IS_TRADING_DAY = !TestTradingDay1.IS_TRADING_DAY;
    public static final Timestamp START_TIME = TimestampUtils.plusDays(TestTradingDay1.START_TIME, 1);
    public static final Timestamp END_TIME = TimestampUtils.plusDays(TestTradingDay1.END_TIME, 1);
    public static final Timestamp OPENING_AUCTION_START_TIME = TimestampUtils.plusDays(TestTradingDay1.OPENING_AUCTION_START_TIME, 1);
    public static final Timestamp CLOSING_AUCTION_END_TIME = TimestampUtils.plusDays(TestTradingDay1.CLOSING_AUCTION_END_TIME, 1);
    public static final Timestamp EVENING_OPENING_AUCTION_START_TIME = TimestampUtils.plusDays(TestTradingDay1.EVENING_OPENING_AUCTION_START_TIME, 1);
    public static final Timestamp EVENING_START_TIME = TimestampUtils.plusDays(TestTradingDay1.EVENING_START_TIME, 1);
    public static final Timestamp EVENING_END_TIME = TimestampUtils.plusDays(TestTradingDay1.EVENING_END_TIME, 1);
    public static final Timestamp CLEARING_START_TIME = TimestampUtils.plusDays(TestTradingDay1.CLEARING_START_TIME, 1);
    public static final Timestamp CLEARING_END_TIME = TimestampUtils.plusDays(TestTradingDay1.CLEARING_END_TIME, 1);
    public static final Timestamp PREMARKET_START_TIME = TimestampUtils.plusDays(TestTradingDay1.PREMARKET_START_TIME, 1);
    public static final Timestamp PREMARKET_END_TIME = TimestampUtils.plusDays(TestTradingDay1.PREMARKET_END_TIME, 1);

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

    public static final String JSON_STRING = "{\"date\":{\"seconds\":1664841600,\"nanos\":0}," +
            "\"isTradingDay\":false," +
            "\"startTime\":{\"seconds\":1664920800,\"nanos\":0}," +
            "\"endTime\":{\"seconds\":1665003600,\"nanos\":0}," +
            "\"openingAuctionStartTime\":{\"seconds\":1664952600,\"nanos\":0}," +
            "\"closingAuctionEndTime\":{\"seconds\":1664985000,\"nanos\":0}," +
            "\"eveningOpeningAuctionStartTime\":{\"seconds\":1664899200,\"nanos\":0}," +
            "\"eveningStartTime\":{\"seconds\":1664899500,\"nanos\":0}," +
            "\"eveningEndTime\":{\"seconds\":1664916600,\"nanos\":0}," +
            "\"clearingStartTime\":{\"seconds\":1664917200,\"nanos\":0}," +
            "\"clearingEndTime\":{\"seconds\":1664920800,\"nanos\":0}," +
            "\"premarketStartTime\":{\"seconds\":1664856000,\"nanos\":0}," +
            "\"premarketEndTime\":{\"seconds\":1664866200,\"nanos\":0}}";

}