package ru.obukhov.trader.test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.test.utils.model.transform.CronExpressionSerializer;
import ru.obukhov.trader.web.client.exchange.CandlesResponse;

import java.time.OffsetDateTime;
import java.util.List;

public class CandlesExpectationResponseCallback implements ExpectationResponseCallback {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new SimpleModule().addSerializer(new CronExpressionSerializer()));

    private static List<Candle> serverCandles = List.of();

    public static void setCandles(final List<Candle> candles) {
        serverCandles = List.copyOf(candles);
    }

    @Override
    public HttpResponse handle(final HttpRequest httpRequest) throws Exception {
        final OffsetDateTime from = OffsetDateTime.parse(httpRequest.getFirstQueryStringParameter("from"));
        final OffsetDateTime to = OffsetDateTime.parse(httpRequest.getFirstQueryStringParameter("to"));
        final List<Candle> candles = serverCandles.stream()
                .filter(candle -> !candle.getTime().isBefore(from) && candle.getTime().isBefore(to))
                .toList();

        final Candles payload = new Candles(null, null, candles);
        final CandlesResponse candlesResponse = new CandlesResponse();
        candlesResponse.setPayload(payload);

        return HttpResponse.response()
                .withBody(OBJECT_MAPPER.writeValueAsString(candlesResponse));
    }
}