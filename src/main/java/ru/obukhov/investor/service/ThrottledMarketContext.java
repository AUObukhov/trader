package ru.obukhov.investor.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.obukhov.investor.config.TokenHolder;
import ru.obukhov.investor.service.aop.Throttled;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;
import ru.tinkoff.invest.openapi.models.market.Orderbook;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ThrottledMarketContext implements MarketContext {

    private final ConnectionService connectionService;
    private MarketContext innerContext;

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketStocks() {
        return getContext().getMarketStocks();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketBonds() {
        return getContext().getMarketBonds();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketEtfs() {
        return getContext().getMarketEtfs();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketCurrencies() {
        return getContext().getMarketCurrencies();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Optional<Orderbook>> getMarketOrderbook(@NotNull String figi, int depth) {
        return getContext().getMarketOrderbook(figi, depth);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Optional<HistoricalCandles>> getMarketCandles(@NotNull String figi, @NotNull OffsetDateTime from, @NotNull OffsetDateTime to, @NotNull CandleInterval interval) {
        return getContext().getMarketCandles(figi, from, to, interval);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(@NotNull String ticker) {
        return getContext().searchMarketInstrumentsByTicker(ticker);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Optional<Instrument>> searchMarketInstrumentByFigi(@NotNull String figi) {
        return getContext().searchMarketInstrumentByFigi(figi);
    }

    @Throttled
    @NotNull
    @Override
    public String getPath() {
        return getContext().getPath();
    }

    private MarketContext getContext() {
        if (this.innerContext == null) {
            this.innerContext = connectionService.getApi(TokenHolder.getToken()).getMarketContext();
        }

        return this.innerContext;
    }
}