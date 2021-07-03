package ru.obukhov.trader.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.obukhov.trader.common.model.transform.CandleResolutionConverter;
import ru.obukhov.trader.common.model.transform.InstrumentTypeConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new CandleResolutionConverter());
        registry.addConverter(new InstrumentTypeConverter());
    }

}