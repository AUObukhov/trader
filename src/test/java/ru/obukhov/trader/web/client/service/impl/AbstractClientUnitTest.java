package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;

class AbstractClientUnitTest {

    // region constructor

    @Test
    void constructor_initializesAuthToken() {
        final String token = "i identify myself as token";

        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, token);
        final ApiProperties apiProperties = new ApiProperties("http://localhost", 8081);

        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        Assertions.assertEquals("Bearer " + token, testClient.authToken);
    }

    @Test
    void constructor_initializesUrl_whenPortIsNull() {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", null);

        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        Assertions.assertEquals("localhost", testClient.url.host());
        Assertions.assertEquals("/openapi/test", testClient.url.encodedPath());
        Assertions.assertEquals(80, testClient.url.port());
    }

    @Test
    void constructor_initializesUrl_whenPortIsNotNull() {
        final int port = 8080;

        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", port);

        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        Assertions.assertEquals("localhost", testClient.url.host());
        Assertions.assertEquals("/openapi/test", testClient.url.encodedPath());
        Assertions.assertEquals(port, testClient.url.port());
    }

    @Test
    void constructor_setsClient() {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", null);

        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        Assertions.assertEquals(okHttpClient, testClient.client);
    }

    @Test
    void constructor_initializesMapper() {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", null);

        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        Assertions.assertNotNull(testClient.mapper);
    }

    // endregion

    @Test
    void prepareRequest_returnsProperRequestBuilder() {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", null);
        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        final HttpUrl requestUrl = testClient.url.newBuilder().build();

        final Request.Builder builder = testClient.prepareRequest(requestUrl);

        final Request request = builder.build();
        Assertions.assertEquals(requestUrl, request.url());
        Assertions.assertEquals(testClient.authToken, request.header("Authorization"));
    }

    @Test
    void buildRequest_returnsProperRequest() {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", null);
        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        final HttpUrl requestUrl = testClient.url.newBuilder().build();

        final Request request = testClient.buildRequest(requestUrl);

        Assertions.assertEquals(requestUrl, request.url());
        Assertions.assertEquals(testClient.authToken, request.header("Authorization"));
    }

    private static class TestClient extends AbstractClient {

        protected TestClient(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
            super(client, tradingProperties, apiProperties);
        }

        @Override
        protected String getPath() {
            return "test";
        }
    }

}