package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.OrderStatus;
import ru.tinkoff.invest.openapi.model.rest.OrderType;

import java.math.BigDecimal;
import java.util.Arrays;

class OrdersControllerIntegrationTest extends ControllerIntegrationTest {

    @MockBean
    private OrdersService ordersService;

    @Test
    void getOrders_requestsAndReturnsOrders() throws Exception {
        Order order1 = new Order()
                .orderId("order1")
                .figi("figi1")
                .operation(OperationType.BUY)
                .status(OrderStatus.FILL)
                .requestedLots(10)
                .executedLots(5)
                .type(OrderType.LIMIT)
                .price(BigDecimal.valueOf(100));

        Order order2 = new Order()
                .orderId("order2")
                .figi("figi2")
                .operation(OperationType.SELL)
                .status(OrderStatus.NEW)
                .requestedLots(1)
                .executedLots(0)
                .type(OrderType.MARKET)
                .price(BigDecimal.valueOf(1000));

        Mockito.when(ordersService.getOrders()).thenReturn(Arrays.asList(order1, order2));

        String expectedResponse = ResourceUtils.getResourceAsString("test-data/GetOrdersResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/orders/get")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(ordersService, Mockito.times(1)).getOrders();
    }

}