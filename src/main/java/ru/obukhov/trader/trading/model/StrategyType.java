package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StrategyType {

    @JsonProperty("conservative")
    CONSERVATIVE,

    @JsonProperty("goldenCross")
    GOLDEN_CROSS,

}