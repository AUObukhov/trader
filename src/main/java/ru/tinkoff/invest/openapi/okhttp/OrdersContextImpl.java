package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.web.client.exchange.OrdersResponse;

import java.io.IOException;
import java.util.List;

final class OrdersContextImpl extends BaseContext implements OrdersContext {

    private static final String PARAM_BROKER_ACCOUNT_ID = "brokerAccountId";

    public OrdersContextImpl(@NotNull final OkHttpClient client, @NotNull final String url, @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @Override
    public String getPath() {
        return "orders";
    }

    @Override
    public List<Order> getOrders(@Nullable final String brokerAccountId) throws IOException {
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
        }
        final HttpUrl requestUrl = builder.build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, OrdersResponse.class).getPayload();
    }

    @Override
    public PlacedLimitOrder placeLimitOrder(
            @Nullable final String brokerAccountId,
            @NotNull final String figi,
            @NotNull final LimitOrderRequest limitOrder
    ) throws IOException {
        final String renderedBody = mapper.writeValueAsString(limitOrder);

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
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
            @Nullable final String brokerAccountId,
            @NotNull final String figi,
            @NotNull final MarketOrderRequest marketOrder
    ) throws IOException {
        final String renderedBody = mapper.writeValueAsString(marketOrder);
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
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
    public void cancelOrder(@Nullable final String brokerAccountId, @NotNull final String orderId) throws IOException {
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
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
