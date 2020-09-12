package ru.obukhov.investor.service;

import ru.tinkoff.invest.openapi.MarketContext;

public interface ConnectionService {

    void setToken(String token);

    MarketContext getMarketContext();

    void closeConnection();
}