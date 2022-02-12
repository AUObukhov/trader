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
import ru.obukhov.trader.market.model.SandboxAccount;
import ru.obukhov.trader.market.model.SandboxRegisterRequest;
import ru.obukhov.trader.market.model.SandboxSetCurrencyBalanceRequest;
import ru.obukhov.trader.market.model.SandboxSetPositionBalanceRequest;
import ru.obukhov.trader.web.client.exchange.SandboxRegisterResponse;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

final class SandboxContextImpl extends BaseContextImpl implements SandboxContext {

    private static final TypeReference<SandboxRegisterResponse> sandboxRegisterResponseReference =
            new TypeReference<SandboxRegisterResponse>() {
            };

    public SandboxContextImpl(@NotNull final OkHttpClient client,
                              @NotNull final String url,
                              @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @Override
    @NotNull
    public String getPath() {
        return "sandbox";
    }

    @Override
    @NotNull
    public CompletableFuture<SandboxAccount> performRegistration(@NotNull final SandboxRegisterRequest registerRequest) {
        final CompletableFuture<SandboxAccount> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("register")
                .build();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(registerRequest);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }
        final RequestBody requestBody = RequestBody.create(renderedBody, MediaType.get("application/json"));

        final Request request = prepareRequest(requestUrl)
                .post(requestBody)
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
                    final SandboxRegisterResponse result = handleResponse(response, sandboxRegisterResponseReference);
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
    public CompletableFuture<Void> setCurrencyBalance(@NotNull final SandboxSetCurrencyBalanceRequest balanceRequest,
                                                      @Nullable String brokerAccountId) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(balanceRequest);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("currencies")
                .addPathSegment("balance")
                .build();
        Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> setPositionBalance(@NotNull final SandboxSetPositionBalanceRequest balanceRequest,
                                                      @Nullable String brokerAccountId) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final String renderedBody;
        try {
            renderedBody = mapper.writeValueAsString(balanceRequest);
        } catch (JsonProcessingException ex) {
            future.completeExceptionally(ex);
            return future;
        }

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("positions")
                .addPathSegment("balance")
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, MediaType.get("application/json")))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> removeAccount(@Nullable String brokerAccountId) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("remove")
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[]{}))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> clearAll(@Nullable final String brokerAccountId) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addPathSegment("clear")
                .build();
        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[]{}))
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
                    handleResponse(response, emptyPayloadTypeReference);
                    future.complete(null);
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
