package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.TimestampUtils;
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
import java.util.List;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceUnitTest {

    @Mock
    private MovingAverager averager;
    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private StatisticsService service;

    @Test
    void getExtendedCandles_extendsCandles_withoutExtremes() {

        // arrange

        final String figi = TestShare1.FIGI;

        final Timestamp from = TimestampUtils.newTimestamp(2020, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final Timestamp time = TimestampUtils.newTimestamp(2020, 1, 1, 10);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                new CandleBuilder().setOpenPrice(10).setClosePrice(15).setHighestPrice(20).setLowestPrice(5).setTime(time).build(),
                new CandleBuilder().setOpenPrice(15).setClosePrice(20).setHighestPrice(25).setLowestPrice(10).setTime(TimestampUtils.plusMinutes(time, 1)).build(),
                new CandleBuilder().setOpenPrice(20).setClosePrice(17).setHighestPrice(24).setLowestPrice(15).setTime(TimestampUtils.plusMinutes(time, 2)).build()
        );

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.createBigDecimalsList(10.0, 15.0, 20.0);
        final List<BigDecimal> longAverages = TestData.createBigDecimalsList(10.0, 12.5, 17.5);

        mockAverages(smallWindow, shortAverages);
        mockAverages(bigWindow, longAverages);

        final OffsetDateTime mockedNow = OffsetDateTime.now();
        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

            // act

            GetCandlesResponse response = service.getExtendedCandles(figi, interval, candleInterval, movingAverageType, smallWindow, bigWindow);

            // assert

            AssertUtils.assertEquals(candles, response.getCandles());
            AssertUtils.assertEquals(shortAverages, response.getAverages1());
            AssertUtils.assertEquals(longAverages, response.getAverages2());
        }
    }

    @Test
    void getExtendedCandles_extendsCandles_withExtremes() {

        // arrange

        final String figi = TestShare1.FIGI;

        final Timestamp from = TimestampUtils.newTimestamp(2020, 1, 1);
        final Timestamp to = TimestampUtils.newTimestamp(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final Timestamp time = TimestampUtils.newTimestamp(2020, 1, 1, 10);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                new CandleBuilder().setOpenPrice(80).setClosePrice(15).setHighestPrice(20).setLowestPrice(5)
                        .setTime(time)
                        .build(),
                new CandleBuilder().setOpenPrice(1000).setClosePrice(20).setHighestPrice(25).setLowestPrice(10)
                        .setTime(TimestampUtils.plusMinutes(time, 1))
                        .build(),
                new CandleBuilder().setOpenPrice(70).setClosePrice(17).setHighestPrice(24).setLowestPrice(15)
                        .setTime(TimestampUtils.plusMinutes(time, 2))
                        .build(),
                new CandleBuilder().setOpenPrice(40).setClosePrice(18).setHighestPrice(22).setLowestPrice(14)
                        .setTime(TimestampUtils.plusMinutes(time, 3))
                        .build(),
                new CandleBuilder().setOpenPrice(50).setClosePrice(18).setHighestPrice(22).setLowestPrice(14)
                        .setTime(TimestampUtils.plusMinutes(time, 4))
                        .build(),
                new CandleBuilder().setOpenPrice(10).setClosePrice(18).setHighestPrice(22).setLowestPrice(14)
                        .setTime(TimestampUtils.plusMinutes(time, 5))
                        .build(),
                new CandleBuilder().setOpenPrice(90).setClosePrice(18).setHighestPrice(22).setLowestPrice(14)
                        .setTime(TimestampUtils.plusMinutes(time, 6))
                        .build(),
                new CandleBuilder().setOpenPrice(1000).setClosePrice(18).setHighestPrice(22).setLowestPrice(14)
                        .setTime(TimestampUtils.plusMinutes(time, 7))
                        .build(),
                new CandleBuilder().setOpenPrice(60).setClosePrice(18).setHighestPrice(22).setLowestPrice(14)
                        .setTime(TimestampUtils.plusMinutes(time, 8))
                        .build(),
                new CandleBuilder().setOpenPrice(30).setClosePrice(18).setHighestPrice(22).setLowestPrice(14)
                        .setTime(TimestampUtils.plusMinutes(time, 9))
                        .build()
        );

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> longAverages = TestData.createBigDecimalsList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);

        mockAverages(smallWindow, shortAverages);
        mockAverages(bigWindow, longAverages);

        final OffsetDateTime mockedNow = OffsetDateTime.now();
        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

            // act

            GetCandlesResponse response = service.getExtendedCandles(figi, interval, candleInterval, movingAverageType, smallWindow, bigWindow);

            // assert

            AssertUtils.assertEquals(candles, response.getCandles());
            AssertUtils.assertEquals(shortAverages, response.getAverages1());
            AssertUtils.assertEquals(longAverages, response.getAverages2());
        }
    }

    private void mockAverages(Integer window, List<BigDecimal> averages) {
        Mockito.when(averager.getAverages(Mockito.anyList(), Mockito.eq(window), Mockito.eq(1))).thenReturn(averages);
    }

}