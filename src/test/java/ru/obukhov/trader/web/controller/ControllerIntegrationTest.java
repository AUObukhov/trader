package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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

    protected static final JsonPathResultMatchers RESULT_MESSAGE_MATCHER = MockMvcResultMatchers.jsonPath("$.message");
    protected static final JsonPathResultMatchers ERRORS_MATCHER = MockMvcResultMatchers.jsonPath("$.errors");
    protected static final ResultMatcher JSON_CONTENT_MATCHER = MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

    /**
     * To prevent token validation on startup. Otherwise, validation performed before MockServer initialization
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

    protected int getPort() {
        return ObjectUtils.defaultIfNull(apiProperties.port(), 8081);
    }

    protected HttpRequest createAuthorizedHttpRequest(final HttpMethod httpMethod) {
        return HttpRequest.request()
                .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tradingProperties.getToken())
                .withMethod(httpMethod.name());
    }

    protected String mockResponse(final HttpRequest httpRequest) {
        HttpResponse apiResponse = HttpResponse.response();

        final Expectation[] expectations = mockServerClient.when(httpRequest, Times.once())
                .respond(apiResponse);
        return expectations[0].getId();
    }

    protected String mockResponse(final HttpRequest httpRequest, final Object response) throws JsonProcessingException {
        final HttpResponse apiResponse = HttpResponse.response()
                .withBody(objectMapper.writeValueAsString(response));

        final Expectation[] expectations = mockServerClient.when(httpRequest, Times.once())
                .respond(apiResponse);
        return expectations[0].getId();
    }

    protected void mockFigiByTicker(final String ticker, final String figi) throws JsonProcessingException {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/market/search/by-ticker")
                .withQueryStringParameter("ticker", ticker);

        final MarketInstrument instrument = new MarketInstrument().figi(figi);
        final MarketInstrumentListResponse marketInstrumentListResponse = TestData.createMarketInstrumentListResponse(List.of(instrument));
        mockResponse(apiRequest, marketInstrumentListResponse);
    }

    protected void performAndExpectResponse(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));
    }

    protected void performAndExpectResponse(final MockHttpServletRequestBuilder builder, final Object expectedResponse) throws Exception {
        final String expectedResponseString = objectMapper.writeValueAsString(expectedResponse);
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }

    protected void performAndExpectBadRequestResult(final MockHttpServletRequestBuilder requestBuilder, final String expectedResultMessage)
            throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value(expectedResultMessage))
                .andExpect(JSON_CONTENT_MATCHER);
    }

    protected void performAndExpectBadRequestError(final String urlTemplate, final Object request, final String expectedError) throws Exception {
        final String requestString = objectMapper.writeValueAsString(request);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(urlTemplate)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value("Invalid request"))
                .andExpect(ERRORS_MATCHER.value(expectedError))
                .andExpect(JSON_CONTENT_MATCHER);
    }

}