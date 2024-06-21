package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.protobuf.Timestamp;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.io.IOException;
import java.time.OffsetDateTime;

public class HistoricCandleDeserializer extends StdDeserializer<HistoricCandle> {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public HistoricCandleDeserializer() {
        super(HistoricCandle.class);
    }

    @Override
    public HistoricCandle deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        final JsonNode openNode = node.get("open");
        final JsonNode closeNode = node.get("close");
        final JsonNode timeNode = node.get("time");

        return HistoricCandle.newBuilder()
                .setOpen(getQuotationValue(openNode))
                .setClose(getQuotationValue(closeNode))
                .setTime(getTimestampValue(timeNode, context))
                .setIsComplete(true)
                .build();
    }

    private static Quotation getQuotationValue(final JsonNode node) {
        return QUOTATION_MAPPER.fromDouble(node.asDouble());
    }

    private static Timestamp getTimestampValue(final JsonNode node, final DeserializationContext context) throws IOException {
        return DATE_TIME_MAPPER.offsetDateTimeToTimestamp(context.readTreeAsValue(node, OffsetDateTime.class));
    }

}