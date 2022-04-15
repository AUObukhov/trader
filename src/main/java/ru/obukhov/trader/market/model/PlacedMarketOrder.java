
package ru.obukhov.trader.market.model;

public record PlacedMarketOrder(
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