package ru.obukhov.trader.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
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
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

class GrafanaControllerIntegrationTest extends ControllerIntegrationTest {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    @Test
    void get_returnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/trader/grafana/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));
    }

    // region getData tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getData_returnsBadRequest_whenRangeIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setInterval(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "interval is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getData_returnsBadRequest_whenTargetsIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setTargets(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "targets is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getData_returnsBadRequest_whenTargetsIsEmpty() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setTargets(List.of());

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "targets is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getData_returnsBadRequest_whenTargetMetricIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.getTargets().get(0).setMetric(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "target.metric is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getData_returnsBadRequest_whenTargetTypeIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.getTargets().get(0).setType(null);

        performAndExpectBadRequestError("/trader/grafana/query", getDataRequest, "target.type is mandatory");
    }

    @Test
    void getData_returnsCandles_whenMetricIsCandles() throws Exception {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 2, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1, 19);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final List<HistoricCandle> historicCandles = List.of(
                new HistoricCandleBuilder().setOpenPrice(100).setTime(from).setIsComplete(true).build(),
                new HistoricCandleBuilder().setOpenPrice(101).setTime(from.plusMinutes(1)).setIsComplete(true).build(),
                new HistoricCandleBuilder().setOpenPrice(102).setTime(from.plusMinutes(2)).setIsComplete(true).build()
        );
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

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
        queryResult.setRows(historicCandles.stream().map(this::mapCandleToGrafanaList).toList());
        final String expectedResponse = TestUtils.OBJECT_MAPPER.writeValueAsString(List.of(queryResult));

        final String request = TestUtils.OBJECT_MAPPER.writeValueAsString(getDataRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/grafana/query")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    void getData_returnsExtendedCandles_whenMetricIsExtendedCandles() throws Exception {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 2, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1, 19);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final List<HistoricCandle> historicCandles = List.of(
                new HistoricCandleBuilder().setOpenPrice(100).setTime(from).setIsComplete(true).build(),
                new HistoricCandleBuilder().setOpenPrice(101).setTime(from.plusMinutes(1)).setIsComplete(true).build(),
                new HistoricCandleBuilder().setOpenPrice(102).setTime(from.plusMinutes(2)).setIsComplete(true).build()
        );
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

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
                        getTimeString(historicCandles.get(0)),
                        getOpenPrice(historicCandles.get(0)),
                        getOpenPrice(historicCandles.get(0)),
                        getOpenPrice(historicCandles.get(0))
                ),
                List.of(
                        getTimeString(historicCandles.get(1)),
                        getOpenPrice(historicCandles.get(1)),
                        getOpenPrice(historicCandles.get(1)),
                        BigDecimal.valueOf(100.5)
                ),
                List.of(
                        getTimeString(historicCandles.get(2)),
                        getOpenPrice(historicCandles.get(2)),
                        getOpenPrice(historicCandles.get(2)),
                        BigDecimal.valueOf(101.5)
                )
        );
        queryResult.setRows(rows);
        final String expectedResponse = TestUtils.OBJECT_MAPPER.writeValueAsString(List.of(queryResult));

        final String request = TestUtils.OBJECT_MAPPER.writeValueAsString(getDataRequest);

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

    private List<Object> mapCandleToGrafanaList(final HistoricCandle candle) {
        return List.of(getTimeString(candle), getOpenPrice(candle));
    }

    private String getTimeString(final HistoricCandle candle) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(DATE_TIME_MAPPER.timestampToOffsetDateTime(candle.getTime()));
    }

    private BigDecimal getOpenPrice(final HistoricCandle candle) {
        return QUOTATION_MAPPER.toBigDecimal(candle.getOpen());
    }

}