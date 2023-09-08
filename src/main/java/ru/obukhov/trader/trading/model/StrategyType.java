package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.obukhov.trader.common.util.MapUtils;

import java.util.Map;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum StrategyType {

    CONSERVATIVE("conservative"),
    CROSS("cross");

    private static final Map<String, StrategyType> LOOKUP = Stream.of(StrategyType.values())
            .collect(MapUtils.newMapKeyCollector(StrategyType::getValue));

    @JsonValue
    private final String value;

    public static StrategyType fromValue(String value) {
        return LOOKUP.get(value);
    }

    @Override
    public String toString() {
        return value;
    }

}