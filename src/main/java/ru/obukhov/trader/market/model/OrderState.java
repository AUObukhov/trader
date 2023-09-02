package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record OrderState(
        String orderId,
        OrderExecutionReportStatus executionReportStatus,
        long lotsRequested,
        long lotsExecuted,
        MoneyValue initialOrderPrice,
        MoneyValue executedOrderPrice,
        MoneyValue totalOrderAmount,
        MoneyValue averagePositionPrice,
        MoneyValue initialCommission,
        MoneyValue executedCommission,
        String figi,
        OrderDirection direction,
        MoneyValue initialSecurityPrice,
        List<OrderStage> stages,
        MoneyValue serviceCommission,
        String currency,
        OrderType orderType,
        OffsetDateTime orderDate,
        String instrumentUid,
        String orderRequestId
) {
}