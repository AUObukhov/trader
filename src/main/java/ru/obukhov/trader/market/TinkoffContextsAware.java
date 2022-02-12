package ru.obukhov.trader.market;

import lombok.RequiredArgsConstructor;
import ru.tinkoff.invest.openapi.okhttp.MarketContext;
import ru.tinkoff.invest.openapi.okhttp.OpenApi;
import ru.tinkoff.invest.openapi.okhttp.OperationsContext;
import ru.tinkoff.invest.openapi.okhttp.OrdersContext;
import ru.tinkoff.invest.openapi.okhttp.PortfolioContext;
import ru.tinkoff.invest.openapi.okhttp.UserContext;

@RequiredArgsConstructor
public abstract class TinkoffContextsAware {

    private final OpenApi opeApi;

    protected MarketContext getMarketContext() {
        return opeApi.getMarketContext();
    }

    protected OperationsContext getOperationsContext() {
        return opeApi.getOperationsContext();
    }

    protected OrdersContext getOrdersContext() {
        return opeApi.getOrdersContext();
    }

    protected PortfolioContext getPortfolioContext() {
        return opeApi.getPortfolioContext();
    }

    protected UserContext getUserContext() {
        return opeApi.getUserContext();
    }

}