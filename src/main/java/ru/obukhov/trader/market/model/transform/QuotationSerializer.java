package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.io.IOException;

public class QuotationSerializer extends JsonSerializer<Quotation> {

    @Override
    public Class<Quotation> handledType() {
        return Quotation.class;
    }

    @Override
    public void serialize(final Quotation value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {
        jgen.writeRawValue(QuotationUtils.toPrettyString(value));
    }

}