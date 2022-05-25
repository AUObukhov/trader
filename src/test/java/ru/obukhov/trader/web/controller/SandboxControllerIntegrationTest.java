package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.web.model.exchange.ClearAllRequest;
import ru.obukhov.trader.web.model.exchange.SetCurrencyBalanceRequest;
import ru.obukhov.trader.web.model.exchange.SetPositionBalanceRequest;

import java.math.BigDecimal;

@SpringBootTest("--trading.sandbox=true")
class SandboxControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    void setCurrencyBalance_callsSetCurrencyBalanceApi() throws Exception {
        final String accountId = "2000124699";
        final String expectationId = mockResponse(HttpMethod.POST, accountId, "/openapi/sandbox/currencies/balance");

        final SetCurrencyBalanceRequest setCurrencyBalanceRequest = new SetCurrencyBalanceRequest();
        setCurrencyBalanceRequest.setAccountId(accountId);
        setCurrencyBalanceRequest.setCurrency(Currency.USD);
        setCurrencyBalanceRequest.setBalance(BigDecimal.valueOf(10000));
        final String request = TestUtils.OBJECT_MAPPER.writeValueAsString(setCurrencyBalanceRequest);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/sandbox/currency-balance")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder);

        mockServerClient.verify(expectationId);
    }

    @Test
    void setPositionBalance_callsSetPositionBalanceApi() throws Exception {
        final String accountId = "2000124699";
        final String figi = "figi";
        final String ticker = "ticker";

        mockShare(figi, ticker, Currency.RUB, 1);

        final String expectationId = mockResponse(HttpMethod.POST, accountId, "/openapi/sandbox/positions/balance");

        final SetPositionBalanceRequest setPositionBalanceRequest = new SetPositionBalanceRequest();
        setPositionBalanceRequest.setAccountId(accountId);
        setPositionBalanceRequest.setTicker(ticker);
        setPositionBalanceRequest.setBalance(BigDecimal.valueOf(100000));
        final String request = TestUtils.OBJECT_MAPPER.writeValueAsString(setPositionBalanceRequest);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/sandbox/position-balance")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder);

        mockServerClient.verify(expectationId);
    }

    @Test
    void clearAll_callsClearAllApi_whenBrokerAccountIdIsNotNull() throws Exception {
        final String accountId = "2000124699";
        final String expectationId = mockResponse(HttpMethod.POST, accountId, "/openapi/sandbox/clear");

        final ClearAllRequest clearAllRequest = new ClearAllRequest();
        clearAllRequest.setAccountId(accountId);
        final String request = TestUtils.OBJECT_MAPPER.writeValueAsString(clearAllRequest);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/sandbox/clear")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder);

        mockServerClient.verify(expectationId);
    }

}