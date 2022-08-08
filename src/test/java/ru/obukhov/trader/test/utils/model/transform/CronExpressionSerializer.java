package ru.obukhov.trader.test.utils.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.quartz.CronExpression;

import java.io.IOException;

public class CronExpressionSerializer extends JsonSerializer<CronExpression> {

    @Override
    public void serialize(final CronExpression value, final JsonGenerator gen, final SerializerProvider serializerProvider) throws IOException {
        gen.writeString(value.toString());
    }

    @Override
    public Class<CronExpression> handledType() {
        return CronExpression.class;
    }

}