package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class HistoricCandleSerializer extends StdSerializer<HistoricCandle> {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public HistoricCandleSerializer() {
        super(HistoricCandle.class);
    }

    @Override
    public void serialize(HistoricCandle value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        final BigDecimal open = QUOTATION_MAPPER.toBigDecimal(value.getOpen());
        final BigDecimal close = QUOTATION_MAPPER.toBigDecimal(value.getClose());
        final OffsetDateTime time = DATE_TIME_MAPPER.timestampToOffsetDateTime(value.getTime());

        gen.writeStartObject();
        gen.writeObjectField("open", open);
        gen.writeObjectField("close", close);
        gen.writeObjectField("time", time);
        gen.writeEndObject();
    }

}