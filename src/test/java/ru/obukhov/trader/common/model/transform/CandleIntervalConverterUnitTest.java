package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

class CandleIntervalConverterUnitTest {

    @Test
    void convert() {
        final CandleIntervalConverter converter = new CandleIntervalConverter();
        for (final CandleInterval candleInterval : CandleInterval.values()) {
            Assertions.assertEquals(candleInterval, converter.convert(candleInterval.name()));
        }
    }

}