package ru.obukhov.investor.service;

import org.springframework.lang.Nullable;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    List<Candle> getCandles(String ticker,
                            OffsetDateTime from,
                            OffsetDateTime to,
                            CandleInterval candleInterval);

    Map<Object, BigDecimal> getDailySaldos(String ticker,
                                           OffsetDateTime from,
                                           OffsetDateTime to,
                                           CandleInterval candleInterval);

    Map<Object, BigDecimal> getWeeklySaldos(String ticker, OffsetDateTime from, OffsetDateTime to);

    Map<Object, BigDecimal> getMonthlySaldos(String ticker, OffsetDateTime from, OffsetDateTime to);

    Map<Object, BigDecimal> getYearlySaldos(String ticker, OffsetDateTime from, OffsetDateTime to);

    List<Instrument> getInstruments(@Nullable TickerType type);

}