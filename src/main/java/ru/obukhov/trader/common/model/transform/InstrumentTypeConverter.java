package ru.obukhov.trader.common.model.transform;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import ru.obukhov.trader.market.model.InstrumentType;

public class InstrumentTypeConverter implements Converter<String, InstrumentType> {
    @Override
    public InstrumentType convert(final @NotNull String source) {
        return InstrumentType.fromValue(source);
    }
}