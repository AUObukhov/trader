package ru.obukhov.investor.service.interfaces;

import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.util.List;

public interface MarketService {

    List<Candle> getCandles(String ticker, Interval interval, CandleInterval candleInterval);

    Candle getLastCandle(String ticker);

    Candle getLastCandle(String ticker, OffsetDateTime to);

    List<Candle> getLastCandles(String ticker, int limit
    );

    Instrument getInstrument(String ticker);

    List<Instrument> getInstruments(TickerType type);

    String getFigi(String ticker);
}