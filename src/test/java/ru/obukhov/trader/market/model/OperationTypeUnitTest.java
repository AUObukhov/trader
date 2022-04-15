package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class OperationTypeUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndOperationTypes() {
        return Stream.of(
                Arguments.of("Buy", OperationType.BUY),
                Arguments.of("Sell", OperationType.SELL)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypes")
    void toString_returnsValue(final String expectedValue, final OperationType operationType) {
        Assertions.assertEquals(expectedValue, operationType.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypes")
    void fromValue_returnProperEnum(final String value, final OperationType expectedOperationType) {
        Assertions.assertEquals(expectedOperationType, OperationType.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypes")
    void jsonMapping_mapsValue(final String value, final OperationType operationType) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(operationType));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationTypes")
    void jsonMapping_createsFromValue(final String value, final OperationType operationType) throws JsonProcessingException {
        Assertions.assertEquals(operationType, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', OperationType.class));
    }

}