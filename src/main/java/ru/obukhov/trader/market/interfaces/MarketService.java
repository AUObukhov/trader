package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.TickerType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.time.OffsetDateTime;
import java.util.List;

public interface MarketService {

    List<Candle> getCandles(String ticker, Interval interval, CandleResolution candleInterval);

    Candle getLastCandle(String ticker);

    Candle getLastCandle(String ticker, OffsetDateTime to);

    List<Candle> getLastCandles(String ticker, int limit, CandleResolution candleResolution);

    MarketInstrument getInstrument(String ticker);

    List<MarketInstrument> getInstruments(TickerType type);

    String getFigi(String ticker);

}