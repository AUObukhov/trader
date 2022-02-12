package ru.obukhov.trader.common.model.transform;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import ru.obukhov.trader.market.model.CandleResolution;

public class CandleResolutionConverter implements Converter<String, CandleResolution> {
    @Override
    public CandleResolution convert(@NotNull String source) {
        return CandleResolution.fromValue(source);
    }
}