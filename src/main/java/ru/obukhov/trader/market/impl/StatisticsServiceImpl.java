package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Line;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.market.model.Extremum;
import ru.obukhov.trader.market.model.TickerType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final MarketService marketService;

    /**
     * Searches candles by conditions
     *
     * @param ticker         ticker of candles
     * @param interval       search interval, default interval.from is start of trading, default interval.to is now
     * @param candleInterval candle interval
     * @return list of found candles
     */
    @Override
    public List<Candle> getCandles(String ticker, Interval interval, CandleResolution candleInterval) {
        return marketService.getCandles(ticker, interval, candleInterval);
    }

    /**
     * Searches extended candles by conditions
     *
     * @param ticker         ticker of candles
     * @param interval       search interval, default interval.from is start of trading, default interval.to is now
     * @param candleInterval candle interval
     * @return list of found candles
     */
    @Override
    public List<ExtendedCandle> getExtendedCandles(String ticker, Interval interval, CandleResolution candleInterval) {
        List<Candle> candles = marketService.getCandles(ticker, interval, candleInterval);
        return createExtendedCandles(candles);
    }

    private List<ExtendedCandle> createExtendedCandles(List<Candle> candles) {
        List<ExtendedCandle> extendedCandles = new ArrayList<>(candles.size());
        List<BigDecimal> averages = getAverages(candles);
        for (int i = 0; i < candles.size(); i++) {
            extendedCandles.add(new ExtendedCandle(candles.get(i), averages.get(i)));
        }

        List<Integer> localMaximums = TrendUtils.getSortedLocalExtremes(averages, Comparator.naturalOrder());
        for (Integer localMaximum : localMaximums) {
            extendedCandles.get(localMaximum).setExtremum(Extremum.MAX);
        }

        List<Integer> localMinimums = TrendUtils.getSortedLocalExtremes(averages, Comparator.reverseOrder());
        for (Integer localMinimum : localMinimums) {
            extendedCandles.get(localMinimum).setExtremum(Extremum.MIN);
        }

        Line supportLine = getLine(averages, localMinimums);
        if (supportLine != null) {
            for (int i = 0; i < extendedCandles.size(); i++) {
                extendedCandles.get(i).setSupportValue(supportLine.getValue(i));
            }
        }

        Line resistanceLine = getLine(averages, localMaximums);
        if (resistanceLine != null) {
            for (int i = 0; i < extendedCandles.size(); i++) {
                extendedCandles.get(i).setResistanceValue(resistanceLine.getValue(i));
            }
        }

        return extendedCandles;
    }

    private List<BigDecimal> getAverages(List<Candle> candles) {
        return TrendUtils.getExponentialWeightedMovingAverages(
                candles,
                Candle::getOpenPrice,
                0.3,
                3
        );
    }

    private Line getLine(List<BigDecimal> values, List<Integer> localExtremes) {
        if (localExtremes.size() < 2) {
            log.warn("Not enough local extremes to get line");
            return null;
        }

        int x1 = localExtremes.get(0);
        BigDecimal y1 = values.get(x1);
        int x2 = localExtremes.get(1);
        BigDecimal y2 = values.get(x2);

        return new Line(x1, y1, x2, y2);
    }

    /**
     * @param type nullable ticker type
     * @return all instruments of given {@code type}, or all instruments at all if {@code type} is null
     */
    @Override
    public List<MarketInstrument> getInstruments(@Nullable TickerType type) {
        return marketService.getInstruments(type);
    }

}