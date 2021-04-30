package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.OrderStatus;
import ru.tinkoff.invest.openapi.model.rest.OrderType;

import java.math.BigDecimal;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class OrdersServiceImplUnitTest extends BaseMockedTest {

    private static final String FIGI = "figi";
    private static final String TICKER = "ticker";

    @Mock
    private TinkoffService tinkoffService;
    @Mock
    private MarketService marketService;

    private OrdersServiceImpl service;

    @BeforeEach
    public void setUp() {
        this.service = new OrdersServiceImpl(tinkoffService, marketService);
    }

    @Test
    void getOrders_filtersOrdersByFigi() {
        String ticker = TICKER;
        String figi = FIGI;

        mockFigi(ticker, figi);
        mockOrders(List.of(
                createOrder("order0", figi),
                createOrder("order1", figi),
                createOrder("order2", "figi3"),
                createOrder("order3", "figi4"),
                createOrder("order4", figi)
        ));

        List<Order> orders = service.getOrders(ticker);

        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals("order0", orders.get(0).getOrderId());
        Assertions.assertEquals("order1", orders.get(1).getOrderId());
        Assertions.assertEquals("order4", orders.get(2).getOrderId());
    }

    private void mockFigi(String ticker, String figi) {
        Mockito.when(marketService.getFigi(ticker)).thenReturn(figi);
    }

    private void mockOrders(List<Order> orders) {
        Mockito.when(tinkoffService.getOrders()).thenReturn(orders);
    }

    private Order createOrder(String id, String figi) {
        return new Order()
                .orderId(id)
                .figi(figi)
                .operation(OperationType.BUY)
                .status(OrderStatus.FILL)
                .requestedLots(1)
                .executedLots(1)
                .type(OrderType.MARKET)
                .price(BigDecimal.TEN);
    }

}