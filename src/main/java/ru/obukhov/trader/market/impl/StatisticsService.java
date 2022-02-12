package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service to get extended statistics about market prices and instruments
 */
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {

    private static final int ORDER = 1;

    private final MarketService marketService;
    private final ApplicationContext applicationContext;

    /**
     * Searches candles by conditions and calculates extra data by them
     *
     * @param ticker           ticker of candles
     * @param interval         search interval, default interval.from is start of trading, default interval.to is now
     * @param candleResolution candle interval
     * @return data structure with list of found candles and extra data
     */
    public GetCandlesResponse getExtendedCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution,
            final MovingAverageType movingAverageType,
            final int smallWindow,
            final int bigWindow
    ) {
        final List<Candle> candles = marketService.getCandles(ticker, interval, candleResolution);

        final MovingAverager averager = applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class);
        final List<BigDecimal> openPrices = candles.stream().map(Candle::getOpenPrice).toList();
        final List<BigDecimal> shortAverages = averager.getAverages(openPrices, smallWindow, ORDER);
        final List<BigDecimal> longAverages = averager.getAverages(openPrices, bigWindow, ORDER);

        return new GetCandlesResponse(candles, shortAverages, longAverages);
    }

}