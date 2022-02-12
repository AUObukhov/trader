package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.web.client.exceptions.NotEnoughBalanceException;
import ru.obukhov.trader.web.client.exceptions.OpenApiException;
import ru.obukhov.trader.web.client.exceptions.OrderAlreadyCancelledException;
import ru.obukhov.trader.web.client.exchange.LimitOrderResponse;
import ru.obukhov.trader.web.client.exchange.MarketOrderResponse;
import ru.obukhov.trader.web.client.exchange.OrdersResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

final class OrdersContextImpl extends BaseContextImpl implements OrdersContext {

    private static final String NOT_ENOUGH_BALANCE_CODE = "NOT_ENOUGH_BALANCE";
    private static final String ORDER_ERROR_CODE = "ORDER_ERROR";

    private static final TypeReference<OrdersResponse> listOrderTypeReference =
            new TypeReference<OrdersResponse>() {
            };
    private static final TypeReference<LimitOrderResponse> placedLimitOrderTypeReference =
            new TypeReference<LimitOrderResponse>() {
            };
    private static final TypeReference<MarketOrderResponse> placedMarketOrderTypeReference =
            new TypeReference<MarketOrderResponse>() {
            };

    public OrdersContextImpl(@NotNull final OkHttpClient client,
                             @NotNull final String url,
                             @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @NotNull
    @Override
    public String getPath() {
        return "orders";
    }

    @Override
    @NotNull
    public CompletableFuture<List<Order>> getOrders(@Nullable final String brokerAccountId) {
        final CompletableFuture<List<Order>> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .build();
        final Request request = prepareRequest(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    final OrdersResponse result = handleResponse(response, listOrderTypeReference);
                    future.complete(result.getPayload());
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(NOT_ENOUGH_BALANCE_CODE)) {
                        future.completeExceptionally(new NotEnoughBalanceException(ex.getMessage(), ex.getCode()));
                    } else {
                        future.completeExceptionally(ex);
                    }
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<PlacedLimitOrder> placeLimitOrder(@NotNull final String figi,
                                                               @NotNull final LimitOrderRequest limitOrder,
                                                               @Nullable final String brokerAccountId) {
        final CompletableFuture<PlacedLimitOrder> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(limitOrder);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("limit-order")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                try {
                    final LimitOrderResponse result = handleResponse(response, placedLimitOrderTypeReference);
                    future.complete(result.getPayload());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<PlacedMarketOrder> placeMarketOrder(@NotNull final String figi,
                                                                 @NotNull final MarketOrderRequest marketOrder,
                                                                 @Nullable final String brokerAccountId) {
        final CompletableFuture<PlacedMarketOrder> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(marketOrder);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("market-order")
                .addQueryParameter("figi", figi)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                try {
                    final MarketOrderResponse result = handleResponse(response, placedMarketOrderTypeReference);
                    future.complete(result.getPayload());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> cancelOrder(@NotNull final String orderId,
                                               @Nullable final String brokerAccountId) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("cancel")
                .addQueryParameter("orderId", orderId)
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[]{}))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                logger.error("При запросе к REST API произошла ошибка", e);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                try {
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (OpenApiException ex) {
                    if (ex.getCode().equals(ORDER_ERROR_CODE)) {
                        future.completeExceptionally(new OrderAlreadyCancelledException(ex.getMessage(), ex.getCode()));
                    } else {
                        future.completeExceptionally(ex);
                    }
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
