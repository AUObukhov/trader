package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.obukhov.trader.common.model.transform.BigDecimalDeserializer;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.transform.CronExpressionSerializer;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

public class TestUtils {

    private static final SimpleModule MODULE = new SimpleModule()
            .addSerializer(new CronExpressionSerializer())
            .addSerializer(new BigDecimalSerializer())
            .addDeserializer(BigDecimal.class, new BigDecimalDeserializer());
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new SimpleDateFormat(DateUtils.OFFSET_DATE_TIME_FORMAT))
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .registerModule(MODULE);

    public static List<HistoricCandle> getHistoricCandles(final String fileName) {
        final List<String> lines = ResourceUtils.getResourceAsStrings("candles/" + fileName);
        return lines.stream().map(TestUtils::parseHistoricCandle).toList();
    }

    public static HistoricCandle parseHistoricCandle(final String string) {
        String[] strings = string.split(",");
        return HistoricCandle.newBuilder()
                .setOpen(QuotationUtils.parseQuotation(strings[0]))
                .setClose(QuotationUtils.parseQuotation(strings[1]))
                .setTime(DateTimeTestData.newTimestamp(Long.parseLong(strings[2]) * 60))
                .setIsComplete(true)
                .build();
    }

}