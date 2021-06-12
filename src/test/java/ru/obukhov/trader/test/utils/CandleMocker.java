package ru.obukhov.trader.test.utils;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandleMocker {
    private final TinkoffService tinkoffService;
    private final String ticker;
    private final CandleResolution candleResolution;
    private final Map<Interval, List<Candle>> intervals;
    private boolean lenient;

    private final Answer<List<Candle>> answer = new Answer<>() {
        @Override
        public List<Candle> answer(InvocationOnMock invocation) {
            final Interval interval = invocation.getArgument(1);
            if (intervals.containsKey(interval)) {
                return List.copyOf(intervals.get(interval));
            } else if (lenient) {
                return List.of();
            } else {
                throw new IllegalArgumentException("Not mocked interval: " + interval.toPrettyString());
            }
        }
    };

    public CandleMocker(
            @NotNull TinkoffService tinkoffService,
            @NotNull String ticker,
            @NotNull CandleResolution candleResolution
    ) {
        this.tinkoffService = tinkoffService;
        this.ticker = ticker;
        this.candleResolution = candleResolution;
        this.intervals = new HashMap<>();
        this.lenient = false;
    }

    public CandleMocker lenient() {
        this.lenient = true;
        return this;
    }

    public CandleMocker add(
            @NotNull final OffsetDateTime from,
            @NotNull final OffsetDateTime to,
            @NotNull final Integer... openPrices
    ) {
        return add(Interval.of(from, to), createCandles(from, openPrices));
    }

    public CandleMocker add(@NotNull final Interval interval, @NotNull final Integer... openPrices) {
        return add(interval, createCandles(interval.getFrom(), openPrices));
    }

    public CandleMocker add(
            @NotNull final Interval interval,
            @NotNull final Integer openPrice,
            @NotNull final OffsetDateTime time
    ) {
        return add(interval, createCandles(openPrice, time));
    }

    public CandleMocker add(
            @NotNull final Interval interval,
            @NotNull final List<Integer> openPrices,
            @NotNull final List<OffsetDateTime> times
    ) {
        return add(interval, createCandles(openPrices, times));
    }

    public CandleMocker add(@NotNull final OffsetDateTime day, @NotNull final Integer... openPrices) {
        return add(Interval.ofDay(day), createCandles(day, openPrices));
    }

    public CandleMocker add(@NotNull final OffsetDateTime day, @NotNull final Candle candle) {
        return add(Interval.ofDay(day), List.of(candle));
    }

    public CandleMocker add(
            @NotNull final OffsetDateTime from,
            @NotNull final OffsetDateTime to,
            @NotNull final Candle candle
    ) {
        return add(Interval.of(from, to), List.of(candle));
    }

    public CandleMocker add(@NotNull final Interval interval, @NotNull final Candle candle) {
        return add(interval, List.of(candle));
    }

    public CandleMocker add(@NotNull final OffsetDateTime day, @NotNull final List<Candle> candles) {
        return add(Interval.ofDay(day), candles);
    }

    public CandleMocker add(
            @NotNull final OffsetDateTime from,
            @NotNull final OffsetDateTime to,
            @NotNull final List<Candle> candles
    ) {
        return add(Interval.of(from, to), candles);
    }

    public CandleMocker add(@NotNull final Interval interval, @NotNull final List<Candle> candles) {
        Assert.notNull(interval.getFrom(), "interval.from is not nullable");
        Assert.notNull(interval.getTo(), "interval.to is not nullable");

        List<Candle> existingCandles = intervals.get(interval);
        if (existingCandles == null) {
            intervals.put(interval, new ArrayList<>(candles));
        } else {
            existingCandles.addAll(candles);
        }

        return this;
    }

    private List<Candle> createCandles(final OffsetDateTime date, final Integer... openPrices) {
        final Interval interval = Interval.of(date, date).extendToWholeDay(true);

        final List<Integer> prices = List.of(openPrices);
        final List<OffsetDateTime> times = Collections.nCopies(openPrices.length, interval.getFrom());

        return createCandles(prices, times);
    }

    private List<Candle> createCandles(final Integer openPrice, final OffsetDateTime time) {
        return List.of(createCandleWithOpenPriceAndTime(openPrice, time));
    }

    private List<Candle> createCandles(final List<Integer> openPrices, final List<OffsetDateTime> times) {
        Assertions.assertEquals(times.size(), openPrices.size(), "times and openPrices must have same size");

        final List<Candle> candles = new ArrayList<>(openPrices.size());
        for (int i = 0; i < openPrices.size(); i++) {
            candles.add(createCandleWithOpenPriceAndTime(openPrices.get(i), times.get(i)));
        }

        return candles;
    }

    private Candle createCandleWithOpenPriceAndTime(final Integer openPrice, final OffsetDateTime time) {
        final Candle candle = new Candle();
        candle.setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
        candle.setTime(time);
        candle.setInterval(candleResolution);
        return candle;
    }

    public void mock() {
        Mockito.when(tinkoffService.getMarketCandles(
                Mockito.eq(ticker),
                Mockito.any(Interval.class),
                Mockito.eq(candleResolution)
        )).then(answer);
    }

}