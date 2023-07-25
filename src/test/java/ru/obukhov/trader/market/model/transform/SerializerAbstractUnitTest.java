package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.StringWriter;

abstract class SerializerAbstractUnitTest<T> {

    protected void test(
            final JsonSerializer<T> serializer,
            final T object,
            final String expectedResult,
            final JsonSerializer<?>... subSerializers
    ) throws IOException {
        final StringWriter stringWriter = new StringWriter();

        final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(stringWriter);
        jsonGenerator.setCodec(createCodec(subSerializers));

        serializer.serialize(object, jsonGenerator, null);

        jsonGenerator.close();

        final String result = stringWriter.toString();

        Assertions.assertEquals(expectedResult, result);
    }

    private static ObjectMapper createCodec(final JsonSerializer<?>[] subSerializers) {
        final SimpleModule module = new SimpleModule();
        for (JsonSerializer<?> subSerializer : subSerializers) {
            module.addSerializer(subSerializer);
        }
        return new ObjectMapper()
                .registerModule(module);
    }

}