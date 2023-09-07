package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderState;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderStates;
import ru.tinkoff.piapi.contract.v1.OrderState;

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
        final String accountId = TestData.ACCOUNT_ID1;
        final TestOrderState testOrderState1 = TestOrderStates.ORDER_STATE1;
        final TestOrderState testOrderState2 = TestOrderStates.ORDER_STATE2;

        final List<OrderState> orderStates = List.of(testOrderState1.tinkoffOrderState(), testOrderState2.tinkoffOrderState());
        Mockito.when(ordersService.getOrdersSync(accountId)).thenReturn(orderStates);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);
        final String expectedResult = "[" + testOrderState1.jsonString() + "," + testOrderState2.jsonString() + "]";
        assertResponse(requestBuilder, expectedResult);
    }

}