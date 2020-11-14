package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    static final int MAX_EMPTY_DAYS_COUNT = 5;

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

        Assert.isTrue(to == null || !to.isAfter(OffsetDateTime.now()), "'to' can't be in future");

        String figi = getFigi(ticker);
        ChronoUnit period = DateUtils.getPeriodByCandleInterval(interval);

        List<Candle> candles = period == ChronoUnit.DAYS
                ? getAllCandlesByDays(figi, from, to, interval)
                : getAllCandlesByYears(figi, from, to, interval);

        log.info("Loaded " + candles.size() + " candles for ticker '" + ticker + "'");

        return candles.stream()
                .sorted(Comparator.comparing(Candle::getTime))
                .collect(Collectors.toList());
    }

    private List<Candle> getAllCandlesByDays(String figi,
                                             @Nullable OffsetDateTime from,
                                             @Nullable OffsetDateTime to,
                                             CandleInterval interval) {

        OffsetDateTime innerTo = to == null ? OffsetDateTime.now() : to;
        OffsetDateTime currentFrom = DateUtils.roundUpToDay(innerTo);
        OffsetDateTime currentTo;

        List<Candle> allCandles = new ArrayList<>();
        List<Candle> currentCandles;
        int emptyDaysCount = 0;
        do {
            currentTo = DateUtils.getEarliestDateTime(currentFrom, innerTo);
            currentFrom = DateUtils.minusLimited(currentFrom, 1, ChronoUnit.DAYS, from);

            currentCandles = loadCandles(figi, currentFrom, currentTo, interval);
            if (currentCandles.isEmpty()) {
                emptyDaysCount++;
            } else {
                emptyDaysCount = 0;
                allCandles.addAll(currentCandles);
            }
        } while (DateUtils.isAfter(currentFrom, from)
                && (!currentCandles.isEmpty() || emptyDaysCount <= MAX_EMPTY_DAYS_COUNT));

        return allCandles;

    }

    private List<Candle> getAllCandlesByYears(String figi,
                                              @Nullable OffsetDateTime from,
                                              @Nullable OffsetDateTime to,
                                              CandleInterval interval) {

        OffsetDateTime currentFrom = DateUtils.roundUpToYear(to == null ? OffsetDateTime.now() : to);
        OffsetDateTime currentTo;

        List<Candle> allCandles = new ArrayList<>();
        List<Candle> currentCandles;
        do {
            currentTo = currentFrom;
            currentFrom = DateUtils.minusLimited(currentFrom, 1, ChronoUnit.YEARS, from);

            currentCandles = loadCandles(figi, currentFrom, currentTo, interval);
            allCandles.addAll(currentCandles);
        } while (DateUtils.isAfter(currentFrom, from) && !currentCandles.isEmpty());

        return allCandles;

    }

    private List<Candle> loadCandles(String figi, OffsetDateTime from, OffsetDateTime to, CandleInterval interval) {
        List<Candle> candles = tinkoffService.getMarketCandles(figi, from, to, interval);
        log.debug("Loaded " + candles.size() + " candles for figi '" + figi + "' in interval " + from + " - " + to);
        return candles;
    }

    /**
     * Searches last candle by {@code ticker} within last {@link MarketServiceImpl#MAX_EMPTY_DAYS_COUNT} days
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    @Override
    @Cacheable("last-candle")
    public Candle getLastCandle(String ticker) {
        return getLastCandle(ticker, DateUtils.getLastWorkDay().plusDays(1));
    }

    /**
     * Searches last candle by {@code ticker} within last {@link MarketServiceImpl#MAX_EMPTY_DAYS_COUNT} days
     * before {@code to}
     *
     * @return found candle
     * @throws IllegalArgumentException if candle not found
     */
    @Override
    @Cacheable("last-candle-to")
    public Candle getLastCandle(String ticker, OffsetDateTime to) {
        String figi = getFigi(ticker);
        OffsetDateTime candlesFrom = to.minusDays(1);
        OffsetDateTime candlesTo = to;

        List<Candle> currentCandles;
        int emptyDaysCount = 0;
        do {
            currentCandles = loadCandles(figi, candlesFrom, candlesTo, CandleInterval.ONE_MIN);
            if (currentCandles.isEmpty()) {
                emptyDaysCount++;
            }

            candlesTo = candlesFrom;
            candlesFrom = candlesFrom.minusDays(1);
        } while (currentCandles.isEmpty() && emptyDaysCount <= MAX_EMPTY_DAYS_COUNT);

        Candle lastCandle = CollectionUtils.lastElement(currentCandles);
        Assert.isTrue(lastCandle != null, "Not found last candle for ticker '" + ticker + "'");

        return lastCandle;
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
    @Cacheable("figi")
    public String getFigi(String ticker) {
        return tinkoffService.searchMarketInstrumentByTicker(ticker).figi;
    }

}