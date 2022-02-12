package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BrokerAccountType {
    TINKOFF("Tinkoff"),
    TINKOFFIIS("TinkoffIis");

    private String value;

    BrokerAccountType(String value) {
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
    public static BrokerAccountType fromValue(String text) {
        for (BrokerAccountType b : BrokerAccountType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}