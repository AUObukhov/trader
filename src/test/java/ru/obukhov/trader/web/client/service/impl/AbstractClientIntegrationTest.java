package ru.obukhov.trader.web.client.service.impl;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpStatusCode;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import ru.obukhov.trader.TokenValidationStartupListener;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.web.TestWithMockedServer;

import java.io.IOException;
import java.util.Map;

class AbstractClientIntegrationTest extends TestWithMockedServer {

    /**
     * To prevent token validation on startup. Otherwise, validation performed before MockServer initialization
     */
    @MockBean
    private TokenValidationStartupListener tokenValidationStartupListener;

    @Test
    void execute() throws IOException {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", 8081);

        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        final HttpUrl requestUrl = testClient.url.newBuilder()
                .addPathSegment("test")
                .build();
        final Map<String, String> requestBodyMap = Map.of("key1", "value1", "key2", "value2");
        final Request request = testClient.prepareRequest(requestUrl)
                .post(testClient.getRequestBody(requestBodyMap))
                .build();

        final Map<String, String> responseBody = Map.of("key3", "value3", "key4", "value4");
        mockResponse(HttpMethod.POST, "/openapi/test/test", responseBody);

        final Response response = testClient.execute(request);

        Assertions.assertEquals(HttpStatusCode.OK_200.code(), response.code());
        final ResponseBody body = response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(TestUtils.OBJECT_MAPPER.writeValueAsString(responseBody), body.string());
    }

    @Test
    void executeAndGetBody() throws IOException {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final TradingProperties tradingProperties = new TradingProperties(false, "i identify myself as token");
        final ApiProperties apiProperties = new ApiProperties("http://localhost", 8081);

        final TestClient testClient = new TestClient(okHttpClient, tradingProperties, apiProperties);

        final HttpUrl requestUrl = testClient.url.newBuilder()
                .addPathSegment("test")
                .build();
        final Map<String, String> requestBodyMap = Map.of("key1", "value1", "key2", "value2");
        final Request request = testClient.prepareRequest(requestUrl)
                .post(testClient.getRequestBody(requestBodyMap))
                .build();

        final Map<String, String> expectedResponseBody = Map.of("key3", "value3", "key4", "value4");
        mockResponse(HttpMethod.POST, "/openapi/test/test", expectedResponseBody);

        final Map<String, String> actualResponseBody = testClient.executeAndGetBody(request, Map.class);

        Assertions.assertEquals(expectedResponseBody, actualResponseBody);
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