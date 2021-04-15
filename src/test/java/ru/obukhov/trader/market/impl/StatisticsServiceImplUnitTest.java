package ru.obukhov.trader.market.impl;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class StatisticsServiceImplUnitTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    @Mock
    private MarketService marketService;

    private StatisticsServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new StatisticsServiceImpl(marketService);
    }

    @Test
    void getCandles_returnsCandlesFromMarketService() {
        final String ticker = TICKER;

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleInterval = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();

        Mockito.when(marketService.getCandles(ticker, interval, candleInterval)).thenReturn(candles);

        final List<Candle> candlesResponse = service.getCandles(ticker, interval, candleInterval);

        Assertions.assertSame(candles, candlesResponse);
    }

    @Test
    void getExtendedCandles_extendsCandles_withoutExtremes() {
        final String ticker = TICKER;

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleInterval = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();
        OffsetDateTime time = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);

        candles.add(TestDataHelper.createCandle(10, 15, 20, 5, time, candleInterval));
        candles.add(TestDataHelper.createCandle(15, 20, 25, 10, time.plusMinutes(1), candleInterval));
        candles.add(TestDataHelper.createCandle(20, 17, 24, 15, time.plusMinutes(2), candleInterval));

        Mockito.when(marketService.getCandles(ticker, interval, candleInterval)).thenReturn(candles);

        final GetCandlesResponse response = service.getExtendedCandles(ticker, interval, candleInterval);

        AssertUtils.assertListsAreEqual(candles, response.getCandles());

        // expected average prices are calculated for MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder
        // with weightDecrease = 0.3 and order = 3
        List<BigDecimal> expectedAverages = TestDataHelper.createBigDecimalsList(14.72325, 15.00000, 15.27675);
        AssertUtils.assertListsAreEqual(expectedAverages, response.getAverages());

        List<Point> expectedMinimums = ImmutableList.of(
                Point.of(candles.get(0).getTime(), 14.72325)
        );
        AssertUtils.assertListsAreEqual(expectedMinimums, response.getLocalMinimums());

        List<Point> expectedMaximums = ImmutableList.of(
                Point.of(candles.get(2).getTime(), 15.27675)
        );
        AssertUtils.assertListsAreEqual(expectedMaximums, response.getLocalMaximums());

        Assertions.assertTrue(response.getSupportLines().isEmpty());
        Assertions.assertTrue(response.getResistanceLines().isEmpty());
    }

    @Test
    void getExtendedCandles_extendsCandles_withExtremes() {
        final String ticker = TICKER;

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleInterval = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();
        OffsetDateTime time = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);

        candles.add(TestDataHelper.createCandle(80, 15, 20, 5, time, candleInterval));
        candles.add(TestDataHelper.createCandle(10, 20, 25, 10, time.plusMinutes(1), candleInterval));
        candles.add(TestDataHelper.createCandle(70, 17, 24, 15, time.plusMinutes(2), candleInterval));
        candles.add(TestDataHelper.createCandle(40, 18, 22, 14, time.plusMinutes(3), candleInterval));
        candles.add(TestDataHelper.createCandle(50, 18, 22, 14, time.plusMinutes(4), candleInterval));
        candles.add(TestDataHelper.createCandle(10, 18, 22, 14, time.plusMinutes(5), candleInterval));
        candles.add(TestDataHelper.createCandle(90, 18, 22, 14, time.plusMinutes(6), candleInterval));
        candles.add(TestDataHelper.createCandle(1000, 18, 22, 14, time.plusMinutes(7), candleInterval));
        candles.add(TestDataHelper.createCandle(60, 18, 22, 14, time.plusMinutes(8), candleInterval));
        candles.add(TestDataHelper.createCandle(30, 18, 22, 14, time.plusMinutes(9), candleInterval));

        Mockito.when(marketService.getCandles(ticker, interval, candleInterval)).thenReturn(candles);

        final GetCandlesResponse response = service.getExtendedCandles(ticker, interval, candleInterval);

        AssertUtils.assertListsAreEqual(candles, response.getCandles());

        // expectedAverages are calculated for MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder
        // with weightDecrease = 0.3 and order = 3
        List<BigDecimal> expectedAverages = TestDataHelper.createBigDecimalsList(
                99.72048, 102.46637, 104.04146, 102.67578, 98.28161,
                89.06988, 75.78625, 71.94727, 71.76162, 81.27242
        );
        AssertUtils.assertListsAreEqual(expectedAverages, response.getAverages());

        List<Point> expectedMinimums = ImmutableList.of(
                Point.of(candles.get(0).getTime(), 99.72048),
                Point.of(candles.get(8).getTime(), 71.76162)
        );
        AssertUtils.assertListsAreEqual(expectedMinimums, response.getLocalMinimums());

        List<Point> expectedMaximums = ImmutableList.of(
                Point.of(candles.get(2).getTime(), 104.04146),
                Point.of(candles.get(9).getTime(), 81.27242)
        );
        AssertUtils.assertListsAreEqual(expectedMaximums, response.getLocalMaximums());

        List<List<Point>> supportLines = response.getSupportLines();
        Assertions.assertEquals(1, supportLines.size());
        List<Point> expectedSupportLine = ImmutableList.of(
                Point.of(candles.get(0).getTime(), 99.72048),
                Point.of(candles.get(1).getTime(), 96.22562),
                Point.of(candles.get(2).getTime(), 92.73077),
                Point.of(candles.get(3).getTime(), 89.23591),
                Point.of(candles.get(4).getTime(), 85.74105),
                Point.of(candles.get(5).getTime(), 82.24619),
                Point.of(candles.get(6).getTime(), 78.75134),
                Point.of(candles.get(7).getTime(), 75.25648),
                Point.of(candles.get(8).getTime(), 71.76162),
                Point.of(candles.get(9).getTime(), 68.26676)
        );
        AssertUtils.assertListsAreEqual(expectedSupportLine, supportLines.get(0));

        List<List<Point>> resistanceLines = response.getResistanceLines();
        Assertions.assertEquals(1, resistanceLines.size());
        List<Point> expectedResistanceLine = ImmutableList.of(
                Point.of(candles.get(2).getTime(), 104.04146),
                Point.of(candles.get(3).getTime(), 100.78874),
                Point.of(candles.get(4).getTime(), 97.53602),
                Point.of(candles.get(5).getTime(), 94.2833),
                Point.of(candles.get(6).getTime(), 91.03058),
                Point.of(candles.get(7).getTime(), 87.77786),
                Point.of(candles.get(8).getTime(), 84.52514),
                Point.of(candles.get(9).getTime(), 81.27242)
        );
        AssertUtils.assertListsAreEqual(expectedResistanceLine, resistanceLines.get(0));
    }

}