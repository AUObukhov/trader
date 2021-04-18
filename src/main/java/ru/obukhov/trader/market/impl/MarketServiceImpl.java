package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.TickerType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

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

    private final TradingProperties tradingProperties;
    private final TinkoffService tinkoffService;

    /**
     * Load candles by conditions period by period.
     *
     * @return sorted by time list of loaded candles
     */
    @Override
    public List<Candle> getCandles(String ticker, Interval interval, CandleResolution candleInterval) {

        DateUtils.assertDateTimeNotFuture(interval.getTo(), tinkoffService.getCurrentDateTime(), "to");

        ChronoUnit period = DateUtils.getPeriodByCandleInterval(candleInterval);

        List<Candle> candles = period == ChronoUnit.DAYS
                ? getAllCandlesByDays(ticker, interval, candleInterval)
                : getAllCandlesByYears(ticker, interval, candleInterval);

        log.info("Loaded {} candles for ticker '{}'", candles.size(), ticker);

        return candles.stream()
                .sorted(Comparator.comparing(Candle::getTime))
                .collect(Collectors.toList());

    }

    private List<Candle> getAllCandlesByDays(String ticker, Interval interval, CandleResolution candleInterval) {

        OffsetDateTime innerFrom = ObjectUtils.defaultIfNull(interval.getFrom(), tradingProperties.getStartDate());
        OffsetDateTime innerTo = ObjectUtils.defaultIfNull(interval.getTo(), tinkoffService.getCurrentDateTime());

        List<Interval> subIntervals = Interval.of(innerFrom, innerTo).splitIntoDailyIntervals();
        ListIterator<Interval> listIterator = subIntervals.listIterator(subIntervals.size());

        List<List<Candle>> candles = new ArrayList<>();
        int emptyDaysCount = 0;
        while (listIterator.hasPrevious() && emptyDaysCount <= tradingProperties.getConsecutiveEmptyDaysLimit()) {
            Interval subInterval = listIterator.previous();
            List<Candle> currentCandles = loadDayCandles(ticker, subInterval, candleInterval);
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

    private List<Candle> getAllCandlesByYears(
            String ticker,
            Interval interval,
            CandleResolution candleResolution
    ) {
        final OffsetDateTime innerFrom = DateUtils.roundDownToYear(interval.getFrom());
        final OffsetDateTime innerTo = ObjectUtils.defaultIfNull(interval.getTo(), tinkoffService.getCurrentDateTime());
        OffsetDateTime currentFrom = DateUtils.roundUpToYear(innerTo);
        OffsetDateTime currentTo;

        List<Candle> allCandles = new ArrayList<>();
        List<Candle> currentCandles;
        do {
            currentTo = currentFrom;
            currentFrom = DateUtils.minusLimited(currentFrom, 1, ChronoUnit.YEARS, innerFrom);

            currentCandles = loadCandles(ticker, currentFrom, currentTo, candleResolution)
                    .stream()
                    .filter(candle -> !candle.getTime().isBefore(interval.getFrom()))
                    .collect(Collectors.toList());
            allCandles.addAll(currentCandles);
        } while (DateUtils.isAfter(currentFrom, interval.getFrom()) && !currentCandles.isEmpty());

        return allCandles;
    }

    private List<Candle> loadCandles(
            String ticker,
            OffsetDateTime from,
            OffsetDateTime to,
            CandleResolution candleInterval
    ) {
        OffsetDateTime innerTo = DateUtils.getEarliestDateTime(to, OffsetDateTime.now());
        return tinkoffService.getMarketCandles(ticker, Interval.of(from, innerTo), candleInterval);
    }

    private List<Candle> loadDayCandles(String ticker, Interval interval, CandleResolution candleInterval) {
        return tinkoffService.getMarketCandles(ticker, interval.extendToWholeDay(true), candleInterval)
                .stream()
                .filter(candle -> interval.contains(candle.getTime()))
                .collect(Collectors.toList());
    }

    /**
     * Searches last candle by {@code ticker} within last {@code trading.consecutive-empty-days-limit} days
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
     * Searches last candle by {@code ticker} within last {@code trading.consecutive-empty-days-limit} days
     * not after {@code to}
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    @Override
    public Candle getLastCandle(String ticker, OffsetDateTime to) {
        OffsetDateTime candlesFrom = to.minusDays(tradingProperties.getConsecutiveEmptyDaysLimit());

        List<Interval> intervals = Interval.of(candlesFrom, to).splitIntoDailyIntervals();
        intervals = CollectionsUtils.getTail(intervals, tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        ListIterator<Interval> listIterator = intervals.listIterator(intervals.size());
        while (listIterator.hasPrevious()) {
            Interval interval = listIterator.previous();
            List<Candle> candles = loadDayCandles(ticker, interval, CandleResolution._1MIN);
            if (!candles.isEmpty()) {
                return CollectionUtils.lastElement(candles);
            }
        }

        throw new IllegalArgumentException("Not found last candle for ticker '" + ticker + "'");
    }

    /**
     * @return last {@code limit} candles by {@code ticker}.
     * Searches from now to past. Stops searching when finds enough candles or when consecutively getting no candles
     * within {@code trading.consecutive-empty-days-limit} days.
     */
    @Override
    public List<Candle> getLastCandles(String ticker, int limit, CandleResolution candleResolution) {

        OffsetDateTime to = tinkoffService.getCurrentDateTime();
        OffsetDateTime from = DateUtils.atStartOfDay(to);
        int consecutiveEmptyDaysCount = 0;
        List<Candle> candles = new ArrayList<>();

        do {
            List<Candle> currentCandles = loadDayCandles(ticker, Interval.of(from, to), candleResolution);
            if (currentCandles.isEmpty()) {
                consecutiveEmptyDaysCount++;
            } else {
                consecutiveEmptyDaysCount = 0;
                candles.addAll(currentCandles);
            }

            from = from.minusDays(1);
            to = DateUtils.atEndOfDay(from);
        } while (candles.size() < limit
                && consecutiveEmptyDaysCount <= tradingProperties.getConsecutiveEmptyDaysLimit());

        candles.sort(Comparator.comparing(Candle::getTime));
        return CollectionsUtils.getTail(candles, limit);
    }

    @Override
    public MarketInstrument getInstrument(String ticker) {
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
    @Override
    public List<MarketInstrument> getInstruments(TickerType type) {
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

    private List<MarketInstrument> getAllInstruments() {

        List<MarketInstrument> result = new ArrayList<>();
        result.addAll(tinkoffService.getMarketEtfs());
        result.addAll(tinkoffService.getMarketStocks());
        result.addAll(tinkoffService.getMarketBonds());
        result.addAll(tinkoffService.getMarketCurrencies());

        return result;
    }

    @Override
    public String getFigi(String ticker) {
        return tinkoffService.searchMarketInstrument(ticker).getFigi();
    }

}