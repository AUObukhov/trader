package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.TradingSchedule;

import java.io.IOException;

public class TradingScheduleSerializer extends JsonSerializer<TradingSchedule> {

    @Override
    public Class<TradingSchedule> handledType() {
        return TradingSchedule.class;
    }

    @Override
    public void serialize(final TradingSchedule value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        jgen.writeObjectField("exchange", value.getExchange());
        jgen.writeObjectField("days", value.getDaysList());
        jgen.writeEndObject();
    }

}