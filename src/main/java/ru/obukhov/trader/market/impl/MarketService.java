package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * Service to get information about market prices and instruments
 */
@Slf4j
@RequiredArgsConstructor
public class MarketService {

    private final MarketProperties marketProperties;
    private final TinkoffService tinkoffService;

    /**
     * Load candles by conditions period by period.
     *
     * @return sorted by time list of loaded candles
     */
    public List<Candle> getCandles(final String ticker, final Interval interval, final CandleResolution candleResolution) {
        DateUtils.assertDateTimeNotFuture(interval.getTo(), tinkoffService.getCurrentDateTime(), "to");

        final ChronoUnit period = DateUtils.getPeriodByCandleResolution(candleResolution);

        final List<Candle> candles = period == ChronoUnit.DAYS
                ? getAllCandlesByDays(ticker, interval, candleResolution)
                : getAllCandlesByYears(ticker, interval, candleResolution);

        log.info("Loaded {} candles for ticker '{}'", candles.size(), ticker);

        return candles.stream()
                .sorted(Comparator.comparing(Candle::getTime))
                .toList();
    }

    private List<Candle> getAllCandlesByDays(final String ticker, final Interval interval, final CandleResolution candleResolution) {
        final OffsetDateTime innerFrom = ObjectUtils.defaultIfNull(interval.getFrom(), marketProperties.getStartDate());
        final OffsetDateTime innerTo = ObjectUtils.defaultIfNull(interval.getTo(), tinkoffService.getCurrentDateTime());

        final List<Interval> subIntervals = Interval.of(innerFrom, innerTo).splitIntoDailyIntervals();
        final ListIterator<Interval> listIterator = subIntervals.listIterator(subIntervals.size());

        final List<List<Candle>> candles = new ArrayList<>();
        int emptyDaysCount = 0;
        while (listIterator.hasPrevious() && emptyDaysCount <= marketProperties.getConsecutiveEmptyDaysLimit()) {
            final Interval subInterval = listIterator.previous();
            final List<Candle> currentCandles = loadCandlesBetterCacheable(ticker, subInterval.extendToDay(), subInterval, candleResolution);
            if (currentCandles.isEmpty()) {
                emptyDaysCount++;
            } else {
                emptyDaysCount = 0;
                candles.add(currentCandles);
            }
        }

        Collections.reverse(candles);
        return candles.stream().flatMap(Collection::stream).toList();
    }

    private List<Candle> getAllCandlesByYears(final String ticker, final Interval interval, final CandleResolution candleResolution) {
        final OffsetDateTime innerFrom = DateUtils.roundDownToYear(interval.getFrom());
        final OffsetDateTime innerTo = ObjectUtils.defaultIfNull(interval.getTo(), tinkoffService.getCurrentDateTime());
        OffsetDateTime currentFrom = DateUtils.roundUpToYear(innerTo);
        OffsetDateTime currentTo;

        final List<Candle> allCandles = new ArrayList<>();
        List<Candle> currentCandles;
        do {
            currentTo = currentFrom;
            currentFrom = DateUtils.minusLimited(currentFrom, 1, ChronoUnit.YEARS, innerFrom);

            currentCandles = loadCandles(ticker, currentFrom, currentTo, candleResolution)
                    .stream()
                    .filter(candle -> interval.contains(candle.getTime()))
                    .toList();
            allCandles.addAll(currentCandles);
        } while (DateUtils.isAfter(currentFrom, interval.getFrom()) && !currentCandles.isEmpty());

        return allCandles;
    }

    private List<Candle> loadCandles(
            final String ticker,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final CandleResolution candleResolution
    ) {
        final OffsetDateTime innerTo = DateUtils.getEarliestDateTime(to, tinkoffService.getCurrentDateTime());
        return tinkoffService.getMarketCandles(ticker, Interval.of(from, innerTo), candleResolution);
    }

    /**
     * Loads candles from often used interval for better benefit from cache hits
     *
     * @param ticker            ticker of loaded candles
     * @param loadInterval      often used interval, better be whole day, year, etc.
     * @param effectiveInterval effective interval.
     *                          Must smaller or equal to {@code loadInterval}, otherwise values outside of {@code loadInterval} will be lost
     * @param candleResolution  resolution of loaded candles
     * @return candles from given {@code effectiveInterval}
     */
    private List<Candle> loadCandlesBetterCacheable(
            final String ticker,
            final Interval loadInterval,
            final Interval effectiveInterval,
            final CandleResolution candleResolution
    ) {
        final List<Candle> candles = tinkoffService.getMarketCandles(ticker, loadInterval, candleResolution);
        return effectiveInterval.equals(loadInterval) ? candles : filterCandles(candles, effectiveInterval);
    }

    private List<Candle> filterCandles(final List<Candle> candles, final Interval interval) {
        final Candle leftCandle = new Candle().setTime(interval.getFrom());
        final Candle rightCandle = new Candle().setTime(interval.getTo());
        final Comparator<Candle> comparator = Comparator.comparing(Candle::getTime);
        final int fromIndex = CollectionsUtils.binarySearch(candles, leftCandle, comparator);
        final int toIndex = CollectionsUtils.binarySearch(candles, rightCandle, comparator);
        return candles.subList(fromIndex, toIndex);
    }

    /**
     * Searches last candle by {@code ticker} within last {@code trading.consecutive-empty-days-limit} days
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    public Candle getLastCandle(final String ticker) {
        final OffsetDateTime to = tinkoffService.getCurrentDateTime();
        return getLastCandle(ticker, to);
    }

    /**
     * Searches last candle by {@code ticker} within last {@code trading.consecutive-empty-days-limit} days
     * not after {@code to}
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    public Candle getLastCandle(final String ticker, final OffsetDateTime to) {
        final OffsetDateTime candlesFrom = to.minusDays(marketProperties.getConsecutiveEmptyDaysLimit());

        List<Interval> intervals = Interval.of(candlesFrom, to).splitIntoDailyIntervals();
        intervals = CollectionsUtils.getTail(intervals, marketProperties.getConsecutiveEmptyDaysLimit() + 1);
        final ListIterator<Interval> listIterator = intervals.listIterator(intervals.size());
        while (listIterator.hasPrevious()) {
            final Interval interval = listIterator.previous();
            final List<Candle> candles = loadCandlesBetterCacheable(ticker, interval.extendToDay(), interval, CandleResolution._1MIN);
            if (!candles.isEmpty()) {
                return CollectionUtils.lastElement(candles);
            }
        }

        throw new IllegalArgumentException("Not found last candle for ticker '" + ticker + "'");
    }

    /**
     * @return last {@code limit} candles by {@code ticker}.
     * Searches from now to past. Stops searching when finds enough candles or when consecutively getting no candles
     * within {@code trading.consecutive-empty-days-limit} days or one year (when candleResolution >= 1 day).
     */
    public List<Candle> getLastCandles(final String ticker, final int limit, final CandleResolution candleResolution) {
        return DateUtils.getPeriodByCandleResolution(candleResolution) == ChronoUnit.DAYS
                ? getLastCandlesDaily(ticker, limit, candleResolution)
                : getLastCandlesYearly(ticker, limit, candleResolution);
    }

    /**
     * @return last {@code limit} candles by {@code ticker}.
     * Searches from now to past. Stops searching when finds enough candles or when consecutively getting no candles
     * within {@code trading.consecutive-empty-days-limit} days.
     */
    private List<Candle> getLastCandlesDaily(final String ticker, final int limit, final CandleResolution candleResolution) {
        final OffsetDateTime to = tinkoffService.getCurrentDateTime();
        final OffsetDateTime from = DateUtils.atStartOfDay(to);
        Interval interval = Interval.of(from, to);

        List<Candle> currentCandles = loadCandlesBetterCacheable(ticker, interval.extendToDay(), interval, candleResolution);
        final List<Candle> candles = new ArrayList<>(currentCandles);
        int consecutiveEmptyDaysCount = candles.isEmpty() ? 1 : 0;

        interval = interval.minusDays(1).extendToDay();

        do {
            currentCandles = tinkoffService.getMarketCandles(ticker, interval, candleResolution);
            if (currentCandles.isEmpty()) {
                consecutiveEmptyDaysCount++;
            } else {
                consecutiveEmptyDaysCount = 0;
                candles.addAll(currentCandles);
            }

            interval = interval.minusDays(1);
        } while (candles.size() < limit && consecutiveEmptyDaysCount <= marketProperties.getConsecutiveEmptyDaysLimit());

        candles.sort(Comparator.comparing(Candle::getTime));
        return CollectionsUtils.getTail(candles, limit);
    }

    private List<Candle> getLastCandlesYearly(String ticker, int limit, CandleResolution candleResolution) {
        final OffsetDateTime to = tinkoffService.getCurrentDateTime();
        final OffsetDateTime from = DateUtils.atStartOfYear(to);
        Interval interval = Interval.of(from, to);

        List<Candle> currentCandles = loadCandlesBetterCacheable(ticker, interval.extendToYear(), interval, candleResolution);
        final List<Candle> candles = new ArrayList<>(currentCandles);

        interval = interval.minusYears(1).extendToYear();

        do {
            currentCandles = tinkoffService.getMarketCandles(ticker, interval, candleResolution);
            candles.addAll(currentCandles);
            interval = interval.minusYears(1);
        } while (candles.size() < limit && !currentCandles.isEmpty());

        candles.sort(Comparator.comparing(Candle::getTime));
        return CollectionsUtils.getTail(candles, limit);
    }

    /**
     * @return market instrument with given {@code ticker}, or null if it does not exist
     */
    public MarketInstrument getInstrument(final String ticker) {
        return getAllInstruments().stream()
                .filter(instrument -> instrument.getTicker().equals(ticker))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return list of available instruments of given {@code type} or all instruments if {@code type} is null
     * @throws IllegalArgumentException when {@code type} is not
     *                                  {@code ETF}, {@code STOCK}, {@code BOND}, or {@code CURRENCY}
     */
    public List<MarketInstrument> getInstruments(final InstrumentType type) {
        if (type == null) {
            return getAllInstruments();
        }

        return switch (type) {
            case ETF -> tinkoffService.getMarketEtfs();
            case STOCK -> tinkoffService.getMarketStocks();
            case BOND -> tinkoffService.getMarketBonds();
            case CURRENCY -> tinkoffService.getMarketCurrencies();
        };
    }

    private List<MarketInstrument> getAllInstruments() {
        List<MarketInstrument> result = new ArrayList<>();
        result.addAll(tinkoffService.getMarketEtfs());
        result.addAll(tinkoffService.getMarketStocks());
        result.addAll(tinkoffService.getMarketBonds());
        result.addAll(tinkoffService.getMarketCurrencies());

        return result;
    }

    /**
     * @return FIGI of market instrument with given {@code ticker}
     * @throws NullPointerException if instrument does not exist
     */
    public String getFigi(final String ticker) {
        return tinkoffService.searchMarketInstrument(ticker).getFigi();
    }

}