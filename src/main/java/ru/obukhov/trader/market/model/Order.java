package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;

public record Order(
        String orderId,
        String figi,
        OperationType operation,
        OrderStatus status,
        Integer requestedLots,
        Integer executedLots,
        OrderType type,
        BigDecimal price) {
}