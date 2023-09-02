package ru.obukhov.trader.market.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.core.MarketDataService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Service to get information about market prices and instruments
 */
@Slf4j
public class ExtMarketDataService {

    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);

    private final ExtInstrumentsService extInstrumentsService;
    private final MarketDataService marketDataService;
    private final ExtMarketDataService self;

    public ExtMarketDataService(
            final ExtInstrumentsService extInstrumentsService,
            final MarketDataService marketDataService,
            @Lazy final ExtMarketDataService self
    ) {
        this.extInstrumentsService = extInstrumentsService;
        this.marketDataService = marketDataService;
        this.self = self;
    }

    /**
     * Loads candles by conditions period by period.
     *
     * @param interval search interval, default {@code interval.from} is first available candle
     * @return sorted by time list of loaded candles
     */
    public List<Candle> getCandles(final String figi, final Interval interval, final CandleInterval candleInterval) {
        final Share share = extInstrumentsService.getShare(figi);
        final ChronoUnit period = DateUtils.getPeriodByCandleInterval(candleInterval);

        final List<Candle> candles = period == ChronoUnit.DAYS
                ? getAllCandlesByDays(share, interval, candleInterval)
                : getAllCandlesByYears(share, interval, candleInterval);

        log.info("Loaded {} candles for FIGI '{}'", candles.size(), figi);

        return candles.stream()
                .sorted(Comparator.comparing(Candle::getTime))
                .toList();
    }

    private List<Candle> getAllCandlesByDays(final Share share, final Interval interval, final CandleInterval candleInterval) {
        final OffsetDateTime innerFrom = ObjectUtils.defaultIfNull(interval.getFrom(), share.first1MinCandleDate());

        final List<Interval> subIntervals = Interval.of(innerFrom, interval.getTo()).splitIntoDailyIntervals();

        final List<List<Candle>> candles = new ArrayList<>();
        for (int i = subIntervals.size() - 1; i >= 0; i--) {
            final Interval subInterval = subIntervals.get(i);
            final List<Candle> currentCandles = loadCandlesBetterCacheable(share.figi(), subInterval.extendToDay(), subInterval, candleInterval);
            candles.add(currentCandles);
        }

        Collections.reverse(candles);
        return candles.stream().flatMap(Collection::stream).toList();
    }

    private List<Candle> getAllCandlesByYears(final Share share, final Interval interval, final CandleInterval candleInterval) {
        final OffsetDateTime innerFrom = ObjectUtils.defaultIfNull(interval.getFrom(), share.first1MinCandleDate());

        final List<Interval> subIntervals = Interval.of(innerFrom, interval.getTo()).splitIntoYearlyIntervals();

        final List<List<Candle>> candles = new ArrayList<>();
        for (int i = subIntervals.size() - 1; i >= 0; i--) {
            final Interval subInterval = subIntervals.get(i);
            final List<Candle> currentCandles = loadCandlesBetterCacheable(share.figi(), subInterval.extendToYear(), subInterval, candleInterval);
            candles.add(currentCandles);
        }

        Collections.reverse(candles);
        return candles.stream().flatMap(Collection::stream).toList();
    }

    /**
     * Loads candles from often used interval for better benefit from cache hits
     *
     * @param figi              FIGI of loaded candles
     * @param loadInterval      often used interval, better be whole day, year, etc.
     * @param effectiveInterval effective interval.
     *                          Must smaller or equal to {@code loadInterval}, otherwise values outside of {@code loadInterval} will be lost
     * @param candleInterval    interval of loaded candles
     * @return candles from given {@code effectiveInterval}
     */
    private List<Candle> loadCandlesBetterCacheable(
            final String figi,
            final Interval loadInterval,
            final Interval effectiveInterval,
            final CandleInterval candleInterval
    ) {
        final List<Candle> candles = self.getMarketCandles(figi, loadInterval, candleInterval);
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
     * Searches last price by {@code figi}
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    public BigDecimal getLastPrice(final String figi, final OffsetDateTime to) {
        final Share share = extInstrumentsService.getShare(figi);

        final List<Interval> intervals = Interval.of(share.first1MinCandleDate(), to).splitIntoDailyIntervals();
        for (int i = intervals.size() - 1; i >= 0; i--) {
            final Interval interval = intervals.get(i);
            final List<Candle> candles = loadCandlesBetterCacheable(figi, interval.extendToDay(), interval, CandleInterval.CANDLE_INTERVAL_1_MIN);
            final Candle lastCandle = CollectionUtils.lastElement(candles);
            if (lastCandle != null) {
                return lastCandle.getClose();
            }
        }

        throw new IllegalArgumentException("Not found last candle for FIGI '" + figi + "'");
    }

    /**
     * @return last {@code limit} candles by {@code figi}.
     * Searches from now to past. Stops searching when finds enough candles or when reaches first candle
     */
    public List<Candle> getLastCandles(
            final String figi,
            final int limit,
            final CandleInterval candleInterval,
            final OffsetDateTime currentDateTIme
    ) {
        final Share share = extInstrumentsService.getShare(figi);
        return DateUtils.getPeriodByCandleInterval(candleInterval) == ChronoUnit.DAYS
                ? getLastCandlesDaily(share, limit, candleInterval, currentDateTIme)
                : getLastCandlesYearly(share, limit, candleInterval, currentDateTIme);
    }

    /**
     * @return last {@code limit} candles by given {@code share}.
     * Searches from now to past. Stops searching when finds enough candles or when the total number of candles is less than {@code limit}.
     */
    private List<Candle> getLastCandlesDaily(
            final Share share,
            final int limit,
            final CandleInterval candleInterval,
            final OffsetDateTime currentTimestamp
    ) {
        final OffsetDateTime from = DateUtils.toStartOfDay(currentTimestamp);
        Interval interval = Interval.of(from, currentTimestamp);

        List<Candle> currentCandles = getMarketCandles(share.figi(), interval, candleInterval);
        final List<Candle> candles = new ArrayList<>(currentCandles);

        interval = interval.minusDays(1).extendToDay();

        do {
            currentCandles = self.getMarketCandles(share.figi(), interval, candleInterval);
            candles.addAll(currentCandles);
            interval = interval.minusDays(1);
        } while (candles.size() < limit && share.first1MinCandleDate().isBefore(interval.getTo()));

        candles.sort(Comparator.comparing(Candle::getTime));
        return CollectionsUtils.getTail(candles, limit);
    }

    /**
     * @return last {@code limit} candles by given {@code share}.
     * Searches from now to past. Stops searching when finds enough candles or when the total number of candles is less than {@code limit}.
     */
    private List<Candle> getLastCandlesYearly(
            final Share share,
            final int limit,
            final CandleInterval candleInterval,
            final OffsetDateTime currentDateTime
    ) {
        final OffsetDateTime from = DateUtils.atStartOfYear(currentDateTime);
        Interval interval = Interval.of(from, currentDateTime);

        List<Candle> currentCandles = getMarketCandles(share.figi(), interval, candleInterval);
        final List<Candle> candles = new ArrayList<>(currentCandles);

        interval = interval.minusYears(1).extendToYear();

        do {
            currentCandles = self.getMarketCandles(share.figi(), interval, candleInterval);
            candles.addAll(currentCandles);
            interval = interval.minusYears(1);
        } while (candles.size() < limit && !currentCandles.isEmpty());

        candles.sort(Comparator.comparing(Candle::getTime));
        return CollectionsUtils.getTail(candles, limit);
    }

    @Cacheable(value = "marketCandles", sync = true)
    public List<Candle> getMarketCandles(final String figi, final Interval interval, final CandleInterval candleInterval) {
        final Instant fromInstant = interval.getFrom().toInstant();
        final Instant toInstant = interval.getTo().toInstant();
        final List<Candle> candles = marketDataService.getCandlesSync(figi, fromInstant, toInstant, candleInterval)
                .stream()
                .filter(HistoricCandle::getIsComplete)
                .map(CANDLE_MAPPER::map)
                .toList();

        if (log.isDebugEnabled()) {
            log.debug("Loaded {} candles for FIGI '{}' in interval {}", candles.size(), figi, interval.toPrettyString());
        }
        return candles;
    }

    public SecurityTradingStatus getTradingStatus(final String id) {
        return marketDataService.getTradingStatusSync(id).getTradingStatus();
    }

}