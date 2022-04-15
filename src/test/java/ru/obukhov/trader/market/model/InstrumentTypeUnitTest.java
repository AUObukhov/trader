package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class InstrumentTypeUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndInstrumentTypes() {
        return Stream.of(
                Arguments.of("Stock", InstrumentType.STOCK),
                Arguments.of("Currency", InstrumentType.CURRENCY),
                Arguments.of("Bond", InstrumentType.BOND),
                Arguments.of("Etf", InstrumentType.ETF)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndInstrumentTypes")
    void toString_returnsValue(final String expectedValue, final InstrumentType instrumentType) {
        Assertions.assertEquals(expectedValue, instrumentType.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndInstrumentTypes")
    void fromValue_returnProperEnum(final String value, final InstrumentType expectedInstrumentType) {
        Assertions.assertEquals(expectedInstrumentType, InstrumentType.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndInstrumentTypes")
    void jsonMapping_mapsValue(final String value, final InstrumentType instrumentType) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(instrumentType));
    }

    @ParameterizedTest
    @MethodSource("valuesAndInstrumentTypes")
    void jsonMapping_createsFromValue(final String value, final InstrumentType instrumentType) throws JsonProcessingException {
        Assertions.assertEquals(instrumentType, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', InstrumentType.class));
    }

}