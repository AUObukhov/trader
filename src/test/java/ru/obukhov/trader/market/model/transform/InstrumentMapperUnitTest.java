package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;

class InstrumentMapperUnitTest {

    private final InstrumentMapper instrumentMapper = Mappers.getMapper(InstrumentMapper.class);

    @Test
    void map() {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final Instrument instrument = instrumentMapper.map(testInstrument.tInstrument());

        Assertions.assertEquals(testInstrument.instrument(), instrument);
    }

    @Test
    void map_whenValueIsNull() {
        final Instrument instrument = instrumentMapper.map(null);

        Assertions.assertNull(instrument);
    }

}