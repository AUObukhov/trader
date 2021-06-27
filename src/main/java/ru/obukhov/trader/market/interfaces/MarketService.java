package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.TickerType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.time.OffsetDateTime;
import java.util.List;

public interface MarketService {

    List<Candle> getCandles(final String ticker, final Interval interval, final CandleResolution candleResolution);

    Candle getLastCandle(final String ticker);

    Candle getLastCandle(final String ticker, final OffsetDateTime to);

    @NotNull
    List<Candle> getLastCandles(final String ticker, final int limit, final CandleResolution candleResolution);

    MarketInstrument getInstrument(final String ticker);

    List<MarketInstrument> getInstruments(final TickerType type);

    String getFigi(final String ticker);

}