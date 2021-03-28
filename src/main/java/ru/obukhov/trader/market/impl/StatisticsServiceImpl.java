package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.market.model.Extremum;
import ru.obukhov.trader.market.model.TickerType;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

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
    public List<Candle> getCandles(String ticker, Interval interval, CandleInterval candleInterval) {
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
    public List<ExtendedCandle> getExtendedCandles(String ticker, Interval interval, CandleInterval candleInterval) {
        List<Candle> candles = marketService.getCandles(ticker, interval, candleInterval);
        return createExtendedCandles(candles);
    }

    private List<ExtendedCandle> createExtendedCandles(List<Candle> candles) {
        List<ExtendedCandle> extendedCandles = new ArrayList<>(candles.size());
        List<BigDecimal> averages = getAverages(candles);
        for (int i = 0; i < candles.size(); i++) {
            extendedCandles.add(new ExtendedCandle(candles.get(i), averages.get(i)));
        }

        fillExtremes(extendedCandles);

        return extendedCandles;
    }

    private List<BigDecimal> getAverages(List<Candle> candles) {
        return MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder(
                candles,
                Candle::getOpenPrice,
                0.3,
                3
        );
    }

    private void fillExtremes(List<ExtendedCandle> extendedCandles) {
        List<Integer> localMaximums =
                MathUtils.getLocalExtremes(extendedCandles, ExtendedCandle::getAveragePrice, Comparator.naturalOrder());
        for (Integer localMaximum : localMaximums) {
            extendedCandles.get(localMaximum).setExtremum(Extremum.MAX);
        }

        List<Integer> localMinimums =
                MathUtils.getLocalExtremes(extendedCandles, ExtendedCandle::getAveragePrice, Comparator.reverseOrder());
        for (Integer localMinimum : localMinimums) {
            extendedCandles.get(localMinimum).setExtremum(Extremum.MIN);
        }
    }

    /**
     * @param type nullable ticker type
     * @return all instruments of given {@code type}, or all instruments at all if {@code type} is null
     */
    @Override
    public List<Instrument> getInstruments(@Nullable TickerType type) {
        return marketService.getInstruments(type);
    }

}