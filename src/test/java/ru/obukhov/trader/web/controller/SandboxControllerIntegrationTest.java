package ru.obukhov.trader.web.controller;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.model.HttpRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
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
        HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.POST)
                .withPath("/openapi/sandbox/currencies/balance");
        if (brokerAccountId != null) {
            apiRequest = apiRequest.withQueryStringParameter("brokerAccountId", brokerAccountId);
        }
        String expectationId = mockResponse(apiRequest);

        final SetCurrencyBalanceRequest setCurrencyBalanceRequest = new SetCurrencyBalanceRequest();
        setCurrencyBalanceRequest.setBrokerAccountId(brokerAccountId);
        setCurrencyBalanceRequest.setCurrency(Currency.USD);
        setCurrencyBalanceRequest.setBalance(BigDecimal.valueOf(10000));
        final String request = objectMapper.writeValueAsString(setCurrencyBalanceRequest);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/sandbox/currency-balance")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockServerClient.verify(expectationId);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void setPositionBalance_callsSetPositionBalanceApi(@Nullable final String brokerAccountId) throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";

        mockFigiByTicker(ticker, figi);

        HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.POST)
                .withPath("/openapi/sandbox/positions/balance");
        if (brokerAccountId != null) {
            apiRequest = apiRequest.withQueryStringParameter("brokerAccountId", brokerAccountId);
        }
        String expectationId = mockResponse(apiRequest);

        final SetPositionBalanceRequest setPositionBalanceRequest = new SetPositionBalanceRequest();
        setPositionBalanceRequest.setBrokerAccountId(brokerAccountId);
        setPositionBalanceRequest.setTicker(ticker);
        setPositionBalanceRequest.setBalance(BigDecimal.valueOf(100000));
        final String request = objectMapper.writeValueAsString(setPositionBalanceRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/position-balance")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockServerClient.verify(expectationId);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void clearAll_callsClearAllApi_whenBrokerAccountIdIsNotNull(@Nullable final String brokerAccountId) throws Exception {
        HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.POST)
                .withPath("/openapi/sandbox/clear");
        if (brokerAccountId != null) {
            apiRequest = apiRequest.withQueryStringParameter("brokerAccountId", brokerAccountId);
        }
        String expectationId = mockResponse(apiRequest);

        final ClearAllRequest clearAllRequest = new ClearAllRequest();
        clearAllRequest.setBrokerAccountId(brokerAccountId);
        final String request = objectMapper.writeValueAsString(clearAllRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/clear")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockServerClient.verify(expectationId);
    }

}