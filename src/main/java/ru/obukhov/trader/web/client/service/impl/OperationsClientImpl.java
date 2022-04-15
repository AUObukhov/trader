package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.Operations;
import ru.obukhov.trader.web.client.service.interfaces.OperationsClient;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OperationsClientImpl extends AbstractClient implements OperationsClient {

    protected OperationsClientImpl(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        super(client, tradingProperties, apiProperties);
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
        HttpUrl.Builder builder = url.newBuilder();
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

        return executeAndGetBody(request, Operations.class).operations();
    }

}