package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
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
    private MovingAverager averager;
    @Mock
    private MarketService marketService;

    @InjectMocks
    private StatisticsServiceImpl service;

    @Test
    void getCandles_returnsCandlesFromMarketService() {

        // arrange

        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleResolution candleResolution = CandleResolution._1MIN;

        final List<Candle> candles = new ArrayList<>();

        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        // act

        final List<Candle> candlesResponse = service.getCandles(ticker, interval, candleResolution);

        // assert

        Assertions.assertSame(candles, candlesResponse);
    }

    @Test
    void getExtendedCandles_extendsCandles_withoutExtremes() {

        // arrange

        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final OffsetDateTime time = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                TestDataHelper.createCandle(10, 15, 20, 5, time, candleResolution),
                TestDataHelper.createCandle(15, 20, 25, 10, time.plusMinutes(1), candleResolution),
                TestDataHelper.createCandle(20, 17, 24, 15, time.plusMinutes(2), candleResolution)
        );

        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        final List<BigDecimal> shortAverages = TestDataHelper.createBigDecimalsList(10.0, 15.0, 20.0);
        final List<BigDecimal> longAverages = TestDataHelper.createBigDecimalsList(10.0, 12.5, 17.5);

        mockAverages(smallWindow, shortAverages);
        mockAverages(bigWindow, longAverages);

        // act

        GetCandlesResponse response;
        try (MockedStatic<MovingAverager> mock = TestDataHelper.mockAveragerByType(movingAverageType, averager)) {
            response = service.getExtendedCandles(
                    ticker, interval, candleResolution, movingAverageType, smallWindow, bigWindow
            );
        }

        // assert

        AssertUtils.assertListsAreEqual(candles, response.getCandles());
        AssertUtils.assertListsAreEqual(shortAverages, response.getAverages1());
        AssertUtils.assertListsAreEqual(longAverages, response.getAverages2());
    }

    @Test
    void getExtendedCandles_extendsCandles_withExtremes() {

        // arrange

        final String ticker = "ticker";

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final OffsetDateTime time = DateUtils.getDateTime(2020, 1, 1, 10, 0, 0);
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                TestDataHelper.createCandle(80, 15, 20, 5, time, candleResolution),
                TestDataHelper.createCandle(1000, 20, 25, 10, time.plusMinutes(1), candleResolution),
                TestDataHelper.createCandle(70, 17, 24, 15, time.plusMinutes(2), candleResolution),
                TestDataHelper.createCandle(40, 18, 22, 14, time.plusMinutes(3), candleResolution),
                TestDataHelper.createCandle(50, 18, 22, 14, time.plusMinutes(4), candleResolution),
                TestDataHelper.createCandle(10, 18, 22, 14, time.plusMinutes(5), candleResolution),
                TestDataHelper.createCandle(90, 18, 22, 14, time.plusMinutes(6), candleResolution),
                TestDataHelper.createCandle(1000, 18, 22, 14, time.plusMinutes(7), candleResolution),
                TestDataHelper.createCandle(60, 18, 22, 14, time.plusMinutes(8), candleResolution),
                TestDataHelper.createCandle(30, 18, 22, 14, time.plusMinutes(9), candleResolution)
        );
        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        final List<BigDecimal> shortAverages = TestDataHelper.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> longAverages = TestDataHelper.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);

        mockAverages(smallWindow, shortAverages);
        mockAverages(bigWindow, longAverages);

        // act

        GetCandlesResponse response;
        try (MockedStatic<MovingAverager> mock = TestDataHelper.mockAveragerByType(movingAverageType, averager)) {
            response = service.getExtendedCandles(
                    ticker, interval, candleResolution, movingAverageType, smallWindow, bigWindow
            );
        }

        // assert

        AssertUtils.assertListsAreEqual(candles, response.getCandles());
        AssertUtils.assertListsAreEqual(shortAverages, response.getAverages1());
        AssertUtils.assertListsAreEqual(longAverages, response.getAverages2());
    }

    private void mockAverages(Integer window, List<BigDecimal> averages) {
        Mockito.when(averager.getAverages(Mockito.anyList(), Mockito.eq(window), Mockito.eq(1))).thenReturn(averages);
    }

}