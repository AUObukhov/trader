package ru.obukhov.trader.test.utils.model.trading_day;

import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestTradingDays {

    public static final TestTradingDay TRADING_DAY1 = readOrderTradingDay("trading-day1.json");
    public static final TestTradingDay TRADING_DAY2 = readOrderTradingDay("trading-day2.json");
    public static final TestTradingDay TRADING_DAY3 = readOrderTradingDay("trading-day3.json");

    private static TestTradingDay readOrderTradingDay(final String fileName) {
        final TradingDay tradingDay = ResourceUtils.getResourceAsObject("trading_days/" + fileName, TradingDay.class);
        return new TestTradingDay(tradingDay);
    }

}