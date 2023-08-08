package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Money;
import ru.obukhov.trader.market.model.transform.BondSerializer;
import ru.obukhov.trader.market.model.transform.InstrumentSerializer;
import ru.obukhov.trader.market.model.transform.MoneyValueSerializer;
import ru.obukhov.trader.market.model.transform.QuotationSerializer;
import ru.obukhov.trader.market.model.transform.ShareSerializer;
import ru.obukhov.trader.market.model.transform.TimestampSerializer;
import ru.obukhov.trader.market.model.transform.TradingDaySerializer;
import ru.obukhov.trader.market.model.transform.TradingScheduleSerializer;
import ru.obukhov.trader.test.utils.model.transform.CronExpressionSerializer;

import java.text.SimpleDateFormat;

public class TestUtils {

    private static final SimpleModule TINKOFF_MODULE = new SimpleModule()
            .addSerializer(new InstrumentSerializer())
            .addSerializer(new ShareSerializer())
            .addSerializer(new BondSerializer())
            .addSerializer(new TradingDaySerializer())
            .addSerializer(new TradingScheduleSerializer())
            .addSerializer(new QuotationSerializer())
            .addSerializer(new TimestampSerializer())
            .addSerializer(new MoneyValueSerializer());

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new SimpleDateFormat(DateUtils.OFFSET_DATE_TIME_FORMAT))
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .registerModule(TINKOFF_MODULE)
            .registerModule(new SimpleModule().addSerializer(new CronExpressionSerializer()));

    public static boolean equals(final Money money1, final Money money2) {
        return money1.currency().equals(money2.currency())
                && DecimalUtils.numbersEqual(money1.value(), money2.value());
    }

}