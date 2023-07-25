package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

import java.io.IOException;

public class MoneyValueSerializer extends JsonSerializer<MoneyValue> {

    @Override
    public Class<MoneyValue> handledType() {
        return MoneyValue.class;
    }

    @Override
    public void serialize(final MoneyValue value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();

        jgen.writeStringField("currency", value.getCurrency());
        jgen.writeNumberField("units", value.getUnits());
        jgen.writeNumberField("nano", value.getNano());

        jgen.writeEndObject();
    }

}