package ru.obukhov.trader.test.utils.model.schedule;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.TradingDay;

public class TestTradingDay3 {

    public static final Timestamp DATE = TimestampUtils.plusDays(TestTradingDay2.DATE, 1);
    public static final boolean IS_TRADING_DAY = TestTradingDay1.IS_TRADING_DAY;
    public static final Timestamp START_TIME = TimestampUtils.plusDays(TestTradingDay2.START_TIME, 1);
    public static final Timestamp END_TIME = TimestampUtils.plusDays(TestTradingDay2.END_TIME, 1);
    public static final Timestamp OPENING_AUCTION_START_TIME = TimestampUtils.plusDays(TestTradingDay2.OPENING_AUCTION_START_TIME, 1);
    public static final Timestamp CLOSING_AUCTION_END_TIME = TimestampUtils.plusDays(TestTradingDay2.CLOSING_AUCTION_END_TIME, 1);
    public static final Timestamp EVENING_OPENING_AUCTION_START_TIME = TimestampUtils.plusDays(TestTradingDay2.EVENING_OPENING_AUCTION_START_TIME, 1);
    public static final Timestamp EVENING_START_TIME = TimestampUtils.plusDays(TestTradingDay2.EVENING_START_TIME, 1);
    public static final Timestamp EVENING_END_TIME = TimestampUtils.plusDays(TestTradingDay2.EVENING_END_TIME, 1);
    public static final Timestamp CLEARING_START_TIME = TimestampUtils.plusDays(TestTradingDay2.CLEARING_START_TIME, 1);
    public static final Timestamp CLEARING_END_TIME = TimestampUtils.plusDays(TestTradingDay2.CLEARING_END_TIME, 1);
    public static final Timestamp PREMARKET_START_TIME = TimestampUtils.plusDays(TestTradingDay2.PREMARKET_START_TIME, 1);
    public static final Timestamp PREMARKET_END_TIME = TimestampUtils.plusDays(TestTradingDay2.PREMARKET_END_TIME, 1);

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

    public static final String JSON_STRING = "{\"date\":{\"seconds\":1664928000,\"nanos\":0}," +
            "\"isTradingDay\":true," +
            "\"startTime\":{\"seconds\":1665007200,\"nanos\":0}," +
            "\"endTime\":{\"seconds\":1665090000,\"nanos\":0}," +
            "\"openingAuctionStartTime\":{\"seconds\":1665039000,\"nanos\":0}," +
            "\"closingAuctionEndTime\":{\"seconds\":1665071400,\"nanos\":0}," +
            "\"eveningOpeningAuctionStartTime\":{\"seconds\":1664985600,\"nanos\":0}," +
            "\"eveningStartTime\":{\"seconds\":1664985900,\"nanos\":0}," +
            "\"eveningEndTime\":{\"seconds\":1665003000,\"nanos\":0}," +
            "\"clearingStartTime\":{\"seconds\":1665003600,\"nanos\":0}," +
            "\"clearingEndTime\":{\"seconds\":1665007200,\"nanos\":0}," +
            "\"premarketStartTime\":{\"seconds\":1664942400,\"nanos\":0}," +
            "\"premarketEndTime\":{\"seconds\":1664952600,\"nanos\":0}}";

}