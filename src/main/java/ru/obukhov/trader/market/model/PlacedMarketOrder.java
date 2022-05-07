
package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.OperationType;

public record PlacedMarketOrder(
        String orderId,
        OperationType operation,
        OrderStatus status,
        String rejectReason,
        String message,
        Long requestedLots,
        Long executedLots,
        MoneyAmount commission
) {
}