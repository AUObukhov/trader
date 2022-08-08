package ru.obukhov.trader.common.model.transform;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

public class CandleIntervalConverter implements Converter<String, CandleInterval> {
    @Override
    public CandleInterval convert(@NotNull String source) {
        return CandleInterval.valueOf(source);
    }
}