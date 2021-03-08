package ru.obukhov.investor.market;

import lombok.RequiredArgsConstructor;
import ru.obukhov.investor.market.interfaces.ConnectionService;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.PortfolioContext;

@RequiredArgsConstructor
public abstract class TinkoffContextsAware {

    private final ConnectionService connectionService;

    protected MarketContext getMarketContext() {
        return connectionService.getMarketContext();
    }

    protected OperationsContext getOperationsContext() {
        return connectionService.getOperationsContext();
    }

    protected OrdersContext getOrdersContext() {
        return connectionService.getOrdersContext();
    }

    protected PortfolioContext getPortfolioContext() {
        return connectionService.getPortfolioContext();
    }

}