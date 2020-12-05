package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.CollectionsUtils;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    static final int CONSECUTIVE_EMPTY_DAYS_LIMIT = 7;

    private final TinkoffService tinkoffService;

    /**
     * Load candles by conditions period by period.
     *
     * @return sorted by time list of loaded candles
     */
    @Override
    @Cacheable("candles")
    public List<Candle> getCandles(String ticker,
                                   @Nullable OffsetDateTime from,
                                   @Nullable OffsetDateTime to,
                                   CandleInterval interval) {

        Assert.isTrue(to == null || !to.isAfter(tinkoffService.getCurrentDateTime()),
                "'to' can't be in future");

        ChronoUnit period = DateUtils.getPeriodByCandleInterval(interval);

        List<Candle> candles = period == ChronoUnit.DAYS
                ? getAllCandlesByDays(ticker, from, to, interval)
                : getAllCandlesByYears(ticker, from, to, interval);

        log.info("Loaded " + candles.size() + " candles for ticker '" + ticker + "'");

        return candles.stream()
                .sorted(Comparator.comparing(Candle::getTime))
                .collect(Collectors.toList());

    }

    private List<Candle> getAllCandlesByDays(String ticker,
                                             @Nullable OffsetDateTime from,
                                             @Nullable OffsetDateTime to,
                                             CandleInterval candleInterval) {

        OffsetDateTime innerFrom = DateUtils.getDefaultFromIfNull(from);
        OffsetDateTime innerTo = ObjectUtils.defaultIfNull(to, tinkoffService.getCurrentDateTime());

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(innerFrom, innerTo);
        ListIterator<Pair<OffsetDateTime, OffsetDateTime>> listIterator = intervals.listIterator(intervals.size());

        List<List<Candle>> candles = new ArrayList<>();
        int emptyDaysCount = 0;
        while (listIterator.hasPrevious() && emptyDaysCount <= CONSECUTIVE_EMPTY_DAYS_LIMIT) {
            Pair<OffsetDateTime, OffsetDateTime> interval = listIterator.previous();
            List<Candle> currentCandles = loadDayCandles(ticker, interval, candleInterval);
            if (currentCandles.isEmpty()) {
                emptyDaysCount++;
            } else {
                emptyDaysCount = 0;
                candles.add(currentCandles);
            }
        }

        Collections.reverse(candles);
        return candles.stream().flatMap(Collection::stream).collect(Collectors.toList());

    }

    private List<Candle> getAllCandlesByYears(String ticker,
                                              @Nullable OffsetDateTime from,
                                              @Nullable OffsetDateTime to,
                                              CandleInterval interval) {

        final OffsetDateTime innerTo = ObjectUtils.defaultIfNull(to, tinkoffService.getCurrentDateTime());
        OffsetDateTime currentFrom = DateUtils.roundUpToYear(innerTo);
        OffsetDateTime currentTo;

        List<Candle> allCandles = new ArrayList<>();
        List<Candle> currentCandles;
        do {
            currentTo = currentFrom;
            currentFrom = DateUtils.minusLimited(currentFrom, 1, ChronoUnit.YEARS, from);

            currentCandles = loadCandles(ticker, currentFrom, currentTo, interval);
            allCandles.addAll(currentCandles);
        } while (DateUtils.isAfter(currentFrom, from) && !currentCandles.isEmpty());

        return allCandles;

    }

    private List<Candle> loadCandles(String ticker, OffsetDateTime from, OffsetDateTime to, CandleInterval interval) {
        OffsetDateTime innerTo = DateUtils.getEarliestDateTime(to, OffsetDateTime.now());
        return tinkoffService.getMarketCandles(ticker, from, innerTo, interval);
    }

    private List<Candle> loadDayCandles(String ticker,
                                        Pair<OffsetDateTime, OffsetDateTime> interval,
                                        CandleInterval candleInterval) {
        return loadDayCandles(ticker, interval.getLeft(), interval.getRight(), candleInterval);
    }

    private List<Candle> loadDayCandles(String ticker,
                                        OffsetDateTime from,
                                        OffsetDateTime to,
                                        CandleInterval candleInterval) {

        Assert.isTrue(DateUtils.equalDates(from, to), "'from' and 'to' must be at same day");

        final OffsetDateTime extendedFrom = DateUtils.atStartOfDay(from);
        final OffsetDateTime extendedTo = DateUtils.getEarliestDateTime(DateUtils.atEndOfDay(to), OffsetDateTime.now());

        return tinkoffService.getMarketCandles(ticker, extendedFrom, extendedTo, candleInterval)
                .stream()
                .filter(candle -> DateUtils.isBetween(candle.getTime(), from, to))
                .collect(Collectors.toList());

    }

    /**
     * Searches last candle by {@code ticker} within last {@link MarketServiceImpl#CONSECUTIVE_EMPTY_DAYS_LIMIT} days
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    @Override
    public Candle getLastCandle(String ticker) {
        OffsetDateTime to = tinkoffService.getCurrentDateTime();
        return getLastCandle(ticker, to);
    }

    /**
     * Searches last candle by {@code ticker} within last {@link MarketServiceImpl#CONSECUTIVE_EMPTY_DAYS_LIMIT} days
     * before {@code to}
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    @Override
    public Candle getLastCandle(String ticker, OffsetDateTime to) {
        OffsetDateTime candlesFrom = to.minusDays(CONSECUTIVE_EMPTY_DAYS_LIMIT);

        List<Pair<OffsetDateTime, OffsetDateTime>> intervals = DateUtils.splitIntervalIntoDays(candlesFrom, to);
        intervals = CollectionsUtils.getTail(intervals, CONSECUTIVE_EMPTY_DAYS_LIMIT + 1);
        ListIterator<Pair<OffsetDateTime, OffsetDateTime>> listIterator = intervals.listIterator(intervals.size());
        while (listIterator.hasPrevious()) {
            Pair<OffsetDateTime, OffsetDateTime> interval = listIterator.previous();
            List<Candle> candles = loadDayCandles(ticker, interval, CandleInterval.ONE_MIN);
            if (!candles.isEmpty()) {
                return CollectionUtils.lastElement(candles);
            }
        }

        throw new IllegalArgumentException("Not found last candle for ticker '" + ticker + "'");
    }

    @Override
    @Cacheable("instrument")
    public Instrument getInstrument(String ticker) {
        return getAllInstruments().stream()
                .filter(instrument -> instrument.ticker.equals(ticker))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return list of available instruments of given {@code type}
     */
    @Override
    @Cacheable("instruments")
    public List<Instrument> getInstruments(TickerType type) {
        if (type == null) {
            return getAllInstruments();
        }

        switch (type) {
            case ETF:
                return tinkoffService.getMarketEtfs();
            case STOCK:
                return tinkoffService.getMarketStocks();
            case BOND:
                return tinkoffService.getMarketBonds();
            case CURRENCY:
                return tinkoffService.getMarketCurrencies();
            default:
                throw new IllegalArgumentException("Unknown ticker type " + type);
        }
    }

    private List<Instrument> getAllInstruments() {

        List<Instrument> result = new ArrayList<>();
        result.addAll(tinkoffService.getMarketEtfs());
        result.addAll(tinkoffService.getMarketStocks());
        result.addAll(tinkoffService.getMarketBonds());
        result.addAll(tinkoffService.getMarketCurrencies());

        return result;
    }

    @Override
    public String getFigi(String ticker) {
        return tinkoffService.searchMarketInstrumentByTicker(ticker).figi;
    }

}