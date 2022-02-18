package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.UserAccounts;
import ru.obukhov.trader.web.client.service.interfaces.UserClient;

import java.io.IOException;
import java.util.List;

@Service
public class UserClientImpl extends AbstractClient implements UserClient {

    protected UserClientImpl(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        super(client, tradingProperties, apiProperties);
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
