package ru.obukhov.trader.market.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currency;

import java.util.stream.Stream;

class CurrencyMapperUnitTest {

    private final CurrencyMapper mapper = Mappers.getMapper(CurrencyMapper.class);

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndCurrencies() {
        return Stream.of(
                Arguments.of("RUB", Currency.RUB),
                Arguments.of("USD", Currency.USD),
                Arguments.of("EUR", Currency.EUR),
                Arguments.of("GBP", Currency.GBP),
                Arguments.of("HKD", Currency.HKD),
                Arguments.of("CHF", Currency.CHF),
                Arguments.of("JPY", Currency.JPY),
                Arguments.of("CNY", Currency.CNY),
                Arguments.of("TRY", Currency.TRY),
                Arguments.of("ILS", Currency.ILS),
                Arguments.of("CAD", Currency.CAD),
                Arguments.of("DKK", Currency.DKK),
                Arguments.of("SEK", Currency.SEK),
                Arguments.of("SGD", Currency.SGD),
                Arguments.of("NOK", Currency.NOK),
                Arguments.of("UNKNOWN", Currency.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void valueOfIgnoreCase(final String value, final Currency currency) {
        Assertions.assertEquals(currency, mapper.map(value.toLowerCase()));
        Assertions.assertEquals(currency, mapper.map(value));
    }

    @Test
    void valueOfIgnoreCase_whenValueIsNull() {
        Assertions.assertEquals(Currency.UNKNOWN, mapper.map(null));
    }

    @Test
    void valueOfIgnoreCase_whenValueIsEmpty() {
        Assertions.assertEquals(Currency.UNKNOWN, mapper.map(StringUtils.EMPTY));
    }

}