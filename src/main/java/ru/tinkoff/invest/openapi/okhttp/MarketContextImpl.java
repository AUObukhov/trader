package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.SearchMarketInstrument;
import ru.obukhov.trader.web.client.exchange.CandlesResponse;
import ru.obukhov.trader.web.client.exchange.MarketInstrumentListResponse;
import ru.obukhov.trader.web.client.exchange.OrderbookResponse;
import ru.obukhov.trader.web.client.exchange.SearchMarketInstrumentResponse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class MarketContextImpl extends BaseContextImpl implements MarketContext {

    public MarketContextImpl(@NotNull final OkHttpClient client, @NotNull final String url, @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @NotNull
    @Override
    public String getPath() {
        return "market";
    }

    @Override
    public List<MarketInstrument> getMarketStocks() throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("stocks")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().getInstruments();
    }

    @Override
    public List<MarketInstrument> getMarketBonds() throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("bonds")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().getInstruments();
    }

    @Override
    public List<MarketInstrument> getMarketEtfs() throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("etfs")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().getInstruments();
    }

    @Override
    public List<MarketInstrument> getMarketCurrencies() throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("currencies")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().getInstruments();
    }

    @Override
    public Orderbook getMarketOrderbook(@NotNull final String figi, final int depth) throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("orderbook")
                .addQueryParameter("figi", figi)
                .addQueryParameter("depth", Integer.toString(depth))
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, OrderbookResponse.class).getPayload();
    }

    @Override
    public Candles getMarketCandles(
            @NotNull final String figi,
            @NotNull final OffsetDateTime from,
            @NotNull final OffsetDateTime to,
            @NotNull final CandleResolution interval
    ) throws IOException {

        String renderedInterval = mapper.writeValueAsString(interval);
        renderedInterval = renderedInterval.substring(1, renderedInterval.length() - 1);

        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("candles")
                .addQueryParameter("figi", figi)
                .addQueryParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("interval", renderedInterval)
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, CandlesResponse.class).getPayload();
    }

    @Override
    public List<MarketInstrument> searchMarketInstrumentsByTicker(@NotNull final String ticker) throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-ticker")
                .addQueryParameter("ticker", ticker)
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().getInstruments();
    }

    @Override
    public SearchMarketInstrument searchMarketInstrumentByFigi(@NotNull final String figi) throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-figi")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, SearchMarketInstrumentResponse.class).getPayload();
    }

}