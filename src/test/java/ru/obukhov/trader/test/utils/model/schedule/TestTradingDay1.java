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
    public static final OffsetDateTime CLOSING_AUCTION_END_TIME = DateTimeTestData.createDateTime(2022, 10, 4, 18, 50);
    public static final OffsetDateTime EVENING_OPENING_AUCTION_START_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 19);
    public static final OffsetDateTime EVENING_START_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 19, 5);
    public static final OffsetDateTime EVENING_END_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 23, 50);
    public static final OffsetDateTime CLEARING_START_TIME = DateTimeTestData.createDateTime(2022, 10, 4);
    public static final OffsetDateTime CLEARING_END_TIME = DateTimeTestData.createDateTime(2022, 10, 4, 1);
    public static final OffsetDateTime PREMARKET_START_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 7);
    public static final OffsetDateTime PREMARKET_END_TIME = DateTimeTestData.createDateTime(2022, 10, 3, 9, 50);

    public static ru.tinkoff.piapi.contract.v1.TradingDay createTinkoffTradingDay() {
        return ru.tinkoff.piapi.contract.v1.TradingDay.newBuilder()
                .setDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(DATE))
                .setIsTradingDay(IS_TRADING_DAY)
                .setStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(START_TIME))
                .setEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(END_TIME))
                .setClosingAuctionEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLOSING_AUCTION_END_TIME))
                .setEveningOpeningAuctionStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(EVENING_OPENING_AUCTION_START_TIME))
                .setEveningStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(EVENING_START_TIME))
                .setEveningEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(EVENING_END_TIME))
                .setClearingStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLEARING_START_TIME))
                .setClearingEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLEARING_END_TIME))
                .setPremarketStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(PREMARKET_START_TIME))
                .setPremarketEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(PREMARKET_END_TIME))
                .build();
    }

    public static TradingDay createTradingDay() {
        return new TradingDay(
                DATE,
                IS_TRADING_DAY,
                START_TIME,
                END_TIME,
                CLOSING_AUCTION_END_TIME,
                EVENING_OPENING_AUCTION_START_TIME,
                EVENING_START_TIME,
                EVENING_END_TIME,
                CLEARING_START_TIME,
                CLEARING_END_TIME,
                PREMARKET_START_TIME,
                PREMARKET_END_TIME
        );
    }

}