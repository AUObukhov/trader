package ru.obukhov.trader.test.utils;

import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.MarketDataService;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class CandleMocker {
    private final MarketDataService marketDataService;
    private final String figi;
    private final CandleInterval candleInterval;
    private final List<HistoricCandle> candles;

    private final Answer<List<HistoricCandle>> answer = new Answer<>() {
        @Override
        public List<HistoricCandle> answer(InvocationOnMock invocation) {
            return candles.stream()
                    .filter(candle -> DateUtils.timestampIsInInterval(candle.getTime(), invocation.getArgument(1), invocation.getArgument(2)))
                    .toList();
        }
    };

    public CandleMocker(
            @NotNull final MarketDataService marketDataService,
            @NotNull final String figi,
            @NotNull final CandleInterval candleInterval
    ) {
        this.marketDataService = marketDataService;
        this.figi = figi;
        this.candleInterval = candleInterval;
        this.candles = new ArrayList<>();
    }

    public CandleMocker add(@NotNull final Integer closePrice, @NotNull final OffsetDateTime time) {
        this.candles.add(TestData.createHistoricCandleClosed(closePrice, time));
        return this;
    }

    public CandleMocker add(@NotNull final HistoricCandle... candles) {
        return add(List.of(candles));
    }

    public CandleMocker add(@NotNull final List<HistoricCandle> candles) {
        this.candles.addAll(candles);

        return this;
    }

    public void mock() throws IOException {
        Mockito.when(marketDataService.getCandlesSync(
                Mockito.eq(figi),
                Mockito.any(Instant.class),
                Mockito.any(Instant.class),
                Mockito.eq(candleInterval)
        )).then(answer);
    }

}