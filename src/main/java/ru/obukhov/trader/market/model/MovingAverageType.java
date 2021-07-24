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

    SIMPLE("simple"),
    LINEAR_WEIGHTED("linearWeighted"),
    EXPONENTIAL_WEIGHTED("exponentialWeighted");

    private static final Map<String, MovingAverageType> LOOKUP = Stream.of(MovingAverageType.values())
            .collect(Collectors.toMap(MovingAverageType::getValue, movingAverage -> movingAverage));

    @JsonValue
    private final String value;

    public static MovingAverageType from(String value) {
        return LOOKUP.get(value);
    }

    @Override
    public String toString() {
        return value;
    }

}