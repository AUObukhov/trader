package ru.obukhov.trader.market.interfaces;

import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.market.model.TickerType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.util.List;

public interface StatisticsService {

    List<Candle> getCandles(String ticker, Interval interval, CandleResolution candleInterval);

    List<ExtendedCandle> getExtendedCandles(String ticker, Interval interval, CandleResolution candleInterval);

    List<MarketInstrument> getInstruments(@Nullable TickerType type);

}