package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

class CandleResolutionConverterTest {

    @Test
    void convert() {
        final CandleResolutionConverter converter = new CandleResolutionConverter();
        for (final CandleResolution candleResolution : CandleResolution.values()) {
            Assertions.assertEquals(candleResolution, converter.convert(candleResolution.getValue()));
        }
    }

}