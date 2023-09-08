package ru.obukhov.trader.grafana.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.obukhov.trader.common.util.MapUtils;

import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor
public enum TableType {
    TABLE("table");

    private static final Map<String, TableType> LOOKUP = Stream.of(TableType.values())
            .collect(MapUtils.newMapKeyCollector(TableType::getValue));

    @Getter
    @JsonValue
    private final String value;

    public static TableType from(String value) {
        return LOOKUP.get(value);
    }
}