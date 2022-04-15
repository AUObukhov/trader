package ru.obukhov.trader.market.model;

public record MarketOrderRequest(Integer lotsCount, OperationType operation) {
}