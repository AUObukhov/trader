package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.transform.CurrencySerializer;
import ru.obukhov.trader.market.model.transform.MoneyValueSerializer;
import ru.obukhov.trader.market.model.transform.QuotationDeserializer;
import ru.obukhov.trader.market.model.transform.QuotationSerializer;
import ru.obukhov.trader.market.model.transform.TimestampSerializer;
import ru.obukhov.trader.test.utils.model.transform.CronExpressionSerializer;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.text.SimpleDateFormat;

public class TestUtils {

    private static final SimpleModule TINKOFF_MODULE = new SimpleModule()
            .addSerializer(new CurrencySerializer())
            .addSerializer(new QuotationSerializer())
            .addSerializer(new TimestampSerializer())
            .addSerializer(new MoneyValueSerializer())
            .addDeserializer(Quotation.class, new QuotationDeserializer());

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new SimpleDateFormat(DateUtils.OFFSET_DATE_TIME_FORMAT))
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .registerModule(TINKOFF_MODULE)
            .registerModule(new SimpleModule().addSerializer(new CronExpressionSerializer()));

}