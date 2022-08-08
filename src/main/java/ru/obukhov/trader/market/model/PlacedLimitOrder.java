package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.OperationType;

public record PlacedLimitOrder(
        String orderId,
        OperationType operation,
        OrderStatus status,
        String rejectReason,
        String message,
        Integer requestedLots,
        Integer executedLots,
        MoneyAmount commission
) {
}