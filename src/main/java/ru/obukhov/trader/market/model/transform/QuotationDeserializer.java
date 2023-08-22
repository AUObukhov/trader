package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.io.IOException;

public class QuotationDeserializer extends JsonDeserializer<Quotation> {

    @Override
    public Class<Quotation> handledType() {
        return Quotation.class;
    }

    @Override
    public Quotation deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return QuotationUtils.newQuotation(node.asDouble());
    }

}