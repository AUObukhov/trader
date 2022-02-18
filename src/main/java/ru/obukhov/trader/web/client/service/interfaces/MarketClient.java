package ru.obukhov.trader.web.client.service.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.SearchMarketInstrument;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

public interface MarketClient {

    List<MarketInstrument> getMarketStocks() throws IOException;

    List<MarketInstrument> getMarketBonds() throws IOException;

    List<MarketInstrument> getMarketEtfs() throws IOException;

    List<MarketInstrument> getMarketCurrencies() throws IOException;

    Orderbook getMarketOrderbook(@NotNull String figi, int depth) throws IOException;

    Candles getMarketCandles(@NotNull String figi, @NotNull OffsetDateTime from, @NotNull OffsetDateTime to, @NotNull CandleResolution interval)
            throws IOException;

    List<MarketInstrument> searchMarketInstrumentsByTicker(@NotNull String ticker) throws IOException;

    SearchMarketInstrument searchMarketInstrumentByFigi(@NotNull String figi) throws IOException;

}