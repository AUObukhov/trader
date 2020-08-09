package ru.obukhov.investor.service;

import ru.tinkoff.invest.openapi.OpenApi;

public interface ConnectionService {

    OpenApi getApi(String token);

    void closeConnection(String token);
}