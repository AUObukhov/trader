package ru.obukhov.investor.service.context;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
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
public class ThrottledMarketContext implements MarketContext {

    @Setter
    private MarketContext innerContext;

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketStocks() {
        return innerContext.getMarketStocks();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketBonds() {
        return innerContext.getMarketBonds();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketEtfs() {
        return innerContext.getMarketEtfs();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> getMarketCurrencies() {
        return innerContext.getMarketCurrencies();
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Optional<Orderbook>> getMarketOrderbook(@NotNull String figi, int depth) {
        return innerContext.getMarketOrderbook(figi, depth);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Optional<HistoricalCandles>> getMarketCandles(@NotNull String figi, @NotNull OffsetDateTime from, @NotNull OffsetDateTime to, @NotNull CandleInterval interval) {
        return innerContext.getMarketCandles(figi, from, to, interval);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<InstrumentsList> searchMarketInstrumentsByTicker(@NotNull String ticker) {
        return innerContext.searchMarketInstrumentsByTicker(ticker);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Optional<Instrument>> searchMarketInstrumentByFigi(@NotNull String figi) {
        return innerContext.searchMarketInstrumentByFigi(figi);
    }

    @Throttled
    @NotNull
    @Override
    public String getPath() {
        return innerContext.getPath();
    }

}