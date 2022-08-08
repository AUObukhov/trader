package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.OperationType;

public record MarketOrderRequest(Long lotsCount, OperationType operation) {
}