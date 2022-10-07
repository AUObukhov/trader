package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay2;

import java.util.List;

class TradingScheduleMapperUnitTest {

    private static final TradingScheduleMapper TRADING_SCHEDULE_MAPPER = Mappers.getMapper(TradingScheduleMapper.class);

    @Test
    void map() {
        final Exchange exchange = Exchange.MOEX;

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange.getValue())
                .addDays(TestTradingDay1.createTinkoffTradingDay())
                .addDays(TestTradingDay2.createTinkoffTradingDay())
                .build();

        final TradingSchedule result = TRADING_SCHEDULE_MAPPER.map(tradingSchedule);

        final TradingDay expectedTradingDay1 = TestTradingDay1.createTradingDay();
        final TradingDay expectedTradingDay2 = TestTradingDay2.createTradingDay();
        final TradingSchedule expectedResult = new TradingSchedule(exchange, List.of(expectedTradingDay1, expectedTradingDay2));

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void map_whenValueIsNull() {
        final TradingSchedule result = TRADING_SCHEDULE_MAPPER.map(null);

        Assertions.assertNull(result);
    }

}