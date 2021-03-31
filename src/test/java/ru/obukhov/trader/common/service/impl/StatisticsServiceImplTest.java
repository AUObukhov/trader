package ru.obukhov.trader.common.service.impl;

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
import ru.obukhov.trader.market.impl.StatisticsServiceImpl;
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
class StatisticsServiceImplTest extends BaseMockedTest {

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
    void getExtendedCandles_extendsCandles() {
        final String ticker = TICKER;

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleInterval = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();
        OffsetDateTime time1 = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);
        candles.add(TestDataHelper.createCandle(10, 15, 20, 5, time1, CandleResolution._1MIN));

        OffsetDateTime time2 = DateUtils.getDateTime(2020, 1, 1, 10, 1, 0);
        candles.add(TestDataHelper.createCandle(15, 20, 25, 10, time2, CandleResolution._1MIN));

        OffsetDateTime time3 = DateUtils.getDateTime(2020, 1, 1, 10, 2, 0);
        candles.add(TestDataHelper.createCandle(20, 17, 24, 15, time3, CandleResolution._1MIN));

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
        AssertUtils.assertEquals(14.72325, extendedCandles.get(0).getAveragePrice());
        Assertions.assertEquals(Extremum.MIN, extendedCandles.get(0).getExtremum());

        AssertUtils.assertEquals(15, extendedCandles.get(1).getAveragePrice());
        Assertions.assertEquals(Extremum.NONE, extendedCandles.get(1).getExtremum());

        AssertUtils.assertEquals(15.27675, extendedCandles.get(2).getAveragePrice());
        Assertions.assertEquals(Extremum.MAX, extendedCandles.get(2).getExtremum());
    }

}