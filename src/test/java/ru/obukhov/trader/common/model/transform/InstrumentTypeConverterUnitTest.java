package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.market.model.InstrumentType;

import java.util.stream.Stream;

class InstrumentTypeConverterUnitTest {

    private final InstrumentTypeConverter converter = new InstrumentTypeConverter();

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndInstrumentTypes() {
        return Stream.of(
                Arguments.of("share", InstrumentType.SHARE),
                Arguments.of("etf", InstrumentType.ETF),
                Arguments.of("bond", InstrumentType.BOND),
                Arguments.of("currency", InstrumentType.CURRENCY)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndInstrumentTypes")
    void convert(final String value, final InstrumentType instrumentType) {
        Assertions.assertEquals(instrumentType, converter.convert(value));
    }

}