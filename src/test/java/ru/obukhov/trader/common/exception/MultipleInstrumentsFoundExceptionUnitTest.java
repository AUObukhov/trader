package ru.obukhov.trader.common.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;

class MultipleInstrumentsFoundExceptionUnitTest {

    @Test
    void constructorSetsMessage() {
        final String instrumentId = TestInstruments.APPLE.getFigi();
        final MultipleInstrumentsFoundException exception = new MultipleInstrumentsFoundException(instrumentId);

        final String expectedMessage = "Multiple instruments found for id " + instrumentId;
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

}