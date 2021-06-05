package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.OrderStatus;
import ru.tinkoff.invest.openapi.model.rest.OrderType;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OrdersServiceImplUnitTest {

    @Mock
    private TinkoffService tinkoffService;
    @Mock
    private MarketService marketService;

    @InjectMocks
    private OrdersServiceImpl service;

    @Test
    void getOrders_filtersOrdersByFigi() {
        final String ticker = "ticker";
        final String figi = "figi";

        Mockito.when(marketService.getFigi(ticker)).thenReturn(figi);
        mockOrders(
                createOrder("order0", figi),
                createOrder("order1", figi),
                createOrder("order2", "figi3"),
                createOrder("order3", "figi4"),
                createOrder("order4", figi)
        );

        final List<Order> orders = service.getOrders(ticker);

        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals("order0", orders.get(0).getOrderId());
        Assertions.assertEquals("order1", orders.get(1).getOrderId());
        Assertions.assertEquals("order4", orders.get(2).getOrderId());
    }

    private void mockOrders(Order... orders) {
        Mockito.when(tinkoffService.getOrders()).thenReturn(List.of(orders));
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