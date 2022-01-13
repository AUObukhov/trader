package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceUnitTest {

    @Mock
    private MovingAverager averager;
    @Mock
    private MarketService marketService;
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private StatisticsService service;

    @Test
    void getExtendedCandles_extendsCandles_withoutExtremes() {

        // arrange

        final String ticker = "ticker";

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final OffsetDateTime time = DateTimeTestData.createDateTime(2020, 1, 1, 10);
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                TestData.createCandle(10, 15, 20, 5, time, candleResolution),
                TestData.createCandle(15, 20, 25, 10, time.plusMinutes(1), candleResolution),
                TestData.createCandle(20, 17, 24, 15, time.plusMinutes(2), candleResolution)
        );

        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.createBigDecimalsList(10.0, 15.0, 20.0);
        final List<BigDecimal> longAverages = TestData.createBigDecimalsList(10.0, 12.5, 17.5);

        mockAverages(smallWindow, shortAverages);
        mockAverages(bigWindow, longAverages);

        // act

        GetCandlesResponse response = service.getExtendedCandles(ticker, interval, candleResolution, movingAverageType, smallWindow, bigWindow);

        // assert

        AssertUtils.assertListsAreEqual(candles, response.getCandles());
        AssertUtils.assertListsAreEqual(shortAverages, response.getAverages1());
        AssertUtils.assertListsAreEqual(longAverages, response.getAverages2());
    }

    @Test
    void getExtendedCandles_extendsCandles_withExtremes() {

        // arrange

        final String ticker = "ticker";

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final OffsetDateTime time = DateTimeTestData.createDateTime(2020, 1, 1, 10);
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                TestData.createCandle(80, 15, 20, 5, time, candleResolution),
                TestData.createCandle(1000, 20, 25, 10, time.plusMinutes(1), candleResolution),
                TestData.createCandle(70, 17, 24, 15, time.plusMinutes(2), candleResolution),
                TestData.createCandle(40, 18, 22, 14, time.plusMinutes(3), candleResolution),
                TestData.createCandle(50, 18, 22, 14, time.plusMinutes(4), candleResolution),
                TestData.createCandle(10, 18, 22, 14, time.plusMinutes(5), candleResolution),
                TestData.createCandle(90, 18, 22, 14, time.plusMinutes(6), candleResolution),
                TestData.createCandle(1000, 18, 22, 14, time.plusMinutes(7), candleResolution),
                TestData.createCandle(60, 18, 22, 14, time.plusMinutes(8), candleResolution),
                TestData.createCandle(30, 18, 22, 14, time.plusMinutes(9), candleResolution)
        );
        Mockito.when(marketService.getCandles(ticker, interval, candleResolution)).thenReturn(candles);

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> longAverages = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);

        mockAverages(smallWindow, shortAverages);
        mockAverages(bigWindow, longAverages);

        // act

        GetCandlesResponse response = service.getExtendedCandles(ticker, interval, candleResolution, movingAverageType, smallWindow, bigWindow);

        // assert

        AssertUtils.assertListsAreEqual(candles, response.getCandles());
        AssertUtils.assertListsAreEqual(shortAverages, response.getAverages1());
        AssertUtils.assertListsAreEqual(longAverages, response.getAverages2());
    }

    private void mockAverages(Integer window, List<BigDecimal> averages) {
        Mockito.when(averager.getAverages(Mockito.anyList(), Mockito.eq(window), Mockito.eq(1))).thenReturn(averages);
    }

}