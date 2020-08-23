package ru.obukhov.investor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TickerType {
    @JsonProperty("etf")
    ETF,
    @JsonProperty("stock")
    STOCK,
    @JsonProperty("bond")
    BOND,
    @JsonProperty("currency")
    CURRENCY
}