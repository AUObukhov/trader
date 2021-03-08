package ru.obukhov.investor.common.service.impl;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.market.impl.OrdersServiceImpl;
import ru.obukhov.investor.market.interfaces.MarketService;
import ru.obukhov.investor.market.interfaces.OrdersService;
import ru.obukhov.investor.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.OrderType;
import ru.tinkoff.invest.openapi.models.orders.Status;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrdersServiceImplTest extends BaseMockedTest {

    private static final String FIGI = "figi";
    private static final String TICKER = "ticker";

    @Mock
    private TinkoffService tinkoffService;
    @Mock
    private MarketService marketService;

    private OrdersService service;

    @Before
    public void setUp() {
        this.service = new OrdersServiceImpl(tinkoffService, marketService);
    }

    @Test
    public void getOrders_filtersOrdersByFigi() {
        String ticker = TICKER;
        String figi = FIGI;

        mockFigi(ticker, figi);
        mockOrders(ImmutableList.of(
                createOrder("order0", figi),
                createOrder("order1", figi),
                createOrder("order2", "figi3"),
                createOrder("order3", "figi4"),
                createOrder("order4", figi)
        ));

        List<Order> orders = service.getOrders(ticker);

        assertEquals(3, orders.size());
        assertEquals("order0", orders.get(0).id);
        assertEquals("order1", orders.get(1).id);
        assertEquals("order4", orders.get(2).id);
    }

    private void mockFigi(String ticker, String figi) {
        when(marketService.getFigi(eq(ticker))).thenReturn(figi);
    }

    private void mockOrders(List<Order> orders) {
        when(tinkoffService.getOrders()).thenReturn(orders);
    }

    private Order createOrder(String id, String figi) {
        return new Order(id, figi,
                Operation.Buy, Status.Fill, 1, 1, OrderType.Market, BigDecimal.TEN);
    }
}