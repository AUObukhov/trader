package ru.obukhov.trader.market.interfaces;

import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.util.List;

public interface StatisticsService {

    List<Candle> getCandles(final String ticker, final Interval interval, final CandleResolution candleResolution);

    GetCandlesResponse getExtendedCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution,
            final MovingAverageType movingAverageType,
            final int smallWindow,
            final int bigWindow
    );

    List<MarketInstrument> getInstruments(@Nullable final InstrumentType type);

}