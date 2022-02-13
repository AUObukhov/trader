package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.SandboxAccount;
import ru.obukhov.trader.market.model.SandboxRegisterRequest;
import ru.obukhov.trader.market.model.SandboxSetCurrencyBalanceRequest;
import ru.obukhov.trader.market.model.SandboxSetPositionBalanceRequest;

import java.io.IOException;

final class SandboxContextImpl extends BaseContextImpl implements SandboxContext {

    private static final String PARAM_BROKER_ACCOUNT_ID = "brokerAccountId";

    public SandboxContextImpl(@NotNull final OkHttpClient client, @NotNull final String url, @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @Override
    public String getPath() {
        return "sandbox";
    }

    @Override
    public SandboxAccount performRegistration(@NotNull final SandboxRegisterRequest registerRequest) throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("register")
                .build();

        final Request request = prepareRequest(requestUrl)
                .post(getRequestBody(registerRequest))
                .build();

        return executeAndGetBody(request, SandboxAccount.class);
    }

    @Override
    public void setCurrencyBalance(@NotNull final SandboxSetCurrencyBalanceRequest balanceRequest, @Nullable final String brokerAccountId)
            throws IOException {
        final String renderedBody = mapper.writeValueAsString(balanceRequest);

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
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
    public void setPositionBalance(@NotNull final SandboxSetPositionBalanceRequest balanceRequest, @Nullable final String brokerAccountId)
            throws IOException {
        final String renderedBody = mapper.writeValueAsString(balanceRequest);

        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
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
    public void removeAccount(@Nullable final String brokerAccountId) throws IOException {
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
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
    public void clearAll(@Nullable final String brokerAccountId) throws IOException {
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
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