package ru.obukhov.trader.market;

import lombok.RequiredArgsConstructor;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.UserContext;

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