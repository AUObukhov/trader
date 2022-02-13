package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.Operations;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class OperationsContextImpl extends BaseContextImpl implements OperationsContext {

    public OperationsContextImpl(@NotNull final OkHttpClient client, @NotNull final String url, @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @Override
    public String getPath() {
        return "operations";
    }

    @Override
    public List<Operation> getOperations(
            @Nullable final String brokerAccountId,
            @NotNull final OffsetDateTime from,
            @NotNull final OffsetDateTime to,
            @Nullable final String figi
    ) throws IOException {
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter("brokerAccountId", brokerAccountId);
        }
        if (StringUtils.isNoneEmpty(figi)) {
            builder.addQueryParameter("figi", figi);
        }
        final HttpUrl requestUrl = builder
                .addQueryParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addQueryParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .build();

        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, Operations.class).getOperations();
    }

}