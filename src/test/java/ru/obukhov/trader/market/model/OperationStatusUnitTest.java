package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class OperationStatusUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndOperationStatuses() {
        return Stream.of(
                Arguments.of("Done", OperationStatus.DONE),
                Arguments.of("Decline", OperationStatus.DECLINE),
                Arguments.of("Progress", OperationStatus.PROGRESS)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationStatuses")
    void toString_returnsValue(final String expectedValue, final OperationStatus operationStatus) {
        Assertions.assertEquals(expectedValue, operationStatus.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationStatuses")
    void fromValue_returnProperEnum(final String value, final OperationStatus expectedOperationStatus) {
        Assertions.assertEquals(expectedOperationStatus, OperationStatus.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationStatuses")
    void jsonMapping_mapsValue(final String value, final OperationStatus operationStatus) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(operationStatus));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOperationStatuses")
    void jsonMapping_createsFromValue(final String value, final OperationStatus operationStatus) throws JsonProcessingException {
        Assertions.assertEquals(operationStatus, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', OperationStatus.class));
    }

}