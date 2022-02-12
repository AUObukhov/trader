package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Интервал свечи и допустимый промежуток запроса: - 1min [1 minute, 1 day] - 2min [2 minutes, 1 day] - 3min [3 minutes, 1 day] - 5min [5 minutes, 1 day] - 10min [10 minutes, 1 day] - 15min [15 minutes, 1 day] - 30min [30 minutes, 1 day] - hour [1 hour, 7 days] - day [1 day, 1 year] - week [7 days, 2 years] - month [1 month, 10 years]
 */
public enum CandleResolution {
  _1MIN("1min"),
  _2MIN("2min"),
  _3MIN("3min"),
  _5MIN("5min"),
  _10MIN("10min"),
  _15MIN("15min"),
  _30MIN("30min"),
  HOUR("hour"),
  DAY("day"),
  WEEK("week"),
  MONTH("month");

  private String value;

  CandleResolution(String value) {
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
  public static CandleResolution fromValue(String text) {
    for (CandleResolution b : CandleResolution.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
