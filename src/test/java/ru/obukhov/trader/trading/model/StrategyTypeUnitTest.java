package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        final ObjectMapper mapper = new ObjectMapper();
        for (final StrategyType strategyType : StrategyType.values()) {
            final String json = '"' + strategyType.getValue() + '"';
            final StrategyType parsedStrategyType = mapper.readValue(json, StrategyType.class);

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