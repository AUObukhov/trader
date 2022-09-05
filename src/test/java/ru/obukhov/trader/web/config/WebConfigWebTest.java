package ru.obukhov.trader.web.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.GenericConversionService;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

@SpringBootTest(args = "--trading.token=i identify myself as token")
class WebConfigWebTest extends IntegrationTest {

    @Autowired
    private GenericConversionService conversionService;

    @Test
    void testCandleIntervalConversion() {
        for (CandleInterval candleInterval : CandleInterval.values()) {
            CandleInterval convertedValue = conversionService.convert(candleInterval.name(), CandleInterval.class);
            Assertions.assertEquals(candleInterval, convertedValue);
        }
    }

    @Test
    void testInstrumentTypeConversion() {
        for (InstrumentType instrumentType : InstrumentType.values()) {
            InstrumentType convertedValue = conversionService.convert(instrumentType.getValue(), InstrumentType.class);
            Assertions.assertEquals(instrumentType, convertedValue);
        }
    }

    @Test
    void testMovingAverageTypeConversion() {
        for (MovingAverageType movingAverageType : MovingAverageType.values()) {
            MovingAverageType convertedValue = conversionService.convert(movingAverageType.getValue(), MovingAverageType.class);
            Assertions.assertEquals(movingAverageType, convertedValue);
        }
    }

}