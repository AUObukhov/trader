package ru.obukhov.trader.market.interfaces;

import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.StreamingContext;
import ru.tinkoff.invest.openapi.UserContext;

public interface ConnectionService {

    MarketContext getMarketContext();

    OperationsContext getOperationsContext();

    OrdersContext getOrdersContext();

    PortfolioContext getPortfolioContext();

    UserContext getUserContext();

    StreamingContext getStreamingContext();

    SandboxContext getSandboxContext();

    void closeConnection();
}