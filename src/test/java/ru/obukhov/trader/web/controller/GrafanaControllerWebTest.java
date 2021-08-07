package ru.obukhov.trader.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.grafana.interfaces.GrafanaService;
import ru.obukhov.trader.grafana.model.Column;
import ru.obukhov.trader.grafana.model.ColumnType;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.Metric;
import ru.obukhov.trader.grafana.model.QueryTableResult;
import ru.obukhov.trader.grafana.model.Target;
import ru.obukhov.trader.grafana.model.TargetType;
import ru.obukhov.trader.test.utils.ResourceUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

class GrafanaControllerWebTest extends ControllerWebTest {

    @MockBean
    private GrafanaService grafanaService;

    @Test
    void get() throws Exception {
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

        final String request = MAPPER.writeValueAsString(getDataRequest);

        assertBadRequestError("/trader/grafana/query", request, "interval is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetsIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setTargets(null);

        final String request = MAPPER.writeValueAsString(getDataRequest);

        assertBadRequestError("/trader/grafana/query", request, "targets is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetsIsEmpty() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setTargets(List.of());

        final String request = MAPPER.writeValueAsString(getDataRequest);

        assertBadRequestError("/trader/grafana/query", request, "targets is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetMetricIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.getTargets().get(0).setMetric(null);

        final String request = MAPPER.writeValueAsString(getDataRequest);

        assertBadRequestError("/trader/grafana/query", request, "target.metric is mandatory");
    }

    @Test
    void getData_returnsBadRequest_whenTargetTypeIsNull() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.getTargets().get(0).setType(null);

        final String request = MAPPER.writeValueAsString(getDataRequest);

        assertBadRequestError("/trader/grafana/query", request, "target.type is mandatory");
    }

    @Test
    void getData_returnsQueryResult_whenRequestIsValid() throws Exception {
        final GetDataRequest getDataRequest = createGetDataRequest();

        final String request = MAPPER.writeValueAsString(getDataRequest);

        final QueryTableResult queryResult = new QueryTableResult();
        queryResult.setColumns(List.of(
                new Column("time", ColumnType.TIME),
                new Column("open price", ColumnType.NUMBER)
        ));
        queryResult.setRows(List.of(
                List.of(DateUtils.getDateTime(2021, 2, 1, 10, 0, 0), 100),
                List.of(DateUtils.getDateTime(2021, 2, 1, 10, 1, 0), 101),
                List.of(DateUtils.getDateTime(2021, 2, 1, 10, 2, 0), 102)
        ));

        Mockito.when(grafanaService.getData(Mockito.any(GetDataRequest.class))).thenReturn(List.of(queryResult));

        final String expectedResponse = ResourceUtils.getTestDataAsString("QueryResults.json");
        mockMvc.perform(MockMvcRequestBuilders.post("/trader/grafana/query")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    void getData_updatesRangeOffset_whenOffsetIsNotDefault() throws Exception {
        final OffsetDateTime from = OffsetDateTime.of(2021, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime to = OffsetDateTime.of(2021, 1, 1, 15, 0, 0, 0, ZoneOffset.UTC);
        final GetDataRequest getDataRequest = createGetDataRequest();
        getDataRequest.setInterval(Interval.of(from, to));

        final String request = MAPPER.writeValueAsString(getDataRequest);

        Mockito.when(grafanaService.getData(Mockito.any(GetDataRequest.class))).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/grafana/query")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        final ArgumentCaptor<GetDataRequest> argumentCaptor = ArgumentCaptor.forClass(GetDataRequest.class);
        Mockito.verify(grafanaService).getData(argumentCaptor.capture());
        final GetDataRequest capturedGetDataRequest = argumentCaptor.getValue();
        final Interval interval = capturedGetDataRequest.getInterval();

        final OffsetDateTime expectedFrom = DateUtils.getDateTime(2021, 1, 1, 13, 0, 0);
        final OffsetDateTime expectedTo = DateUtils.getDateTime(2021, 1, 1, 18, 0, 0);

        Assertions.assertEquals(expectedFrom, interval.getFrom());
        Assertions.assertEquals(expectedTo, interval.getTo());
    }

    @NotNull
    private GetDataRequest createGetDataRequest() {
        final GetDataRequest getDataRequest = new GetDataRequest();
        getDataRequest.setInterval(Interval.ofDay(OffsetDateTime.now()));

        final Target target = new Target();
        target.setMetric(Metric.CANDLES);
        target.setType(TargetType.TABLE);
        target.setData(Map.of());
        getDataRequest.setTargets(List.of(target));
        return getDataRequest;
    }

    // endregion

}