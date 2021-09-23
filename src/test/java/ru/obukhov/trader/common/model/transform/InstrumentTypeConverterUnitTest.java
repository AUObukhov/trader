package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;

class InstrumentTypeConverterUnitTest {

    @Test
    void convert() {
        final InstrumentTypeConverter converter = new InstrumentTypeConverter();
        for (final InstrumentType instrumentType : InstrumentType.values()) {
            Assertions.assertEquals(instrumentType, converter.convert(instrumentType.getValue()));
        }
    }

}