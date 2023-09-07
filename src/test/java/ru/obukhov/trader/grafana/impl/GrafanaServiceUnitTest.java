package ru.obukhov.trader.grafana.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.grafana.GrafanaService;
import ru.obukhov.trader.grafana.model.Column;
import ru.obukhov.trader.grafana.model.ColumnType;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.Metric;
import ru.obukhov.trader.grafana.model.QueryResult;
import ru.obukhov.trader.grafana.model.QueryTableResult;
import ru.obukhov.trader.grafana.model.Target;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class GrafanaServiceUnitTest {

    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private StatisticsService statisticsService;
    @Mock
    @SuppressWarnings("unused")
    private Context context;

    @InjectMocks
    private GrafanaService service;

    // region getData tests

    @Test
    void getData_throwsIllegalArgumentException_whenNoTarget() {
        final GetDataRequest request = new GetDataRequest();

        request.setTargets(List.of());

        final Executable executable = () -> service.getData(request);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Expected single target");
    }

    @Test
    void getData_throwsIllegalArgumentException_whenMultipleTargets() {
        final GetDataRequest request = new GetDataRequest();

        request.setTargets(List.of(new Target(), new Target()));

        final Executable executable = () -> service.getData(request);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Expected single target");
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetData_throwsIllegalArgumentException_whenDataIsNotValid() {
        return Stream.of(
                Arguments.of(
                        Metric.CANDLES,
                        null,
                        "data is mandatory"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        null,
                        "data is mandatory"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name()),
                        "\"figi\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"figi\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("figi", StringUtils.EMPTY, "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name()),
                        "\"figi\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "figi", StringUtils.EMPTY,
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"figi\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("figi", "     ", "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name()),
                        "\"figi\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "figi", "     ",
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"figi\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("figi", TestShare1.FIGI),
                        "\"candleInterval\" is mandatory"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "figi", TestShare1.FIGI,
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"candleInterval\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "figi", TestShare1.FIGI,
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"movingAverageType\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "figi", TestShare1.FIGI,
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window2", 5
                        ),
                        "\"window1\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "figi", TestShare1.FIGI,
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2
                        ),
                        "\"window2\" is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetData_throwsIllegalArgumentException_whenDataIsNotValid")
    void getData_throwsIllegalArgumentException_whenDataIsNotValid(Metric metric, Map<String, Object> data, String expectedMessage) {
        final GetDataRequest request = new GetDataRequest();
        final Target target = new Target().setMetric(metric).setData(data);
        request.setTargets(List.of(target));

        final Executable executable = () -> service.getData(request);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getData_returnsCandles_whenMetricIsCandles_andParamsAreValid() {
        final GetDataRequest request = new GetDataRequest();

        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> data = Map.of("figi", figi, "candleInterval", candleInterval.name());
        final Target target = new Target().setMetric(Metric.CANDLES).setData(data);
        request.setTargets(List.of(target));

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = OffsetDateTime.now();
        final Interval interval = Interval.of(from, to);
        request.setInterval(interval);

        final List<Candle> candles = List.of(
                new CandleBuilder().setOpen(1000).setTime(from.plusHours(1)).build(),
                new CandleBuilder().setOpen(2000).setTime(from.plusHours(2)).build(),
                new CandleBuilder().setOpen(3000).setTime(from.plusHours(3)).build(),
                new CandleBuilder().setOpen(4000).setTime(from.plusHours(4)).build(),
                new CandleBuilder().setOpen(5000).setTime(from.plusHours(5)).build()
        );

        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(currentDateTime)) {
            final List<QueryResult> results = service.getData(request);

            Assertions.assertEquals(1, results.size());
            QueryTableResult result = (QueryTableResult) results.get(0);

            final List<Column> expectedColumns = List.of(
                    new Column("time", ColumnType.TIME),
                    new Column("open price", ColumnType.NUMBER)
            );
            AssertUtils.assertEquals(expectedColumns, result.getColumns());


            final List<List<Object>> expectedRows = new ArrayList<>(5);
            for (Candle candle : candles) {
                expectedRows.add(List.of(candle.getTime(), candle.getOpen()));
            }
            AssertUtils.assertEquals(expectedRows, result.getRows());
        }
    }

    @Test
    void getData_returnsExtendedCandles_whenMetricIsExtendedCandles_andParamsAreValid() {

        // arrange

        final GetDataRequest request = new GetDataRequest();

        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer window1 = 2;
        final Integer window2 = 5;

        final Map<String, Object> data = Map.of(
                "figi", figi,
                "candleInterval", candleInterval.name(),
                "movingAverageType", movingAverageType.getValue(),
                "window1", window1,
                "window2", window2
        );
        final Target target = new Target().setMetric(Metric.EXTENDED_CANDLES).setData(data);
        request.setTargets(List.of(target));

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = OffsetDateTime.now();
        final Interval interval = Interval.of(from, to);
        request.setInterval(interval);

        final List<Candle> candles = List.of(
                new CandleBuilder().setOpen(80).setClose(15).setHighest(20).setLowest(5)
                        .setTime(from)
                        .build(),
                new CandleBuilder().setOpen(1000).setClose(20).setHighest(25).setLowest(10)
                        .setTime(from.plusMinutes(1))
                        .build(),
                new CandleBuilder().setOpen(70).setClose(17).setHighest(24).setLowest(15)
                        .setTime(from.plusMinutes(2))
                        .build(),
                new CandleBuilder().setOpen(40).setClose(18).setHighest(22).setLowest(14)
                        .setTime(from.plusMinutes(3))
                        .build(),
                new CandleBuilder().setOpen(50).setClose(18).setHighest(22).setLowest(14)
                        .setTime(from.plusMinutes(4))
                        .build(),
                new CandleBuilder().setOpen(10).setClose(18).setHighest(22).setLowest(14)
                        .setTime(from.plusMinutes(5))
                        .build(),
                new CandleBuilder().setOpen(90).setClose(18).setHighest(22).setLowest(14)
                        .setTime(from.plusMinutes(6))
                        .build(),
                new CandleBuilder().setOpen(1000).setClose(18).setHighest(22).setLowest(14)
                        .setTime(from.plusMinutes(7))
                        .build(),
                new CandleBuilder().setOpen(60).setClose(18).setHighest(22).setLowest(14)
                        .setTime(from.plusMinutes(8))
                        .build(),
                new CandleBuilder().setOpen(30).setClose(18).setHighest(22).setLowest(14)
                        .setTime(from.plusMinutes(9))
                        .build()
        );
        final List<BigDecimal> averages1 = TestData.newBigDecimalList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> averages2 = TestData.newBigDecimalList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        GetCandlesResponse response = new GetCandlesResponse(candles, averages1, averages2);
        Mockito.when(statisticsService.getExtendedCandles(
                figi, interval, candleInterval, movingAverageType, window1, window2
        )).thenReturn(response);

        // action

        final List<QueryResult> results = service.getData(request);

        // assert

        Assertions.assertEquals(1, results.size());
        QueryTableResult result = (QueryTableResult) results.get(0);

        final List<Column> expectedColumns = List.of(
                new Column("time", ColumnType.TIME),
                new Column("open price", ColumnType.NUMBER),
                new Column("SMA(" + window1 + ")", ColumnType.NUMBER),
                new Column("SMA(" + window2 + ")", ColumnType.NUMBER)
        );
        AssertUtils.assertEquals(expectedColumns, result.getColumns());

        final List<List<Object>> expectedRows = new ArrayList<>(5);
        for (int i = 0; i < candles.size(); i++) {
            final Candle candle = candles.get(i);
            expectedRows.add(List.of(candle.getTime(), candle.getOpen(), averages1.get(i), averages2.get(i)));
        }
        AssertUtils.assertEquals(expectedRows, result.getRows());
    }

    // endregion

}