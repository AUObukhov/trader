package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.SearchMarketInstrument;
import ru.obukhov.trader.web.client.exchange.CandlesResponse;
import ru.obukhov.trader.web.client.exchange.MarketInstrumentListResponse;
import ru.obukhov.trader.web.client.exchange.OrderbookResponse;
import ru.obukhov.trader.web.client.exchange.SearchMarketInstrumentResponse;
import ru.obukhov.trader.web.client.service.interfaces.MarketClient;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MarketClientImpl extends AbstractClient implements MarketClient {

    protected MarketClientImpl(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        super(client, tradingProperties, apiProperties);
    }

    @Override
    public String getPath() {
        return "market";
    }

    @Override
    public List<MarketInstrument> getMarketStocks() throws IOException {
        final HttpUrl requestUrl = url.newBuilder()
                .addPathSegment("stocks")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().instruments();
    }

    @Override
    public List<MarketInstrument> getMarketBonds() throws IOException {
        final HttpUrl requestUrl = url.newBuilder()
                .addPathSegment("bonds")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().instruments();
    }

    @Override
    public List<MarketInstrument> getMarketEtfs() throws IOException {
        final HttpUrl requestUrl = url.newBuilder()
                .addPathSegment("etfs")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().instruments();
    }

    @Override
    public List<MarketInstrument> getMarketCurrencies() throws IOException {
        final HttpUrl requestUrl = url.newBuilder()
                .addPathSegment("currencies")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().instruments();
    }

    @Override
    public Orderbook getMarketOrderbook(@NotNull final String figi, final int depth) throws IOException {
        final HttpUrl requestUrl = url.newBuilder()
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
            @NotNull final CandleInterval interval
    ) throws IOException {

        String renderedInterval = mapper.writeValueAsString(interval);
        renderedInterval = renderedInterval.substring(1, renderedInterval.length() - 1);

        final HttpUrl requestUrl = url.newBuilder()
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
        final HttpUrl requestUrl = url.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-ticker")
                .addQueryParameter("ticker", ticker)
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, MarketInstrumentListResponse.class).getPayload().instruments();
    }

    @Override
    public SearchMarketInstrument searchMarketInstrumentByFigi(@NotNull final String figi) throws IOException {
        final HttpUrl requestUrl = url.newBuilder()
                .addPathSegment("search")
                .addPathSegment("by-figi")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, SearchMarketInstrumentResponse.class).getPayload();
    }

}