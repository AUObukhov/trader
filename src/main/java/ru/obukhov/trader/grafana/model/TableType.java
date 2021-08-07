package ru.obukhov.trader.grafana.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public enum TableType {
    TABLE("table");

    private static final Map<String, TableType> LOOKUP = Stream.of(TableType.values())
            .collect(Collectors.toMap(TableType::getValue, tableType -> tableType));

    @Getter
    @JsonValue
    private final String value;

    public static TableType from(String value) {
        return LOOKUP.get(value);
    }
}