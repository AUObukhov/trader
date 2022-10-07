package ru.obukhov.trader.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.grafana.GrafanaService;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.Metric;
import ru.obukhov.trader.grafana.model.Target;
import ru.obukhov.trader.grafana.model.TargetType;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.web.controller.ControllerIntegrationTest;
import ru.obukhov.trader.web.model.exchange.GetCandlesRequest;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

class DefaultTimeZoneAspectIntegrationTest extends ControllerIntegrationTest {

    // inner beans need to be mocked because it is the only way to verify DefaultTimeZoneAspect applied
    @MockBean
    private GrafanaService grafanaService;
    @MockBean
    private StatisticsService statisticsService;
    @MockBean
    private ExtInstrumentsService extInstrumentsService;

    @SuppressWarnings("unused")
    static Stream<Arguments> getData() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        null,
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        null
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        DateTimeTestData.createDateTime(2020, 10, 5, 10, ZoneOffset.UTC),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                ),
                Arguments.of(
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13),
                        DateTimeTestData.createDateTime(2020, 10, 5, 13)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData")
    void beforeGrafanaControllerGetData(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) throws Exception {
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

        final Interval expectedInterval = Interval.of(expectedFrom, expectedTo);

        final ArgumentCaptor<GetDataRequest> argumentCaptor = ArgumentCaptor.forClass(GetDataRequest.class);
        Mockito.verify(grafanaService, Mockito.times(1)).getData(argumentCaptor.capture());

        final Interval actualInterval = argumentCaptor.getValue().getInterval();
        Assertions.assertEquals(expectedInterval, actualInterval);
    }

    @ParameterizedTest
    @MethodSource("getData")
    void beforeStatisticsControllerGetCandles(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) throws Exception {
        final Interval interval = Interval.of(from, to);

        final GetCandlesRequest getCandlesRequest = new GetCandlesRequest();
        getCandlesRequest.setTicker(TestShare1.TICKER);
        getCandlesRequest.setInterval(interval);
        getCandlesRequest.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        getCandlesRequest.setMovingAverageType(MovingAverageType.SIMPLE);
        getCandlesRequest.setSmallWindow(50);
        getCandlesRequest.setBigWindow(200);
        getCandlesRequest.setSaveToFile(false);

        final String request = TestUtils.OBJECT_MAPPER.writeValueAsString(getCandlesRequest);

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/candles")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON));

        final ArgumentCaptor<Interval> argumentCaptor = ArgumentCaptor.forClass(Interval.class);
        Mockito.verify(statisticsService, Mockito.times(1)).getExtendedCandles(
                Mockito.anyString(),
                argumentCaptor.capture(),
                Mockito.any(CandleInterval.class),
                Mockito.any(MovingAverageType.class),
                Mockito.anyInt(),
                Mockito.anyInt()
        );

        final Interval expectedInterval = Interval.of(expectedFrom, expectedTo);

        final Interval actualInterval = argumentCaptor.getValue();
        Assertions.assertEquals(expectedInterval, actualInterval);
    }

    @ParameterizedTest
    @MethodSource("getData")
    void aroundInstrumentsControllerGetTradingSchedule(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) throws Exception {
        final Exchange exchange = Exchange.MOEX;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange.getValue())
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder);

        final ArgumentCaptor<Interval> argumentCaptor = ArgumentCaptor.forClass(Interval.class);
        Mockito.verify(extInstrumentsService, Mockito.times(1))
                .getTradingSchedule(Mockito.any(Exchange.class), argumentCaptor.capture());

        final Interval expectedInterval = Interval.of(expectedFrom, expectedTo);

        final Interval actualInterval = argumentCaptor.getValue();
        Assertions.assertEquals(expectedInterval, actualInterval);
    }

    @ParameterizedTest
    @MethodSource("getData")
    void aroundInstrumentsControllerGetTradingSchedules(
            final OffsetDateTime from,
            final OffsetDateTime to,
            final OffsetDateTime expectedFrom,
            final OffsetDateTime expectedTo
    ) throws Exception {
        final Exchange exchange = Exchange.MOEX;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedules")
                .param("exchange", exchange.getValue())
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder);

        final ArgumentCaptor<Interval> argumentCaptor = ArgumentCaptor.forClass(Interval.class);
        Mockito.verify(extInstrumentsService, Mockito.times(1))
                .getTradingSchedules(argumentCaptor.capture());

        final Interval expectedInterval = Interval.of(expectedFrom, expectedTo);

        final Interval actualInterval = argumentCaptor.getValue();
        Assertions.assertEquals(expectedInterval, actualInterval);
    }

}