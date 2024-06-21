package ru.obukhov.trader.common.model.transform;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer extends StdSerializer<BigDecimal> {

    public BigDecimalSerializer() {
        super(BigDecimal.class);
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeNumber(DecimalUtils.stripTrailingZerosSafe(value));
    }
}