package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.Portfolio;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.web.client.service.interfaces.PortfolioClient;

import java.io.IOException;
import java.util.List;

@Service
public class PortfolioClientImpl extends AbstractClient implements PortfolioClient {

    private static final String PARAM_BROKER_ACCOUNT_ID = "brokerAccountId";

    protected PortfolioClientImpl(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        super(client, tradingProperties, apiProperties);
    }

    @Override
    public String getPath() {
        return "portfolio";
    }

    @Override
    public List<PortfolioPosition> getPortfolio(@Nullable final String brokerAccountId) throws IOException {
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
        }
        final HttpUrl requestUrl = builder.build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, Portfolio.class).getPositions();
    }

    @Override
    public List<CurrencyPosition> getPortfolioCurrencies(@Nullable final String brokerAccountId) throws IOException {
        HttpUrl.Builder builder = finalUrl.newBuilder();
        if (StringUtils.isNoneEmpty(brokerAccountId)) {
            builder.addQueryParameter(PARAM_BROKER_ACCOUNT_ID, brokerAccountId);
        }
        final HttpUrl requestUrl = builder
                .addPathSegment("currencies")
                .build();
        final Request request = buildRequest(requestUrl);

        return executeAndGetBody(request, Currencies.class).getCurrencies();
    }

}
