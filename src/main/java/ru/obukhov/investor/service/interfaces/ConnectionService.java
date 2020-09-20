package ru.obukhov.investor.service.interfaces;

import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.StreamingContext;
import ru.tinkoff.invest.openapi.UserContext;

public interface ConnectionService {

    void setToken(String token);

    MarketContext getMarketContext();

    OperationsContext getOperationsContext();

    OrdersContext getOrdersContext();

    PortfolioContext getPortfolioContext();

    UserContext getUserContext();

    StreamingContext getStreamingContext();

    void closeConnection();
}