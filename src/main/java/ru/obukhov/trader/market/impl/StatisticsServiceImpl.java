package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

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
            final CandleResolution candleResolution
    ) {
        final List<Candle> candles = marketService.getCandles(ticker, interval, candleResolution);
        return extendCandles(candles);
    }

    private GetCandlesResponse extendCandles(final List<Candle> candles) {
        final List<BigDecimal> averages = getAverages(candles);

        final List<OffsetDateTime> times = candles.stream().map(Candle::getTime).collect(Collectors.toList());
        final List<Integer> localMinimumsIndices = TrendUtils.getLocalExtremes(averages, Comparator.reverseOrder());
        final List<Integer> localMaximumsIndices = TrendUtils.getLocalExtremes(averages, Comparator.naturalOrder());

        final List<Point> localMinimumsPoints = TrendUtils.getLocalExtremes(averages, times, localMinimumsIndices);
        final List<Point> localMaximumsPoints = TrendUtils.getLocalExtremes(averages, times, localMaximumsIndices);
        final List<List<Point>> supportLines = TrendUtils.getRestraintLines(times, averages, localMinimumsIndices);
        final List<List<Point>> resistanceLines = TrendUtils.getRestraintLines(times, averages, localMaximumsIndices);

        return new GetCandlesResponse(
                candles,
                averages,
                localMinimumsPoints,
                localMaximumsPoints,
                supportLines,
                resistanceLines
        );
    }

    private List<BigDecimal> getAverages(final List<Candle> candles) {
        return TrendUtils.getExponentialWeightedMovingAverages(
                candles,
                Candle::getOpenPrice,
                0.3,
                3
        );
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