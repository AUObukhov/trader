package ru.obukhov.trader.common.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;

import java.util.List;

class InstrumentNotFoundExceptionUnitTest {

    @Test
    void constructorSetsMessage_whenSingleInstrumentId() {
        final String instrumentId = TestInstruments.APPLE.instrument().figi();
        final InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        final String expectedMessage = "Instrument not found for id " + instrumentId;
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructorSetsMessage_whenMultipleInstrumentIds() {
        final String instrumentId1 = TestInstruments.APPLE.instrument().figi();
        final String instrumentId2 = TestInstruments.SBER.instrument().figi();
        final InstrumentNotFoundException exception = new InstrumentNotFoundException(List.of(instrumentId1, instrumentId2));

        final String expectedMessage = "Instruments not found for ids [" + instrumentId1 + ", " + instrumentId2 + "]";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

}