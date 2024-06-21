package ru.obukhov.trader.common.model.transform;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalDeserializer extends StdDeserializer<BigDecimal> {

    public BigDecimalDeserializer() {
        super(BigDecimal.class);
    }

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        return DecimalUtils.setDefaultScale(node.asDouble());
    }

}