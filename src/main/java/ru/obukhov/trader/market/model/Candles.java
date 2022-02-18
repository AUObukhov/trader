package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Candles {
  @JsonProperty("figi")
  private String figi = null;

  @JsonProperty("interval")
  private CandleInterval interval = null;

  @JsonProperty("candles")
  private List<Candle> candles = new ArrayList<>();

  public Candles figi(String figi) {
    this.figi = figi;
    return this;
  }

  /**
   * Get figi
   *
   * @return figi
   **/
  @Schema(required = true, description = "")
  public String getFigi() {
    return figi;
  }

  public void setFigi(String figi) {
    this.figi = figi;
  }

  public Candles interval(CandleInterval interval) {
    this.interval = interval;
    return this;
  }

  /**
   * Get interval
   *
   * @return interval
   **/
  @Schema(required = true, description = "")
  public CandleInterval getInterval() {
    return interval;
  }

  public void setInterval(CandleInterval interval) {
    this.interval = interval;
  }

  public Candles candles(List<Candle> candles) {
    this.candles = candles;
    return this;
  }

  public Candles addCandlesItem(Candle candlesItem) {
    this.candles.add(candlesItem);
    return this;
  }

  /**
   * Get candles
   *
   * @return candles
   **/
  @Schema(required = true, description = "")
  public List<Candle> getCandles() {
    return candles;
  }

  public void setCandles(List<Candle> candles) {
    this.candles = candles;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Candles candles = (Candles) o;
    return Objects.equals(this.figi, candles.figi) &&
            Objects.equals(this.interval, candles.interval) &&
            Objects.equals(this.candles, candles.candles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(figi, interval, candles);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Candles {\n");

    sb.append("    figi: ").append(toIndentedString(figi)).append("\n");
    sb.append("    interval: ").append(toIndentedString(interval)).append("\n");
    sb.append("    candles: ").append(toIndentedString(candles)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
