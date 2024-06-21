package ru.obukhov.trader.common.model.transform;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalDeserializerUnitTest {

    private final BigDecimalDeserializer deserializer = new BigDecimalDeserializer();

    @Mock
    private static JsonParser parser;
    @Mock
    private static ObjectCodec codec;

    @BeforeAll
    public static void setUp() {
        parser = Mockito.mock(JsonParser.class);
        codec = Mockito.mock(ObjectCodec.class);
        Mockito.when(parser.getCodec()).thenReturn(codec);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.000000000",
            "1000.5, 1000.500000000",
            "0.123456789123, 0.123456789",
    })
    void deserialize(final double value, final BigDecimal expectedResult) throws IOException {
        final JsonNode node = new DoubleNode(value);
        Mockito.when(codec.readTree(parser)).thenReturn(node);

        final BigDecimal actualResult = deserializer.deserialize(parser, null);

        Assertions.assertEquals(expectedResult, actualResult);
    }

}