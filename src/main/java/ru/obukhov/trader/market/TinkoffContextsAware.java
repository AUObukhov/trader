package ru.obukhov.trader.market;

import lombok.RequiredArgsConstructor;
import ru.obukhov.trader.web.client.service.MarketClient;
import ru.obukhov.trader.web.client.service.OpenApi;
import ru.obukhov.trader.web.client.service.OperationsClient;
import ru.obukhov.trader.web.client.service.OrdersClient;
import ru.obukhov.trader.web.client.service.PortfolioClient;
import ru.obukhov.trader.web.client.service.UserClient;

@RequiredArgsConstructor
public abstract class TinkoffContextsAware {

    private final OpenApi opeApi;

    protected MarketClient getMarketContext() {
        return opeApi.getMarketClient();
    }

    protected OperationsClient getOperationsContext() {
        return opeApi.getOperationsClient();
    }

    protected OrdersClient getOrdersContext() {
        return opeApi.getOrdersClient();
    }

    protected PortfolioClient getPortfolioContext() {
        return opeApi.getPortfolioClient();
    }

    protected UserClient getUserContext() {
        return opeApi.getUserClient();
    }

}