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
                new CandleBuilder().setOpen(10).setClose(15).setHighest(20).setLowest(5).setTime(time).build(),
                new CandleBuilder().setOpen(15).setClose(20).setHighest(25).setLowest(10).setTime(TimestampUtils.plusMinutes(time, 1)).build(),
                new CandleBuilder().setOpen(20).setClose(17).setHighest(24).setLowest(15).setTime(TimestampUtils.plusMinutes(time, 2)).build()
        );

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.createBigDecimals(10.0, 15.0, 20.0);
        final List<BigDecimal> longAverages = TestData.createBigDecimals(10.0, 12.5, 17.5);

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
                new CandleBuilder().setOpen(80).setClose(15).setHighest(20).setLowest(5)
                        .setTime(time)
                        .build(),
                new CandleBuilder().setOpen(1000).setClose(20).setHighest(25).setLowest(10)
                        .setTime(TimestampUtils.plusMinutes(time, 1))
                        .build(),
                new CandleBuilder().setOpen(70).setClose(17).setHighest(24).setLowest(15)
                        .setTime(TimestampUtils.plusMinutes(time, 2))
                        .build(),
                new CandleBuilder().setOpen(40).setClose(18).setHighest(22).setLowest(14)
                        .setTime(TimestampUtils.plusMinutes(time, 3))
                        .build(),
                new CandleBuilder().setOpen(50).setClose(18).setHighest(22).setLowest(14)
                        .setTime(TimestampUtils.plusMinutes(time, 4))
                        .build(),
                new CandleBuilder().setOpen(10).setClose(18).setHighest(22).setLowest(14)
                        .setTime(TimestampUtils.plusMinutes(time, 5))
                        .build(),
                new CandleBuilder().setOpen(90).setClose(18).setHighest(22).setLowest(14)
                        .setTime(TimestampUtils.plusMinutes(time, 6))
                        .build(),
                new CandleBuilder().setOpen(1000).setClose(18).setHighest(22).setLowest(14)
                        .setTime(TimestampUtils.plusMinutes(time, 7))
                        .build(),
                new CandleBuilder().setOpen(60).setClose(18).setHighest(22).setLowest(14)
                        .setTime(TimestampUtils.plusMinutes(time, 8))
                        .build(),
                new CandleBuilder().setOpen(30).setClose(18).setHighest(22).setLowest(14)
                        .setTime(TimestampUtils.plusMinutes(time, 9))
                        .build()
        );

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.createBigDecimals(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> longAverages = TestData.createBigDecimals(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);

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