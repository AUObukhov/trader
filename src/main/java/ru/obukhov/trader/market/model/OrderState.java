package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record OrderState(
        String orderId,
        OrderExecutionReportStatus executionReportStatus,
        long lotsRequested,
        long lotsExecuted,
        BigDecimal initialOrderPrice,
        BigDecimal executedOrderPrice,
        BigDecimal totalOrderAmount,
        BigDecimal averagePositionPrice,
        BigDecimal initialCommission,
        BigDecimal executedCommission,
        String figi,
        OrderDirection direction,
        BigDecimal initialSecurityPrice,
        List<OrderStage> stages,
        BigDecimal serviceCommission,
        String currency,
        OrderType orderType,
        OffsetDateTime orderDate,
        String instrumentUid,
        String orderRequestId
) {
}