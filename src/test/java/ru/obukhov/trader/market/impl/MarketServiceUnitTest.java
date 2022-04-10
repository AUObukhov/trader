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
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.DateTestUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class MarketServiceUnitTest {

    private static final MarketProperties MARKET_PROPERTIES = TestData.createMarketProperties();

    @Mock
    private TinkoffService tinkoffService;

    private MarketService service;

    @BeforeEach
    public void setUpEach() {
        this.service = new MarketService(MARKET_PROPERTIES, tinkoffService);
    }

    // region getCandles tests

    @Test
    void getCandles_skipsCandlesByDays_whenFromIsReached() throws IOException {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final CandleInterval candleInterval = CandleInterval._1MIN;
        final String ticker = "ticker";

        new CandleMocker(tinkoffService, ticker, candleInterval)
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
        final List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByDays_whenEmptyDaysLimitIsReached() throws IOException {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final CandleInterval candleInterval = CandleInterval._1MIN;
        final String ticker = "ticker";

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(10, DateTimeTestData.createDateTime(2020, 1, 1))
                .add(0, DateTimeTestData.createDateTime(2020, 1, 10))
                .add(1, DateTimeTestData.createDateTime(2020, 1, 11))
                .add(2, DateTimeTestData.createDateTime(2020, 1, 12))
                .add(3, DateTimeTestData.createDateTime(2020, 1, 20))
                .add(4, DateTimeTestData.createDateTime(2020, 1, 21))
                .add(5, DateTimeTestData.createDateTime(2020, 1, 22))
                .mock();

        final OffsetDateTime from = DateTimeTestData.createDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 1, 23);
        final List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_filterCandlesByYears() throws IOException {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final CandleInterval candleInterval = CandleInterval.DAY;
        final String ticker = "ticker";

        new CandleMocker(tinkoffService, ticker, candleInterval)
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
        final List<Candle> candles = service.getCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(5, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByYears_whenFromIsReached() throws IOException {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final CandleInterval candleInterval = CandleInterval.MONTH;
        final String ticker = "ticker";

        new CandleMocker(tinkoffService, ticker, candleInterval)
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
        final List<Candle> candles = service.getCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByYears_whenNoCandlesForOneYear() throws IOException {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final CandleInterval candleInterval = CandleInterval.MONTH;
        final String ticker = "ticker";

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(10, DateTimeTestData.createDateTime(2015, 1, 1, 1))
                .add(0, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(1, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(2, DateTimeTestData.createDateTime(2017, 1, 1, 1))
                .add(3, DateTimeTestData.createDateTime(2018, 1, 1, 1))
                .add(4, DateTimeTestData.createDateTime(2018, 1, 1, 1))
                .add(5, DateTimeTestData.createDateTime(2019, 1, 1, 1))
                .mock();

        final Interval interval = Interval.of(
                DateTimeTestData.createDateTime(2010, 1, 1),
                DateTimeTestData.createDateTime(2020, 1, 1)
        );
        final List<Candle> candles = service.getCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesBeforeFromByYears_whenFromInTheMiddleOfYear() throws IOException {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final CandleInterval candleInterval = CandleInterval.MONTH;
        final String ticker = "ticker";

        new CandleMocker(tinkoffService, ticker, candleInterval)
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
        final List<Candle> candles = service.getCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(3, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(2).getOpenPrice());
    }

    // endregion

    // region getLastCandle tests

    @Test
    void getLastCandle_throwsIllegalArgumentException_whenNoCandles() {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final String ticker = "ticker";

        final Executable executable = () -> service.getLastCandle(ticker);
        final String expectedMessage = "Not found last candle for ticker '" + ticker + "'";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getLastCandle_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() throws IOException {
        final OffsetDateTime now = OffsetDateTime.now();
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(now);

        final String ticker = "ticker";
        final OffsetDateTime from = DateTestUtils.getLastWorkDay(now).minusDays(MARKET_PROPERTIES.getConsecutiveEmptyDaysLimit() + 1);

        new CandleMocker(tinkoffService, ticker, CandleInterval._1MIN)
                .add(10, from)
                .mock();

        final Executable executable = () -> service.getLastCandle(ticker);
        final String expectedMessage = "Not found last candle for ticker '" + ticker + "'";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getLastCandle_returnsCandle_whenCandleExistsInMaxDayToSearch() throws IOException {
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final String ticker = "ticker";
        final OffsetDateTime earliestDayToSearch = OffsetDateTime.now().minusDays(MARKET_PROPERTIES.getConsecutiveEmptyDaysLimit());
        final int openPrice = 10;

        new CandleMocker(tinkoffService, ticker, CandleInterval._1MIN)
                .add(openPrice, earliestDayToSearch)
                .mock();

        final Candle candle = service.getLastCandle(ticker);

        Assertions.assertNotNull(candle);
        AssertUtils.assertEquals(openPrice, candle.getOpenPrice());
    }

    // endregion

    // region getLastCandle with to tests

    @Test
    void getLastCandleTo_throwsIllegalArgumentException_whenNoCandles() {
        final String ticker = "ticker";
        final OffsetDateTime to = OffsetDateTime.now().minusDays(10);

        final Executable executable = () -> service.getLastCandle(ticker, to);
        final String expectedMessage = "Not found last candle for ticker '" + ticker + "'";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getLastCandleTo_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() throws IOException {
        final String ticker = "ticker";
        final OffsetDateTime to = DateTimeTestData.createDateTime(2020, 1, 10);
        final OffsetDateTime candlesTo = to.minusDays(MARKET_PROPERTIES.getConsecutiveEmptyDaysLimit() + 1);
        final OffsetDateTime candlesFrom = candlesTo.minusDays(1);

        new CandleMocker(tinkoffService, ticker, CandleInterval._1MIN)
                .add(10, candlesFrom)
                .mock();

        final Executable executable = () -> service.getLastCandle(ticker, to);
        final String expectedMessage = "Not found last candle for ticker '" + ticker + "'";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getLastCandleTo_returnsCandle_whenCandleExistsInMaxDayToSearch() throws IOException {
        final String ticker = "ticker";
        final OffsetDateTime to = DateUtils.atEndOfDay(DateTimeTestData.createDateTime(2020, 1, 10));
        final OffsetDateTime candlesTo = to.minusDays(MARKET_PROPERTIES.getConsecutiveEmptyDaysLimit() - 1);
        final OffsetDateTime candlesFrom = DateUtils.atStartOfDay(candlesTo);
        final int openPrice = 10;

        new CandleMocker(tinkoffService, ticker, CandleInterval._1MIN)
                .add(openPrice, candlesFrom)
                .mock();

        final Candle candle = service.getLastCandle(ticker, to);

        Assertions.assertNotNull(candle);
        AssertUtils.assertEquals(openPrice, candle.getOpenPrice());
    }

    // endregion

    // region getLastCandles daily tests

    @Test
    void getLastCandlesDaily_returnsNoCandles_whenThereAreNoCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;

        final OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateTimeTestData.createDateTime(2020, 9, 10));
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        final List<Candle> candles = service.getLastCandles(ticker, limit, CandleInterval._1MIN);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandlesDaily_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval._1MIN;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 8, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 8, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateTimeTestData.createDateTime(2020, 9, 10, 2));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(4).getOpenPrice());
    }

    @Test
    void getLastCandlesDaily_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() throws IOException {
        final String ticker = "ticker";
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval._1MIN;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 8, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 8, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateTimeTestData.createDateTime(2020, 9, 10, 2));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(5).getOpenPrice());
    }

    @Test
    void getLastCandlesDaily_returnsNoCandles_whenThereIsBigEmptyIntervalAfterCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval._1MIN;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 8, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 8, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .mock();

        final OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateTimeTestData.createDateTime(2020, 9, 10))
                .plusDays(MARKET_PROPERTIES.getConsecutiveEmptyDaysLimit() + 1);
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandlesDaily_returnsCandlesOnlyAfterManyEmptyDays_whenThereIsBigEmptyIntervalBetweenCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval._1MIN;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 1, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 1, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 1, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 10, 1))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 10, 2))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 11, 1))
                .mock();

        final OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateTimeTestData.createDateTime(2020, 9, 11))
                .plusDays(MARKET_PROPERTIES.getConsecutiveEmptyDaysLimit());
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(4, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(2).getOpenPrice());
    }

    @Test
    void getLastCandlesDaily_returnsNoFutureCandles_whenThereAreFutureCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval._1MIN;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 9, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 9, 4))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 9, 5))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateTimeTestData.createDateTime(2020, 9, 9, 4));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(2).getOpenPrice());
    }

    // endregion

    // region getLastCandles yearly tests

    @Test
    void getLastCandlesYearly_returnsNoCandles_whenThereAreNoCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;

        final OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateTimeTestData.createDateTime(2020, 9, 10));
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        final List<Candle> candles = service.getLastCandles(ticker, limit, CandleInterval.DAY);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandlesYearly_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.DAY;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 13))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(DateTimeTestData.createDateTime(2020, 9, 15));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(4).getOpenPrice());
    }

    @Test
    void getLastCandlesYearly_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() throws IOException {
        final String ticker = "ticker";
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.DAY;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 13))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(DateTimeTestData.createDateTime(2020, 9, 15));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(5).getOpenPrice());
    }

    @Test
    void getLastCandlesYearly_returnsPastYearCandles_whenThereAreNoCandlesInCurrentYear() throws IOException {
        final String ticker = "ticker";
        final int limit = 10;
        final CandleInterval candleInterval = CandleInterval.DAY;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2019, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2019, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2019, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2019, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2019, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2019, 9, 13))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(DateTimeTestData.createDateTime(2020, 9, 10));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(5).getOpenPrice());
    }

    @Test
    void getLastCandlesYearly_returnsNoCandles_whenThereIsEmptyYearAfterCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.DAY;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2018, 9, 8))
                .add(2, DateTimeTestData.createDateTime(2018, 9, 9))
                .add(3, DateTimeTestData.createDateTime(2018, 9, 10))
                .add(4, DateTimeTestData.createDateTime(2018, 9, 11))
                .add(5, DateTimeTestData.createDateTime(2018, 9, 12))
                .add(6, DateTimeTestData.createDateTime(2018, 9, 13))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(DateTimeTestData.createDateTime(2020, 9, 10));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandlesYearly_returnsCandlesOnlyAfterEmptyYear_whenThereEmptyYearBetweenCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.DAY;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2018, 9, 1))
                .add(2, DateTimeTestData.createDateTime(2018, 9, 2))
                .add(3, DateTimeTestData.createDateTime(2018, 9, 3))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 10))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 11))
                .add(6, DateTimeTestData.createDateTime(2020, 9, 12))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(DateTimeTestData.createDateTime(2020, 9, 15));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(4, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(2).getOpenPrice());
    }

    @Test
    void getLastCandlesYearly_returnsNoFutureCandles_whenThereAreFutureCandles() throws IOException {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleInterval candleInterval = CandleInterval.DAY;

        new CandleMocker(tinkoffService, ticker, candleInterval)
                .add(1, DateTimeTestData.createDateTime(2020, 9, 12))
                .add(2, DateTimeTestData.createDateTime(2020, 9, 13))
                .add(3, DateTimeTestData.createDateTime(2020, 9, 15))
                .add(4, DateTimeTestData.createDateTime(2020, 9, 16))
                .add(5, DateTimeTestData.createDateTime(2020, 9, 17))
                .mock();

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(DateTimeTestData.createDateTime(2020, 9, 15));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(1).getOpenPrice());
    }

    // endregion

    // region getInstrument tests

    @Test
    void getInstrument_returnsInstrument() throws IOException {
        final String ticker = "ticker";

        final MarketInstrument instrument = new MarketInstrument().ticker(ticker);
        Mockito.when(tinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        final MarketInstrument returnedInstrument = service.getInstrument(ticker);

        Assertions.assertSame(instrument, returnedInstrument);
    }

    // endregion

    // region getInstruments tests

    @Test
    void getInstruments_returnsEtfs_whenTypeIsEtf() throws IOException {
        final MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        final MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(List.of(etf1, etf2));

        final List<MarketInstrument> instruments = service.getInstruments(InstrumentType.ETF);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(etf1, instruments.get(0));
        Assertions.assertSame(etf2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsStocks_whenTypeIsStock() throws IOException {
        final MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        final MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(List.of(stock1, stock2));

        final List<MarketInstrument> instruments = service.getInstruments(InstrumentType.STOCK);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(stock1, instruments.get(0));
        Assertions.assertSame(stock2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsBonds_whenTypeIsBond() throws IOException {
        final MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        final MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(List.of(bond1, bond2));

        final List<MarketInstrument> instruments = service.getInstruments(InstrumentType.BOND);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(bond1, instruments.get(0));
        Assertions.assertSame(bond2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsCurrencies_whenTypeIsCurrency() throws IOException {
        final MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        final MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(List.of(currency1, currency2));

        final List<MarketInstrument> instruments = service.getInstruments(InstrumentType.CURRENCY);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(currency1, instruments.get(0));
        Assertions.assertSame(currency2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsAllInstruments_whenTypeIsNull() throws IOException {
        final MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        final MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(List.of(etf1, etf2));

        final MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        final MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(List.of(stock1, stock2));

        final MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        final MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(List.of(bond1, bond2));

        final MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        final MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(List.of(currency1, currency2));

        final List<MarketInstrument> instruments = service.getInstruments(null);

        Assertions.assertEquals(8, instruments.size());
        Assertions.assertSame(etf1, instruments.get(0));
        Assertions.assertSame(etf2, instruments.get(1));
        Assertions.assertSame(stock1, instruments.get(2));
        Assertions.assertSame(stock2, instruments.get(3));
        Assertions.assertSame(bond1, instruments.get(4));
        Assertions.assertSame(bond2, instruments.get(5));
        Assertions.assertSame(currency1, instruments.get(6));
        Assertions.assertSame(currency2, instruments.get(7));
    }

    // endregion

}