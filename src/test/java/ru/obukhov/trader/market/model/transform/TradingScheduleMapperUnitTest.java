package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.model.trading_day.TestTradingDay;
import ru.obukhov.trader.test.utils.model.trading_day.TestTradingDays;

import java.util.List;

class TradingScheduleMapperUnitTest {

    private static final TradingScheduleMapper TRADING_SCHEDULE_MAPPER = Mappers.getMapper(TradingScheduleMapper.class);

    @Test
    void map() {
        final String exchange = "MOEX";

        final TestTradingDay testTradingDay1 = TestTradingDays.TRADING_DAY1;
        final TestTradingDay testTradingDay2 = TestTradingDays.TRADING_DAY2;

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange)
                .addDays(testTradingDay1.tTradingDay())
                .addDays(testTradingDay2.tTradingDay())
                .build();

        final TradingSchedule result = TRADING_SCHEDULE_MAPPER.map(tradingSchedule);

        final TradingDay expectedTradingDay1 = testTradingDay1.tradingDay();
        final TradingDay expectedTradingDay2 = testTradingDay2.tradingDay();
        final TradingSchedule expectedResult = new TradingSchedule(exchange, List.of(expectedTradingDay1, expectedTradingDay2));

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void map_whenValueIsNull() {
        final TradingSchedule result = TRADING_SCHEDULE_MAPPER.map(null);

        Assertions.assertNull(result);
    }

}