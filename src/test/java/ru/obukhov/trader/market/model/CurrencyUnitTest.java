package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class CurrencyUnitTest {

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
                Arguments.of("TRY", Currency.TRY)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void jsonMapping_mapsValue(final String value, final Currency currency) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(currency));
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void jsonMapping_createsFromValue(final String value, final Currency currency) throws JsonProcessingException {
        Assertions.assertEquals(currency, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', Currency.class));
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void valueOfIgnoreCase(final String value, final Currency currency) {
        Assertions.assertEquals(currency, Currency.valueOfIgnoreCase(value.toLowerCase()));
        Assertions.assertEquals(currency, Currency.valueOfIgnoreCase(value));
    }

}