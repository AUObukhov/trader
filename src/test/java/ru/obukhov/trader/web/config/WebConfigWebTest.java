package ru.obukhov.trader.web.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.GenericConversionService;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;

@SpringBootTest(args = "--trading.token=i identify myself as token")
class WebConfigWebTest {

    @Autowired
    private GenericConversionService conversionService;

    @Test
    void testCandleResolutionConversion() {
        for (CandleResolution candleResolution : CandleResolution.values()) {
            CandleResolution convertedValue = conversionService.convert(candleResolution.getValue(), CandleResolution.class);
            Assertions.assertEquals(candleResolution, convertedValue);
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