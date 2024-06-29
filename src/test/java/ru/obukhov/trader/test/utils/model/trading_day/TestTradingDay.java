package ru.obukhov.trader.test.utils.model.trading_day;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;

public record TestTradingDay(TradingDay tradingDay, ru.tinkoff.piapi.contract.v1.TradingDay tinkoffTradingDay) {

    TestTradingDay(final TradingDay tradingDay) {
        this(tradingDay, buildTinkoffTradingDay(tradingDay));
    }

    private static ru.tinkoff.piapi.contract.v1.TradingDay buildTinkoffTradingDay(final TradingDay tradingDay) {
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        return ru.tinkoff.piapi.contract.v1.TradingDay.newBuilder()
                .setDate(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.date()))
                .setIsTradingDay(tradingDay.isTradingDay())
                .setStartTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.startTime()))
                .setEndTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.endTime()))
                .setOpeningAuctionStartTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.openingAuctionStartTime()))
                .setClosingAuctionEndTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.closingAuctionEndTime()))
                .setEveningOpeningAuctionStartTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.eveningOpeningAuctionStartTime()))
                .setEveningStartTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.eveningStartTime()))
                .setEveningEndTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.eveningEndTime()))
                .setClearingStartTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.clearingStartTime()))
                .setClearingEndTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.clearingEndTime()))
                .setPremarketStartTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.premarketStartTime()))
                .setPremarketEndTime(dateTimeMapper.offsetDateTimeToTimestamp(tradingDay.premarketEndTime()))
                .build();
    }

}