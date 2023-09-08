package ru.obukhov.trader.grafana.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.obukhov.trader.common.util.MapUtils;

import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor
public enum ColumnType {
    TIME("time"),
    NUMBER("number");

    private static final Map<String, ColumnType> LOOKUP = Stream.of(ColumnType.values())
            .collect(MapUtils.newMapKeyCollector(ColumnType::getValue));

    @Getter
    @JsonValue
    private final String value;

    public static ColumnType from(String value) {
        return LOOKUP.get(value);
    }
}