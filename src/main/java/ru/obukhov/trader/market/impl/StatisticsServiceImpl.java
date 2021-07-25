package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final int ORDER = 1;

    private final MarketService marketService;

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
            final CandleResolution candleResolution,
            final MovingAverageType movingAverageType,
            final int smallWindow,
            final int bigWindow
    ) {
        final List<Candle> candles = marketService.getCandles(ticker, interval, candleResolution);

        final MovingAverager averager = MovingAverager.getByType(movingAverageType);
        final List<BigDecimal> openPrices = candles.stream().map(Candle::getOpenPrice).collect(Collectors.toList());
        final List<BigDecimal> shortAverages = averager.getAverages(openPrices, smallWindow, ORDER);
        final List<BigDecimal> longAverages = averager.getAverages(openPrices, bigWindow, ORDER);

        return new GetCandlesResponse(candles, shortAverages, longAverages);
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