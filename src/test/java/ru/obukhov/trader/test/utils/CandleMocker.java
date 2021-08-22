package ru.obukhov.trader.test.utils;

import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CandleMocker {
    private final TinkoffService tinkoffService;
    private final String ticker;
    private final CandleResolution candleResolution;
    private final List<Candle> candles;

    private final Answer<List<Candle>> answer = new Answer<>() {
        @Override
        public List<Candle> answer(InvocationOnMock invocation) {
            final Interval interval = invocation.getArgument(1);
            return candles.stream()
                    .filter(candle -> interval.contains(candle.getTime()))
                    .collect(Collectors.toList());
        }
    };

    public CandleMocker(
            @NotNull final TinkoffService tinkoffService,
            @NotNull final String ticker,
            @NotNull final CandleResolution candleResolution
    ) {
        this.tinkoffService = tinkoffService;
        this.ticker = ticker;
        this.candleResolution = candleResolution;
        this.candles = new ArrayList<>();
    }

    public CandleMocker add(@NotNull final Integer openPrice, @NotNull final OffsetDateTime time) {
        return add(createCandleWithOpenPrice(openPrice, time));
    }

    public CandleMocker add(@NotNull final Candle... candles) {
        this.candles.addAll(List.of(candles));

        return this;
    }

    private Candle createCandleWithOpenPrice(final Integer openPrice, final OffsetDateTime time) {
        final Candle candle = new Candle();
        candle.setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
        candle.setTime(time);
        candle.setInterval(candleResolution);
        return candle;
    }

    public void mock() {
        Mockito.when(tinkoffService.getMarketCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(candleResolution))).then(answer);
    }

}