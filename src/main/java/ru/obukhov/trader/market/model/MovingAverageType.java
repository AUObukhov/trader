package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum MovingAverageType {

    SIMPLE("SMA", "simpleMovingAverager"),
    LINEAR_WEIGHTED("LWMA", "linearMovingAverager"),
    EXPONENTIAL_WEIGHTED("EWMA", "exponentialMovingAverager");

    private static final Map<String, MovingAverageType> LOOKUP = Stream.of(MovingAverageType.values())
            .collect(Collectors.toUnmodifiableMap(MovingAverageType::getValue, movingAverage -> movingAverage));

    @JsonValue
    private final String value;
    private final String averagerName;

    public static MovingAverageType from(String value) {
        return LOOKUP.get(value.toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }

}