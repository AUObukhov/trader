package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Timestamp;

import java.io.IOException;

public class TimestampSerializer extends JsonSerializer<Timestamp> {

    @Override
    public Class<Timestamp> handledType() {
        return Timestamp.class;
    }

    @Override
    public void serialize(final Timestamp value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("seconds", value.getSeconds());
        jgen.writeNumberField("nanos", value.getNanos());
        jgen.writeEndObject();
    }

}