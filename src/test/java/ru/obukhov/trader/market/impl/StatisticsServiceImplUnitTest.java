package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.market.model.Extremum;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

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

        candles.add(TestDataHelper.createCandle(10, 15, 20, 5, time, CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(15, 20, 25, 10, time.plusMinutes(1), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(20, 17, 24, 15, time.plusMinutes(2), CandleResolution._1MIN));

        Mockito.when(marketService.getCandles(ticker, interval, candleInterval)).thenReturn(candles);

        final List<ExtendedCandle> extendedCandles = service.getExtendedCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(candles.size(), extendedCandles.size());
        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);
            ExtendedCandle extendedCandle = extendedCandles.get(i);

            AssertUtils.assertEquals(candle.getOpenPrice(), extendedCandle.getOpenPrice());
            AssertUtils.assertEquals(candle.getClosePrice(), extendedCandle.getClosePrice());
            AssertUtils.assertEquals(candle.getHighestPrice(), extendedCandle.getHighestPrice());
            AssertUtils.assertEquals(candle.getLowestPrice(), extendedCandle.getLowestPrice());
            Assertions.assertEquals(candle.getTime(), extendedCandle.getTime());
            Assertions.assertEquals(candle.getInterval(), extendedCandle.getInterval());
        }

        // expected average prices are calculated for MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder
        // with weightDecrease = 0.3 and order = 3
        AssertUtils.assertExtendedCandle(extendedCandles.get(0), 14.72325, Extremum.MIN, null, null);
        AssertUtils.assertExtendedCandle(extendedCandles.get(1), 15, Extremum.NONE, null, null);
        AssertUtils.assertExtendedCandle(extendedCandles.get(2), 15.27675, Extremum.MAX, null, null);
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

        candles.add(TestDataHelper.createCandle(80, 15, 20, 5, time, CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(10, 20, 25, 10, time.plusMinutes(1), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(70, 17, 24, 15, time.plusMinutes(2), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(40, 18, 22, 14, time.plusMinutes(3), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(50, 18, 22, 14, time.plusMinutes(3), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(10, 18, 22, 14, time.plusMinutes(3), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(90, 18, 22, 14, time.plusMinutes(3), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(1000, 18, 22, 14, time.plusMinutes(3), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(60, 18, 22, 14, time.plusMinutes(3), CandleResolution._1MIN));
        candles.add(TestDataHelper.createCandle(30, 18, 22, 14, time.plusMinutes(3), CandleResolution._1MIN));

        Mockito.when(marketService.getCandles(ticker, interval, candleInterval)).thenReturn(candles);

        final List<ExtendedCandle> extendedCandles = service.getExtendedCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(candles.size(), extendedCandles.size());
        for (int i = 0; i < candles.size(); i++) {
            Candle candle = candles.get(i);
            ExtendedCandle extendedCandle = extendedCandles.get(i);

            AssertUtils.assertEquals(candle.getOpenPrice(), extendedCandle.getOpenPrice());
            AssertUtils.assertEquals(candle.getClosePrice(), extendedCandle.getClosePrice());
            AssertUtils.assertEquals(candle.getHighestPrice(), extendedCandle.getHighestPrice());
            AssertUtils.assertEquals(candle.getLowestPrice(), extendedCandle.getLowestPrice());
            Assertions.assertEquals(candle.getTime(), extendedCandle.getTime());
            Assertions.assertEquals(candle.getInterval(), extendedCandle.getInterval());
        }

        // expected average prices are calculated for MathUtils.getExponentialWeightedMovingAveragesOfArbitraryOrder
        // with weightDecrease = 0.3 and order = 3
        AssertUtils.assertExtendedCandle(extendedCandles.get(0), 99.72048, Extremum.MIN, 99.72048, 110.54690);
        AssertUtils.assertExtendedCandle(extendedCandles.get(1), 102.46637, Extremum.NONE, 96.22562, 107.29418);
        AssertUtils.assertExtendedCandle(extendedCandles.get(2), 104.04146, Extremum.MAX, 92.73077, 104.04146);
        AssertUtils.assertExtendedCandle(extendedCandles.get(3), 102.67578, Extremum.NONE, 89.23591, 100.78874);
        AssertUtils.assertExtendedCandle(extendedCandles.get(4), 98.28161, Extremum.NONE, 85.74105, 97.53602);
        AssertUtils.assertExtendedCandle(extendedCandles.get(5), 89.06988, Extremum.NONE, 82.24619, 94.28330);
        AssertUtils.assertExtendedCandle(extendedCandles.get(6), 75.78625, Extremum.NONE, 78.75134, 91.03058);
        AssertUtils.assertExtendedCandle(extendedCandles.get(7), 71.94727, Extremum.NONE, 75.25648, 87.77786);
        AssertUtils.assertExtendedCandle(extendedCandles.get(8), 71.76162, Extremum.MIN, 71.76162, 84.52514);
        AssertUtils.assertExtendedCandle(extendedCandles.get(9), 81.27242, Extremum.MAX, 68.26676, 81.27242);

    }

}