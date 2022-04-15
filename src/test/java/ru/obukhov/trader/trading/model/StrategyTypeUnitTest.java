package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class StrategyTypeUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndCurrencies() {
        return Stream.of(
                Arguments.of("conservative", StrategyType.CONSERVATIVE),
                Arguments.of("cross", StrategyType.CROSS)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void toString_returnsValue(final String expectedValue, final StrategyType strategyType) {
        Assertions.assertEquals(expectedValue, strategyType.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void fromValue_returnProperEnum(final String value, final StrategyType expectedStrategyType) {
        Assertions.assertEquals(expectedStrategyType, StrategyType.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void jsonMapping_mapsValue(final String value, final StrategyType strategyType) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(strategyType));
    }

    @ParameterizedTest
    @MethodSource("valuesAndCurrencies")
    void jsonMapping_createsFromValue(final String value, final StrategyType strategyType) throws JsonProcessingException {
        Assertions.assertEquals(strategyType, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', StrategyType.class));
    }

}