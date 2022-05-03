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

    @Mock
    private JsonGenerator generator;
    @Captor
    private ArgumentCaptor<BigDecimal> argumentCaptor;

    private final BigDecimalSerializer serializer = new BigDecimalSerializer();

    @Test
    void serialize_passesNull_whenValueIsNull() throws IOException {
        final BigDecimal value = null;

        serializer.serialize(value, generator, null);

        Mockito.verify(generator, Mockito.times(1)).writeNumber(argumentCaptor.capture());

        Assertions.assertNull(argumentCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource({
            ",",
            "100, 100.000000000",
            "100.111123456, 100.111123456",
            "100.1111234564, 100.111123456",
            "100.1111234567, 100.111123457",
    })
    void serialize(BigDecimal value, BigDecimal expectedPassedValue) throws IOException {
        serializer.serialize(value, generator, null);

        Mockito.verify(generator, Mockito.times(1))
                .writeNumber(expectedPassedValue);
    }

}