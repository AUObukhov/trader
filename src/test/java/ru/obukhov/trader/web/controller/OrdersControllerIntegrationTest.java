package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderState1;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderState2;
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

        final List<OrderState> orderStates = List.of(TestOrderState1.TINKOFF_ORDER_STATE, TestOrderState2.TINKOFF_ORDER_STATE);
        Mockito.when(ordersService.getOrdersSync(accountId)).thenReturn(orderStates);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/orders/get")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);
        final String expectedResult = "[" + TestOrderState1.JSON_STRING + "," + TestOrderState2.JSON_STRING + "]";
        assertResponse(requestBuilder, expectedResult);
    }

}