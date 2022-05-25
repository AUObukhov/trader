package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.OrderStatus;
import ru.obukhov.trader.market.model.OrderType;
import ru.obukhov.trader.web.model.exchange.GetOrdersResponse;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.util.List;

class OrdersControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getOrders_returnsOrders() throws Exception {
        final String accountId = "2000124699";

        final Order order1 = new Order(
                "order1",
                "figi1",
                OperationType.OPERATION_TYPE_BUY,
                OrderStatus.FILL,
                10,
                5,
                OrderType.LIMIT,
                DecimalUtils.setDefaultScale(100)
        );

        final Order order2 = new Order(
                "order2",
                "figi2",
                OperationType.OPERATION_TYPE_SELL,
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
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder, new GetOrdersResponse(orders));
    }

}