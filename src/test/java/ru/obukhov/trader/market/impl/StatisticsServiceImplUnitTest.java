package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplUnitTest {

    @Mock
    private MarketService marketService;

    @InjectMocks
    private StatisticsServiceImpl service;

    @Test
    void getCandles_returnsCandlesFromMarketService() {
        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleResolution = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();

        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        final List<Candle> candlesResponse = service.getCandles(ticker, interval, candleResolution);

        Assertions.assertSame(candles, candlesResponse);
    }

    @Test
    void getExtendedCandles_extendsCandles_withoutExtremes() {
        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleResolution = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();
        final OffsetDateTime time = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);

        candles.add(TestDataHelper.createCandle(10, 15, 20, 5, time, candleResolution));
        candles.add(TestDataHelper.createCandle(15, 20, 25, 10, time.plusMinutes(1), candleResolution));
        candles.add(TestDataHelper.createCandle(20, 17, 24, 15, time.plusMinutes(2), candleResolution));

        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        final GetCandlesResponse response = service.getExtendedCandles(ticker, interval, candleResolution);

        AssertUtils.assertListsAreEqual(candles, response.getCandles());

        // expected average prices are calculated for MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder
        // with weightDecrease = 0.3 and order = 3
        final List<BigDecimal> expectedAverages = TestDataHelper.createBigDecimalsList(10.00000, 10.13500, 10.55350);
        AssertUtils.assertListsAreEqual(expectedAverages, response.getAverages());

        final List<Point> expectedMinimums = List.of(
                Point.of(candles.get(0).getTime(), 10.00000)
        );
        AssertUtils.assertListsAreEqual(expectedMinimums, response.getLocalMinimums());

        final List<Point> expectedMaximums = List.of(
                Point.of(candles.get(2).getTime(), 10.55350)
        );
        AssertUtils.assertListsAreEqual(expectedMaximums, response.getLocalMaximums());

        Assertions.assertTrue(response.getSupportLines().isEmpty());
        Assertions.assertTrue(response.getResistanceLines().isEmpty());
    }

    @Test
    void getExtendedCandles_extendsCandles_withExtremes() {
        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleResolution = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();
        final OffsetDateTime time = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);

        candles.add(TestDataHelper.createCandle(80, 15, 20, 5, time, candleResolution));
        candles.add(TestDataHelper.createCandle(1000, 20, 25, 10, time.plusMinutes(1), candleResolution));
        candles.add(TestDataHelper.createCandle(70, 17, 24, 15, time.plusMinutes(2), candleResolution));
        candles.add(TestDataHelper.createCandle(40, 18, 22, 14, time.plusMinutes(3), candleResolution));
        candles.add(TestDataHelper.createCandle(50, 18, 22, 14, time.plusMinutes(4), candleResolution));
        candles.add(TestDataHelper.createCandle(10, 18, 22, 14, time.plusMinutes(5), candleResolution));
        candles.add(TestDataHelper.createCandle(90, 18, 22, 14, time.plusMinutes(6), candleResolution));
        candles.add(TestDataHelper.createCandle(1000, 18, 22, 14, time.plusMinutes(7), candleResolution));
        candles.add(TestDataHelper.createCandle(60, 18, 22, 14, time.plusMinutes(8), candleResolution));
        candles.add(TestDataHelper.createCandle(30, 18, 22, 14, time.plusMinutes(9), candleResolution));

        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        final GetCandlesResponse response = service.getExtendedCandles(ticker, interval, candleResolution);

        AssertUtils.assertListsAreEqual(candles, response.getCandles());

        // expectedAverages are calculated for MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder
        // with weightDecrease = 0.3 and order = 3
        final List<BigDecimal> expectedAverages = TestDataHelper.createBigDecimalsList(
                80.00000, 104.84000, 131.89400, 151.38260, 161.32940,
                161.76896, 156.91483, 174.05676, 191.96114, 201.88675
        );
        AssertUtils.assertListsAreEqual(expectedAverages, response.getAverages());

        final List<Point> expectedMinimums = List.of(
                Point.of(candles.get(0).getTime(), 80.00000),
                Point.of(candles.get(6).getTime(), 156.91483)
        );
        AssertUtils.assertListsAreEqual(expectedMinimums, response.getLocalMinimums());

        final List<Point> expectedMaximums = List.of(
                Point.of(candles.get(5).getTime(), 161.76896),
                Point.of(candles.get(9).getTime(), 201.88675)
        );
        AssertUtils.assertListsAreEqual(expectedMaximums, response.getLocalMaximums());

        final List<List<Point>> supportLines = response.getSupportLines();
        Assertions.assertEquals(1, supportLines.size());
        final List<Point> expectedSupportLine = List.of(
                Point.of(candles.get(0).getTime(), 80.00000),
                Point.of(candles.get(1).getTime(), 92.81914),
                Point.of(candles.get(2).getTime(), 105.63828),
                Point.of(candles.get(3).getTime(), 118.45742),
                Point.of(candles.get(4).getTime(), 131.27655),
                Point.of(candles.get(5).getTime(), 144.09569),
                Point.of(candles.get(6).getTime(), 156.91483),
                Point.of(candles.get(7).getTime(), 169.73397),
                Point.of(candles.get(8).getTime(), 182.55311),
                Point.of(candles.get(9).getTime(), 195.37225)
        );
        AssertUtils.assertListsAreEqual(expectedSupportLine, supportLines.get(0));

        final List<List<Point>> resistanceLines = response.getResistanceLines();
        Assertions.assertEquals(1, resistanceLines.size());
        final List<Point> expectedResistanceLine = List.of(
                Point.of(candles.get(5).getTime(), 161.76896),
                Point.of(candles.get(6).getTime(), 171.79841),
                Point.of(candles.get(7).getTime(), 181.82786),
                Point.of(candles.get(8).getTime(), 191.85730),
                Point.of(candles.get(9).getTime(), 201.88675)
        );
        AssertUtils.assertListsAreEqual(expectedResistanceLine, resistanceLines.get(0));
    }

}