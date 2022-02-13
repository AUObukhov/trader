package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.UserAccounts;

import java.io.IOException;
import java.util.List;

final class UserContextImpl extends BaseContextImpl implements UserContext {

    public UserContextImpl(@NotNull final OkHttpClient client, @NotNull final String url, @NotNull final String authToken) {
        super(client, url, authToken);
    }

    @Override
    public String getPath() {
        return "user";
    }

    @Override
    public List<UserAccount> getAccounts() throws IOException {
        final HttpUrl requestUrl = finalUrl.newBuilder()
                .addPathSegment("accounts")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, UserAccounts.class).getAccounts();
    }

}
