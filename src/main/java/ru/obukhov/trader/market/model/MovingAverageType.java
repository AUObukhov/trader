package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Type of moving average algorithm
 *
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average">Moving average</a>
 */
@Getter
@AllArgsConstructor
public enum MovingAverageType {

    SIMPLE("SMA"),
    LINEAR_WEIGHTED("LWMA"),
    EXPONENTIAL_WEIGHTED("EWMA");

    private static final Map<String, MovingAverageType> LOOKUP = Stream.of(MovingAverageType.values())
            .collect(Collectors.toUnmodifiableMap(MovingAverageType::getValue, movingAverage -> movingAverage));

    @JsonValue
    private final String value;

    public static MovingAverageType from(String value) {
        return LOOKUP.get(value.toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }

}