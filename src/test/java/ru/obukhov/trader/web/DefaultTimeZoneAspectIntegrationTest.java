package ru.obukhov.trader.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.grafana.GrafanaService;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.Metric;
import ru.obukhov.trader.grafana.model.Target;
import ru.obukhov.trader.grafana.model.TargetType;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.web.controller.ControllerIntegrationTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

class DefaultTimeZoneAspectIntegrationTest extends ControllerIntegrationTest {
    @MockBean
    private GrafanaService grafanaService; // need to be mocked because it is the only way to verify DefaultTimeZoneAspect applied

    @Test
    void test_throughGrafanaController() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 2, 1, 7, ZoneOffset.UTC);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1, 16, ZoneOffset.UTC);
        final Interval interval = Interval.of(from, to);

        final Target target = new Target();
        target.setMetric(Metric.CANDLES);
        target.setType(TargetType.TABLE);

        final GetDataRequest getDataRequest = new GetDataRequest();
        getDataRequest.setInterval(interval);
        getDataRequest.setTargets(List.of(target));

        final String request = TestUtils.OBJECT_MAPPER.writeValueAsString(getDataRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/grafana/query")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON));

        final OffsetDateTime expectedFrom = DateTimeTestData.createDateTime(2021, 2, 1, 10);
        final OffsetDateTime expectedTo = DateTimeTestData.createDateTime(2021, 2, 1, 19);
        final Interval expectedInterval = Interval.of(expectedFrom, expectedTo);

        final ArgumentCaptor<GetDataRequest> argumentCaptor = ArgumentCaptor.forClass(GetDataRequest.class);
        Mockito.verify(grafanaService, Mockito.times(1)).getData(argumentCaptor.capture());

        final Interval actualInterval = argumentCaptor.getValue().getInterval();
        Assertions.assertEquals(expectedInterval, actualInterval);
    }

}