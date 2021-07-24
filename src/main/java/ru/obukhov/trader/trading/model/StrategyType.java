package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum StrategyType {

    CONSERVATIVE("conservative"),
    GOLDEN_CROSS("goldenCross");

    private static final Map<String, StrategyType> LOOKUP = Stream.of(StrategyType.values())
            .collect(Collectors.toMap(StrategyType::getValue, strategyType -> strategyType));

    @JsonValue
    private final String value;

    public static StrategyType from(String value) {
        return LOOKUP.get(value);
    }

    @Override
    public String toString() {
        return value;
    }

}