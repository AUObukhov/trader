package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;

class TradingDayMapperUnitTest {

    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);

    @Test
    void mapTinkoffToCustom() {
        final ru.tinkoff.piapi.contract.v1.TradingDay source = TestTradingDay1.TINKOFF_TRADING_DAY;

        final TradingDay result = TRADING_DAY_MAPPER.map(source);

        final TradingDay expectedResult = TestTradingDay1.TRADING_DAY;
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
        final TradingDay source = TestTradingDay1.TRADING_DAY;

        final ru.tinkoff.piapi.contract.v1.TradingDay result = TRADING_DAY_MAPPER.map(source);

        final ru.tinkoff.piapi.contract.v1.TradingDay expectedResult = TestTradingDay1.TINKOFF_TRADING_DAY;
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void mapCustomToTinkoff_whenValueIsNull() {
        final TradingDay source = null;

        final ru.tinkoff.piapi.contract.v1.TradingDay result = TRADING_DAY_MAPPER.map(source);

        Assertions.assertNull(result);
    }

}