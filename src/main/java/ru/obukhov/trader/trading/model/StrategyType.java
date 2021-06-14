package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StrategyType {

    @JsonProperty("conservative")
    CONSERVATIVE,

    @JsonProperty("simpleGoldenCross")
    SIMPLE_GOLDEN_CROSS,

    @JsonProperty("linearGoldenCross")
    LINEAR_GOLDEN_CROSS,

}