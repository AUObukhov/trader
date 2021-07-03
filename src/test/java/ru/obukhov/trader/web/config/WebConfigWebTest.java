package ru.obukhov.trader.web.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.GenericConversionService;

@SpringBootTest(args = "--trading.token=i identify myself as token")
class WebConfigWebTest {

    @Autowired
    private GenericConversionService conversionService;

    @Test
    void initializesCandleResolutionConverter() {
        final String stringConversionService = conversionService.toString();

        Assertions.assertTrue(stringConversionService.contains("CandleResolutionConverter"));
    }

}