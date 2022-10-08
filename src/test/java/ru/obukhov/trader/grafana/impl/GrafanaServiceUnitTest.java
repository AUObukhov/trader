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
                        "\"ticker\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"ticker\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("ticker", StringUtils.EMPTY, "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name()),
                        "\"ticker\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", StringUtils.EMPTY,
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"ticker\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("ticker", "     ", "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name()),
                        "\"ticker\" must be not blank"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", "     ",
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"ticker\" must be not blank"
                ),

                Arguments.of(
                        Metric.CANDLES,
                        Map.of("ticker", TestShare1.TICKER),
                        "\"candleInterval\" is mandatory"
                ),
                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", TestShare1.TICKER,
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"candleInterval\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", TestShare1.TICKER,
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "window1", 2,
                                "window2", 5
                        ),
                        "\"movingAverageType\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", TestShare1.TICKER,
                                "candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name(),
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "window2", 5
                        ),
                        "\"window1\" is mandatory"
                ),

                Arguments.of(
                        Metric.EXTENDED_CANDLES,
                        Map.of(
                                "ticker", TestShare1.TICKER,
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

        final String ticker = TestShare1.TICKER;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final Map<String, Object> data = Map.of("ticker", ticker, "candleInterval", candleInterval.name());
        final Target target = new Target().setMetric(Metric.CANDLES).setData(data);
        request.setTargets(List.of(target));

        final OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        final OffsetDateTime to = OffsetDateTime.now();
        final Interval interval = Interval.of(from, to);
        request.setInterval(interval);

        final List<Candle> candles = List.of(
                new CandleBuilder().setOpenPrice(1000).setTime(from.plusHours(1)).build(),
                new CandleBuilder().setOpenPrice(2000).setTime(from.plusHours(2)).build(),
                new CandleBuilder().setOpenPrice(3000).setTime(from.plusHours(3)).build(),
                new CandleBuilder().setOpenPrice(4000).setTime(from.plusHours(4)).build(),
                new CandleBuilder().setOpenPrice(5000).setTime(from.plusHours(5)).build()
        );

        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mockito.when(extMarketDataService.getCandles(ticker, interval, candleInterval)).thenReturn(candles);

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
                expectedRows.add(List.of(candle.getTime(), candle.getOpenPrice()));
            }
            AssertUtils.assertEquals(expectedRows, result.getRows());
        }
    }

    @Test
    void getData_returnsExtendedCandles_whenMetricIsExtendedCandles_andParamsAreValid() {

        // arrange

        final GetDataRequest request = new GetDataRequest();

        final String ticker = TestShare1.TICKER;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer window1 = 2;
        final Integer window2 = 5;

        final Map<String, Object> data = Map.of(
                "ticker", ticker,
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
                new CandleBuilder().setOpenPrice(80).setClosePrice(15).setHighestPrice(20).setLowestPrice(5).setTime(from).build(),
                new CandleBuilder().setOpenPrice(1000).setClosePrice(20).setHighestPrice(25).setLowestPrice(10).setTime(from.plusMinutes(1)).build(),
                new CandleBuilder().setOpenPrice(70).setClosePrice(17).setHighestPrice(24).setLowestPrice(15).setTime(from.plusMinutes(2)).build(),
                new CandleBuilder().setOpenPrice(40).setClosePrice(18).setHighestPrice(22).setLowestPrice(14).setTime(from.plusMinutes(3)).build(),
                new CandleBuilder().setOpenPrice(50).setClosePrice(18).setHighestPrice(22).setLowestPrice(14).setTime(from.plusMinutes(4)).build(),
                new CandleBuilder().setOpenPrice(10).setClosePrice(18).setHighestPrice(22).setLowestPrice(14).setTime(from.plusMinutes(5)).build(),
                new CandleBuilder().setOpenPrice(90).setClosePrice(18).setHighestPrice(22).setLowestPrice(14).setTime(from.plusMinutes(6)).build(),
                new CandleBuilder().setOpenPrice(1000).setClosePrice(18).setHighestPrice(22).setLowestPrice(14).setTime(from.plusMinutes(7)).build(),
                new CandleBuilder().setOpenPrice(60).setClosePrice(18).setHighestPrice(22).setLowestPrice(14).setTime(from.plusMinutes(8)).build(),
                new CandleBuilder().setOpenPrice(30).setClosePrice(18).setHighestPrice(22).setLowestPrice(14).setTime(from.plusMinutes(9)).build()
        );
        final List<BigDecimal> averages1 = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> averages2 = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        GetCandlesResponse response = new GetCandlesResponse(candles, averages1, averages2);
        Mockito.when(statisticsService.getExtendedCandles(
                ticker, interval, candleInterval, movingAverageType, window1, window2
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
            Candle candle = candles.get(i);
            expectedRows.add(List.of(candle.getTime(), candle.getOpenPrice(), averages1.get(i), averages2.get(i)));
        }
        AssertUtils.assertEquals(expectedRows, result.getRows());
    }

    // endregion

}