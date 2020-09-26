package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.model.transform.CandleMapper;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService, DisposableBean {

    static final int MAX_EMPTY_DAYS_COUNT = 5;

    private final ConnectionService connectionService;
    private final MarketContext marketContext;

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    /**
     * Load candles by conditions period by period.
     *
     * @return sorted by time list of loaded candles
     */
    @Override
    public List<Candle> getCandles(String ticker,
                                   @Nullable OffsetDateTime from,
                                   @Nullable OffsetDateTime to,
                                   CandleInterval interval) {

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

        OffsetDateTime currentFrom = DateUtils.roundUpToDay(to == null ? OffsetDateTime.now() : to);
        OffsetDateTime currentTo;

        List<Candle> allCandles = new ArrayList<>();
        List<Candle> currentCandles;
        int emptyDaysCount = 0;
        do {
            currentTo = currentFrom;
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
        List<Candle> candles = marketContext.getMarketCandles(figi, from, to, interval).join()
                .map(candleMapper::map)
                .orElse(Collections.emptyList());
        log.debug("Loaded " + candles.size() + " candles for figi '" + figi + "' in interval " + from + " - " + to);
        return candles;
    }

    @Override
    public Candle getLastCandle(String ticker) {
        String figi = getFigi(ticker);
        OffsetDateTime from = DateUtils.getLastWorkDay();
        OffsetDateTime to = from.plusDays(1);

        List<Candle> currentCandles;
        int emptyDaysCount = 0;
        do {
            currentCandles = loadCandles(figi, from, to, CandleInterval.ONE_MIN);
            if (currentCandles.isEmpty()) {
                emptyDaysCount++;
            }

            to = from;
            from = from.minusDays(1);
        } while (currentCandles.isEmpty() && emptyDaysCount <= MAX_EMPTY_DAYS_COUNT);

        Candle lastCandle = CollectionUtils.lastElement(currentCandles);
        Assert.isTrue(lastCandle != null, "Not found last candle for ticker '" + ticker + "'");

        return lastCandle;
    }

    /**
     * @return list of available instruments of given {@code type}
     */
    @Override
    public List<Instrument> getInstruments(TickerType type) {
        if (type == null) {
            return getAllInstruments();
        }

        switch (type) {
            case ETF:
                return marketContext.getMarketEtfs().join().instruments;
            case STOCK:
                return marketContext.getMarketStocks().join().instruments;
            case BOND:
                return marketContext.getMarketBonds().join().instruments;
            case CURRENCY:
                return marketContext.getMarketCurrencies().join().instruments;
            default:
                throw new IllegalArgumentException("Unknown ticker type " + type);
        }
    }

    private List<Instrument> getAllInstruments() {
        CompletableFuture<InstrumentsList> etfs = marketContext.getMarketEtfs();
        CompletableFuture<InstrumentsList> stocks = marketContext.getMarketStocks();
        CompletableFuture<InstrumentsList> bonds = marketContext.getMarketBonds();
        CompletableFuture<InstrumentsList> currencies = marketContext.getMarketCurrencies();

        List<Instrument> result = new ArrayList<>();
        result.addAll(etfs.join().instruments);
        result.addAll(stocks.join().instruments);
        result.addAll(bonds.join().instruments);
        result.addAll(currencies.join().instruments);

        return result;
    }

    @Override
    public String getFigi(String ticker) {
        List<Instrument> instruments = marketContext.searchMarketInstrumentsByTicker(ticker).join().instruments;
        Assert.isTrue(instruments.size() == 1, "Expected one instrument by ticker " + ticker);

        return instruments.get(0).figi;
    }

    @Override
    public void destroy() {
        connectionService.closeConnection();
    }
}