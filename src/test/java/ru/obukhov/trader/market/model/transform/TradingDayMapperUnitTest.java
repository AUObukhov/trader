package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.trading_day.TestTradingDay;
import ru.obukhov.trader.test.utils.model.trading_day.TestTradingDays;

class TradingDayMapperUnitTest {

    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);

    @Test
    void mapTToCustom() {
        final TestTradingDay testTradingDay = TestTradingDays.TRADING_DAY1;

        final ru.tinkoff.piapi.contract.v1.TradingDay source = testTradingDay.tTradingDay();

        final TradingDay result = TRADING_DAY_MAPPER.map(source);

        final TradingDay expectedResult = testTradingDay.tradingDay();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void mapTToCustom_whenValueIsNull() {
        final ru.tinkoff.piapi.contract.v1.TradingDay source = null;

        final TradingDay result = TRADING_DAY_MAPPER.map(source);

        Assertions.assertNull(result);
    }

    @Test
    void mapCustomToT() {
        final TestTradingDay testTradingDay = TestTradingDays.TRADING_DAY1;

        final TradingDay source = testTradingDay.tradingDay();

        final ru.tinkoff.piapi.contract.v1.TradingDay result = TRADING_DAY_MAPPER.map(source);

        final ru.tinkoff.piapi.contract.v1.TradingDay expectedResult = testTradingDay.tTradingDay();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void mapCustomToT_whenValueIsNull() {
        final TradingDay source = null;

        final ru.tinkoff.piapi.contract.v1.TradingDay result = TRADING_DAY_MAPPER.map(source);

        Assertions.assertNull(result);
    }

}