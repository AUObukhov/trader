package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstrumentType {
    STOCK("Stock"),
    CURRENCY("Currency"),
    BOND("Bond"),
    ETF("Etf");

    private String value;

    InstrumentType(String value) {
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
    public static InstrumentType fromValue(String text) {
        for (InstrumentType b : InstrumentType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
