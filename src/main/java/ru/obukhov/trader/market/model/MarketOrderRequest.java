package ru.obukhov.trader.market.model;

public record MarketOrderRequest(Integer lots, OperationType operation) {
}