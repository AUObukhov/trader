package ru.obukhov.trader.common.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;

class BigDecimalSerializerUnitTest {

    private final BigDecimalSerializer serializer = new BigDecimalSerializer();

    @Mock
    private static JsonGenerator jsonGenerator;

    @BeforeAll
    public static void setUp() {
        jsonGenerator = Mockito.mock(JsonGenerator.class);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, null",
            "0.000000000, 0",
            "1000.500000000, 1000.5",
            "0.123456789123, 0.123456789123",
    },
            nullValues = "null")
    void serialize(final BigDecimal value, final BigDecimal expectedValue) throws IOException {
        serializer.serialize(value, jsonGenerator, null);
        Mockito.verify(jsonGenerator).writeNumber(expectedValue);
    }

}