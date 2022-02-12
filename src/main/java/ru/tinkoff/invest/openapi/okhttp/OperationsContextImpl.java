package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.Operations;
import ru.obukhov.trader.web.client.exchange.OperationsResponse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

final class OperationsContextImpl extends BaseContextImpl implements OperationsContext {

    private static final TypeReference<OperationsResponse> operationsListTypeReference =
            new TypeReference<OperationsResponse>() {
            };

    public OperationsContextImpl(@NotNull final OkHttpClient client,
                                 @NotNull final String url,
                                 @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @NotNull
    @Override
    public String getPath() {
        return "operations";
    }

    @Override
    @NotNull
    public CompletableFuture<Operations> getOperations(@NotNull final OffsetDateTime from,
                                                       @NotNull final OffsetDateTime to,
                                                       @Nullable final String figi,
                                                       @Nullable final String brokerAccountId) {
        final CompletableFuture<Operations> future = new CompletableFuture<>();
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (Objects.nonNull(figi) && !figi.isEmpty())
            builder.addQueryParameter("figi", figi);
        if (Objects.nonNull(brokerAccountId) && !brokerAccountId.isEmpty())
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        final HttpUrl requestUrl = builder
                .addQueryParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
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
                    final OperationsResponse result = handleResponse(response, operationsListTypeReference);
                    future.complete(result.getPayload());
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            }
        });

        return future;
    }

}
