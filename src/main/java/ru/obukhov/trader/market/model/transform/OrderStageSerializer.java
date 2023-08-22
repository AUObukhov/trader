package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.OrderStage;

import java.io.IOException;

public class OrderStageSerializer extends JsonSerializer<OrderStage> {

    @Override
    public Class<OrderStage> handledType() {
        return OrderStage.class;
    }

    @Override
    public void serialize(final OrderStage value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();

        jgen.writeObjectField("price", value.getPrice());
        jgen.writeNumberField("quantity", value.getQuantity());
        jgen.writeStringField("tradeId", value.getTradeId());

        jgen.writeEndObject();
    }

}