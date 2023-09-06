package ru.obukhov.trader.common.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;

class InstrumentNotFoundExceptionUnitTest {

    @Test
    void constructorSetsMessage() {
        final String instrumentId = TestInstrument1.FIGI;
        final InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        final String expectedMessage = "Instrument " + instrumentId + " not found";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

}