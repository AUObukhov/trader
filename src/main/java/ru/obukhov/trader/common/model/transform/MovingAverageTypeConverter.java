package ru.obukhov.trader.common.model.transform;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import ru.obukhov.trader.market.model.MovingAverageType;

public class MovingAverageTypeConverter implements Converter<String, MovingAverageType> {
    @Override
    public MovingAverageType convert(final @NotNull String source) {
        return MovingAverageType.from(source);
    }
}