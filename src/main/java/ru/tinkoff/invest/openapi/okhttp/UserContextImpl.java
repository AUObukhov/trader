package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.model.UserAccounts;
import ru.obukhov.trader.web.client.exchange.UserAccountsResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

final class UserContextImpl extends BaseContextImpl implements UserContext {

    private static final TypeReference<UserAccountsResponse> accountsListTypeReference =
            new TypeReference<UserAccountsResponse>() {
            };

    public UserContextImpl(@NotNull final OkHttpClient client,
                           @NotNull final String url,
                           @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @NotNull
    @Override
    public String getPath() {
        return "user";
    }

    @Override
    @NotNull
    public CompletableFuture<UserAccounts> getAccounts() {
        final CompletableFuture<UserAccounts> future = new CompletableFuture<>();
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("accounts")
                .build();
        final Request request = prepareRequest(requestUrl)
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
                    final UserAccountsResponse result = handleResponse(response, accountsListTypeReference);
                    future.complete(result.getPayload());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
