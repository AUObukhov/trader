package ru.obukhov.trader.test.utils.model.schedule;

import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

public class TestTradingDays {

    private static final TradingDay TRADING_DAY_OBJECT1 = new TradingDay(
            DateTimeTestData.newDateTime(2022, 10, 3, 3),
            true,
            DateTimeTestData.newDateTime(2022, 10, 4, 1),
            DateTimeTestData.newDateTime(2022, 10, 5),
            DateTimeTestData.newDateTime(2022, 10, 4, 9, 50),
            DateTimeTestData.newDateTime(2022, 10, 4, 18, 50),
            DateTimeTestData.newDateTime(2022, 10, 3, 19),
            DateTimeTestData.newDateTime(2022, 10, 3, 19, 5),
            DateTimeTestData.newDateTime(2022, 10, 3, 23, 50),
            DateTimeTestData.newDateTime(2022, 10, 4),
            DateTimeTestData.newDateTime(2022, 10, 4, 1),
            DateTimeTestData.newDateTime(2022, 10, 3, 7),
            DateTimeTestData.newDateTime(2022, 10, 3, 9, 50)
    );

    private static final TradingDay TRADING_DAY_OBJECT2 = new TradingDay(
            TRADING_DAY_OBJECT1.date().plusDays(1),
            !TRADING_DAY_OBJECT1.isTradingDay(),
            TRADING_DAY_OBJECT1.startTime().plusDays(1),
            TRADING_DAY_OBJECT1.endTime().plusDays(1),
            TRADING_DAY_OBJECT1.openingAuctionStartTime().plusDays(1),
            TRADING_DAY_OBJECT1.closingAuctionEndTime().plusDays(1),
            TRADING_DAY_OBJECT1.eveningOpeningAuctionStartTime().plusDays(1),
            TRADING_DAY_OBJECT1.eveningStartTime().plusDays(1),
            TRADING_DAY_OBJECT1.eveningEndTime().plusDays(1),
            TRADING_DAY_OBJECT1.clearingStartTime().plusDays(1),
            TRADING_DAY_OBJECT1.clearingEndTime().plusDays(1),
            TRADING_DAY_OBJECT1.premarketStartTime().plusDays(1),
            TRADING_DAY_OBJECT1.premarketEndTime().plusDays(1)
    );

    private static final TradingDay TRADING_DAY_OBJECT3 = new TradingDay(
            TRADING_DAY_OBJECT2.date().plusDays(1),
            TRADING_DAY_OBJECT1.isTradingDay(),
            TRADING_DAY_OBJECT2.startTime().plusDays(1),
            TRADING_DAY_OBJECT2.endTime().plusDays(1),
            TRADING_DAY_OBJECT2.openingAuctionStartTime().plusDays(1),
            TRADING_DAY_OBJECT2.closingAuctionEndTime().plusDays(1),
            TRADING_DAY_OBJECT2.eveningOpeningAuctionStartTime().plusDays(1),
            TRADING_DAY_OBJECT2.eveningStartTime().plusDays(1),
            TRADING_DAY_OBJECT2.eveningEndTime().plusDays(1),
            TRADING_DAY_OBJECT2.clearingStartTime().plusDays(1),
            TRADING_DAY_OBJECT2.clearingEndTime().plusDays(1),
            TRADING_DAY_OBJECT2.premarketStartTime().plusDays(1),
            TRADING_DAY_OBJECT2.premarketEndTime().plusDays(1)
    );


    public static final TestTradingDay TRADING_DAY1 = new TestTradingDay(TRADING_DAY_OBJECT1);
    public static final TestTradingDay TRADING_DAY2 = new TestTradingDay(TRADING_DAY_OBJECT2);
    public static final TestTradingDay TRADING_DAY3 = new TestTradingDay(TRADING_DAY_OBJECT3);


}