package ru.obukhov.investor.service;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;
import ru.tinkoff.invest.openapi.models.market.Orderbook;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class MarketContextRestrictedProxy implements MarketContext {

    private MarketContext innerContext;

    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketStocks() {
        return innerContext.getMarketStocks();
    }

    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketBonds() {
        return innerContext.getMarketBonds();
    }

    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketEtfs() {
        return innerContext.getMarketEtfs();
    }

    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketCurrencies() {
        return innerContext.getMarketCurrencies();
    }

    @NotNull
    @Override
    public CompletableFuture<Optional<Orderbook>> getMarketOrderbook(@NotNull String figi, int depth) {
        return innerContext.getMarketOrderbook(figi, depth);
    }

    @NotNull
    @Override
    public CompletableFuture<Optional<HistoricalCandles>> getMarketCandles(@NotNull String figi, @NotNull OffsetDateTime from, @NotNull OffsetDateTime to, @NotNull CandleInterval interval) {
        return innerContext.getMarketCandles(figi, from, to, interval);
    }

    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(@NotNull String ticker) {
        return innerContext.searchMarketInstrumentsByTicker(ticker);
    }

    @NotNull
    @Override
    public CompletableFuture<Optional<Instrument>> searchMarketInstrumentByFigi(@NotNull String figi) {
        return innerContext.searchMarketInstrumentByFigi(figi);
    }

    @NotNull
    @Override
    public String getPath() {
        return innerContext.getPath();
    }
}