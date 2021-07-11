package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
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
    public List<Candle> getCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    ) {
        DateUtils.assertDateTimeNotFuture(interval.getTo(), tinkoffService.getCurrentDateTime(), "to");

        final ChronoUnit period = DateUtils.getPeriodByCandleResolution(candleResolution);

        final List<Candle> candles = period == ChronoUnit.DAYS
                ? getAllCandlesByDays(ticker, interval, candleResolution)
                : getAllCandlesByYears(ticker, interval, candleResolution);

        log.info("Loaded {} candles for ticker '{}'", candles.size(), ticker);

        return candles.stream()
                .sorted(Comparator.comparing(Candle::getTime))
                .collect(Collectors.toList());
    }

    private List<Candle> getAllCandlesByDays(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    ) {
        final OffsetDateTime innerFrom = ObjectUtils.defaultIfNull(interval.getFrom(), tradingProperties.getStartDate());
        final OffsetDateTime innerTo = ObjectUtils.defaultIfNull(interval.getTo(), tinkoffService.getCurrentDateTime());

        final List<Interval> subIntervals = Interval.of(innerFrom, innerTo).splitIntoDailyIntervals();
        final ListIterator<Interval> listIterator = subIntervals.listIterator(subIntervals.size());

        final List<List<Candle>> candles = new ArrayList<>();
        int emptyDaysCount = 0;
        while (listIterator.hasPrevious() && emptyDaysCount <= tradingProperties.getConsecutiveEmptyDaysLimit()) {
            final Interval subInterval = listIterator.previous();
            final List<Candle> currentCandles = loadDayCandles(ticker, subInterval, candleResolution);
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
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    ) {
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
                    .collect(Collectors.toList());
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

    private List<Candle> loadDayCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    ) {
        return tinkoffService.getMarketCandles(ticker, interval.extendToDay(), candleResolution)
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
    @Override
    public Candle getLastCandle(final String ticker, final OffsetDateTime to) {
        final OffsetDateTime candlesFrom = to.minusDays(tradingProperties.getConsecutiveEmptyDaysLimit());

        List<Interval> intervals = Interval.of(candlesFrom, to).splitIntoDailyIntervals();
        intervals = CollectionsUtils.getTail(intervals, tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        final ListIterator<Interval> listIterator = intervals.listIterator(intervals.size());
        while (listIterator.hasPrevious()) {
            final Interval interval = listIterator.previous();
            final List<Candle> candles = loadDayCandles(ticker, interval, CandleResolution._1MIN);
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
    @Override
    @NotNull
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
    private List<Candle> getLastCandlesDaily(
            final String ticker,
            final int limit,
            final CandleResolution candleResolution
    ) {
        final OffsetDateTime to = tinkoffService.getCurrentDateTime();
        final OffsetDateTime from = DateUtils.atStartOfDay(to);
        Interval interval = Interval.of(from, to);

        final List<Candle> candles = tinkoffService.getMarketCandles(ticker, interval, candleResolution);
        int consecutiveEmptyDaysCount = candles.isEmpty() ? 1 : 0;

        interval = interval.minusDays(1).extendToDay();

        do {
            final List<Candle> currentCandles = loadDayCandles(ticker, interval, candleResolution);
            if (currentCandles.isEmpty()) {
                consecutiveEmptyDaysCount++;
            } else {
                consecutiveEmptyDaysCount = 0;
                candles.addAll(currentCandles);
            }

            interval = interval.minusDays(1);
        } while (candles.size() < limit
                && consecutiveEmptyDaysCount <= tradingProperties.getConsecutiveEmptyDaysLimit());

        candles.sort(Comparator.comparing(Candle::getTime));
        return CollectionsUtils.getTail(candles, limit);
    }

    private List<Candle> getLastCandlesYearly(String ticker, int limit, CandleResolution candleResolution) {
        final OffsetDateTime to = tinkoffService.getCurrentDateTime();
        final OffsetDateTime from = DateUtils.atStartOfYear(to);
        Interval interval = Interval.of(from, to);

        List<Candle> currentCandles = tinkoffService.getMarketCandles(ticker, interval, candleResolution);
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

    @Override
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
    @Override
    public List<MarketInstrument> getInstruments(final InstrumentType type) {
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
    public String getFigi(final String ticker) {
        return tinkoffService.searchMarketInstrument(ticker).getFigi();
    }

}