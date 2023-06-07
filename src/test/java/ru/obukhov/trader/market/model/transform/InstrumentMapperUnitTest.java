package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;

class InstrumentMapperUnitTest {

    private final InstrumentMapper instrumentMapper = Mappers.getMapper(InstrumentMapper.class);

    @Test
    void map() {
        final Instrument result = instrumentMapper.map(TestInstrument1.TINKOFF_INSTRUMENT);

        Assertions.assertEquals(TestInstrument1.INSTRUMENT, result);
    }

    @Test
    void map_whenValueIsNull() {
        final Instrument instrument = instrumentMapper.map(null);

        Assertions.assertNull(instrument);
    }

}