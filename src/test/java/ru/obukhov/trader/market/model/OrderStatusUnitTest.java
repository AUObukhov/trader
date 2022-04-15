package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class OrderStatusUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndOrderStatuses() {
        return Stream.of(
                Arguments.of("New", OrderStatus.NEW),
                Arguments.of("PartiallyFill", OrderStatus.PARTIALLYFILL),
                Arguments.of("Fill", OrderStatus.FILL),
                Arguments.of("Cancelled", OrderStatus.CANCELLED),
                Arguments.of("Replaced", OrderStatus.REPLACED),
                Arguments.of("PendingCancel", OrderStatus.PENDINGCANCEL),
                Arguments.of("Rejected", OrderStatus.REJECTED),
                Arguments.of("PendingReplace", OrderStatus.PENDINGREPLACE),
                Arguments.of("PendingNew", OrderStatus.PENDINGNEW)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderStatuses")
    void toString_returnsValue(final String expectedValue, final OrderStatus orderStatus) {
        Assertions.assertEquals(expectedValue, orderStatus.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderStatuses")
    void fromValue_returnProperEnum(final String value, final OrderStatus expectedOrderStatus) {
        Assertions.assertEquals(expectedOrderStatus, OrderStatus.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderStatuses")
    void jsonMapping_mapsValue(final String value, final OrderStatus orderStatus) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(orderStatus));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderStatuses")
    void jsonMapping_createsFromValue(final String value, final OrderStatus orderStatus) throws JsonProcessingException {
        Assertions.assertEquals(orderStatus, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', OrderStatus.class));
    }

}