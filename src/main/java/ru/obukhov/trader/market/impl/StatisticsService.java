package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.util.List;

/**
 * Service to get extended statistics about market prices and instruments
 */
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {

    private static final int ORDER = 1;

    private final ExtMarketDataService extMarketDataService;
    private final ApplicationContext applicationContext;

    /**
     * Searches candles by conditions and calculates extra data by them
     *
     * @param figi           Financial Instrument Global Identifier
     * @param interval       search interval, default {@code interval.from} is start of trading, default {@code interval.to} is now
     * @param candleInterval candle interval
     * @return data structure with list of found candles and extra data
     */
    public GetCandlesResponse getExtendedCandles(
            final String figi,
            final Interval interval,
            final CandleInterval candleInterval,
            final MovingAverageType movingAverageType,
            final int smallWindow,
            final int bigWindow
    ) {
        final Interval innerInterval = Interval.of(interval.getFrom(), TimestampUtils.nowIfNull(interval.getTo()));
        final List<Candle> candles = extMarketDataService.getCandles(figi, innerInterval, candleInterval);

        final MovingAverager averager = applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class);
        final List<Quotation> opens = candles.stream().map(Candle::getOpen).toList();
        final List<Quotation> shortAverages = averager.getAverages(opens, smallWindow, ORDER);
        final List<Quotation> longAverages = averager.getAverages(opens, bigWindow, ORDER);

        return new GetCandlesResponse(candles, shortAverages, longAverages);
    }

}