package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.util.List;

public record Candles(String figi, CandleInterval interval, @JsonProperty("candles") List<Candle> candleList) {
}