package ru.obukhov.trader.common.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.MarketServiceImpl;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.TickerType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class MarketServiceImplTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    private TradingProperties tradingProperties;
    @Mock
    private TinkoffService tinkoffService;

    private MarketService service;

    @BeforeEach
    public void setUp() {
        this.tradingProperties = new TradingProperties();
        this.tradingProperties.setConsecutiveEmptyDaysLimit(7);
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        mockAnyCandles();

        this.service = new MarketServiceImpl(tradingProperties, tinkoffService);
    }

    // region getCandles tests

    @Test
    void getCandles_skipsCandlesByDays_whenFromIsReached() {
        final CandleResolution candleInterval = CandleResolution._1MIN;
        final String ticker = TICKER;

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 5),
                candleInterval,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 7),
                candleInterval,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 11),
                candleInterval,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 12),
                candleInterval,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 6);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 13);
        List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(BigDecimal.valueOf(0), candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(1), candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(2), candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(3), candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(4), candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(5), candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByDays_whenEmptyDaysLimitIsReached() {
        final CandleResolution candleInterval = CandleResolution._1MIN;
        final String ticker = TICKER;

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 1),
                candleInterval,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 10),
                candleInterval,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 18),
                candleInterval,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 19),
                candleInterval,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 21);
        List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(BigDecimal.valueOf(0), candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(1), candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(2), candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(3), candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(4), candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(5), candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByYears_whenFromIsReached() {
        final CandleResolution candleInterval = CandleResolution.MONTH;
        final String ticker = TICKER;

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2016, 1, 1),
                DateUtils.getDate(2017, 1, 1),
                candleInterval,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2017, 1, 1),
                DateUtils.getDate(2018, 1, 1),
                candleInterval,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2018, 1, 1),
                DateUtils.getDate(2019, 1, 1),
                candleInterval,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2019, 1, 1),
                DateUtils.getDate(2020, 1, 1),
                candleInterval,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2017, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 1);
        List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(BigDecimal.valueOf(0), candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(1), candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(2), candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(3), candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(4), candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(5), candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByYears_whenNoCandles() {
        final CandleResolution candleInterval = CandleResolution.MONTH;
        final String ticker = TICKER;

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2015, 1, 1),
                DateUtils.getDate(2016, 1, 1),
                candleInterval,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2017, 1, 1),
                DateUtils.getDate(2018, 1, 1),
                candleInterval,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2018, 1, 1),
                DateUtils.getDate(2019, 1, 1),
                candleInterval,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2019, 1, 1),
                DateUtils.getDate(2020, 1, 1),
                candleInterval,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2010, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 1);
        List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleInterval);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(BigDecimal.valueOf(0), candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(1), candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(2), candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(3), candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(4), candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(BigDecimal.valueOf(5), candles.get(5).getOpenPrice());
    }

    // endregion

    // region getLastCandle tests

    @Test
    void getLastCandle_throwsIllegalArgumentException_whenNoCandles() {
        final String ticker = TICKER;
        AssertUtils.assertThrowsWithMessage(() -> service.getLastCandle(ticker),
                IllegalArgumentException.class,
                "Not found last candle for ticker '" + ticker + "'");
    }

    @Test
    void getLastCandle_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime from = DateUtils.getLastWorkDay()
                .minusDays(tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        final OffsetDateTime to = from.plusDays(1);

        mockCandlesSimple(ticker, Interval.of(from, to), CandleResolution._1MIN, 10);

        AssertUtils.assertThrowsWithMessage(() -> service.getLastCandle(ticker),
                IllegalArgumentException.class,
                "Not found last candle for ticker '" + ticker + "'");
    }

    @Test
    void getLastCandle_returnsCandle_whenCandleExistsInMaxDayToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime earliestDayToSearch = OffsetDateTime.now()
                .minusDays(tradingProperties.getConsecutiveEmptyDaysLimit());
        final Interval interval = Interval.ofDay(earliestDayToSearch);
        final int openPrice = 10;

        mockCandlesSimple(ticker, interval, CandleResolution._1MIN, openPrice, earliestDayToSearch);

        Candle candle = service.getLastCandle(ticker);

        Assertions.assertNotNull(candle);
        AssertUtils.assertEquals(openPrice, candle.getOpenPrice());
    }

    // endregion

    // region getLastCandle with to tests

    @Test
    void getLastCandleTo_throwsIllegalArgumentException_whenNoCandles() {
        final String ticker = TICKER;
        final OffsetDateTime to = OffsetDateTime.now().minusDays(10);

        AssertUtils.assertThrowsWithMessage(() -> service.getLastCandle(ticker, to),
                IllegalArgumentException.class,
                "Not found last candle for ticker '" + ticker + "'");
    }

    @Test
    void getLastCandleTo_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 10);
        final OffsetDateTime candlesTo = to.minusDays(tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        final OffsetDateTime candlesFrom = candlesTo.minusDays(1);

        mockCandlesSimple(ticker, Interval.of(candlesFrom, candlesTo), CandleResolution._1MIN, 10);

        AssertUtils.assertThrowsWithMessage(() -> service.getLastCandle(ticker, to),
                IllegalArgumentException.class,
                "Not found last candle for ticker '" + ticker + "'");
    }

    @Test
    void getLastCandleTo_returnsCandle_whenCandleExistsInMaxDayToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime to = DateUtils.atEndOfDay(DateUtils.getDate(2020, 1, 10));
        final OffsetDateTime candlesTo = to.minusDays(tradingProperties.getConsecutiveEmptyDaysLimit() - 1);
        final OffsetDateTime candlesFrom = DateUtils.atStartOfDay(candlesTo);
        final int openPrice = 10;

        mockCandlesSimple(ticker, candlesFrom, CandleResolution._1MIN, openPrice);

        Candle candle = service.getLastCandle(ticker, to);

        Assertions.assertNotNull(candle);
        AssertUtils.assertEquals(openPrice, candle.getOpenPrice());
    }

    // endregion

    // region getLastCandles tests

    @Test
    void getLastCandles_returnsNoCandles_whenThereAreNoCandles() {
        final String ticker = TICKER;
        int limit = 5;

        OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateUtils.getDate(2020, 9, 10));
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        List<Candle> candles = service.getLastCandles(ticker, limit);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandles_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() {
        final String ticker = TICKER;
        int limit = 5;

        Interval interval1 = Interval.ofDay(2020, 9, 8);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, CandleResolution._1MIN, prices1, times1);

        Interval interval2 = Interval.ofDay(2020, 9, 9);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, CandleResolution._1MIN, prices2, times2);

        Interval interval3 = Interval.ofDay(2020, 9, 10);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, CandleResolution._1MIN, prices3, times3);

        Mockito.when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateUtils.getDateTime(2020, 9, 10, 2, 0, 0));

        List<Candle> candles = service.getLastCandles(ticker, limit);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(4).getOpenPrice());
    }

    @Test
    void getLastCandles_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() {
        final String ticker = TICKER;
        int limit = 10;

        Interval interval1 = Interval.ofDay(2020, 9, 8);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, CandleResolution._1MIN, prices1, times1);

        Interval interval2 = Interval.ofDay(2020, 9, 9);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, CandleResolution._1MIN, prices2, times2);

        Interval interval3 = Interval.ofDay(2020, 9, 10);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, CandleResolution._1MIN, prices3, times3);

        Mockito.when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateUtils.getDateTime(2020, 9, 10, 2, 0, 0));

        List<Candle> candles = service.getLastCandles(ticker, limit);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(1, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(5).getOpenPrice());
    }

    @Test
    void getLastCandles_returnsNoCandles_whenThereIsBigEmptyIntervalAfterCandles() {
        final String ticker = TICKER;
        int limit = 5;

        Interval interval1 = Interval.ofDay(2020, 9, 8);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, CandleResolution._1MIN, prices1, times1);

        Interval interval2 = Interval.ofDay(2020, 9, 9);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, CandleResolution._1MIN, prices2, times2);

        Interval interval3 = Interval.ofDay(2020, 9, 10);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, CandleResolution._1MIN, prices3, times3);

        OffsetDateTime currentDateTime = interval3.getTo()
                .plusDays(tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        List<Candle> candles = service.getLastCandles(ticker, limit);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandles_returnsCandlesOnlyAfterManyEmptyDays_whenThereIsBigEmptyIntervalBetweenCandles() {
        final String ticker = TICKER;
        int limit = 5;

        Interval interval1 = Interval.ofDay(2020, 9, 1);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, CandleResolution._1MIN, prices1, times1);

        Interval interval2 = Interval.ofDay(2020, 9, 10);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, CandleResolution._1MIN, prices2, times2);

        Interval interval3 = Interval.ofDay(2020, 9, 11);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, CandleResolution._1MIN, prices3, times3);

        OffsetDateTime currentDateTime = interval3.getTo().plusDays(tradingProperties.getConsecutiveEmptyDaysLimit());
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        List<Candle> candles = service.getLastCandles(ticker, limit);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(4, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(2).getOpenPrice());
    }

    // endregion

    // region getInstrument tests

    @Test
    void getInstrument_returnsNull_whenNoMatchingTicker() {
        MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(Arrays.asList(etf1, etf2));

        MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(Arrays.asList(stock1, stock2));

        MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(Arrays.asList(bond1, bond2));

        MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(Arrays.asList(currency1, currency2));

        MarketInstrument marketInstrument = service.getInstrument(TICKER);

        Assertions.assertNull(marketInstrument);
    }

    @Test
    void getInstrument_filtersInstruments() {
        MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(Arrays.asList(etf1, etf2));

        MarketInstrument stock1 = new MarketInstrument().ticker(TICKER);
        MarketInstrument stock2 = new MarketInstrument().ticker(TICKER);
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(Arrays.asList(stock1, stock2));

        MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(Arrays.asList(bond1, bond2));

        MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(Arrays.asList(currency1, currency2));

        MarketInstrument marketInstrument = service.getInstrument(TICKER);

        Assertions.assertSame(stock1, marketInstrument);
    }

    // endregion

    // region getInstruments tests

    @Test
    void getInstruments_returnsEtfs_whenTypeIsEtf() {
        MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(Arrays.asList(etf1, etf2));

        MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(Arrays.asList(stock1, stock2));

        MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(Arrays.asList(bond1, bond2));

        MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(Arrays.asList(currency1, currency2));

        List<MarketInstrument> instruments = service.getInstruments(TickerType.ETF);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(etf1, instruments.get(0));
        Assertions.assertSame(etf2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsStocks_whenTypeIsStock() {
        MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(Arrays.asList(etf1, etf2));

        MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(Arrays.asList(stock1, stock2));

        MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(Arrays.asList(bond1, bond2));

        MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(Arrays.asList(currency1, currency2));

        List<MarketInstrument> instruments = service.getInstruments(TickerType.STOCK);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(stock1, instruments.get(0));
        Assertions.assertSame(stock2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsBonds_whenTypeIsBond() {
        MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(Arrays.asList(etf1, etf2));

        MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(Arrays.asList(stock1, stock2));

        MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(Arrays.asList(bond1, bond2));

        MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(Arrays.asList(currency1, currency2));

        List<MarketInstrument> instruments = service.getInstruments(TickerType.BOND);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(bond1, instruments.get(0));
        Assertions.assertSame(bond2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsCurrencies_whenTypeIsCurrency() {
        MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(Arrays.asList(etf1, etf2));

        MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(Arrays.asList(stock1, stock2));

        MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(Arrays.asList(bond1, bond2));

        MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(Arrays.asList(currency1, currency2));

        List<MarketInstrument> instruments = service.getInstruments(TickerType.CURRENCY);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(currency1, instruments.get(0));
        Assertions.assertSame(currency2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsAllInstruments_whenTypeIsNull() {
        MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(Arrays.asList(etf1, etf2));

        MarketInstrument stock1 = new MarketInstrument().ticker("stock1");
        MarketInstrument stock2 = new MarketInstrument().ticker("stock2");
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(Arrays.asList(stock1, stock2));

        MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(Arrays.asList(bond1, bond2));

        MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(Arrays.asList(currency1, currency2));

        List<MarketInstrument> instruments = service.getInstruments(null);

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

    // region mocks

    private void mockAnyCandles() {
        Mockito.when(
                tinkoffService.getMarketCandles(
                        ArgumentMatchers.eq(TICKER),
                        ArgumentMatchers.any(Interval.class),
                        ArgumentMatchers.any(CandleResolution.class)
                )
        ).thenReturn(Collections.emptyList());
    }

    private void mockCandlesSimple(String ticker,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleResolution candleInterval,
                                   Integer... openPrices) {

        mockCandlesSimple(ticker, Interval.of(from, to), candleInterval, openPrices);

    }

    private void mockCandlesSimple(String ticker,
                                   Interval interval,
                                   CandleResolution candleInterval,
                                   Integer... openPrices) {

        List<Integer> prices = Arrays.asList(openPrices);
        List<OffsetDateTime> times = Collections.nCopies(openPrices.length, interval.getFrom());

        mockCandlesSimple(ticker, interval, candleInterval, prices, times);

    }

    private void mockCandlesSimple(String ticker,
                                   OffsetDateTime date,
                                   CandleResolution candleInterval,
                                   Integer... openPrices) {

        Interval interval = Interval.of(date, date).extendToWholeDay(false);

        List<Integer> prices = Arrays.asList(openPrices);
        List<OffsetDateTime> times = Collections.nCopies(openPrices.length, interval.getFrom());

        mockCandlesSimple(ticker, interval, candleInterval, prices, times);

    }

    private void mockCandlesSimple(String ticker,
                                   Interval interval,
                                   CandleResolution candleInterval,
                                   Integer openPrice,
                                   OffsetDateTime time) {

        final List<Integer> openPrices = Collections.singletonList(openPrice);
        final List<OffsetDateTime> times = Collections.singletonList(time);

        mockCandlesSimple(ticker, interval, candleInterval, openPrices, times);

    }

    private void mockCandlesSimple(String ticker,
                                   Interval interval,
                                   CandleResolution candleInterval,
                                   List<Integer> openPrices,
                                   List<OffsetDateTime> times) {

        List<Candle> candles = createCandlesSimple(openPrices, times);

        Mockito.when(tinkoffService.getMarketCandles(ticker, interval, candleInterval)).thenReturn(candles);

    }

    private List<Candle> createCandlesSimple(List<Integer> openPrices, List<OffsetDateTime> times) {
        Assertions.assertEquals(times.size(), openPrices.size(), "times and openPrices must have same size");

        List<Candle> candles = new ArrayList<>(openPrices.size());
        for (int i = 0; i < openPrices.size(); i++) {
            candles.add(TestDataHelper.createCandleWithOpenPriceAndTime(openPrices.get(i), times.get(i)));
        }

        return candles;
    }

    // endregion

}