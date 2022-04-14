package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Candles {

  private String figi;

  private CandleInterval interval;

  @JsonProperty("candles")
  private List<Candle> candleList;

}