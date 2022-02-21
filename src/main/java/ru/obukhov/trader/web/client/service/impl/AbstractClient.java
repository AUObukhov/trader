package ru.obukhov.trader.web.client.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;

import java.io.IOException;
import java.util.Objects;

public abstract class AbstractClient {

    protected static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json");

    protected final String authToken;
    protected final HttpUrl url;
    protected final OkHttpClient client;
    protected final ObjectMapper mapper;

    protected abstract String getPath();

    protected AbstractClient(final OkHttpClient client, final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        this.authToken = "Bearer " + tradingProperties.getToken();
        this.url = buildUrl(tradingProperties, apiProperties);
        this.client = client;
        this.mapper = createMapper();
    }

    private HttpUrl buildUrl(final TradingProperties tradingProperties, final ApiProperties apiProperties) {
        final HttpUrl httpUrl = HttpUrl.parse(apiProperties.host());
        final HttpUrl.Builder builder = Objects.requireNonNull(httpUrl).newBuilder();
        builder.addPathSegment("openapi");
        if (tradingProperties.isSandbox()) {
            builder.addPathSegment("sandbox");
        }
        builder.addPathSegment(this.getPath());
        if (apiProperties.port() != null) {
            builder.port(apiProperties.port());
        }
        return builder.build();
    }

    private ObjectMapper createMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        return objectMapper;
    }

    protected Request.Builder prepareRequest(@NotNull final HttpUrl requestUrl) {
        return new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken);
    }

    protected Request buildRequest(@NotNull final HttpUrl requestUrl) {
        return prepareRequest(requestUrl).build();
    }

    protected Response execute(final Request request) throws IOException {
        final Response response = client.newCall(request).execute();
        if (HttpStatus.valueOf(response.code()).isError()) {
            throw new IllegalStateException(String.format("Request %s failed with response: %s", request, response));
        }

        return response;
    }

    protected <T> T executeAndGetBody(final Request request, final Class<T> valueType) throws IOException {
        final ResponseBody body = execute(request).body();
        if (body == null) {
            return null;
        }

        return mapper.readValue(body.string(), valueType);
    }

    protected RequestBody getRequestBody(final Object request) throws JsonProcessingException {
        final String renderedBody = mapper.writeValueAsString(request);
        return RequestBody.create(renderedBody, JSON_MEDIA_TYPE);
    }

}