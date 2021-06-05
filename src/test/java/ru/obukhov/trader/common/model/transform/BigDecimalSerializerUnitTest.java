package ru.obukhov.trader.common.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class BigDecimalSerializerUnitTest {

    private final BigDecimalSerializer serializer = new BigDecimalSerializer();

    @Mock
    private JsonGenerator generator;
    @Captor
    private ArgumentCaptor<BigDecimal> argumentCaptor;

    @Test
    void serialize_passesNull_whenValueIsNull() throws IOException {
        BigDecimal value = null;

        serializer.serialize(value, generator, null);

        Mockito.verify(generator, Mockito.times(1)).writeNumber(argumentCaptor.capture());

        Assertions.assertNull(argumentCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            ",",
            "100, 100.00000",
            "100.12345, 100.12345",
            "100.123454, 100.12345",
            "100.123456, 100.12346",
    })
    void serialize(BigDecimal value, BigDecimal expectedPassedValue) throws IOException {
        serializer.serialize(value, generator, null);

        Mockito.verify(generator, Mockito.times(1))
                .writeNumber(expectedPassedValue);
    }

}