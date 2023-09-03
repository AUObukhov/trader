package ru.obukhov.trader.grafana.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public enum TargetType {

    TIME_SERIES("timeseries"),
    TABLE("table");

    private static final Map<String, TargetType> LOOKUP = Stream.of(TargetType.values())
            .collect(Collectors.toMap(TargetType::getValue, Function.identity()));

    @Getter
    @JsonValue
    private final String value;

    public static TargetType from(String value) {
        return LOOKUP.get(value);
    }
}
