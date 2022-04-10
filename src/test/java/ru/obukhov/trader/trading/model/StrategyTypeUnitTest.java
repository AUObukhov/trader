package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.TestUtils;

class StrategyTypeUnitTest {

    @Test
    void from_returnsProperValue() {
        for (final StrategyType strategyType : StrategyType.values()) {
            final StrategyType lookupValue = StrategyType.from(strategyType.getValue());

            Assertions.assertEquals(strategyType, lookupValue);
        }
    }

    @Test
    void testParsingFromJson() throws JsonProcessingException {
        for (final StrategyType strategyType : StrategyType.values()) {
            final String json = '"' + strategyType.getValue() + '"';
            final StrategyType parsedStrategyType = TestUtils.OBJECT_MAPPER.readValue(json, StrategyType.class);

            Assertions.assertEquals(strategyType, parsedStrategyType);
        }
    }

    @Test
    void testToString() {
        for (final StrategyType type : StrategyType.values()) {
            final String stringValue = type.toString();

            Assertions.assertEquals(type.getValue(), stringValue);
        }
    }

}