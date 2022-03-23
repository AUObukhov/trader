package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.grafana.model.Column;
import ru.obukhov.trader.grafana.model.ColumnType;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.Metric;
import ru.obukhov.trader.grafana.model.QueryTableResult;
import ru.obukhov.trader.grafana.model.Target;
import ru.obukhov.trader.grafana.model.TargetType;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.client.exchange.CandlesResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

class GrafanaControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    void get_returnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/trader/grafana/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));
    }

    // region getData tests

    @Test
    void getData_returnsBadRequest_whenRangeIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setInterval(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "interval is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetsIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setTargets(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "targets is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetsIsEmpty() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setTargets(List.of());

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "targets is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetMetricIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.getTargets().get(0).setMetric(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "target.metric is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetTypeIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.getTargets().get(0).setType(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "target.type is mandatory");
    }

    @Test
    void getData_returnsCandles_whenMetricIsCandles() throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";

        mockFigiByTicker(ticker, figi);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 2, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1, 19);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval._1MIN;

        final List<Candle> candles = List.of(
                TestData.createCandleWithOpenPriceAndTime(100, from),
                TestData.createCandleWithOpenPriceAndTime(101, from.plusMinutes(1)),
                TestData.createCandleWithOpenPriceAndTime(102, from.plusMinutes(2))
        );
        mockCandles(figi, interval.extendToDay(), candleInterval, candles);

        final GetDataRequest getDataRequest = new GetDataRequest();
        getDataRequest.setInterval(interval);

        final Target target = new Target();
        target.setMetric(Metric.CANDLES);
        target.setType(TargetType.TABLE);
        target.setData(Map.of(
                "ticker", ticker,
                "candleInterval", candleInterval.toString()
        ));
        getDataRequest.setTargets(List.of(target));

        final QueryTableResult queryResult = new QueryTableResult();
        queryResult.setColumns(List.of(
                new Column("time", ColumnType.TIME),
                new Column("open price", ColumnType.NUMBER)
        ));
        queryResult.setRows(candles.stream().map(this::mapCandleToGrafanaList).toList());
        final String expectedResponse = objectMapper.writeValueAsString(List.of(queryResult));

        final String request = objectMapper.writeValueAsString(getDataRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/grafana/query")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    void getData_returnsExtendedCandles_whenMetricIsExtendedCandles() throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";

        mockFigiByTicker(ticker, figi);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 2, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1, 19);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval._1MIN;

        final List<Candle> candles = List.of(
                TestData.createCandleWithOpenPriceAndTime(100, from),
                TestData.createCandleWithOpenPriceAndTime(101, from.plusMinutes(1)),
                TestData.createCandleWithOpenPriceAndTime(102, from.plusMinutes(2))
        );
        mockCandles(figi, interval.extendToDay(), candleInterval, candles);

        final GetDataRequest getDataRequest = new GetDataRequest();
        getDataRequest.setInterval(interval);

        final Target target = new Target();
        target.setMetric(Metric.EXTENDED_CANDLES);
        target.setType(TargetType.TABLE);
        target.setData(Map.of(
                "ticker", ticker,
                "candleInterval", candleInterval.toString(),
                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                "window1", 1,
                "window2", 2
        ));
        getDataRequest.setTargets(List.of(target));

        final QueryTableResult queryResult = new QueryTableResult();
        queryResult.setColumns(List.of(
                new Column("time", ColumnType.TIME),
                new Column("open price", ColumnType.NUMBER),
                new Column("SMA(1)", ColumnType.NUMBER),
                new Column("SMA(2)", ColumnType.NUMBER)
        ));
        final List<List<Object>> rows = List.of(
                List.of(
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(candles.get(0).getTime()),
                        candles.get(0).getOpenPrice(),
                        candles.get(0).getOpenPrice(),
                        candles.get(0).getOpenPrice()
                ),
                List.of(
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(candles.get(1).getTime()),
                        candles.get(1).getOpenPrice(),
                        candles.get(1).getOpenPrice(),
                        BigDecimal.valueOf(100.5)
                ),
                List.of(
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(candles.get(2).getTime()),
                        candles.get(2).getOpenPrice(),
                        candles.get(2).getOpenPrice(),
                        BigDecimal.valueOf(101.5)
                )
        );
        queryResult.setRows(rows);
        final String expectedResponse = objectMapper.writeValueAsString(List.of(queryResult));

        final String request = objectMapper.writeValueAsString(getDataRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/grafana/query")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    private GetDataRequest createGetDataRequest() {
        final GetDataRequest getDataRequest = new GetDataRequest();
        getDataRequest.setInterval(DateTimeTestData.createIntervalOfDay(OffsetDateTime.now()));

        final Target target = new Target();
        target.setMetric(Metric.CANDLES);
        target.setType(TargetType.TABLE);
        target.setData(Map.of());
        getDataRequest.setTargets(List.of(target));
        return getDataRequest;
    }

    // endregion

    private void mockCandles(
            final String figi,
            final Interval interval,
            final CandleInterval candleInterval,
            final List<Candle> candles
    ) throws JsonProcessingException {
        mockCandles(figi, interval.getFrom(), interval.getTo(), candleInterval, candles);
    }

    private void mockCandles(
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
        final Candles payload = new Candles();
        payload.setCandles(candles);
        candlesResponse.setPayload(payload);
        mockResponse(apiRequest, candlesResponse);
    }

    private List<Object> mapCandleToGrafanaList(final Candle candle) {
        final String time = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(candle.getTime());
        return List.of(time, candle.getOpenPrice());
    }

}