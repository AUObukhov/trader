package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderState;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderStates;

import java.util.List;

class OrdersControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    void getOrders_returnsBadRequest_whenAccountIdIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'accountId' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getOrders_returnsOrders() throws Exception {
        final String accountId = TestAccounts.TINKOFF.getId();
        final TestOrderState testOrderState1 = TestOrderStates.ORDER_STATE1;
        final TestOrderState testOrderState2 = TestOrderStates.ORDER_STATE2;

        final List<ru.tinkoff.piapi.contract.v1.OrderState> orderStates =
                List.of(testOrderState1.tinkoffOrderState(), testOrderState2.tinkoffOrderState());
        Mockito.when(ordersService.getOrdersSync(accountId)).thenReturn(orderStates);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);
        final List<OrderState> expectedResult = List.of(testOrderState1.orderState(), testOrderState2.orderState());
        assertResponse(requestBuilder, expectedResult);
    }

}