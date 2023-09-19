package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.exception.MultipleInstrumentsFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
class ExtMarketDataServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private ExtMarketDataService extMarketDataService;

    // region getCandles tests

    @Test
    @DirtiesContext
    void getCandles_skipsCandlesByDays_whenFromIsReached() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

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

        final List<Candle> candles = extMarketDataService.getCandles(figi, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getClose());
        AssertUtils.assertEquals(1, candles.get(1).getClose());
        AssertUtils.assertEquals(2, candles.get(2).getClose());
        AssertUtils.assertEquals(3, candles.get(3).getClose());
        AssertUtils.assertEquals(4, candles.get(4).getClose());
        AssertUtils.assertEquals(5, candles.get(5).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_filterCandlesByYears() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

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

        final List<Candle> candles = extMarketDataService.getCandles(figi, interval, candleInterval);

        Assertions.assertEquals(5, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getClose());
        AssertUtils.assertEquals(1, candles.get(1).getClose());
        AssertUtils.assertEquals(2, candles.get(2).getClose());
        AssertUtils.assertEquals(3, candles.get(3).getClose());
        AssertUtils.assertEquals(4, candles.get(4).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_skipsCandlesByYears_whenFromIsReached() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

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

        final List<Candle> candles = extMarketDataService.getCandles(figi, interval, candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getClose());
        AssertUtils.assertEquals(1, candles.get(1).getClose());
        AssertUtils.assertEquals(2, candles.get(2).getClose());
        AssertUtils.assertEquals(3, candles.get(3).getClose());
        AssertUtils.assertEquals(4, candles.get(4).getClose());
        AssertUtils.assertEquals(5, candles.get(5).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_skipsCandlesBeforeFromByYears_whenFromInTheMiddleOfYear() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

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

        final List<Candle> candles = extMarketDataService.getCandles(figi, interval, candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(3, candles.get(0).getClose());
        AssertUtils.assertEquals(4, candles.get(1).getClose());
        AssertUtils.assertEquals(5, candles.get(2).getClose());
    }

    // endregion

    @Test
    @DirtiesContext
    void getLastPrice_returnsPrice_whenCandleExists() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final OffsetDateTime to = DateTimeTestData.createEndOfDay(2020, 1, 10);
        final OffsetDateTime candlesTo = testShare.share().first1MinCandleDate().plusDays(1);
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(candlesTo);
        final int close = 10;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getLastPrice(figi, to);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    // region getLastPrices tests

    @Test
    void getLastPrices_returnsPricesInProperOrder() {
        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.APPLE.share().figi();
        final String figi3 = TestShares.YANDEX.share().figi();

        final BigDecimal price1 = DecimalUtils.setDefaultScale(111.111);
        final BigDecimal price2 = DecimalUtils.setDefaultScale(222.222);
        final BigDecimal price3 = DecimalUtils.setDefaultScale(333.333);

        final List<String> figies = List.of(figi1, figi2, figi3);

        final Map<String, BigDecimal> figiesToPrices = new LinkedHashMap<>(3, 1);
        figiesToPrices.put(figi1, price1);
        figiesToPrices.put(figi2, price2);
        figiesToPrices.put(figi3, price3);
        Mocker.mockLastPricesBigDecimal(marketDataService, figiesToPrices);

        final Map<String, BigDecimal> actualResult = extMarketDataService.getLastPrices(figies);

        AssertUtils.assertEquals(figiesToPrices.entrySet(), actualResult.entrySet());
    }

    @Test
    void getLastPrices_throwsInstrumentNotFoundException_whenPriceNotFound() {
        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.APPLE.share().figi();
        final String figi3 = TestShares.YANDEX.share().figi();
        final String figi4 = TestShares.DIOD.share().figi();

        final double price1 = 111.111;
        final double price2 = 222.222;
        final double price3 = 333.333;

        final List<String> figies = List.of(figi1, figi2, figi3, figi4);

        final LastPrice lastPrice1 = TestData.newLastPrice(figi1, price1);
        final LastPrice lastPrice2 = TestData.newLastPrice(figi2, price2);
        final LastPrice lastPrice3 = TestData.newLastPrice(figi3, price3);

        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(List.of(lastPrice1, lastPrice2, lastPrice3));

        final Executable executable = () -> extMarketDataService.getLastPrices(figies);
        final String expectedMessage = "Instrument not found for id " + figi4;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    @Test
    void getLastPrices_throwsMultipleInstrumentsFoundException_whenMultiplePricesForSingleFigi() {
        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.APPLE.share().figi();
        final String figi3 = TestShares.YANDEX.share().figi();

        final double price1 = 111.111;
        final double price2 = 222.222;
        final double price3 = 333.333;

        final List<String> figies = List.of(figi1, figi2, figi3);

        final LastPrice lastPrice1 = TestData.newLastPrice(figi1, price1);
        final LastPrice lastPrice2 = TestData.newLastPrice(figi2, price2);
        final LastPrice lastPrice3 = TestData.newLastPrice(figi3, price3);
        final LastPrice lastPrice4 = TestData.newLastPrice(figi1, price3);

        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(List.of(lastPrice1, lastPrice2, lastPrice3, lastPrice4));

        final Executable executable = () -> extMarketDataService.getLastPrices(figies);
        final String expectedMessage = "Multiple instruments found for id " + figi1;
        AssertUtils.assertThrowsWithMessage(MultipleInstrumentsFoundException.class, executable, expectedMessage);
    }

    // endregion

    // region getLastCandles daily tests

    @Test
    @DirtiesContext
    void getLastCandlesDaily_returnsNoCandles_whenThereAreNoCandles() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final OffsetDateTime currentDateTIme = DateTimeTestData.createEndOfDay(2020, 9, 10);

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, CandleInterval.CANDLE_INTERVAL_1_MIN, currentDateTIme);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    @DirtiesContext
    void getLastCandlesDaily_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 8, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 8, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10, 2);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
        AssertUtils.assertEquals(4, candles.get(2).getClose());
        AssertUtils.assertEquals(5, candles.get(3).getClose());
        AssertUtils.assertEquals(6, candles.get(4).getClose());
    }

    @Test
    @DirtiesContext
    void getLastCandlesDaily_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 8, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 8, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10, 2);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClose());
        AssertUtils.assertEquals(2, candles.get(1).getClose());
        AssertUtils.assertEquals(3, candles.get(2).getClose());
        AssertUtils.assertEquals(4, candles.get(3).getClose());
        AssertUtils.assertEquals(5, candles.get(4).getClose());
        AssertUtils.assertEquals(6, candles.get(5).getClose());
    }

    @Test
    @DirtiesContext
    void getLastCandlesDaily_returnsNoFutureCandles_whenThereAreFutureCandles() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 9, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 4))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 5))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 9, 4);
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 9, 23);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

            Assertions.assertEquals(3, candles.size());
            AssertUtils.assertEquals(1, candles.get(0).getClose());
            AssertUtils.assertEquals(2, candles.get(1).getClose());
            AssertUtils.assertEquals(3, candles.get(2).getClose());
        }
    }

    @Test
    @DirtiesContext
    void getLastCandlesDaily_cachesLastDayCandles_whenLastInPast() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 9, 3))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 9, 4);
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2021, 1, 1);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);
        }

        Mockito.reset(marketDataService);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 9, 3))
                .mock();

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

            Assertions.assertEquals(3, candles.size());
            AssertUtils.assertEquals(1, candles.get(0).getClose());
            AssertUtils.assertEquals(2, candles.get(1).getClose());
            AssertUtils.assertEquals(3, candles.get(2).getClose());
        }
    }

    @Test
    @DirtiesContext
    void getLastCandlesDaily_doesNotCacheLastDayCandles_whenLastInToday() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 9, 3))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 9, 4);
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 9, 23);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);
        }

        Mockito.reset(marketDataService);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 9, 3))
                .mock();

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

            Assertions.assertEquals(3, candles.size());
            AssertUtils.assertEquals(4, candles.get(0).getClose());
            AssertUtils.assertEquals(5, candles.get(1).getClose());
            AssertUtils.assertEquals(6, candles.get(2).getClose());
        }
    }

    // endregion

    // region getLastCandles yearly tests

    @Test
    @DirtiesContext
    void getLastCandlesYearly_returnsNoCandles_whenThereAreNoCandles() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final OffsetDateTime currentDateTime = DateTimeTestData.createEndOfDay(2020, 9, 10);

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, CandleInterval.CANDLE_INTERVAL_DAY, currentDateTime);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    @DirtiesContext
    void getLastCandlesYearly_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
        AssertUtils.assertEquals(4, candles.get(2).getClose());
        AssertUtils.assertEquals(5, candles.get(3).getClose());
        AssertUtils.assertEquals(6, candles.get(4).getClose());
    }

    @Test
    @DirtiesContext
    void getLastCandlesYearly_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClose());
        AssertUtils.assertEquals(2, candles.get(1).getClose());
        AssertUtils.assertEquals(3, candles.get(2).getClose());
        AssertUtils.assertEquals(4, candles.get(3).getClose());
        AssertUtils.assertEquals(5, candles.get(4).getClose());
        AssertUtils.assertEquals(6, candles.get(5).getClose());
    }

    @Test
    @DirtiesContext
    void getLastCandlesYearly_returnsPastYearCandles_whenThereAreNoCandlesInCurrentYear() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2019, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2019, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2019, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2019, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2019, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2019, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClose());
        AssertUtils.assertEquals(2, candles.get(1).getClose());
        AssertUtils.assertEquals(3, candles.get(2).getClose());
        AssertUtils.assertEquals(4, candles.get(3).getClose());
        AssertUtils.assertEquals(5, candles.get(4).getClose());
        AssertUtils.assertEquals(6, candles.get(5).getClose());
    }

    @Test
    @DirtiesContext
    void getLastCandlesYearly_returnsNoCandles_whenThereIsEmptyYearAfterCandles() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2018, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2018, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2018, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2018, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2018, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2018, 9, 13))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 10);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    @DirtiesContext
    void getLastCandlesYearly_returnsCandlesOnlyAfterEmptyYear_whenThereEmptyYearBetweenCandles() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2018, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2018, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2018, 9, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 12))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(4, candles.get(0).getClose());
        AssertUtils.assertEquals(5, candles.get(1).getClose());
        AssertUtils.assertEquals(6, candles.get(2).getClose());
    }

    @Test
    @DirtiesContext
    void getLastCandlesYearly_returnsNoFutureCandles_whenThereAreFutureCandles() {
        final TestShare testShare = TestShares.APPLE;
        final String figi = testShare.share().figi();
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 13))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 15))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 16))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 17))
                .mock();

        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 15);

        final List<Candle> candles = extMarketDataService.getLastCandles(figi, limit, candleInterval, currentDateTime);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getClose());
        AssertUtils.assertEquals(2, candles.get(1).getClose());
    }

    // endregion

    // region getMarketCandles tests

    @Test
    @DirtiesContext
    void getMarketCandles_returnsMappedCandles() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final int open1 = 1000;
        final int close1 = 1500;
        final int high1 = 2000;
        final int low1 = 500;
        final OffsetDateTime time1 = from.plusMinutes(1);
        final HistoricCandle historicCandle1 = new HistoricCandleBuilder()
                .setOpen(open1)
                .setClose(close1)
                .setHigh(high1)
                .setLow(low1)
                .setTime(time1)
                .setIsComplete(true)
                .build();

        final int open2 = 1500;
        final int close2 = 2000;
        final int high2 = 2500;
        final int low2 = 1000;
        final OffsetDateTime time2 = from.plusMinutes(2);
        final HistoricCandle historicCandle2 = new HistoricCandleBuilder()
                .setOpen(open2)
                .setClose(close2)
                .setHigh(high2)
                .setLow(low2)
                .setTime(time2)
                .setIsComplete(true)
                .build();

        final int open3 = 2000;
        final int close3 = 2500;
        final int high3 = 3000;
        final int low3 = 500;
        final OffsetDateTime time3 = from.plusMinutes(3);
        final HistoricCandle historicCandle3 = new HistoricCandleBuilder()
                .setOpen(open3)
                .setClose(close3)
                .setHigh(high3)
                .setLow(low3)
                .setTime(time3)
                .setIsComplete(false)
                .build();

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandle1, historicCandle2, historicCandle3)
                .mock();

        final List<Candle> candles = extMarketDataService.getMarketCandles(figi, interval, candleInterval);

        Assertions.assertEquals(2, candles.size());
        final Candle expectedCandle1 = new CandleBuilder()
                .setOpen(open1)
                .setClose(close1)
                .setHigh(high1)
                .setLow(low1)
                .setTime(time1)
                .build();

        final Candle expectedCandle2 = new CandleBuilder()
                .setOpen(open2)
                .setClose(close2)
                .setHigh(high2)
                .setLow(low2)
                .setTime(time2)
                .build();

        Assertions.assertEquals(expectedCandle1, candles.get(0));
        Assertions.assertEquals(expectedCandle2, candles.get(1));
    }

    @Test
    @DirtiesContext
    void getMarketCandles_returnsEmptyList_whenGetsNoCandles() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        new CandleMocker(marketDataService, figi, candleInterval)
                .mock();

        final List<Candle> candles = extMarketDataService.getMarketCandles(figi, interval, candleInterval);

        Assertions.assertTrue(candles.isEmpty());
    }

    // endregion

    @Test
    void getTradingStatus_returnsTradingStatus_whenInstrumentExists() {
        final String figi = TestShares.APPLE.share().figi();

        final SecurityTradingStatus status = SecurityTradingStatus.SECURITY_TRADING_STATUS_OPENING_PERIOD;
        Mocker.mockTradingStatus(marketDataService, figi, status);

        final SecurityTradingStatus result = extMarketDataService.getTradingStatus(figi);

        Assertions.assertEquals(status, result);
    }

    // region convertCurrency tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConvertCurrencyIntoItself() {
        return Stream.of(
                Arguments.of(TestCurrencies.USD),
                Arguments.of(TestCurrencies.RUB)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forConvertCurrencyIntoItself")
    void convertCurrencyIntoItself(final TestCurrency testCurrency) {
        final String currencyIsoName = testCurrency.tinkoffCurrency().getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final BigDecimal actualResult = extMarketDataService.convertCurrency(currencyIsoName, currencyIsoName, sourceValue);

        AssertUtils.assertEquals(sourceValue, actualResult);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConvertCurrency() {
        return Stream.of(
                Arguments.of(TestCurrencies.USD, TestCurrencies.RUB, 97.31, 1, 97310),
                Arguments.of(TestCurrencies.RUB, TestCurrencies.USD, 1, 97.31, 10.276436132),
                Arguments.of(TestCurrencies.USD, TestCurrencies.CNY, 97.31, 13.322, 7304.458789971),
                Arguments.of(TestCurrencies.CNY, TestCurrencies.USD, 13.322, 97.31, 136.90268215)
        );
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency(
            final TestCurrency sourceTestCurrency,
            final TestCurrency targetTestCurrency,
            final double price1,
            final double price2,
            final double expectedResult
    ) {
        final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency = sourceTestCurrency.tinkoffCurrency();
        final ru.tinkoff.piapi.contract.v1.Currency targetCurrency = targetTestCurrency.tinkoffCurrency();

        Mocker.mockAllCurrencies(instrumentsService, sourceCurrency, targetCurrency);

        final Map<String, Double> figiesToPrices = new LinkedHashMap<>();
        figiesToPrices.put(sourceCurrency.getFigi(), price1);
        figiesToPrices.put(targetCurrency.getFigi(), price2);
        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final BigDecimal actualResult1 = extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue);

        AssertUtils.assertEquals(expectedResult, actualResult1);
    }

    @Test
    @DirtiesContext
    void convertCurrency_throwsIllegalArgumentException_whenCurrencyNotFound() {
        final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency = TestCurrencies.USD.tinkoffCurrency();
        final ru.tinkoff.piapi.contract.v1.Currency targetCurrency = TestCurrencies.RUB.tinkoffCurrency();

        Mocker.mockAllCurrencies(instrumentsService, targetCurrency);

        final Map<String, Double> figiesToPrices = new LinkedHashMap<>(2, 1);
        figiesToPrices.put(targetCurrency.getFigi(), 1.0);

        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);

        final Executable executable = () -> extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue);
        final String expectedMessage = "Instrument not found for id " + sourceCurrencyIsoName;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

}