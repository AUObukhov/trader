package ru.obukhov.trader.market.impl;

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
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.TickerType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class MarketServiceImplUnitTest extends BaseMockedTest {

    private TradingProperties tradingProperties;
    @Mock
    private TinkoffService tinkoffService;

    private MarketServiceImpl service;

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
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final String ticker = "ticker";

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 5),
                candleResolution,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 7),
                candleResolution,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 11),
                candleResolution,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 12),
                candleResolution,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 6);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 13);
        final List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleResolution);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByDays_whenEmptyDaysLimitIsReached() {
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final String ticker = "ticker";

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 1),
                candleResolution,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 10),
                candleResolution,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 18),
                candleResolution,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2020, 1, 19),
                candleResolution,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2020, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 21);
        final List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleResolution);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByYears_whenFromIsReached() {
        final CandleResolution candleResolution = CandleResolution.MONTH;
        final String ticker = "ticker";

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2016, 1, 1),
                DateUtils.getDate(2017, 1, 1),
                candleResolution,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2017, 1, 1),
                DateUtils.getDate(2018, 1, 1),
                candleResolution,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2018, 1, 1),
                DateUtils.getDate(2019, 1, 1),
                candleResolution,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2019, 1, 1),
                DateUtils.getDate(2020, 1, 1),
                candleResolution,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2017, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 1);
        final List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleResolution);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesByYears_whenNoCandlesForOneYear() {
        final CandleResolution candleResolution = CandleResolution.MONTH;
        final String ticker = "ticker";

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2015, 1, 1),
                DateUtils.getDate(2016, 1, 1),
                candleResolution,
                10
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2017, 1, 1),
                DateUtils.getDate(2018, 1, 1),
                candleResolution,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2018, 1, 1),
                DateUtils.getDate(2019, 1, 1),
                candleResolution,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2019, 1, 1),
                DateUtils.getDate(2020, 1, 1),
                candleResolution,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2010, 1, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 1);
        final List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleResolution);

        Assertions.assertEquals(6, candles.size());
        AssertUtils.assertEquals(0, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(1, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(2, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(4).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(5).getOpenPrice());
    }

    @Test
    void getCandles_skipsCandlesBeforeFromByYears_whenFromInTheMiddleOfYear() {
        final CandleResolution candleResolution = CandleResolution.MONTH;
        final String ticker = "ticker";

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2017, 1, 1),
                DateUtils.getDate(2018, 1, 1),
                candleResolution,
                0, 1, 2
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2018, 1, 1),
                DateUtils.getDate(2019, 1, 1),
                candleResolution,
                3, 4
        );

        mockCandlesSimple(
                ticker,
                DateUtils.getDate(2019, 1, 1),
                DateUtils.getDate(2020, 1, 1),
                candleResolution,
                5
        );

        final OffsetDateTime from = DateUtils.getDate(2017, 4, 1);
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 1);
        final List<Candle> candles = service.getCandles(ticker, Interval.of(from, to), candleResolution);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(3, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(2).getOpenPrice());
    }

    // endregion

    // region getLastCandle tests

    @Test
    void getLastCandle_throwsIllegalArgumentException_whenNoCandles() {
        final String ticker = "ticker";
        AssertUtils.assertThrowsWithMessage(() -> service.getLastCandle(ticker),
                IllegalArgumentException.class,
                "Not found last candle for ticker '" + ticker + "'");
    }

    @Test
    void getLastCandle_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String ticker = "ticker";
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
        final String ticker = "ticker";
        final OffsetDateTime earliestDayToSearch = OffsetDateTime.now()
                .minusDays(tradingProperties.getConsecutiveEmptyDaysLimit());
        final Interval interval = Interval.ofDay(earliestDayToSearch);
        final int openPrice = 10;

        mockCandlesSimple(ticker, interval, CandleResolution._1MIN, openPrice, earliestDayToSearch);

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

        AssertUtils.assertThrowsWithMessage(() -> service.getLastCandle(ticker, to),
                IllegalArgumentException.class,
                "Not found last candle for ticker '" + ticker + "'");
    }

    @Test
    void getLastCandleTo_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String ticker = "ticker";
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
        final String ticker = "ticker";
        final OffsetDateTime to = DateUtils.atEndOfDay(DateUtils.getDate(2020, 1, 10));
        final OffsetDateTime candlesTo = to.minusDays(tradingProperties.getConsecutiveEmptyDaysLimit() - 1);
        final OffsetDateTime candlesFrom = DateUtils.atStartOfDay(candlesTo);
        final int openPrice = 10;

        mockCandlesSimple(ticker, candlesFrom, CandleResolution._1MIN, openPrice);

        final Candle candle = service.getLastCandle(ticker, to);

        Assertions.assertNotNull(candle);
        AssertUtils.assertEquals(openPrice, candle.getOpenPrice());
    }

    // endregion

    // region getLastCandles tests

    @Test
    void getLastCandles_returnsNoCandles_whenThereAreNoCandles() {
        final String ticker = "ticker";
        final int limit = 5;

        final OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateUtils.getDate(2020, 9, 10));
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        final List<Candle> candles = service.getLastCandles(ticker, limit, CandleResolution._1MIN);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandles_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final Interval interval1 = Interval.ofDay(2020, 9, 8);
        final List<Integer> prices1 = List.of(1, 2, 3);
        final List<OffsetDateTime> times1 = List.of(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, candleResolution, prices1, times1);

        final Interval interval2 = Interval.ofDay(2020, 9, 9);
        final List<Integer> prices2 = List.of(4, 5);
        final List<OffsetDateTime> times2 = List.of(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, candleResolution, prices2, times2);

        final Interval interval3 = Interval.ofDay(2020, 9, 10);
        final List<Integer> prices3 = List.of(6);
        final List<OffsetDateTime> times3 = List.of(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, candleResolution, prices3, times3);

        Mockito.when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateUtils.getDateTime(2020, 9, 10, 2, 0, 0));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleResolution);

        Assertions.assertEquals(limit, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(3, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(4, candles.get(2).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(3).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(4).getOpenPrice());
    }

    @Test
    void getLastCandles_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() {
        final String ticker = "ticker";
        final int limit = 10;
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final Interval interval1 = Interval.ofDay(2020, 9, 8);
        final List<Integer> prices1 = List.of(1, 2, 3);
        final List<OffsetDateTime> times1 = List.of(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, candleResolution, prices1, times1);

        final Interval interval2 = Interval.ofDay(2020, 9, 9);
        final List<Integer> prices2 = List.of(4, 5);
        final List<OffsetDateTime> times2 = List.of(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, candleResolution, prices2, times2);

        final Interval interval3 = Interval.ofDay(2020, 9, 10);
        final List<Integer> prices3 = List.of(6);
        final List<OffsetDateTime> times3 = List.of(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, candleResolution, prices3, times3);

        Mockito.when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateUtils.getDateTime(2020, 9, 10, 2, 0, 0));

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleResolution);

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
        final String ticker = "ticker";
        final int limit = 5;
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final Interval interval1 = Interval.ofDay(2020, 9, 8);
        final List<Integer> prices1 = List.of(1, 2, 3);
        final List<OffsetDateTime> times1 = List.of(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, candleResolution, prices1, times1);

        final Interval interval2 = Interval.ofDay(2020, 9, 9);
        final List<Integer> prices2 = List.of(4, 5);
        final List<OffsetDateTime> times2 = List.of(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, candleResolution, prices2, times2);

        final Interval interval3 = Interval.ofDay(2020, 9, 10);
        final List<Integer> prices3 = List.of(6);
        final List<OffsetDateTime> times3 = List.of(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, candleResolution, prices3, times3);

        final OffsetDateTime currentDateTime = interval3.getTo()
                .plusDays(tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleResolution);

        Assertions.assertTrue(candles.isEmpty());
    }

    @Test
    void getLastCandles_returnsCandlesOnlyAfterManyEmptyDays_whenThereIsBigEmptyIntervalBetweenCandles() {
        final String ticker = "ticker";
        final int limit = 5;
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final Interval interval1 = Interval.ofDay(2020, 9, 1);
        final List<Integer> prices1 = List.of(1, 2, 3);
        final List<OffsetDateTime> times1 = List.of(
                interval1.getFrom().withHour(1),
                interval1.getFrom().withHour(2),
                interval1.getFrom().withHour(3));
        mockCandlesSimple(ticker, interval1, candleResolution, prices1, times1);

        final Interval interval2 = Interval.ofDay(2020, 9, 10);
        final List<Integer> prices2 = List.of(4, 5);
        final List<OffsetDateTime> times2 = List.of(interval2.getFrom().withHour(1), interval2.getFrom().withHour(2));
        mockCandlesSimple(ticker, interval2, candleResolution, prices2, times2);

        final Interval interval3 = Interval.ofDay(2020, 9, 11);
        final List<Integer> prices3 = List.of(6);
        final List<OffsetDateTime> times3 = List.of(interval3.getFrom().withHour(1));
        mockCandlesSimple(ticker, interval3, candleResolution, prices3, times3);

        final OffsetDateTime currentDateTime = interval3.getTo().plusDays(tradingProperties.getConsecutiveEmptyDaysLimit());
        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        final List<Candle> candles = service.getLastCandles(ticker, limit, candleResolution);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(4, candles.get(0).getOpenPrice());
        AssertUtils.assertEquals(5, candles.get(1).getOpenPrice());
        AssertUtils.assertEquals(6, candles.get(2).getOpenPrice());
    }

    // endregion

    // region getInstrument tests

    @Test
    void getInstrument_returnsNull_whenNoMatchingTicker() {
        final String ticker = "ticker";

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

        final MarketInstrument marketInstrument = service.getInstrument(ticker);

        Assertions.assertNull(marketInstrument);
    }

    @Test
    void getInstrument_filtersInstruments() {
        final String ticker = "ticker";

        final MarketInstrument etf1 = new MarketInstrument().ticker("etf1");
        final MarketInstrument etf2 = new MarketInstrument().ticker("etf2");
        Mockito.when(tinkoffService.getMarketEtfs()).thenReturn(List.of(etf1, etf2));

        final MarketInstrument stock1 = new MarketInstrument().ticker(ticker);
        final MarketInstrument stock2 = new MarketInstrument().ticker(ticker);
        Mockito.when(tinkoffService.getMarketStocks()).thenReturn(List.of(stock1, stock2));

        final MarketInstrument bond1 = new MarketInstrument().ticker("bond1");
        final MarketInstrument bond2 = new MarketInstrument().ticker("bond2");
        Mockito.when(tinkoffService.getMarketBonds()).thenReturn(List.of(bond1, bond2));

        final MarketInstrument currency1 = new MarketInstrument().ticker("currency1");
        final MarketInstrument currency2 = new MarketInstrument().ticker("currency2");
        Mockito.when(tinkoffService.getMarketCurrencies()).thenReturn(List.of(currency1, currency2));

        final MarketInstrument marketInstrument = service.getInstrument(ticker);

        Assertions.assertSame(stock1, marketInstrument);
    }

    // endregion

    // region getInstruments tests

    @Test
    void getInstruments_returnsEtfs_whenTypeIsEtf() {
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

        final List<MarketInstrument> instruments = service.getInstruments(TickerType.ETF);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(etf1, instruments.get(0));
        Assertions.assertSame(etf2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsStocks_whenTypeIsStock() {
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

        final List<MarketInstrument> instruments = service.getInstruments(TickerType.STOCK);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(stock1, instruments.get(0));
        Assertions.assertSame(stock2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsBonds_whenTypeIsBond() {
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

        final List<MarketInstrument> instruments = service.getInstruments(TickerType.BOND);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(bond1, instruments.get(0));
        Assertions.assertSame(bond2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsCurrencies_whenTypeIsCurrency() {
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

        final List<MarketInstrument> instruments = service.getInstruments(TickerType.CURRENCY);

        Assertions.assertEquals(2, instruments.size());
        Assertions.assertSame(currency1, instruments.get(0));
        Assertions.assertSame(currency2, instruments.get(1));
    }

    @Test
    void getInstruments_returnsAllInstruments_whenTypeIsNull() {
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

    // region mocks

    private void mockAnyCandles() {
        final String ticker = "ticker";

        Mockito.when(
                tinkoffService.getMarketCandles(
                        ArgumentMatchers.eq(ticker),
                        ArgumentMatchers.any(Interval.class),
                        ArgumentMatchers.any(CandleResolution.class)
                )
        ).thenReturn(Collections.emptyList());
    }

    private void mockCandlesSimple(
            String ticker,
            OffsetDateTime from,
            OffsetDateTime to,
            CandleResolution candleResolution,
            Integer... openPrices
    ) {
        mockCandlesSimple(ticker, Interval.of(from, to), candleResolution, openPrices);
    }

    private void mockCandlesSimple(
            String ticker,
            Interval interval,
            CandleResolution candleResolution,
            Integer... openPrices
    ) {
        final List<Integer> prices = List.of(openPrices);
        final List<OffsetDateTime> times = Collections.nCopies(openPrices.length, interval.getFrom());

        mockCandlesSimple(ticker, interval, candleResolution, prices, times);
    }

    private void mockCandlesSimple(
            String ticker,
            OffsetDateTime date,
            CandleResolution candleResolution,
            Integer... openPrices
    ) {
        final Interval interval = Interval.of(date, date).extendToWholeDay(false);

        final List<Integer> prices = List.of(openPrices);
        final List<OffsetDateTime> times = Collections.nCopies(openPrices.length, interval.getFrom());

        mockCandlesSimple(ticker, interval, candleResolution, prices, times);
    }

    private void mockCandlesSimple(
            String ticker,
            Interval interval,
            CandleResolution candleResolution,
            Integer openPrice,
            OffsetDateTime time
    ) {
        final List<Integer> openPrices = List.of(openPrice);
        final List<OffsetDateTime> times = List.of(time);

        mockCandlesSimple(ticker, interval, candleResolution, openPrices, times);
    }

    private void mockCandlesSimple(
            String ticker,
            Interval interval,
            CandleResolution candleResolution,
            List<Integer> openPrices,
            List<OffsetDateTime> times
    ) {
        final List<Candle> candles = createCandlesSimple(openPrices, times);

        Mockito.when(tinkoffService.getMarketCandles(ticker, interval, candleResolution)).thenReturn(candles);
    }

    private List<Candle> createCandlesSimple(List<Integer> openPrices, List<OffsetDateTime> times) {
        Assertions.assertEquals(times.size(), openPrices.size(), "times and openPrices must have same size");

        final List<Candle> candles = new ArrayList<>(openPrices.size());
        for (int i = 0; i < openPrices.size(); i++) {
            candles.add(TestDataHelper.createCandleWithOpenPriceAndTime(openPrices.get(i), times.get(i)));
        }

        return candles;
    }

    // endregion

}