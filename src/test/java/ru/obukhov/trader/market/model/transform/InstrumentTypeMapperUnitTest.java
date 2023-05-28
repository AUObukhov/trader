package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.InstrumentType;

import java.util.stream.Stream;

class InstrumentTypeMapperUnitTest {

    private final InstrumentTypeMapper ExchangeMapper = Mappers.getMapper(InstrumentTypeMapper.class);

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
    void map(final String value, final InstrumentType Exchange) {
        Assertions.assertEquals(Exchange, ExchangeMapper.map(value));
    }

}