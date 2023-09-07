package ru.obukhov.trader.common.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;

class InstrumentNotFoundExceptionUnitTest {

    @Test
    void constructorSetsMessage() {
        final String instrumentId = TestInstruments.APPLE.instrument().figi();
        final InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        final String expectedMessage = "Instrument not found for id " + instrumentId;
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

}