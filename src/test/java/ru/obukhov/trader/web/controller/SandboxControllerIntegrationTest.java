package ru.obukhov.trader.web.controller;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.web.model.exchange.ClearAllRequest;
import ru.obukhov.trader.web.model.exchange.SetCurrencyBalanceRequest;
import ru.obukhov.trader.web.model.exchange.SetPositionBalanceRequest;

import java.math.BigDecimal;

@SpringBootTest("--trading.sandbox=true")
class SandboxControllerIntegrationTest extends ControllerIntegrationTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void setCurrencyBalance_callsSetCurrencyBalanceApi(@Nullable final String brokerAccountId) throws Exception {
        final String expectationId = mockResponse(HttpMethod.POST, brokerAccountId, "/openapi/sandbox/currencies/balance");

        final SetCurrencyBalanceRequest setCurrencyBalanceRequest = new SetCurrencyBalanceRequest();
        setCurrencyBalanceRequest.setBrokerAccountId(brokerAccountId);
        setCurrencyBalanceRequest.setCurrency(Currency.USD);
        setCurrencyBalanceRequest.setBalance(BigDecimal.valueOf(10000));
        final String request = objectMapper.writeValueAsString(setCurrencyBalanceRequest);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/sandbox/currency-balance")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder);

        mockServerClient.verify(expectationId);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void setPositionBalance_callsSetPositionBalanceApi(@Nullable final String brokerAccountId) throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";

        mockFigiByTicker(ticker, figi);

        final String expectationId = mockResponse(HttpMethod.POST, brokerAccountId, "/openapi/sandbox/positions/balance");

        final SetPositionBalanceRequest setPositionBalanceRequest = new SetPositionBalanceRequest();
        setPositionBalanceRequest.setBrokerAccountId(brokerAccountId);
        setPositionBalanceRequest.setTicker(ticker);
        setPositionBalanceRequest.setBalance(BigDecimal.valueOf(100000));
        final String request = objectMapper.writeValueAsString(setPositionBalanceRequest);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/sandbox/position-balance")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder);

        mockServerClient.verify(expectationId);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void clearAll_callsClearAllApi_whenBrokerAccountIdIsNotNull(@Nullable final String brokerAccountId) throws Exception {
        final String expectationId = mockResponse(HttpMethod.POST, brokerAccountId, "/openapi/sandbox/clear");

        final ClearAllRequest clearAllRequest = new ClearAllRequest();
        clearAllRequest.setBrokerAccountId(brokerAccountId);
        final String request = objectMapper.writeValueAsString(clearAllRequest);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/sandbox/clear")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder);

        mockServerClient.verify(expectationId);
    }

}