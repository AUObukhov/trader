package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Order(
        String orderId,
        OrderExecutionReportStatus executionReportStatus,
        long quantityLots,
        BigDecimal initialOrderPrice,
        BigDecimal totalOrderAmount,
        BigDecimal averagePositionPrice,
        BigDecimal commission,
        String figi,
        OrderDirection direction,
        BigDecimal initialSecurityPrice,
        BigDecimal serviceCommission,
        Currency currency,
        ru.tinkoff.piapi.contract.v1.OrderType type,
        OffsetDateTime dateTime
) {
}