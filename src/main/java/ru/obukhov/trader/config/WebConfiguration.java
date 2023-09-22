package ru.obukhov.trader.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.obukhov.trader.common.model.transform.MovingAverageTypeConverter;

@Configuration
@SuppressWarnings("unused")
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addConverter(new MovingAverageTypeConverter());
    }
}