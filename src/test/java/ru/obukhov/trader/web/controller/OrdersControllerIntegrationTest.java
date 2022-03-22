package ru.obukhov.trader.web.controller;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.model.HttpRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.OrderStatus;
import ru.obukhov.trader.market.model.OrderType;
import ru.obukhov.trader.web.client.exchange.OrdersResponse;
import ru.obukhov.trader.web.model.exchange.GetOrdersResponse;

import java.math.BigDecimal;
import java.util.List;

class OrdersControllerIntegrationTest extends ControllerIntegrationTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOrders_returnsOrders(@Nullable final String brokerAccountId) throws Exception {

        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/orders");

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
        final List<Order> orders = List.of(order1, order2);

        final OrdersResponse ordersResponse = new OrdersResponse();
        ordersResponse.setPayload(orders);
        mockResponse(apiRequest, ordersResponse);

        final String expectedResponse = objectMapper.writeValueAsString(new GetOrdersResponse(orders));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .param("brokerAccountId", brokerAccountId)
                .contentType(MediaType.APPLICATION_JSON);
        performAndVerifyResponse(requestBuilder, expectedResponse);
    }

}