package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.TestUtils;

import java.util.stream.Stream;

class TradeStatusUnitTest {

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndTradeStatuses() {
        return Stream.of(
                Arguments.of("NormalTrading", TradeStatus.NORMALTRADING),
                Arguments.of("NotAvailableForTrading", TradeStatus.NOTAVAILABLEFORTRADING)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndTradeStatuses")
    void toString_returnsValue(final String expectedValue, final TradeStatus tradeStatus) {
        Assertions.assertEquals(expectedValue, tradeStatus.toString());
    }

    @ParameterizedTest
    @MethodSource("valuesAndTradeStatuses")
    void fromValue_returnProperEnum(final String value, final TradeStatus expectedTradeStatus) {
        Assertions.assertEquals(expectedTradeStatus, TradeStatus.fromValue(value));
    }

    @ParameterizedTest
    @MethodSource("valuesAndTradeStatuses")
    void jsonMapping_mapsValue(final String value, final TradeStatus tradeStatus) throws JsonProcessingException {
        Assertions.assertEquals('"' + value + '"', TestUtils.OBJECT_MAPPER.writeValueAsString(tradeStatus));
    }

    @ParameterizedTest
    @MethodSource("valuesAndTradeStatuses")
    void jsonMapping_createsFromValue(final String value, final TradeStatus tradeStatus) throws JsonProcessingException {
        Assertions.assertEquals(tradeStatus, TestUtils.OBJECT_MAPPER.readValue('"' + value + '"', TradeStatus.class));
    }

}