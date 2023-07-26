package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderStage;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.time.OffsetDateTime;
import java.util.List;

class OrderMapperUnitTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void map() {
        // todo realistic data
        final String currency = Currency.EUR;
        final String orderId = "orderId";
        final OrderExecutionReportStatus executionReportStatus = OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL;
        final int lotsRequested = 1;
        final int lotsExecuted = 2;
        final double initialOrderPrice = 3;
        final double totalOrderAmount = 4;
        final double averagePositionPrice = 5;
        final double initialCommission = 6;
        final double executedCommission = 7;
        final String figi = TestShare1.FIGI;
        final OrderDirection orderDirection = OrderDirection.ORDER_DIRECTION_BUY;
        final double initialSecurityPrice = 8;
        final List<OrderStage> stages = List.of(
                TestData.createOrderStage(currency, 9, 10, "tradeId1"),
                TestData.createOrderStage(currency, 11, 12, "tradeId1")
        );
        final double serviceCommission = 13;
        final ru.tinkoff.piapi.contract.v1.OrderType orderType = OrderType.ORDER_TYPE_MARKET;
        final OffsetDateTime orderDate = OffsetDateTime.now();

        final OrderState orderState = TestData.createOrderState(
                currency,
                orderId,
                executionReportStatus,
                lotsRequested,
                lotsExecuted,
                initialOrderPrice,
                totalOrderAmount,
                averagePositionPrice,
                initialCommission,
                executedCommission,
                figi,
                orderDirection,
                initialSecurityPrice,
                stages,
                serviceCommission,
                orderType,
                orderDate
        );

        final Order order = orderMapper.map(orderState);

        Assertions.assertEquals(orderId, order.orderId());
        Assertions.assertEquals(executionReportStatus, order.executionReportStatus());
        Assertions.assertEquals(lotsExecuted, order.quantityLots());
        AssertUtils.assertEquals(initialOrderPrice, order.initialOrderPrice());
        AssertUtils.assertEquals(totalOrderAmount, order.totalOrderAmount());
        AssertUtils.assertEquals(averagePositionPrice, order.averagePositionPrice());
        AssertUtils.assertEquals(executedCommission, order.commission());
        Assertions.assertEquals(figi, order.figi());
        Assertions.assertEquals(orderDirection, order.direction());
        AssertUtils.assertEquals(initialSecurityPrice, order.initialSecurityPrice());
        AssertUtils.assertEquals(serviceCommission, order.serviceCommission());
        Assertions.assertEquals(currency, order.currency());
        Assertions.assertEquals(orderType, order.type());
        Assertions.assertEquals(orderDate, order.dateTime());
    }

}