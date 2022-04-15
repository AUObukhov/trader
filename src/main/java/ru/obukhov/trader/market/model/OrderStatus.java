package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum OrderStatus {

    NEW("New"),
    PARTIALLYFILL("PartiallyFill"),
    FILL("Fill"),
    CANCELLED("Cancelled"),
    REPLACED("Replaced"),
    PENDINGCANCEL("PendingCancel"),
    REJECTED("Rejected"),
    PENDINGREPLACE("PendingReplace"),
    PENDINGNEW("PendingNew");

    private static final Map<String, OrderStatus> LOOKUP = Stream.of(OrderStatus.values())
            .collect(Collectors.toMap(OrderStatus::getValue, orderStatus -> orderStatus));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static OrderStatus fromValue(String text) {
        return LOOKUP.get(text);
    }

}