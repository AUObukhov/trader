package ru.obukhov.trader.market.interfaces;

import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.market.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.util.List;

public interface StatisticsService {

    List<Candle> getCandles(String ticker, Interval interval, CandleInterval candleInterval);

    List<ExtendedCandle> getExtendedCandles(String ticker, Interval interval, CandleInterval candleInterval);

    List<Instrument> getInstruments(@Nullable TickerType type);

}