package ru.obukhov.trader.market.impl;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.exception.MultipleInstrumentsFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Periods;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.FirstCandleUtils;
import ru.obukhov.trader.common.util.SingleItemCollector;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.core.MarketDataService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to get information about market prices and instruments
 */
@Slf4j
@Service
public class ExtMarketDataService {

    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

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
        final Period period = Periods.getPeriodByCandleInterval(candleInterval);
        final OffsetDateTime adjustedFrom = adjustFrom(interval.getFrom(), share, candleInterval);
        final List<Interval> subIntervals = Interval.of(adjustedFrom, interval.getTo()).splitIntoIntervals(period);

        final List<Candle> candles = new ArrayList<>();
        for (final Interval subInterval : subIntervals) {
            final List<Candle> currentCandles = loadCandlesBetterCacheable(share.figi(), subInterval.extendTo(period), subInterval, candleInterval);
            candles.addAll(currentCandles);
        }
        log.info("Loaded {} candles of {} size for FIGI '{}' in interval [{}]", candles.size(), candleInterval, figi, interval.toPrettyString());

        return candles;
    }

    private OffsetDateTime adjustFrom(final OffsetDateTime from, final Share share, final CandleInterval candleInterval) {
        return from == null
                ? FirstCandleUtils.getFirstCandleDate(share.first1MinCandleDate(), share.first1DayCandleDate(), candleInterval)
                : from;
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
        final List<Candle> candles = loadInterval.isAnyPeriod()
                ? self.getMarketCandles(figi, loadInterval, candleInterval)
                : getMarketCandles(figi, loadInterval, candleInterval);
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
        final Period period = Periods.DAY;

        final List<Interval> intervals = Interval.of(share.first1MinCandleDate(), to).splitIntoIntervals(period);
        for (int i = intervals.size() - 1; i >= 0; i--) {
            final Interval interval = intervals.get(i);
            final List<Candle> candles = loadCandlesBetterCacheable(figi, interval.extendTo(period), interval, CandleInterval.CANDLE_INTERVAL_1_MIN);
            final Candle lastCandle = CollectionUtils.lastElement(candles);
            if (lastCandle != null) {
                return lastCandle.getClose();
            }
        }

        throw new IllegalArgumentException("Not found last candle for FIGI '" + figi + "'");
    }

    /**
     * @return map of FIGIes to corresponding last prices. Keeps the same order for the keys as for the given {@code figies}.
     * @throws IllegalArgumentException if there is no last price for any of the given {@code figies}.
     */
    public Map<String, BigDecimal> getLastPrices(final List<String> figies) {
        final List<LastPrice> lastPrices = marketDataService.getLastPricesSync(figies);
        final Map<String, BigDecimal> result = new LinkedHashMap<>(figies.size(), 1);

        for (final String figi : figies) {
            final SingleItemCollector<BigDecimal> collector = createSingleItemCollector(figi);
            final BigDecimal currentLastPrice = lastPrices.stream()
                    .filter(lastPrice -> lastPrice.getFigi().equals(figi))
                    .map(lastPrice -> QUOTATION_MAPPER.toBigDecimal(lastPrice.getPrice()))
                    .collect(collector);
            result.put(figi, currentLastPrice);
        }

        return result;
    }

    @Cacheable(value = "marketCandles", sync = true)
    List<Candle> getMarketCandles(final String figi, final Interval interval, final CandleInterval candleInterval) {
        final Instant fromInstant = interval.getFrom().toInstant();
        final Instant toInstant = interval.getTo().toInstant();
        final List<Candle> candles = marketDataService.getCandlesSync(figi, fromInstant, toInstant, candleInterval)
                .stream()
                .filter(HistoricCandle::getIsComplete)
                .map(CANDLE_MAPPER::map)
                .toList();

        if (log.isDebugEnabled()) {
            log.debug("Loaded {} candles of {} size for FIGI '{}' in interval [{}]", candles.size(), candleInterval, figi, interval.toPrettyString());
        }
        return candles;
    }

    public SecurityTradingStatus getTradingStatus(final String id) {
        return marketDataService.getTradingStatusSync(id).getTradingStatus();
    }

    /**
     * Converts the given {@code sourceValue} of the given {@code sourceCurrencyIsoName}<br/>
     * to corresponding value of the given {@code targetCurrencyIsoName}
     *
     * @return conversion result
     */
    public BigDecimal convertCurrency(final String sourceCurrencyIsoName, final String targetCurrencyIsoName, final BigDecimal sourceValue) {
        if (sourceCurrencyIsoName.equals(targetCurrencyIsoName)) {
            return sourceValue;
        }

        final List<Currency> currencies = extInstrumentsService.getCurrenciesByIsoNames(sourceCurrencyIsoName, targetCurrencyIsoName);
        final List<String> figies = currencies.stream().map(Currency::figi).toList();
        final Map<String, BigDecimal> lastPrices = getLastPrices(figies);

        final BigDecimal sourceCurrencyLastPrice = getTomCurrencyLastPrice(sourceCurrencyIsoName, currencies, lastPrices);
        final BigDecimal targetCurrencyLastPrice = getTomCurrencyLastPrice(targetCurrencyIsoName, currencies, lastPrices);

        return DecimalUtils.divide(sourceValue.multiply(sourceCurrencyLastPrice), targetCurrencyLastPrice);
    }

    private static BigDecimal getTomCurrencyLastPrice(
            final String currencyIsoName,
            final List<Currency> currencies,
            final Map<String, BigDecimal> lastPrices
    ) {
        if (Currencies.RUB.equals(currencyIsoName)) {
            return DecimalUtils.ONE;
        }

        final SingleItemCollector<String> collector = createSingleItemCollector(currencyIsoName);
        final String figi = currencies.stream()
                .filter(currency -> currencyIsoName.equals(currency.isoCurrencyName()) && currency.ticker().endsWith("TOM"))
                .map(Currency::figi)
                .collect(collector);

        return lastPrices.get(figi);
    }

    private static <T> SingleItemCollector<T> createSingleItemCollector(final String instrumentId) {
        return new SingleItemCollector<>(
                () -> new InstrumentNotFoundException(instrumentId),
                () -> new MultipleInstrumentsFoundException(instrumentId)
        );
    }

}