package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.tinkoff.piapi.contract.v1.TradingDay;

import java.io.IOException;

public class TradingDaySerializer extends JsonSerializer<TradingDay> {

    @Override
    public Class<TradingDay> handledType() {
        return TradingDay.class;
    }

    @Override
    public void serialize(final TradingDay value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();

        jgen.writeObjectField("date", value.getDate());

        jgen.writeObjectField("isTradingDay", value.getIsTradingDay());

        jgen.writeObjectField("startTime", value.getStartTime());
        jgen.writeObjectField("endTime", value.getEndTime());

        jgen.writeObjectField("openingAuctionStartTime", value.getOpeningAuctionStartTime());
        jgen.writeObjectField("closingAuctionEndTime", value.getClosingAuctionEndTime());

        jgen.writeObjectField("eveningOpeningAuctionStartTime", value.getEveningOpeningAuctionStartTime());

        jgen.writeObjectField("eveningStartTime", value.getEveningStartTime());
        jgen.writeObjectField("eveningEndTime", value.getEveningEndTime());

        jgen.writeObjectField("clearingStartTime", value.getClearingStartTime());
        jgen.writeObjectField("clearingEndTime", value.getClearingEndTime());

        jgen.writeObjectField("premarketStartTime", value.getPremarketStartTime());
        jgen.writeObjectField("premarketEndTime", value.getPremarketEndTime());

        jgen.writeEndObject();
    }

}