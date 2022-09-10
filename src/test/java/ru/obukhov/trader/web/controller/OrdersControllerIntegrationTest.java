package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.web.model.exchange.GetOrdersResponse;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderStage;
import ru.tinkoff.piapi.contract.v1.OrderState;

import java.time.OffsetDateTime;
import java.util.List;

class OrdersControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getOrders_returnsBadRequest_whenAccountIdIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'accountId' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getOrders_returnsOrders() throws Exception {
        final String accountId = "2000124699";

        final Currency currency1 = Currency.RUB;
        final List<OrderStage> stages1 = List.of(
                TestData.createOrderStage(currency1, 100, 1, ""), // todo realistic tradeId
                TestData.createOrderStage(currency1, 200, 2, "")
        );
        final OffsetDateTime dateTime1 = OffsetDateTime.now();
        final OrderState orderState1 = TestData.createOrderState( // todo realistic data
                Currency.RUB,
                "order1",
                OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL,
                10,
                5,
                100,
                100,
                100,
                100,
                100,
                "figi1",
                OrderDirection.ORDER_DIRECTION_BUY,
                100,
                stages1,
                1,
                ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_LIMIT,
                dateTime1
        );

        final Currency currency2 = Currency.RUB;
        final List<OrderStage> stages2 = List.of(
                TestData.createOrderStage(currency2, 100, 1, ""), // todo realistic tradeId
                TestData.createOrderStage(currency2, 200, 2, "")
        );
        final OffsetDateTime dateTime2 = OffsetDateTime.now();
        final OrderState orderState2 = TestData.createOrderState( // todo realistic data
                Currency.RUB,
                "order2",
                OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_PARTIALLYFILL,
                10,
                5,
                100,
                100,
                100,
                100,
                100,
                "figi2",
                OrderDirection.ORDER_DIRECTION_SELL,
                100,
                stages2,
                1,
                ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_LIMIT,
                dateTime2
        );

        final List<OrderState> orderStates = List.of(orderState1, orderState2);

        Mockito.when(ordersService.getOrdersSync(accountId)).thenReturn(orderStates);

        final Order order1 = TestData.createOrder(
                Currency.RUB,
                "order1",
                OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL,
                5,
                100,
                100,
                100,
                100,
                "figi1",
                OrderDirection.ORDER_DIRECTION_BUY,
                100,
                1,
                ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_LIMIT,
                dateTime1
        );
        final Order order2 = TestData.createOrder(
                Currency.RUB,
                "order2",
                OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_PARTIALLYFILL,
                5,
                100,
                100,
                100,
                100,
                "figi2",
                OrderDirection.ORDER_DIRECTION_SELL,
                100,
                1,
                ru.tinkoff.piapi.contract.v1.OrderType.ORDER_TYPE_LIMIT,
                dateTime2
        );
        final List<Order> orders = List.of(order1, order2);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder, new GetOrdersResponse(orders));
    }


}