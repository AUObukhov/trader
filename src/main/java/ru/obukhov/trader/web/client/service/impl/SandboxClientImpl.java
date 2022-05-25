package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.SandboxAccount;
import ru.obukhov.trader.market.model.SandboxRegisterRequest;
import ru.obukhov.trader.market.model.SandboxSetCurrencyBalanceRequest;
import ru.obukhov.trader.market.model.SandboxSetPositionBalanceRequest;
import ru.obukhov.trader.web.client.exchange.SandboxRegisterResponse;
import ru.obukhov.trader.web.client.service.interfaces.SandboxClient;

import java.io.IOException;

@Service
@ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
public class SandboxClientImpl extends AbstractClient implements SandboxClient {

    private static final String PARAM_BROKER_ACCOUNT_ID = "accountId";

    protected SandboxClientImpl(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        super(client, tradingProperties, apiProperties);
    }

    @Override
    public String getPath() {
        return "sandbox";
    }

    @Override
    public SandboxAccount performRegistration(@NotNull final SandboxRegisterRequest registerRequest) throws IOException {
        final HttpUrl requestUrl = url.newBuilder()
                .addPathSegment("register")
                .build();

        final Request request = prepareRequest(requestUrl)
                .post(getRequestBody(registerRequest))
                .build();

        return executeAndGetBody(request, SandboxRegisterResponse.class).getPayload();
    }

    @Override
    public void setCurrencyBalance(@NotNull final SandboxSetCurrencyBalanceRequest balanceRequest, final String accountId)
            throws IOException {
        final String renderedBody = mapper.writeValueAsString(balanceRequest);

        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("currencies")
                .addPathSegment("balance")
                .build();

        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, JSON_MEDIA_TYPE))
                .build();

        execute(request);
    }

    @Override
    public void setPositionBalance(@NotNull final SandboxSetPositionBalanceRequest balanceRequest, final String accountId)
            throws IOException {
        final String renderedBody = mapper.writeValueAsString(balanceRequest);

        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("positions")
                .addPathSegment("balance")
                .build();

        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(renderedBody, JSON_MEDIA_TYPE))
                .build();

        execute(request);
    }

    @Override
    public void removeAccount(final String accountId) throws IOException {
        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("remove")
                .build();

        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[]{}))
                .build();

        execute(request);
    }

    @Override
    public void clearAll(final String accountId) throws IOException {
        HttpUrl.Builder builder = url.newBuilder();
        if (StringUtils.isNoneEmpty(accountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, accountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("clear")
                .build();

        final Request request = prepareRequest(requestUrl)
                .post(RequestBody.create(new byte[]{}))
                .build();

        execute(request);
    }

}