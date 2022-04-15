package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum OperationType {

    BUY("Buy"),
    SELL("Sell");

    private static final Map<String, OperationType> LOOKUP = Stream.of(OperationType.values())
            .collect(Collectors.toMap(OperationType::getValue, operationType -> operationType));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static OperationType fromValue(String text) {
        return LOOKUP.get(text);
    }

}