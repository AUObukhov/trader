package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TradeStatus {
    NORMALTRADING("NormalTrading"),
    NOTAVAILABLEFORTRADING("NotAvailableForTrading");

    private String value;

    TradeStatus(String value) {
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
    public static TradeStatus fromValue(String text) {
        for (TradeStatus b : TradeStatus.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
