package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.obukhov.trader.common.model.transform.CronExpressionSerializer;

import java.text.SimpleDateFormat;

public class TestUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new SimpleDateFormat("yyyy-MM-d24'T'HH:mm:ssZ"))
            .registerModule(new SimpleModule().addSerializer(new CronExpressionSerializer()));

}
