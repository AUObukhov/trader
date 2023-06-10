package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.MarketDataService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExtMarketDataServiceUnitTest {

    @Mock
    private RealExtInstrumentsService realExtInstrumentsService;
    @Mock
    private MarketDataService marketDataService;

    private ExtMarketDataService service;
    private final ExtMarketDataService self = new ExtMarketDataService(realExtInstrumentsService, marketDataService, null) {
        public List<Candle> getMarketCandles(final String figi, final Interval interval, final CandleInterval candleInterval) {
            return service.getMarketCandles(figi, interval, candleInterval);
        }
    };

    @BeforeEach
    public void setUpEach() {
        this.service = new ExtMarketDataService(realExtInstrumentsService, marketDataService, self);
    }

    // region getCandles tests

    @Test
    void getCandles_skipsCandlesByDays_whenFromIsReached() {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(10, DateTimeTestData.createDateTime(2020, 1, 5))
                .add(0, DateTimeTestData.createDateTime(2020, 1, 7))
                .add(1, DateTimeTestData.createDateTime(2020, 1, 8))
                .add(2, DateTimeTestData.createDateTime(2020, 1, 9))
                .add(3, DateTimeTestData.createDateTime(2020, 1, 11))
                .add(4, DateTimeTestData.createDateTime(2020, 1, 11))
                .add(5, DateTimeTestData.createDateTime(2020, 1, 12))
                .mock();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 1, 6);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 1, 13);

        final List<Candle> candles = service.getCandles(figi, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(1, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(4).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(5).getClosePrice());
    }

    @Test
    void getCandles_filterCandlesByYears() {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(10, DateTimeTestData.createDateTime(2016, 1, 1))
                .add(0, DateTimeTestData.createDateTime(2016, 2, 2))
                .add(1, DateTimeTestData.createDateTime(2016, 2, 2))
                .add(2, DateTimeTestData.createDateTime(2016, 2, 2))
                .add(3, DateTimeTestData.createDateTime(2016, 2, 3))
                .add(4, DateTimeTestData.createDateTime(2016, 2, 3))
                .add(5, DateTimeTestData.createDateTime(2016, 3, 1))
                .mock();

        final Interval interval = Interval.of(
                DateTimeTestData.createDateTime(2016, 2, 1),
                DateTimeTestData.createDateTime(2016, 2, 29)
        );

        final List<Candle> candles = service.getCandles(figi, interval, candleInterval);

        Assertions.assertEquals(5, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(1, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(4).getClosePrice());
    }

    @Test
    void getCandles_skipsCandlesByYears_whenFromIsReached() {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(10, DateTimeTestData.createDateTime(2016, 1, 1, 1))
                .add(0, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(1, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(2, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(3, DateTimeTestData.createDateTime(2018, 1, 1, 1))
                .add(4, DateTimeTestData.createDateTime(2018, 1, 1, 1))
                .add(5, DateTimeTestData.createDateTime(2019, 1, 1, 1))
                .mock();

        final Interval interval = Interval.of(
                DateTimeTestData.createDateTime(2017, 1, 1),
                DateTimeTestData.createDateTime(2020, 1, 1)
        );

        final List<Candle> candles = service.getCandles(figi, interval, candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(1, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(4).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(5).getClosePrice());
    }

    @Test
    void getCandles_skipsCandlesBeforeFromByYears_whenFromInTheMiddleOfYear() {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(0, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(1, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(2, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(3, DateTimeTestData.createDateTime(2018, 1, 1, 1))
                .add(4, DateTimeTestData.createDateTime(2018, 1, 1, 1))
                .add(5, DateTimeTestData.createDateTime(2019, 1, 1, 1))
                .mock();

        final Interval interval = Interval.of(
                DateTimeTestData.createDateTime(2017, 1, 4),
                DateTimeTestData.createDateTime(2020, 1, 1)
        );

        final List<Candle> candles = service.getCandles(figi, interval, candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(3, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(2).getClosePrice());
    }

    // endregion

    // region getLastPrice tests

    @Test
    void getLastPrice_throwsIllegalArgumentException_whenNoCandles() {
        final String figi = TestShare1.FIGI;
        final OffsetDateTime to = OffsetDateTime.now().minusDays(10);

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        final Executable executable = () -> service.getLastPrice(figi, to);
        final String expectedMessage = "Not found last candle for FIGI '" + figi + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getLastPrice_returnsCandle_whenCandleExists() {
        final String figi = TestShare1.FIGI;
        final OffsetDateTime from = TestShare1.FIRST_1_MIN_CANDLE_DATE;
        final OffsetDateTime to = from.plusDays(1);
        final int closePrice = 10;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(closePrice, from)
                .mock();

        final BigDecimal price = service.getLastPrice(figi, to);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(closePrice, price);
    }

    // endregion

    // region getLastCandles daily tests

    @Test
    void getLastCandlesDaily_returnsNoCandles_whenThereAreNoCandles() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;

        final OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateTimeTestData.createDateTime(2020, 9, 10));

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        final List<Candle> candles = service.getLastCandles(figi, limit, CandleInterval.CANDLE_INTERVAL_1_MIN, currentDateTime);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandlesDaily_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 8, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 8, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10, 2);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(6, candles.get(4).getClosePrice());
    }

    @Test
    void getLastCandlesDaily_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() {
        final String figi = TestShare1.FIGI;
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 8, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 8, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10, 2);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(4).getClosePrice());
        AssertUtils.assertEquals(6, candles.get(5).getClosePrice());
    }

    @Test
    void getLastCandlesDaily_returnsNoFutureCandles_whenThereAreFutureCandles() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 9, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 4))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 5))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 9, 4);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(2).getClosePrice());
    }

    // endregion

    // region getLastCandles yearly tests

    @Test
    void getLastCandlesYearly_returnsNoCandles_whenThereAreNoCandles() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;

        final OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateTimeTestData.createDateTime(2020, 9, 10));

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        final List<Candle> candles = service.getLastCandles(figi, limit, CandleInterval.CANDLE_INTERVAL_DAY, currentDateTime);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandlesYearly_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(6, candles.get(4).getClosePrice());
    }

    @Test
    void getLastCandlesYearly_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() {
        final String figi = TestShare1.FIGI;
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(4).getClosePrice());
        AssertUtils.assertEquals(6, candles.get(5).getClosePrice());
    }

    @Test
    void getLastCandlesYearly_returnsPastYearCandles_whenThereAreNoCandlesInCurrentYear() {
        final String figi = TestShare1.FIGI;
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2019, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2019, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2019, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2019, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2019, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2019, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(3, candles.get(2).getClosePrice());
        AssertUtils.assertEquals(4, candles.get(3).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(4).getClosePrice());
        AssertUtils.assertEquals(6, candles.get(5).getClosePrice());
    }

    @Test
    void getLastCandlesYearly_returnsNoCandles_whenThereIsEmptyYearAfterCandles() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2018, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2018, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2018, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2018, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2018, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2018, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandlesYearly_returnsCandlesOnlyAfterEmptyYear_whenThereEmptyYearBetweenCandles() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2018, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2018, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2018, 9, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 12))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(4, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(5, candles.get(1).getClosePrice());
        AssertUtils.assertEquals(6, candles.get(2).getClosePrice());
    }

    @Test
    void getLastCandlesYearly_returnsNoFutureCandles_whenThereAreFutureCandles() {
        final String figi = TestShare1.FIGI;
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mockito.when(realExtInstrumentsService.getShare(figi)).thenReturn(TestShare1.SHARE);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 13))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 15))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 16))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 17))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = service.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClosePrice());
        AssertUtils.assertEquals(2, candles.get(1).getClosePrice());
    }

    // endregion

    // region getMarketCandles tests

    @Test
    void getMarketCandles_returnsMappedCandles() {
        final String figi = TestShare1.FIGI;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final int openPrice1 = 1000;
        final int closePrice1 = 1500;
        final int highestPrice1 = 2000;
        final int lowestPrice1 = 500;
        final OffsetDateTime time1 = from.plusMinutes(1);
        final HistoricCandle historicCandle1 = new HistoricCandleBuilder()
                .setOpenPrice(openPrice1)
                .setClosePrice(closePrice1)
                .setHighestPrice(highestPrice1)
                .setLowestPrice(lowestPrice1)
                .setTime(time1)
                .setIsComplete(true)
                .build();

        final int openPrice2 = 1500;
        final int closePrice2 = 2000;
        final int highestPrice2 = 2500;
        final int lowestPrice2 = 1000;
        final OffsetDateTime time2 = from.plusMinutes(2);
        final HistoricCandle historicCandle2 = new HistoricCandleBuilder()
                .setOpenPrice(openPrice2)
                .setClosePrice(closePrice2)
                .setHighestPrice(highestPrice2)
                .setLowestPrice(lowestPrice2)
                .setTime(time2)
                .setIsComplete(true)
                .build();

        final int openPrice3 = 2000;
        final int closePrice3 = 2500;
        final int highestPrice3 = 3000;
        final int lowestPrice3 = 500;
        final OffsetDateTime time3 = from.plusMinutes(3);
        final HistoricCandle historicCandle3 = new HistoricCandleBuilder()
                .setOpenPrice(openPrice3)
                .setClosePrice(closePrice3)
                .setHighestPrice(highestPrice3)
                .setLowestPrice(lowestPrice3)
                .setTime(time3)
                .setIsComplete(false)
                .build();

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandle1, historicCandle2, historicCandle3)
                .mock();

        final List<Candle> candles = service.getMarketCandles(figi, interval, candleInterval);

        Assertions.assertEquals(2, candles.size());
        final Candle expectedCandle1 = new CandleBuilder()
                .setOpenPrice(openPrice1)
                .setClosePrice(closePrice1)
                .setHighestPrice(highestPrice1)
                .setLowestPrice(lowestPrice1)
                .setTime(time1)
                .build();
        final Candle expectedCandle2 = new CandleBuilder()
                .setOpenPrice(openPrice2)
                .setClosePrice(closePrice2)
                .setHighestPrice(highestPrice2)
                .setLowestPrice(lowestPrice2)
                .setTime(time2)
                .build();
        Assertions.assertEquals(expectedCandle1, candles.get(0));
        Assertions.assertEquals(expectedCandle2, candles.get(1));
    }

    @Test
    void getMarketCandles_returnsEmptyList_whenGetsNoCandles() {
        final String figi = TestShare1.FIGI;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        new CandleMocker(marketDataService, figi, candleInterval)
                .mock();

        final List<Candle> candles = service.getMarketCandles(figi, interval, candleInterval);

        Assertions.assertTrue(candles.isEmpty());
    }

    // endregion

}