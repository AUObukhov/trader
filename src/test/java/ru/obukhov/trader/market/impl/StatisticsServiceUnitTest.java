package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
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
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.SetCapitalization;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceUnitTest {

    @Mock
    private MovingAverager averager;
    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private StatisticsService service;

    // region getExtendedCandles tests

    @Test
    void getExtendedCandles_extendsCandles_withoutExtremes() {

        // arrange

        final String figi = TestShares.APPLE.share().figi();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final OffsetDateTime time = DateTimeTestData.createDateTime(2020, 1, 1, 10);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                new CandleBuilder().setOpen(10).setClose(15).setHigh(20).setLow(5).setTime(time).build(),
                new CandleBuilder().setOpen(15).setClose(20).setHigh(25).setLow(10).setTime(time.plusMinutes(1)).build(),
                new CandleBuilder().setOpen(20).setClose(17).setHigh(24).setLow(15).setTime(time.plusMinutes(2)).build()
        );

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.newBigDecimalList(10, 15, 20);
        final List<BigDecimal> longAverages = TestData.newBigDecimalList(10.0, 12.5, 17.5);

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

        final String figi = TestShares.APPLE.share().figi();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final OffsetDateTime time = DateTimeTestData.createDateTime(2020, 1, 1, 10);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;

        final int smallWindow = 1;
        final int bigWindow = 2;

        final List<Candle> candles = List.of(
                new CandleBuilder().setOpen(80).setClose(15).setHigh(20).setLow(5)
                        .setTime(time)
                        .build(),
                new CandleBuilder().setOpen(1000).setClose(20).setHigh(25).setLow(10)
                        .setTime(time.plusMinutes(1))
                        .build(),
                new CandleBuilder().setOpen(70).setClose(17).setHigh(24).setLow(15)
                        .setTime(time.plusMinutes(2))
                        .build(),
                new CandleBuilder().setOpen(40).setClose(18).setHigh(22).setLow(14)
                        .setTime(time.plusMinutes(3))
                        .build(),
                new CandleBuilder().setOpen(50).setClose(18).setHigh(22).setLow(14)
                        .setTime(time.plusMinutes(4))
                        .build(),
                new CandleBuilder().setOpen(10).setClose(18).setHigh(22).setLow(14)
                        .setTime(time.plusMinutes(5))
                        .build(),
                new CandleBuilder().setOpen(90).setClose(18).setHigh(22).setLow(14)
                        .setTime(time.plusMinutes(6))
                        .build(),
                new CandleBuilder().setOpen(1000).setClose(18).setHigh(22).setLow(14)
                        .setTime(time.plusMinutes(7))
                        .build(),
                new CandleBuilder().setOpen(60).setClose(18).setHigh(22).setLow(14)
                        .setTime(time.plusMinutes(8))
                        .build(),
                new CandleBuilder().setOpen(30).setClose(18).setHigh(22).setLow(14)
                        .setTime(time.plusMinutes(9))
                        .build()
        );

        Mockito.when(applicationContext.getBean(movingAverageType.getAveragerName(), MovingAverager.class)).thenReturn(averager);

        final List<BigDecimal> shortAverages = TestData.newBigDecimalList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);
        final List<BigDecimal> longAverages = TestData.newBigDecimalList(80, 540, 535, 55, 45, 30, 50, 545, 530, 45);

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

    // endregion

    @Test
    void getIndexWeights() {
        // arrange

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();
        final Share share3 = TestShares.YANDEX.share();
        final List<String> figies = List.of(share1.figi(), share2.figi(), share3.figi());

        final List<Share> shares = List.of(share1, share2, share3);
        Mockito.when(extInstrumentsService.getShares(figies)).thenReturn(shares);

        final BigDecimal share1Price = DecimalUtils.setDefaultScale(178.7);
        final BigDecimal share2Price = DecimalUtils.setDefaultScale(258.79);
        final BigDecimal share3Price = DecimalUtils.setDefaultScale(2585.6);
        final Map<String, BigDecimal> lastPrices = Map.of(
                share1.figi(), share1Price,
                share2.figi(), share2Price,
                share3.figi(), share3Price
        );
        Mockito.when(extMarketDataService.getLastPrices(figies)).thenReturn(lastPrices);

        Mockito.when(extMarketDataService.convertCurrency(share1.currency(), Currencies.RUB, share1Price))
                .thenReturn(DecimalUtils.setDefaultScale(17588.10075)); // USD = 98.4225 RUB
        Mockito.when(extMarketDataService.convertCurrency(share2.currency(), Currencies.RUB, share2Price))
                .thenReturn(share2Price);
        Mockito.when(extMarketDataService.convertCurrency(share3.currency(), Currencies.RUB, share3Price))
                .thenReturn(share3Price);

        // action

        final Map<String, BigDecimal> actualResult = service.getCapitalizationWeights(figies);

        // assert

        final Map<String, BigDecimal> expectedWeights = new LinkedHashMap<>(3, 1);
        expectedWeights.put(share1.figi(), DecimalUtils.setDefaultScale(0.978361221));
        expectedWeights.put(share2.figi(), DecimalUtils.setDefaultScale(0.018799306));
        expectedWeights.put(share3.figi(), DecimalUtils.setDefaultScale(0.002839473));

        Assertions.assertEquals(expectedWeights, actualResult);
    }

    @Test
    void getCapitalization() {
        // arrange

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();
        final Share share3 = TestShares.YANDEX.share();
        final List<String> figies = List.of(share1.figi(), share2.figi(), share3.figi());

        final List<Share> shares = List.of(share1, share2, share3);
        Mockito.when(extInstrumentsService.getShares(figies)).thenReturn(shares);

        final BigDecimal share1Price = DecimalUtils.setDefaultScale(178.7);
        final BigDecimal share2Price = DecimalUtils.setDefaultScale(258.79);
        final BigDecimal share3Price = DecimalUtils.setDefaultScale(2585.6);
        final Map<String, BigDecimal> lastPrices = Map.of(
                share1.figi(), share1Price,
                share2.figi(), share2Price,
                share3.figi(), share3Price
        );
        Mockito.when(extMarketDataService.getLastPrices(figies)).thenReturn(lastPrices);

        Mockito.when(extMarketDataService.convertCurrency(share1.currency(), Currencies.RUB, share1Price))
                .thenReturn(DecimalUtils.setDefaultScale(17588.10075)); // USD = 98.4225 RUB
        Mockito.when(extMarketDataService.convertCurrency(share2.currency(), Currencies.RUB, share2Price))
                .thenReturn(share2Price);
        Mockito.when(extMarketDataService.convertCurrency(share3.currency(), Currencies.RUB, share3Price))
                .thenReturn(share3Price);

        // action

        final SetCapitalization actualResult = service.getCapitalization(figies);

        // assert

        final Map<String, BigDecimal> securitiesCapitalizations = Map.of(
                share1.figi(), DecimalUtils.setDefaultScale(290734225022224.5),
                share2.figi(), DecimalUtils.setDefaultScale(5586486272920L),
                share3.figi(), DecimalUtils.setDefaultScale(843790573312L)
        );
        final BigDecimal totalCapitalization = DecimalUtils.setDefaultScale(297164501868456.5);
        final SetCapitalization expectedResult = new SetCapitalization(securitiesCapitalizations, totalCapitalization);

        Assertions.assertEquals(expectedResult, actualResult);
    }

}