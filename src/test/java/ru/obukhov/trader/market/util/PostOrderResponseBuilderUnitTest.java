package ru.obukhov.trader.market.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.math.BigDecimal;

class PostOrderResponseBuilderUnitTest {

    @Test
    void test_whenTypesAreSimple_andOrderIdIsSet() {
        final String currency = TestShare1.CURRENCY;
        final double totalPrice = 1000;
        final double totalCommissionAmount = 5;
        final double currentPrice = 100;
        final long quantityLots = 10;
        final String figi = TestShare1.FIGI;
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;
        final String orderId = "orderId";

        final PostOrderResponse response = new PostOrderResponseBuilder()
                .setCurrency(currency)
                .setTotalOrderAmount(totalPrice)
                .setTotalCommissionAmount(totalCommissionAmount)
                .setInitialSecurityPrice(currentPrice)
                .setQuantityLots(quantityLots)
                .setFigi(figi)
                .setDirection(direction)
                .setType(type)
                .setOrderId(orderId)
                .build();

        Assertions.assertEquals(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL, response.getExecutionReportStatus());
        Assertions.assertEquals(quantityLots, response.getLotsRequested());
        Assertions.assertEquals(quantityLots, response.getLotsExecuted());
        AssertUtils.assertEquals(totalPrice + totalCommissionAmount, response.getInitialOrderPrice());
        AssertUtils.assertEquals(totalPrice + totalCommissionAmount, response.getExecutedOrderPrice());
        AssertUtils.assertEquals(totalPrice, response.getTotalOrderAmount());
        AssertUtils.assertEquals(totalCommissionAmount, response.getInitialCommission());
        AssertUtils.assertEquals(totalCommissionAmount, response.getExecutedCommission());
        Assertions.assertEquals(figi, response.getFigi());
        Assertions.assertEquals(direction, response.getDirection());
        AssertUtils.assertEquals(currentPrice, response.getInitialSecurityPrice());
        Assertions.assertEquals(type, response.getOrderType());
        Assertions.assertEquals(orderId, response.getOrderId());
    }

    @Test
    void test_whenTypesAreSimple_andOrderIdIsNotSet() {
        final String currency = TestShare1.CURRENCY;
        final double totalPrice = 1000;
        final double totalCommissionAmount = 5;
        final double currentPrice = 100;
        final long quantityLots = 10;
        final String figi = TestShare1.FIGI;
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;

        final PostOrderResponse response = new PostOrderResponseBuilder()
                .setCurrency(currency)
                .setTotalOrderAmount(totalPrice)
                .setTotalCommissionAmount(totalCommissionAmount)
                .setInitialSecurityPrice(currentPrice)
                .setQuantityLots(quantityLots)
                .setFigi(figi)
                .setDirection(direction)
                .setType(type)
                .setOrderId(null)
                .build();

        Assertions.assertEquals(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL, response.getExecutionReportStatus());
        Assertions.assertEquals(quantityLots, response.getLotsRequested());
        Assertions.assertEquals(quantityLots, response.getLotsExecuted());
        AssertUtils.assertEquals(totalPrice + totalCommissionAmount, response.getInitialOrderPrice());
        AssertUtils.assertEquals(totalPrice + totalCommissionAmount, response.getExecutedOrderPrice());
        AssertUtils.assertEquals(totalPrice, response.getTotalOrderAmount());
        AssertUtils.assertEquals(totalCommissionAmount, response.getInitialCommission());
        AssertUtils.assertEquals(totalCommissionAmount, response.getExecutedCommission());
        Assertions.assertEquals(figi, response.getFigi());
        Assertions.assertEquals(direction, response.getDirection());
        AssertUtils.assertEquals(currentPrice, response.getInitialSecurityPrice());
        Assertions.assertEquals(type, response.getOrderType());
        Assertions.assertTrue(response.getOrderId().isEmpty());
    }

    @Test
    void test_whenTypesAreComplex() {
        final String currency = TestShare1.CURRENCY;
        final BigDecimal totalPrice = DecimalUtils.setDefaultScale(1000);
        final BigDecimal totalCommissionAmount = DecimalUtils.setDefaultScale(5);
        final BigDecimal currentPrice = DecimalUtils.setDefaultScale(100);
        final long quantityLots = 10;
        final String figi = TestShare1.FIGI;
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;
        final String orderId = "orderId";

        final PostOrderResponse response = new PostOrderResponseBuilder()
                .setCurrency(currency)
                .setTotalOrderAmount(totalPrice)
                .setTotalCommissionAmount(totalCommissionAmount)
                .setInitialSecurityPrice(currentPrice)
                .setQuantityLots(quantityLots)
                .setFigi(figi)
                .setDirection(direction)
                .setType(type)
                .setOrderId(orderId)
                .build();

        Assertions.assertEquals(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL, response.getExecutionReportStatus());
        Assertions.assertEquals(quantityLots, response.getLotsRequested());
        Assertions.assertEquals(quantityLots, response.getLotsExecuted());
        AssertUtils.assertEquals(totalPrice.add(totalCommissionAmount), response.getInitialOrderPrice());
        AssertUtils.assertEquals(totalPrice.add(totalCommissionAmount), response.getExecutedOrderPrice());
        AssertUtils.assertEquals(totalPrice, response.getTotalOrderAmount());
        AssertUtils.assertEquals(totalCommissionAmount, response.getInitialCommission());
        AssertUtils.assertEquals(totalCommissionAmount, response.getExecutedCommission());
        Assertions.assertEquals(figi, response.getFigi());
        Assertions.assertEquals(direction, response.getDirection());
        AssertUtils.assertEquals(currentPrice, response.getInitialSecurityPrice());
        Assertions.assertEquals(type, response.getOrderType());
        Assertions.assertEquals(orderId, response.getOrderId());
    }

}