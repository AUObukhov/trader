package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MovingAverageTypeUnitTest {

    @Test
    void from_returnsProperValue() {
        for (final MovingAverageType type : MovingAverageType.values()) {
            final MovingAverageType lookupValue = MovingAverageType.from(type.getValue());

            Assertions.assertEquals(type, lookupValue);
        }
    }

    @Test
    void testParsingFromJson() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        for (final MovingAverageType type : MovingAverageType.values()) {
            final String json = '"' + type.getValue() + '"';
            final MovingAverageType parsedType = mapper.readValue(json, MovingAverageType.class);

            Assertions.assertEquals(type, parsedType);
        }
    }

    @Test
    void testToString() {
        for (final MovingAverageType type : MovingAverageType.values()) {
            final String stringValue = type.toString();

            Assertions.assertEquals(type.getValue(), stringValue);
        }
    }

}