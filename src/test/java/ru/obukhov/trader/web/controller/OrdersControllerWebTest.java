package ru.obukhov.trader.web.controller;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.List;

class OrdersControllerWebTest extends ControllerWebTest {

    @MockBean
    private OrdersService ordersService;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOrders_requestsAndReturnsOrders(@Nullable final String brokerAccountId) throws Exception {
        final Order order1 = new Order()
                .orderId("order1")
                .figi("figi1")
                .operation(OperationType.BUY)
                .status(OrderStatus.FILL)
                .requestedLots(10)
                .executedLots(5)
                .type(OrderType.LIMIT)
                .price(BigDecimal.valueOf(100));

        final Order order2 = new Order()
                .orderId("order2")
                .figi("figi2")
                .operation(OperationType.SELL)
                .status(OrderStatus.NEW)
                .requestedLots(1)
                .executedLots(0)
                .type(OrderType.MARKET)
                .price(BigDecimal.valueOf(1000));

        Mockito.when(ordersService.getOrders(brokerAccountId)).thenReturn(List.of(order1, order2));

        final String expectedResponse = ResourceUtils.getTestDataAsString("GetOrdersResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/orders/get")
                        .param("brokerAccountId", brokerAccountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(ordersService, Mockito.times(1)).getOrders(brokerAccountId);
    }

}