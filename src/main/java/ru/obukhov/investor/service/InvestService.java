package ru.obukhov.investor.service;

import org.springframework.lang.Nullable;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

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

    List<Instrument> getInstruments(String token, @Nullable TickerType type);
}