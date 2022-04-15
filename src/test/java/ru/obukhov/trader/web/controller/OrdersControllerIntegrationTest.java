package ru.obukhov.trader.web.controller;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.OrderStatus;
import ru.obukhov.trader.market.model.OrderType;
import ru.obukhov.trader.web.model.exchange.GetOrdersResponse;

import java.util.List;

class OrdersControllerIntegrationTest extends ControllerIntegrationTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getOrders_returnsOrders(@Nullable final String brokerAccountId) throws Exception {

        final Order order1 = new Order(
                "order1",
                "figi1",
                OperationType.BUY,
                OrderStatus.FILL,
                10,
                5,
                OrderType.LIMIT,
                DecimalUtils.setDefaultScale(100)
        );

        final Order order2 = new Order(
                "order2",
                "figi2",
                OperationType.SELL,
                OrderStatus.NEW,
                1,
                0,
                OrderType.MARKET,
                DecimalUtils.setDefaultScale(1000)
        );
        final List<Order> orders = List.of(order1, order2);

        mockOrders(orders);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .param("brokerAccountId", brokerAccountId)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder, new GetOrdersResponse(orders));
    }

}