
package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;

public record PlacedMarketOrder(
        String orderId,
        OperationType operation,
        OrderStatus status,
        String rejectReason,
        String message,
        BigDecimal requestedLots,
        BigDecimal executedLots,
        Money commission
) {
}