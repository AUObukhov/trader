package ru.obukhov.investor.market.interfaces;

import org.springframework.lang.Nullable;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.market.model.Candle;
import ru.obukhov.investor.market.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.util.List;

public interface StatisticsService {

    List<Candle> getCandles(String ticker, Interval interval, CandleInterval candleInterval);

    List<Instrument> getInstruments(@Nullable TickerType type);

}