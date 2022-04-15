package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum InstrumentType {

    STOCK("Stock"),
    CURRENCY("Currency"),
    BOND("Bond"),
    ETF("Etf");

    private static final Map<String, InstrumentType> LOOKUP = Stream.of(InstrumentType.values())
            .collect(Collectors.toMap(InstrumentType::getValue, instrumentType -> instrumentType));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static InstrumentType fromValue(String text) {
        return LOOKUP.get(text);
    }

}