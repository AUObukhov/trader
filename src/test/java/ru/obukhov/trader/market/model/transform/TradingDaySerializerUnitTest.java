package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.tinkoff.piapi.contract.v1.TradingDay;

import java.io.IOException;

class TradingDaySerializerUnitTest extends SerializerAbstractUnitTest<TradingDay> {

    private final TradingDaySerializer tradingDaySerializer = new TradingDaySerializer();

    @Test
    void test() throws IOException {
        test(tradingDaySerializer, TestTradingDay1.TRADING_DAY, TestTradingDay1.JSON_STRING, new TimestampSerializer());
    }

}