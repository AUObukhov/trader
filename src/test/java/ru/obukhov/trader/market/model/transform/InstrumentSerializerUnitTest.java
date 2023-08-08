package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;
import ru.tinkoff.piapi.contract.v1.Instrument;

import java.io.IOException;

class InstrumentSerializerUnitTest extends SerializerAbstractUnitTest<Instrument> {

    private final InstrumentSerializer instrumentSerializer = new InstrumentSerializer();

    @Test
    void test() throws IOException {
        test(instrumentSerializer, TestInstrument1.INSTRUMENT, TestInstrument1.JSON_STRING, new QuotationSerializer(), new TimestampSerializer());
    }

}