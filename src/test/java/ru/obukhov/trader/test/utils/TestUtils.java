package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.test.utils.model.transform.CronExpressionSerializer;

import java.text.SimpleDateFormat;

public class TestUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new SimpleDateFormat("yyyy-MM-d24'T'HH:mm:ssZ"))
            .registerModule(new SimpleModule().addSerializer(new CronExpressionSerializer()));

    public static boolean equals(final MoneyAmount moneyAmount1, final MoneyAmount moneyAmount2) {
        return StringUtils.equalsIgnoreCase(moneyAmount1.currency(), moneyAmount2.currency())
                && DecimalUtils.numbersEqual(moneyAmount1.value(), moneyAmount2.value());
    }

}
