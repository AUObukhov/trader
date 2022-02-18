package ru.obukhov.trader.web.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.GenericConversionService;
import ru.obukhov.trader.ContextTest;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MovingAverageType;

@SpringBootTest(args = "--trading.token=i identify myself as token")
class WebConfigWebTest extends ContextTest {

    @Autowired
    private GenericConversionService conversionService;

    @Test
    void testCandleIntervalConversion() {
        for (CandleInterval candleInterval : CandleInterval.values()) {
            CandleInterval convertedValue = conversionService.convert(candleInterval.getValue(), CandleInterval.class);
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