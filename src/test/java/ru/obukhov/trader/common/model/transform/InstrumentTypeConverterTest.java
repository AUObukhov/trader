package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;

class InstrumentTypeConverterTest {

    @Test
    void convert() {
        InstrumentTypeConverter converter = new InstrumentTypeConverter();
        for (InstrumentType instrumentType : InstrumentType.values()) {
            Assertions.assertEquals(instrumentType, converter.convert(instrumentType.getValue()));
        }
    }

}