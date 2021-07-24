package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.MovingAverager;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final int WINDOW = 2;
    private static final int ORDER = 1;

    private final MarketService marketService;
    private final MovingAverager averager;

    /**
     * Searches candles by conditions
     *
     * @param ticker           ticker of candles
     * @param interval         search interval, default interval.from is start of trading, default interval.to is now
     * @param candleResolution candle interval
     * @return list of found candles
     */
    @Override
    public List<Candle> getCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    ) {
        return marketService.getCandles(ticker, interval, candleResolution);
    }

    /**
     * Searches candles by conditions and calculates extra data by them
     *
     * @param ticker           ticker of candles
     * @param interval         search interval, default interval.from is start of trading, default interval.to is now
     * @param candleResolution candle interval
     * @return data structure with list of found candles and extra data
     */
    @Override
    public GetCandlesResponse getExtendedCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    ) {
        final List<Candle> candles = marketService.getCandles(ticker, interval, candleResolution);
        final List<BigDecimal> averages = averager.getAverages(candles, Candle::getOpenPrice, WINDOW, ORDER);
        return new GetCandlesResponse(candles, averages);
    }

    /**
     * @param type nullable instrument type
     * @return all instruments of given {@code type}, or all instruments at all if {@code type} is null
     */
    @Override
    public List<MarketInstrument> getInstruments(@Nullable final InstrumentType type) {
        return marketService.getInstruments(type);
    }

}