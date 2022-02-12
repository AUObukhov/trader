package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    private String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static OrderStatus fromValue(String text) {
        for (OrderStatus b : OrderStatus.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
