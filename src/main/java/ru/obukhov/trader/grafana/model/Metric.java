package ru.obukhov.trader.grafana.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Metric {
    CANDLES("Candles"),
    EXTENDED_CANDLES("Extended candles");

    @Getter
    @JsonValue
    private final String value;

    public static Metric from(String value) {
        for (Metric metric : Metric.values()) {
            if (metric.getValue().equalsIgnoreCase(value)) {
                return metric;
            }
        }

        throw new IllegalArgumentException("Unknown metric value: " + value);
    }
}