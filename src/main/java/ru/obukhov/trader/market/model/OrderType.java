package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum OrderType {

    LIMIT("Limit"),
    MARKET("Market");

    private static final Map<String, OrderType> LOOKUP = Stream.of(OrderType.values())
            .collect(Collectors.toMap(OrderType::getValue, orderType -> orderType));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static OrderType fromValue(String text) {
        return LOOKUP.get(text);
    }

}