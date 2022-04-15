package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class OrderTypeUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndOrderTypes() {
        return Stream.of(
                Arguments.of("Limit", OrderType.LIMIT),
                Arguments.of("Market", OrderType.MARKET)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderTypes")
    void toString_returnsValue(final String expectedValue, final OrderType orderType) {
        Assertions.assertEquals(expectedValue, orderType.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderTypes")
    void fromValue_returnProperEnum(final String value, final OrderType expectedOrderType) {
        Assertions.assertEquals(expectedOrderType, OrderType.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderTypes")
    void jsonMapping_mapsValue(final String value, final OrderType orderType) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(orderType));
    }

    @ParameterizedTest
    @MethodSource("valuesAndOrderTypes")
    void jsonMapping_createsFromValue(final String value, final OrderType orderType) throws JsonProcessingException {
        Assertions.assertEquals(orderType, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', OrderType.class));
    }

}