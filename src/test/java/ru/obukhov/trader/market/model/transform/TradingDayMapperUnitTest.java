package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;

class TradingDayMapperUnitTest {

    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);

    @Test
    void map() {
        final ru.tinkoff.piapi.contract.v1.TradingDay source = TestTradingDay1.createTinkoffTradingDay();

        final TradingDay result = TRADING_DAY_MAPPER.map(source);

        final TradingDay expectedResult = TestTradingDay1.createTradingDay();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    void map_whenValueIsNull() {
        final TradingDay result = TRADING_DAY_MAPPER.map(null);

        Assertions.assertNull(result);
    }

}