package ru.obukhov.trader.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpClassCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.UserAccounts;
import ru.obukhov.trader.test.utils.CandlesExpectationResponseCallback;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.web.client.exchange.CandlesResponse;
import ru.obukhov.trader.web.client.exchange.MarketInstrumentListResponse;
import ru.obukhov.trader.web.client.exchange.OrdersResponse;
import ru.obukhov.trader.web.client.exchange.UserAccountsResponse;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@MockServerTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public abstract class TestWithMockedServer {

    private ClientAndServer mockServer;

    protected MockServerClient mockServerClient;

    @Autowired
    protected TradingProperties tradingProperties;
    @Autowired
    protected ApiProperties apiProperties;

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

    protected void mockUserAccounts(final List<UserAccount> userAccounts) throws JsonProcessingException {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/user/accounts");

        final UserAccounts payload = new UserAccounts(userAccounts);
        final UserAccountsResponse userAccountsResponse = new UserAccountsResponse();
        userAccountsResponse.setPayload(payload);

        mockResponse(apiRequest, userAccountsResponse);
    }

    protected void mockOrders(final List<Order> orders) throws JsonProcessingException {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/orders");

        final OrdersResponse ordersResponse = new OrdersResponse();
        ordersResponse.setPayload(orders);
        mockResponse(apiRequest, ordersResponse);
    }

    protected void mockFigiByTicker(final String ticker, final String figi) throws JsonProcessingException {
        mockInstrumentByTicker(ticker, TestData.createMarketInstrument(ticker, figi));
    }

    protected void mockInstrumentByTicker(final String ticker, final MarketInstrument instrument) throws JsonProcessingException {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/market/search/by-ticker")
                .withQueryStringParameter("ticker", ticker);

        final MarketInstrumentListResponse marketInstrumentListResponse = TestData.createMarketInstrumentListResponse(List.of(instrument));
        mockResponse(apiRequest, marketInstrumentListResponse);
    }

    protected void mockCandles(
            final String figi,
            final Interval interval,
            final CandleInterval candleInterval,
            final List<Candle> candles
    ) throws JsonProcessingException {
        mockCandles(figi, interval.getFrom(), interval.getTo(), candleInterval, candles);
    }

    protected void mockCandles(
            final String figi,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final CandleInterval candleInterval,
            final List<Candle> candles
    ) throws JsonProcessingException {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/market/candles")
                .withQueryStringParameter("figi", figi)
                .withQueryStringParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .withQueryStringParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .withQueryStringParameter("interval", candleInterval.toString());

        final CandlesResponse candlesResponse = new CandlesResponse();
        final Candles payload = new Candles(null, null, candles);
        candlesResponse.setPayload(payload);
        mockResponse(apiRequest, candlesResponse);
    }

    protected void mockAllCandles(final String figi, final List<Candle> candles) {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/market/candles")
                .withQueryStringParameter("figi", figi);

        CandlesExpectationResponseCallback.setCandles(candles);
        mockServer.when(apiRequest).respond(HttpClassCallback.callback().withCallbackClass(CandlesExpectationResponseCallback.class));
    }

    protected String mockResponse(final HttpMethod httpMethod, final String brokerAccountId, final String path) {
        HttpRequest apiRequest = createAuthorizedHttpRequest(httpMethod, brokerAccountId)
                .withPath(path);

        return mockResponse(apiRequest);
    }

    protected void mockResponse(final HttpMethod httpMethod, final String path, final Object response) throws JsonProcessingException {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(httpMethod)
                .withPath(path);

        mockResponse(apiRequest, response);
    }

    private void mockResponse(final HttpRequest httpRequest, final Object response) throws JsonProcessingException {
        final HttpResponse apiResponse = HttpResponse.response()
                .withBody(TestUtils.OBJECT_MAPPER.writeValueAsString(response));

        mockServerClient.when(httpRequest, Times.once())
                .respond(apiResponse);
    }

    private String mockResponse(final HttpRequest httpRequest) {
        HttpResponse apiResponse = HttpResponse.response();

        final Expectation[] expectations = mockServerClient.when(httpRequest, Times.once())
                .respond(apiResponse);
        return expectations[0].getId();
    }

    private HttpRequest createAuthorizedHttpRequest(final HttpMethod httpMethod, final String brokerAccountId) {
        HttpRequest apiRequest = createAuthorizedHttpRequest(httpMethod);
        if (brokerAccountId != null) {
            apiRequest = apiRequest.withQueryStringParameter("brokerAccountId", brokerAccountId);
        }
        return apiRequest;
    }

    private HttpRequest createAuthorizedHttpRequest(final HttpMethod httpMethod) {
        return HttpRequest.request()
                .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tradingProperties.getToken())
                .withMethod(httpMethod.name());
    }

}