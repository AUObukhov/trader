package ru.obukhov.trader.test.utils.model.schedule;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.time.OffsetDateTime;

public class TestTradingDay1 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    public static final OffsetDateTime DATE = DateTimeTestData.createDateTime(2022, 10, 3, 3);
    public static final boolean IS_TRADING_DAY = true;
    public static final OffsetDateTime START_TIME = DateTimeTestData.createDateTime(2022, 10, 4, 1);
    public static final OffsetDateTime END_TIME = DateTimeTestData.createDateTime(2022, 10, 5);
    public static final OffsetDateTime OPENING_AUCTION_START_TIME = DateTimeTestData.createDateTime(2022, 10, 4, 9, 50);
    public static final OffsetDateTime CLOSING_AUCTION_END_TIME = DateTimeTestData.createDateTime(2022, 10, 4, 18, 50);
    public static final OffsetDateTime EVENING_OPENING_AUCTION_START_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 19);
    public static final OffsetDateTime EVENING_START_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 19, 5);
    public static final OffsetDateTime EVENING_END_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 23, 50);
    public static final OffsetDateTime CLEARING_START_TIME = DateTimeTestData.createDateTime(2022, 10, 4);
    public static final OffsetDateTime CLEARING_END_TIME = DateTimeTestData.createDateTime(2022, 10, 4, 1);
    public static final OffsetDateTime PREMARKET_START_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 7);
    public static final OffsetDateTime PREMARKET_END_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 9, 50);

    public static final TradingDay TRADING_DAY = new TradingDay(
            DATE,
            IS_TRADING_DAY,
            START_TIME,
            END_TIME,
            OPENING_AUCTION_START_TIME,
            CLOSING_AUCTION_END_TIME,
            EVENING_OPENING_AUCTION_START_TIME,
            EVENING_START_TIME,
            EVENING_END_TIME,
            CLEARING_START_TIME,
            CLEARING_END_TIME,
            PREMARKET_START_TIME,
            PREMARKET_END_TIME
    );

    public static final ru.tinkoff.piapi.contract.v1.TradingDay TINKOFF_TRADING_DAY = ru.tinkoff.piapi.contract.v1.TradingDay.newBuilder()
            .setDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(DATE))
            .setIsTradingDay(IS_TRADING_DAY)
            .setStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(START_TIME))
            .setEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(END_TIME))
            .setOpeningAuctionStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(OPENING_AUCTION_START_TIME))
            .setClosingAuctionEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLOSING_AUCTION_END_TIME))
            .setEveningOpeningAuctionStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(EVENING_OPENING_AUCTION_START_TIME))
            .setEveningStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(EVENING_START_TIME))
            .setEveningEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(EVENING_END_TIME))
            .setClearingStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLEARING_START_TIME))
            .setClearingEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLEARING_END_TIME))
            .setPremarketStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(PREMARKET_START_TIME))
            .setPremarketEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(PREMARKET_END_TIME))
            .build();

    public static final String JSON_STRING = "date=2022-10-03T03:00:00+03:00, " +
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