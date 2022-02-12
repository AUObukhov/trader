package ru.tinkoff.invest.openapi.okhttp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.web.client.exceptions.Error;
import ru.obukhov.trader.web.client.exceptions.ErrorPayload;
import ru.obukhov.trader.web.client.exceptions.OpenApiException;
import ru.obukhov.trader.web.client.exceptions.WrongTokenException;
import ru.obukhov.trader.web.client.model.Empty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public abstract class BaseContextImpl implements Context {

    protected static final TypeReference<Error> errorTypeReference =
            new TypeReference<Error>() {
            };
    protected static final TypeReference<Empty> emptyPayloadTypeReference =
            new TypeReference<Empty>() {
            };

    protected final String authToken;
    protected final HttpUrl finalUrl;
    protected final OkHttpClient client;
    protected final ObjectMapper mapper;
    protected final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public BaseContextImpl(@NotNull final OkHttpClient client,
                           @NotNull final String url,
                           @NotNull final String authToken) {

        this.authToken = authToken;
        this.finalUrl = Objects.requireNonNull(HttpUrl.parse(url))
                .newBuilder()
                .addPathSegment(this.getPath())
                .build();
        this.client = client;
        this.mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    @NotNull
    protected Request.Builder prepareRequest(@NotNull final HttpUrl requestUrl) {
        return new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", this.authToken);
    }

    @NotNull
    protected <D> D handleResponse(@NotNull final Response response,
                                   @NotNull final TypeReference<D> tr) throws IOException, OpenApiException {
        switch (response.code()) {
            case 200:
                final InputStream bodyStream = Objects.requireNonNull(response.body()).byteStream();
                return mapper.readValue(bodyStream, tr);
            case 401:
                throw new WrongTokenException();
            default:
                final InputStream errorStream = Objects.requireNonNull(response.body()).byteStream();
                if (errorStream.available() == 0) {
                    final String message = String.format("Запрос завершился с кодом %d без тела", response.code());
                    logger.error(message);
                    throw new RuntimeException(message);
                } else {
                    final Error answerBody = mapper.readValue(errorStream, errorTypeReference);
                    final ErrorPayload error = answerBody.getPayload();
                    final String message = "Ошибка при исполнении запроса, trackingId = " + answerBody.getTrackingId();
                    logger.error(message);
                    throw new OpenApiException(error.getMessage(), error.getCode());
                }

        }
    }

}
