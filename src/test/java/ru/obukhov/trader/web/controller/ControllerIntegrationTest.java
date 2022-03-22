package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.TokenValidationStartupListener;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.client.exchange.MarketInstrumentListResponse;

import java.util.List;

@MockServerTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
abstract class ControllerIntegrationTest {

    private static final JsonPathResultMatchers RESULT_MESSAGE_MATCHER = MockMvcResultMatchers.jsonPath("$.message");

    /**
     * To prevent token validation on startup
     */
    @MockBean
    private TokenValidationStartupListener tokenValidationStartupListener;

    private ClientAndServer mockServer;

    protected MockServerClient mockServerClient;
    protected final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    protected TradingProperties tradingProperties;
    @Autowired
    protected ApiProperties apiProperties;
    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    public void startMockServer() {
        mockServer = ClientAndServer.startClientAndServer(getPort());
        mockServerClient = new MockServerClient("localhost", getPort());
    }

    @AfterEach
    public void stopMockServer() {
        mockServer.stop();
    }

    protected String mockResponse(final HttpRequest apiRequest) {
        HttpResponse apiResponse = HttpResponse.response();

        final Expectation[] expectations = mockServerClient.when(apiRequest, Times.once())
                .respond(apiResponse);
        return expectations[0].getId();
    }

    protected String mockResponse(final HttpRequest apiRequest, final Object response) throws JsonProcessingException {
        final HttpResponse apiResponse = HttpResponse.response()
                .withBody(objectMapper.writeValueAsString(response));

        final Expectation[] expectations = mockServerClient.when(apiRequest, Times.once())
                .respond(apiResponse);
        return expectations[0].getId();
    }

    protected int getPort() {
        return ObjectUtils.defaultIfNull(apiProperties.port(), 8081);
    }

    protected String getAuthorizationHeader() {
        return "Bearer " + tradingProperties.getToken();
    }

    protected void performAndVerifyResponse(final MockHttpServletRequestBuilder builder, final String expectedResponse) throws Exception {
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    protected ResultMatcher getJsonPathMessageMatcher(final String expectedMessage) {
        return RESULT_MESSAGE_MATCHER.value(expectedMessage);
    }

    protected void mockFigiByTicker(final String ticker, final String figi) throws JsonProcessingException {
        final HttpRequest apiRequest = HttpRequest.request()
                .withHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                .withMethod(HttpMethod.GET.name())
                .withPath("/openapi/market/search/by-ticker")
                .withQueryStringParameter("ticker", ticker);

        final MarketInstrument instrument = new MarketInstrument().figi(figi);
        final MarketInstrumentListResponse marketInstrumentListResponse = TestData.createMarketInstrumentListResponse(List.of(instrument));
        mockResponse(apiRequest, marketInstrumentListResponse);
    }

}