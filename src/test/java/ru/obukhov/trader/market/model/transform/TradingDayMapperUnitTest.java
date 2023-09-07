package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDays;

class TradingDayMapperUnitTest {

    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);

    @Test
    void mapTinkoffToCustom() {
        final TestTradingDay testTradingDay = TestTradingDays.TRADING_DAY1;

        final ru.tinkoff.piapi.contract.v1.TradingDay source = testTradingDay.tinkoffTradingDay();

        final TradingDay result = TRADING_DAY_MAPPER.map(source);

        final TradingDay expectedResult = testTradingDay.tradingDay();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void mapTinkoffToCustom_whenValueIsNull() {
        final ru.tinkoff.piapi.contract.v1.TradingDay source = null;

        final TradingDay result = TRADING_DAY_MAPPER.map(source);

        Assertions.assertNull(result);
    }

    @Test
    void mapCustomToTinkoff() {
        final TestTradingDay testTradingDay = TestTradingDays.TRADING_DAY1;

        final TradingDay source = testTradingDay.tradingDay();

        final ru.tinkoff.piapi.contract.v1.TradingDay result = TRADING_DAY_MAPPER.map(source);

        final ru.tinkoff.piapi.contract.v1.TradingDay expectedResult = testTradingDay.tinkoffTradingDay();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void mapCustomToTinkoff_whenValueIsNull() {
        final TradingDay source = null;

        final ru.tinkoff.piapi.contract.v1.TradingDay result = TRADING_DAY_MAPPER.map(source);

        Assertions.assertNull(result);
    }

}