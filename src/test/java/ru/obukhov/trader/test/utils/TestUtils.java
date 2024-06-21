package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.obukhov.trader.common.model.transform.BigDecimalDeserializer;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.model.transform.CronExpressionSerializer;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class TestUtils {

    private static final SimpleModule MODULE = new SimpleModule()
            .addSerializer(new CronExpressionSerializer())
            .addSerializer(new BigDecimalSerializer())
            .addSerializer(new HistoricCandleSerializer())
            .addDeserializer(BigDecimal.class, new BigDecimalDeserializer())
            .addDeserializer(HistoricCandle.class, new HistoricCandleDeserializer());
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new SimpleDateFormat(DateUtils.OFFSET_DATE_TIME_FORMAT))
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .registerModule(MODULE);

}