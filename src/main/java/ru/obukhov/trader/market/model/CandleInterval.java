package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum CandleInterval {
    _1MIN("1min"),
    _2MIN("2min"),
    _3MIN("3min"),
    _5MIN("5min"),
    _10MIN("10min"),
    _15MIN("15min"),
    _30MIN("30min"),
    HOUR("hour"),
    _2HOUR("2hour"),
    _4HOUR("4hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private static final Map<String, CandleInterval> LOOKUP = Stream.of(CandleInterval.values())
            .collect(Collectors.toMap(CandleInterval::getValue, candleInterval -> candleInterval));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static CandleInterval fromValue(String text) {
        return LOOKUP.get(text);
    }

}