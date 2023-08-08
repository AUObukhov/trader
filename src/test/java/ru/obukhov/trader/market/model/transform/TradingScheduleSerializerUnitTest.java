package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay2;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay3;
import ru.tinkoff.piapi.contract.v1.TradingSchedule;

import java.io.IOException;

class TradingScheduleSerializerUnitTest extends SerializerAbstractUnitTest<TradingSchedule> {

    private final TradingScheduleSerializer tradingScheduleSerializer = new TradingScheduleSerializer();

    @Test
    void test() throws IOException {
        final TradingSchedule tradingSchedule = TradingSchedule.newBuilder()
                .setExchange("MOEX")
                .addDays(TestTradingDay1.TRADING_DAY)
                .addDays(TestTradingDay2.TRADING_DAY)
                .addDays(TestTradingDay3.TRADING_DAY)
                .build();
        final String expectedResult = "{\"exchange\":\"MOEX\",\"days\":["
                + TestTradingDay1.JSON_STRING + ","
                + TestTradingDay2.JSON_STRING + ","
                + TestTradingDay3.JSON_STRING + "]}";
        test(tradingScheduleSerializer, tradingSchedule, expectedResult, new TradingDaySerializer(), new TimestampSerializer());
    }

}