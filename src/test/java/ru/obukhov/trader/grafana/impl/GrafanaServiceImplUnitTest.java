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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.grafana.model.Column;
import ru.obukhov.trader.grafana.model.ColumnType;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.Metric;
import ru.obukhov.trader.grafana.model.QueryResult;
import ru.obukhov.trader.grafana.model.QueryTableResult;
import ru.obukhov.trader.grafana.model.Target;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class GrafanaServiceImplUnitTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private GrafanaServiceImpl service;

    // region getData tests

    @Test
    void getData_throwsIllegalArgumentException_whenNoTarget() {
        final GetDataRequest request = new GetDataRequest();

        request.setTargets(List.of());

        final Executable executable = () -> service.getData(request);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "Expected single target");
    }

    @Test
    void getData_throwsIllegalArgumentException_whenMultipleTargets() {
        final GetDataRequest request = new GetDataRequest();

        request.setTargets(List.of(new Target(), new Target()));

        final Executable executable = () -> service.getData(request);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "Expected single target");
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
                        Map.of("candleResolution", CandleResolution._1MIN.getValue()),
                        "\"ticker\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "candleResolution", CandleResolution._1MIN.getValue(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"ticker\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("ticker", StringUtils.EMPTY, "candleResolution", CandleResolution._1MIN.getValue()),
                        "\"ticker\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", StringUtils.EMPTY,
                                "candleResolution", CandleResolution._1MIN.getValue(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"ticker\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("ticker", "     ", "candleResolution", CandleResolution._1MIN.getValue()),
                        "\"ticker\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", "     ",
                                "candleResolution", CandleResolution._1MIN.getValue(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"ticker\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("ticker", "ticker"),
                        "\"candleResolution\" is mandatory"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", "ticker",
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"candleResolution\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", "ticker",
                                "candleResolution", CandleResolution._1MIN.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"movingAverageType\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", "ticker",
                                "candleResolution", CandleResolution._1MIN.getValue(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window2", 5
                        ),
                        "\"window1\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", "ticker",
                                "candleResolution", CandleResolution._1MIN.getValue(),
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
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getData_returnsCandles_whenMetricIsCandles_andParamsAreValid() {
        final GetDataRequest request = new GetDataRequest();

        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final Map<String, Object> data = Map.of("ticker", "ticker", "candleResolution", candleResolution.getValue());
        final Target target = new Target().setMetric(Metric.CANDLES).setData(data);
        request.setTargets(List.of(target));

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = OffsetDateTime.now();
        final Interval interval = Interval.of(from, to);
        request.setInterval(interval);

        final List<Candle> candles = List.of(
                TestData.createCandleWithOpenPrice(1000).setTime(from.plusHours(1)),
                TestData.createCandleWithOpenPrice(2000).setTime(from.plusHours(2)),
                TestData.createCandleWithOpenPrice(3000).setTime(from.plusHours(3)),
                TestData.createCandleWithOpenPrice(4000).setTime(from.plusHours(4)),
                TestData.createCandleWithOpenPrice(5000).setTime(from.plusHours(5))
        );
        Mockito.when(statisticsService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        final List<QueryResult> results = service.getData(request);

        Assertions.assertEquals(1, results.size());
        QueryTableResult result = (QueryTableResult) results.get(0);

        final List<Column> expectedColumns = List.of(
                new Column("time", ColumnType.TIME),
                new Column("open price", ColumnType.NUMBER)
        );
        AssertUtils.assertListsAreEqual(expectedColumns, result.getColumns());


        final List<List<Object>> expectedRows = new ArrayList<>(5);
        for (Candle candle : candles) {
            expectedRows.add(List.of(candle.getTime(), candle.getOpenPrice()));
        }
        AssertUtils.assertListsAreEqual(expectedRows, result.getRows());
    }

    @Test
    void getData_returnsExtendedCandles_whenMetricIsExtendedCandles_andParamsAreValid() {

        // arrange

        final GetDataRequest request = new GetDataRequest();

        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer window1 = 2;
        final Integer window2 = 5;

        final Map<String, Object> data = Map.of(
                "ticker", ticker,
                "candleResolution", candleResolution.getValue(),
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
                TestData.createCandle(80, 15, 20, 5, from, candleResolution),
                TestData.createCandle(1000, 20, 25, 10, from.plusMinutes(1), candleResolution),
                TestData.createCandle(70, 17, 24, 15, from.plusMinutes(2), candleResolution),
                TestData.createCandle(40, 18, 22, 14, from.plusMinutes(3), candleResolution),
                TestData.createCandle(50, 18, 22, 14, from.plusMinutes(4), candleResolution),
                TestData.createCandle(10, 18, 22, 14, from.plusMinutes(5), candleResolution),
                TestData.createCandle(90, 18, 22, 14, from.plusMinutes(6), candleResolution),
                TestData.createCandle(1000, 18, 22, 14, from.plusMinutes(7), candleResolution),
                TestData.createCandle(60, 18, 22, 14, from.plusMinutes(8), candleResolution),
                TestData.createCandle(30, 18, 22, 14, from.plusMinutes(9), candleResolution)
        );
        final List<BigDecimal> averages1 = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> averages2 = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        GetCandlesResponse response = new GetCandlesResponse(candles, averages1, averages2);
        Mockito.when(statisticsService.getExtendedCandles(
                ticker, interval, candleResolution, movingAverageType, window1, window2
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
        AssertUtils.assertListsAreEqual(expectedColumns, result.getColumns());

        final List<List<Object>> expectedRows = new ArrayList<>(5);
        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);
            expectedRows.add(List.of(candle.getTime(), candle.getOpenPrice(), averages1.get(i), averages2.get(i)));
        }
        AssertUtils.assertListsAreEqual(expectedRows, result.getRows());
    }

    // endregion

}