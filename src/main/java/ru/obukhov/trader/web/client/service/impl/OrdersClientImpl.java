package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.web.client.exchange.OrdersResponse;
import ru.obukhov.trader.web.client.service.interfaces.OrdersClient;

import java.io.IOException;
import java.util.List;

@Service
public class OrdersClientImpl extends AbstractClient implements OrdersClient {

    private static final String PARAM_BROKER_ACCOUNT_ID = "accountId";

    protected OrdersClientImpl(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        super(client, tradingProperties, apiProperties);
    }

    @Override
    public String getPath() {
        return "orders";
    }

    @Override
    public List<Order> getOrders(final String accountId) throws IOException {
        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder.build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, OrdersResponse.class).getPayload();
    }

    @Override
    public PlacedLimitOrder placeLimitOrder(
            final String accountId,
            @NotNull final String figi,
            @NotNull final LimitOrderRequest limitOrder
    ) throws IOException {
        final String renderedBody = mapper.writeValueAsString(limitOrder);

        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("limit-order")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, JSON_MEDIA_TYPE))
                .build();

        return executeAndGetBody(request, PlacedLimitOrder.class);
    }

    @Override
    public PlacedMarketOrder placeMarketOrder(
            final String accountId,
            @NotNull final String figi,
            @NotNull final MarketOrderRequest marketOrder
    ) throws IOException {
        final String renderedBody = mapper.writeValueAsString(marketOrder);
        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("market-order")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, JSON_MEDIA_TYPE))
                .build();

        return executeAndGetBody(request, PlacedMarketOrder.class);
    }

    @Override
    public void cancelOrder(final String accountId, @NotNull final String orderId) throws IOException {
        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("cancel")
                .addQueryParameter("orderId", orderId)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[]{}))
                .build();

        execute(request);
    }

}
