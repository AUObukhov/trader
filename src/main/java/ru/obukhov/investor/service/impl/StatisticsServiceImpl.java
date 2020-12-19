package ru.obukhov.investor.service.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.StatisticsService;
import ru.obukhov.investor.util.CollectionsUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

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
     * Searches saldos by conditions and groups then by time
     *
     * @param ticker         ticker of candles
     * @param interval       search interval, default interval.from is start of trading, default interval.to is now
     * @param candleInterval candle interval, allowed values:
     *                       ONE_MIN, TWO_MIN, THREE_MIN, FIVE_MIN, TEN_MIN, QUARTER_HOUR, HALF_HOUR,
     *                       HOUR, TWO_HOURS, FOUR_HOURS
     * @return Map time to saldo
     */
    @Override
    public Map<Object, BigDecimal> getDailySaldos(String ticker, Interval interval, CandleInterval candleInterval) {

        return getSaldos(ticker, interval, candleInterval, OffsetDateTime::toLocalTime);
    }

    /**
     * Searches saldos by conditions and groups them by day of week
     *
     * @param ticker   ticker of candles
     * @param interval search interval, default interval.from is start of trading, default interval.to is now
     * @return Map day of week to saldo
     */
    @Override
    public Map<Object, BigDecimal> getWeeklySaldos(String ticker, Interval interval) {

        return getSaldos(ticker, interval, CandleInterval.DAY, OffsetDateTime::getDayOfWeek);

    }

    /**
     * Searches saldos by conditions and groups them by day of month
     *
     * @param ticker   ticker of candles
     * @param interval search interval, default interval.from is start of trading, default interval.to is now
     * @return Map day of month to saldo
     */
    @Override
    public Map<Object, BigDecimal> getMonthlySaldos(String ticker, Interval interval) {

        return getSaldos(ticker, interval, CandleInterval.DAY, OffsetDateTime::getDayOfMonth);

    }

    /**
     * Searches saldos by conditions and groups them by year
     *
     * @param ticker   ticker of candles
     * @param interval search interval, default interval.from is start of trading, default interval.to is now
     * @return Map year to saldo
     */
    @Override
    public Map<Object, BigDecimal> getYearlySaldos(String ticker, Interval interval) {

        return getSaldos(ticker, interval, CandleInterval.MONTH, OffsetDateTime::getMonth);

    }

    /**
     * Searches saldos by conditions and groups them by time unit provided by {@code keyExtractor}
     *
     * @param ticker         ticker of candles
     * @param interval       search interval, default interval.from is start of trading, default interval.to is now
     * @param candleInterval candle interval
     * @param keyExtractor   function getting of Map key from {@code OffsetDateTime}
     * @return ordered Map with saldos as values
     */
    private Map<Object, BigDecimal> getSaldos(String ticker,
                                              Interval interval,
                                              CandleInterval candleInterval,
                                              Function<OffsetDateTime, Object> keyExtractor) {

        List<Candle> candles = getCandles(ticker, interval, candleInterval);

        Multimap<Object, BigDecimal> saldosByTimes = (Multimap) MultimapBuilder.treeKeys().linkedListValues().build();
        for (Candle candle : candles) {
            saldosByTimes.put(keyExtractor.apply(candle.getTime()), candle.getSaldo());
        }

        return new TreeMap<>(CollectionsUtils.reduceMultimap(saldosByTimes, MathUtils::getAverage));

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