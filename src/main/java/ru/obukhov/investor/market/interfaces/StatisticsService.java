package ru.obukhov.investor.market.interfaces;

import org.springframework.lang.Nullable;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.market.model.Candle;
import ru.obukhov.investor.market.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    List<Candle> getCandles(String ticker, Interval interval, CandleInterval candleInterval);

    Map<Object, BigDecimal> getDailySaldos(String ticker, Interval interval, CandleInterval candleInterval);

    Map<Object, BigDecimal> getWeeklySaldos(String ticker, Interval interval);

    Map<Object, BigDecimal> getMonthlySaldos(String ticker, Interval interval);

    Map<Object, BigDecimal> getYearlySaldos(String ticker, Interval interval);

    List<Instrument> getInstruments(@Nullable TickerType type);

}