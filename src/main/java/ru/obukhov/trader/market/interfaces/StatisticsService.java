package ru.obukhov.trader.market.interfaces;

import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.TickerType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.util.List;

public interface StatisticsService {

    List<Candle> getCandles(final String ticker, final Interval interval, final CandleResolution candleResolution);

    GetCandlesResponse getExtendedCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    );

    List<MarketInstrument> getInstruments(@Nullable final TickerType type);

}