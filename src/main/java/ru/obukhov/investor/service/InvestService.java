package ru.obukhov.investor.service;

import ru.obukhov.investor.model.Candle;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface InvestService {

    List<Candle> getCandles(String token,
                            String ticker,
                            OffsetDateTime from,
                            OffsetDateTime to,
                            CandleInterval candleInterval);

    Map<LocalTime, BigDecimal> getDailySaldos(String token,
                                              String ticker,
                                              OffsetDateTime from,
                                              OffsetDateTime to,
                                              CandleInterval candleInterval);

    Map<DayOfWeek, BigDecimal> getWeeklySaldos(String token,
                                               String ticker,
                                               OffsetDateTime from,
                                               OffsetDateTime to);
}