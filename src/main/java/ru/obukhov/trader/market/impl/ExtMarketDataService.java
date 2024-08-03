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
import ru.obukhov.trader.common.util.Asserter;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.FirstCandleUtils;
import ru.obukhov.trader.common.util.SingleItemCollector;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

/**
 * Service to get information about market prices and instruments
 */
@Slf4j
@Service
public class ExtMarketDataService {

    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);
    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

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
        final Instrument instrument = extInstrumentsService.getInstrument(figi);
        final Period period = Periods.getPeriodByCandleInterval(candleInterval);
        final OffsetDateTime adjustedFrom = adjustFrom(interval.getFrom(), instrument, candleInterval);
        final List<Interval> subIntervals = Interval.of(adjustedFrom, interval.getTo()).splitIntoIntervals(period);

        final List<Candle> candles = new ArrayList<>();
        for (final Interval subInterval : subIntervals) {
            candles.addAll(loadCandlesCacheable(instrument.figi(), subInterval, period, candleInterval));
        }
        log.info("Loaded {} candles of {} size for FIGI '{}' in interval [{}]", candles.size(), candleInterval, figi, interval.toPrettyString());

        return candles;
    }

    private OffsetDateTime adjustFrom(final OffsetDateTime from, final Instrument instrument, final CandleInterval candleInterval) {
        return from == null
                ? FirstCandleUtils.getFirstCandleDate(instrument.first1MinCandleDate(), instrument.first1DayCandleDate(), candleInterval)
                : from;
    }

    /**
     * Loads candles from often used interval for better benefit from cache hits.
     * If given {@code interval} is in current {@code period} then cache is not used.
     *
     * @param figi           FIGI of loaded candles
     * @param interval       interval to search candles
     * @param period         period of time to cache candles
     * @param candleInterval interval of loaded candles
     * @return candles from given {@code effectiveInterval}
     */
    private List<Candle> loadCandlesCacheable(
            final String figi,
            final Interval interval,
            final Period period,
            final CandleInterval candleInterval
    ) {
        final Interval loadInterval = interval.extendTo(period);
        final List<Candle> candles = loadInterval.isAnyPeriod()
                ? self.getMarketCandles(figi, loadInterval, candleInterval)
                : getMarketCandles(figi, loadInterval, candleInterval);
        return interval.equals(loadInterval) ? candles : filterCandles(candles, interval);
    }

    private List<Candle> filterCandles(final List<Candle> candles, final Interval interval) {
        final Candle leftCandle = new Candle().setTime(interval.getFrom());
        final Candle rightCandle = new Candle().setTime(interval.getTo());
        final Comparator<Candle> comparator = Comparator.comparing(Candle::getTime);
        final int fromIndex = CollectionsUtils.binarySearch(candles, leftCandle, comparator);
        final int toIndex = CollectionsUtils.binarySearch(candles, rightCandle, comparator);
        return candles.subList(fromIndex, toIndex);
    }

    // region prices

    /**
     * @return last price not after given {@code dateTime} by given {@code figi}
     * @throws IllegalArgumentException    if candle not found
     * @throws InstrumentNotFoundException if instrument not found
     */
    public BigDecimal getPrice(final String figi, final OffsetDateTime dateTime) {
        final Instrument instrument = extInstrumentsService.getInstrument(figi);
        Asserter.notNull(instrument, () -> new InstrumentNotFoundException(figi));

        return getPrice(figi, dateTime, instrument.first1MinCandleDate(), instrument.first1DayCandleDate());
    }

    /**
     * @return last price of given {@code currency} not after given {@code dateTime}
     * @throws IllegalArgumentException    if candle not found
     * @throws InstrumentNotFoundException if instrument not found
     */
    public BigDecimal getPrice(final Currency currency, final OffsetDateTime dateTime) {
        if (currency.figi().equals(Currencies.RUB_FIGI)) {
            return DecimalUtils.ONE;
        }

        return getPrice(currency.figi(), dateTime, currency.first1MinCandleDate(), currency.first1DayCandleDate());
    }

    private BigDecimal getPrice(
            final String figi,
            final OffsetDateTime dateTime,
            final OffsetDateTime first1MinCandleDate,
            final OffsetDateTime first1DayCandleDate
    ) {
        final OffsetDateTime from;
        final OffsetDateTime to;
        final CandleInterval candleInterval;
        final Period period;
        if (first1MinCandleDate.isBefore(dateTime)) {
            from = first1MinCandleDate;
            to = dateTime;
            candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
            period = Periods.DAY;
        } else if (first1MinCandleDate.isEqual(dateTime)) {
            from = first1MinCandleDate;
            to = dateTime.plusNanos(1);
            candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
            period = Periods.DAY;
        } else if (first1DayCandleDate.isBefore(dateTime)) {
            from = first1DayCandleDate;
            to = dateTime;
            candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;
            period = Periods.YEAR;
        } else if (first1DayCandleDate.isEqual(dateTime)) {
            from = first1DayCandleDate;
            to = dateTime.plusNanos(1);
            candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;
            period = Periods.YEAR;
        } else {
            throw new IllegalArgumentException("No candles found for FIGI " + figi + " before " + dateTime);
        }

        final List<Interval> intervals = Interval.of(from, to).splitIntoIntervals(period);
        for (final Interval interval : intervals.reversed()) {
            final List<Candle> candles = loadCandlesCacheable(figi, interval, period, candleInterval);
            final Candle lastCandle = CollectionUtils.lastElement(candles);
            if (lastCandle != null) {
                final OffsetDateTime endTime = DateUtils.getCandleEndTime(lastCandle.getTime(), candleInterval);
                return endTime.isAfter(dateTime) ? lastCandle.getOpen() : lastCandle.getClose();
            }
        }

        throw new IllegalArgumentException("No candles found for FIGI " + figi + " before " + dateTime);
    }

    /**
     * @return map of FIGIes to corresponding last prices. Keeps the same order for the keys as for the given {@code figies}.
     * @throws IllegalArgumentException if there is no last price for any of the given {@code figies}.
     */
    public SequencedMap<String, BigDecimal> getLastPrices(final List<String> figies) {
        final List<LastPrice> lastPrices = marketDataService.getLastPricesSync(figies);
        final SequencedMap<String, BigDecimal> result = new LinkedHashMap<>(figies.size(), 1);

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

    public Map<String, BigDecimal> getSharesPrices(final List<Share> shares, final OffsetDateTime dateTime) {
        final List<String> figies = shares.stream().map(Share::figi).toList();
        final Map<String, BigDecimal> result = getPrices(figies, dateTime);

        for (final Share share : shares) {
            if (!result.containsKey(share.figi())) {
                final BigDecimal lastPriceValue = getPrice(share.figi(), dateTime, share.first1MinCandleDate(), share.first1DayCandleDate());
                result.put(share.figi(), lastPriceValue);
            }
        }

        return result;
    }

    private Map<String, BigDecimal> getCurrenciesPrices(final List<Currency> currencies, final OffsetDateTime dateTime) {
        final List<String> figies = currencies.stream().map(Currency::figi).toList();
        final Map<String, BigDecimal> result = getPrices(figies, dateTime);

        for (final Currency currency : currencies) {
            if (!result.containsKey(currency.figi())) {
                final BigDecimal lastPriceValue = getPrice(currency, dateTime);
                result.put(currency.figi(), lastPriceValue);
            }
        }

        return result;
    }

    private Map<String, BigDecimal> getPrices(final List<String> figies, final OffsetDateTime dateTime) {
        final Map<String, BigDecimal> result = new HashMap<>();
        final List<LastPrice> lastPrices = marketDataService.getLastPricesSync(figies);
        for (final LastPrice lastPrice : lastPrices) {
            final OffsetDateTime lastPriceDateTime = DATE_TIME_MAPPER.timestampToOffsetDateTime(lastPrice.getTime());
            if (lastPriceDateTime.isBefore(dateTime)) {
                final BigDecimal lastPriceValue = QUOTATION_MAPPER.toBigDecimal(lastPrice.getPrice());
                result.put(lastPrice.getFigi(), lastPriceValue);
            }
        }

        return result;
    }

    // endregion

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

    /**
     * Converts the given {@code sourceValue} of the given {@code sourceCurrencyIsoName}<br/>
     * to corresponding value of the given {@code targetCurrencyIsoName}<br/>
     * by course on given {@code dateTime}
     *
     * @return conversion result
     */
    public BigDecimal convertCurrency(
            final String sourceCurrencyIsoName,
            final String targetCurrencyIsoName,
            final BigDecimal sourceValue,
            final OffsetDateTime dateTime
    ) {
        if (sourceCurrencyIsoName.equals(targetCurrencyIsoName)) {
            return sourceValue;
        }

        final Currency sourceCurrency = extInstrumentsService.getTomCurrencyByIsoName(sourceCurrencyIsoName);
        final Currency targetCurrency = extInstrumentsService.getTomCurrencyByIsoName(targetCurrencyIsoName);

        final Map<String, BigDecimal> lastPrices = getCurrenciesPrices(List.of(sourceCurrency, targetCurrency), dateTime);
        final BigDecimal sourceCurrencyLastPrice = lastPrices.get(sourceCurrency.figi());
        final BigDecimal targetCurrencyLastPrice = lastPrices.get(targetCurrency.figi());

        return DecimalUtils.divide(sourceValue.multiply(sourceCurrencyLastPrice), targetCurrencyLastPrice);
    }

    private static <T> SingleItemCollector<T> createSingleItemCollector(final String instrumentId) {
        return new SingleItemCollector<>(
                () -> new InstrumentNotFoundException(instrumentId),
                () -> new MultipleInstrumentsFoundException(instrumentId)
        );
    }

}