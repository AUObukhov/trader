package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

class MarketDataControllerIntegrationTest extends ControllerIntegrationTest {

    // region getTradingStatus tests

    @Test
    void getTradingStatus_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getTradingStatus_returnsStatus() throws Exception {
        final String figi = TestShare1.FIGI;

        final SecurityTradingStatus status = SecurityTradingStatus.SECURITY_TRADING_STATUS_OPENING_PERIOD;
        Mocker.mockTradingStatus(marketDataService, figi, status);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .param("figi", figi)
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, status);
    }

    // endregion

}