package ru.obukhov.investor.model.transform;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

@Component
@ConfigurationPropertiesBinding
public class OffsetTimeConverter implements Converter<String, OffsetTime> {

    @Override
    public OffsetTime convert(@NotNull String source) {
        return OffsetTime.parse(source, DateTimeFormatter.ISO_OFFSET_TIME);
    }
}