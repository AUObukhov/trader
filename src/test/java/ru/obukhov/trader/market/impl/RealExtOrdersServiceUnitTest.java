package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.util.PostOrderResponseBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderState;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderStates;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.core.OrdersService;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RealExtOrdersServiceUnitTest {

    @Mock
    private OrdersService ordersService;

    @InjectMocks
    private RealExtOrdersService realExtOrdersService;

    @Test
    void getOrders_returnsOrders() {

        // arrange

        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<TestOrderState> testOrderStates = List.of(TestOrderStates.ORDER_STATE1, TestOrderStates.ORDER_STATE2);

        Mockito.when(ordersService.getOrdersSync(accountId))
                .thenReturn(testOrderStates.stream().map(TestOrderState::tinkoffOrderState).toList());

        // action

        final List<OrderState> result = realExtOrdersService.getOrders(accountId);

        // assert
        final List<OrderState> expectedOrderStates = testOrderStates.stream().map(TestOrderState::orderState).toList();
        AssertUtils.assertEquals(expectedOrderStates, result);
    }

    @Test
    void getOrders_filtersOrdersByFigi() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final String figi = TestShares.APPLE.share().figi();

        mockOrders(
                accountId,
                TestData.newOrderState("order0", figi),
                TestData.newOrderState("order1", figi),
                TestData.newOrderState("order2", "figi3"),
                TestData.newOrderState("order3", "figi4"),
                TestData.newOrderState("order4", figi)
        );

        final List<OrderState> orders = realExtOrdersService.getOrders(accountId, figi);

        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals("order0", orders.get(0).orderId());
        Assertions.assertEquals("order1", orders.get(1).orderId());
        Assertions.assertEquals("order4", orders.get(2).orderId());
    }

    @Test
    void postOrder() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String figi = TestShares.APPLE.share().figi();

        final String currency = Currencies.USD;
        final int totalOrderAmount = 2000;
        final int totalCommissionAmount = 10;
        final int initialSecurityPrice = 20;
        final long lots = 30;
        final BigDecimal price = DecimalUtils.setDefaultScale(200);
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;
        final String orderId = "orderId";

        final PostOrderResponse response = new PostOrderResponseBuilder()
                .setCurrency(currency)
                .setTotalOrderAmount(totalOrderAmount)
                .setTotalCommissionAmount(totalCommissionAmount)
                .setInitialSecurityPrice(initialSecurityPrice)
                .setLots(lots)
                .setFigi(figi)
                .setDirection(direction)
                .setType(type)
                .setOrderId(orderId)
                .build();

        Mockito.when(ordersService.postOrderSync(figi, lots, DecimalUtils.toQuotation(price), direction, accountId, type, orderId))
                .thenReturn(response);

        final PostOrderResponse result = realExtOrdersService.postOrder(accountId, figi, lots, price, direction, type, orderId);

        final PostOrderResponse expectedResponse = new PostOrderResponseBuilder()
                .setCurrency(currency)
                .setTotalOrderAmount(totalOrderAmount)
                .setTotalCommissionAmount(totalCommissionAmount)
                .setInitialSecurityPrice(initialSecurityPrice)
                .setLots(lots)
                .setFigi(figi)
                .setDirection(direction)
                .setType(type)
                .setOrderId(orderId)
                .build();
        Assertions.assertEquals(expectedResponse, result);
    }

    @Test
    void cancelOrder() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String orderId = "orderId";

        realExtOrdersService.cancelOrder(accountId, orderId);

        Mockito.verify(ordersService, Mockito.times(1)).cancelOrderSync(accountId, orderId);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockOrders(final String accountId, final ru.tinkoff.piapi.contract.v1.OrderState... orderStates) {
        Mockito.when(ordersService.getOrdersSync(accountId)).thenReturn(List.of(orderStates));
    }

}