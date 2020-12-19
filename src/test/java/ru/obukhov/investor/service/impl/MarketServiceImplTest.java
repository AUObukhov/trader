package ru.obukhov.investor.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.Assert;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.obukhov.investor.util.DateUtils.getDate;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

@RunWith(MockitoJUnitRunner.class)
public class MarketServiceImplTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    private TradingProperties tradingProperties;
    @Mock
    private TinkoffService tinkoffService;

    private MarketService service;

    @Before
    public void setUp() {
        this.tradingProperties = new TradingProperties();
        this.tradingProperties.setConsecutiveEmptyDaysLimit(7);
        when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        mockAnyCandles();

        this.service = new MarketServiceImpl(tradingProperties, tinkoffService);
    }

    // region getCandles tests

    @Test
    public void getCandles_skipsCandlesByDays_whenFromIsReached() {
        final CandleInterval candleInterval = CandleInterval.ONE_MIN;
        final String ticker = TICKER;

        mockCandlesSimple(ticker,
                getDate(2020, 1, 5),
                candleInterval,
                10);

        mockCandlesSimple(ticker,
                getDate(2020, 1, 7),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(ticker,
                getDate(2020, 1, 11),
                candleInterval,
                3, 4);

        mockCandlesSimple(ticker,
                getDate(2020, 1, 12),
                candleInterval,
                5);

        final OffsetDateTime from = getDate(2020, 1, 6);
        final OffsetDateTime to = getDate(2020, 1, 13);
        List<Candle> candles = service.getCandles(ticker, from, to, candleInterval);

        assertEquals(6, candles.size());
        assertTrue(numbersEqual(BigDecimal.valueOf(0), candles.get(0).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(1), candles.get(1).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(2), candles.get(2).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(3), candles.get(3).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(4), candles.get(4).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(5), candles.get(5).getOpenPrice()));
    }

    @Test
    public void getCandles_skipsCandlesByDays_whenEmptyDaysLimitIsReached() {
        final CandleInterval candleInterval = CandleInterval.ONE_MIN;
        final String ticker = TICKER;

        mockCandlesSimple(ticker,
                getDate(2020, 1, 1),
                candleInterval,
                10);

        mockCandlesSimple(ticker,
                getDate(2020, 1, 10),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(ticker,
                getDate(2020, 1, 18),
                candleInterval,
                3, 4);

        mockCandlesSimple(ticker,
                getDate(2020, 1, 19),
                candleInterval,
                5);

        final OffsetDateTime from = getDate(2020, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 21);
        List<Candle> candles = service.getCandles(ticker, from, to, candleInterval);

        assertEquals(6, candles.size());
        assertTrue(numbersEqual(BigDecimal.valueOf(0), candles.get(0).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(1), candles.get(1).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(2), candles.get(2).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(3), candles.get(3).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(4), candles.get(4).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(5), candles.get(5).getOpenPrice()));
    }

    @Test
    public void getCandles_skipsCandlesByYears_whenFromIsReached() {
        final CandleInterval candleInterval = CandleInterval.MONTH;
        final String ticker = TICKER;

        mockCandlesSimple(ticker,
                getDate(2016, 1, 1),
                getDate(2017, 1, 1),
                candleInterval,
                10);

        mockCandlesSimple(ticker,
                getDate(2017, 1, 1),
                getDate(2018, 1, 1),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(ticker,
                getDate(2018, 1, 1),
                getDate(2019, 1, 1),
                candleInterval,
                3, 4);

        mockCandlesSimple(ticker,
                getDate(2019, 1, 1),
                getDate(2020, 1, 1),
                candleInterval,
                5);

        final OffsetDateTime from = getDate(2017, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 1);
        List<Candle> candles = service.getCandles(ticker, from, to, candleInterval);

        assertEquals(6, candles.size());
        assertTrue(numbersEqual(BigDecimal.valueOf(0), candles.get(0).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(1), candles.get(1).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(2), candles.get(2).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(3), candles.get(3).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(4), candles.get(4).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(5), candles.get(5).getOpenPrice()));
    }

    @Test
    public void getCandles_skipsCandlesByYears_whenNoCandles() {
        final CandleInterval candleInterval = CandleInterval.MONTH;
        final String ticker = TICKER;

        mockCandlesSimple(ticker,
                getDate(2015, 1, 1),
                getDate(2016, 1, 1),
                candleInterval,
                10);

        mockCandlesSimple(ticker,
                getDate(2017, 1, 1),
                getDate(2018, 1, 1),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(ticker,
                getDate(2018, 1, 1),
                getDate(2019, 1, 1),
                candleInterval,
                3, 4);

        mockCandlesSimple(ticker,
                getDate(2019, 1, 1),
                getDate(2020, 1, 1),
                candleInterval,
                5);

        final OffsetDateTime from = getDate(2010, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 1);
        List<Candle> candles = service.getCandles(ticker, from, to, candleInterval);

        assertEquals(6, candles.size());
        assertTrue(numbersEqual(BigDecimal.valueOf(0), candles.get(0).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(1), candles.get(1).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(2), candles.get(2).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(3), candles.get(3).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(4), candles.get(4).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(5), candles.get(5).getOpenPrice()));
    }

    // endregion

    // region getLastCandle tests

    @Test(expected = IllegalArgumentException.class)
    public void getLastCandle_throwsIllegalArgumentException_whenNoCandles() {
        service.getLastCandle(TICKER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLastCandle_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime from = DateUtils.getLastWorkDay()
                .minusDays(tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        final OffsetDateTime to = from.plusDays(1);

        mockCandlesSimple(ticker, from, to, CandleInterval.ONE_MIN, 10);

        service.getLastCandle(ticker);
    }

    @Test
    public void getLastCandle_returnsCandle_whenCandleExistsInMaxDayToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime earliestDayToSearch = OffsetDateTime.now()
                .minusDays(tradingProperties.getConsecutiveEmptyDaysLimit());
        final OffsetDateTime from = DateUtils.atStartOfDay(earliestDayToSearch);
        final OffsetDateTime to = DateUtils.atEndOfDay(earliestDayToSearch);
        final int openPrice = 10;

        mockCandlesSimple(ticker, from, to, CandleInterval.ONE_MIN, openPrice, earliestDayToSearch);

        Candle candle = service.getLastCandle(ticker);

        assertNotNull(candle);
        assertTrue(MathUtils.numbersEqual(candle.getOpenPrice(), openPrice));
    }

    // endregion

    // region getLastCandle with to tests

    @Test(expected = IllegalArgumentException.class)
    public void getLastCandleTo_throwsIllegalArgumentException_whenNoCandles() {
        final OffsetDateTime to = OffsetDateTime.now().minusDays(10);

        service.getLastCandle(TICKER, to);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLastCandleTo_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 10);
        final OffsetDateTime candlesTo = to.minusDays(tradingProperties.getConsecutiveEmptyDaysLimit() + 1);
        final OffsetDateTime candlesFrom = candlesTo.minusDays(1);

        mockCandlesSimple(ticker, candlesFrom, candlesTo, CandleInterval.ONE_MIN, 10);

        service.getLastCandle(ticker, to);
    }

    @Test
    public void getLastCandleTo_returnsCandle_whenCandleExistsInMaxDayToSearch() {
        final String ticker = TICKER;
        final OffsetDateTime to = DateUtils.atEndOfDay(DateUtils.getDate(2020, 1, 10));
        final OffsetDateTime candlesTo = to.minusDays(tradingProperties.getConsecutiveEmptyDaysLimit() - 1);
        final OffsetDateTime candlesFrom = DateUtils.atStartOfDay(candlesTo);
        final int openPrice = 10;

        mockCandlesSimple(ticker, candlesFrom, CandleInterval.ONE_MIN, openPrice);

        Candle candle = service.getLastCandle(ticker, to);

        assertNotNull(candle);
        assertTrue(MathUtils.numbersEqual(candle.getOpenPrice(), openPrice));
    }

    // endregion

    // region getLastCandles tests

    @Test
    public void getLastCandles_returnsNoCandles_whenThereAreNoCandles() {
        final String ticker = TICKER;
        int limit = 5;

        OffsetDateTime currentDateTime = DateUtils.atEndOfDay(DateUtils.getDate(2020, 9, 10));
        when(tinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);

        List<Candle> candles = service.getLastCandles(ticker, limit);

        assertTrue(candles.isEmpty());
    }

    @Test
    public void getLastCandles_returnsLimitedNumberOfCandles_whenThereAreMoreCandlesThanLimited() {
        final String ticker = TICKER;
        int limit = 5;

        OffsetDateTime from1 = DateUtils.getDate(2020, 9, 8);
        OffsetDateTime to1 = DateUtils.atEndOfDay(from1);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(from1.withHour(1), from1.withHour(2), from1.withHour(3));
        mockCandlesSimple(ticker, from1, to1, CandleInterval.ONE_MIN, prices1, times1);

        OffsetDateTime from2 = DateUtils.getDate(2020, 9, 9);
        OffsetDateTime to2 = DateUtils.atEndOfDay(from2);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(from2.withHour(1), from2.withHour(2));
        mockCandlesSimple(ticker, from2, to2, CandleInterval.ONE_MIN, prices2, times2);

        OffsetDateTime from3 = DateUtils.getDate(2020, 9, 10);
        OffsetDateTime to3 = DateUtils.atEndOfDay(from3);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(from3.withHour(1));
        mockCandlesSimple(ticker, from3, to3, CandleInterval.ONE_MIN, prices3, times3);

        when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateUtils.getDateTime(2020, 9, 10, 2, 0, 0));

        List<Candle> candles = service.getLastCandles(ticker, limit);

        assertEquals(limit, candles.size());
        assertTrue(MathUtils.numbersEqual(candles.get(0).getOpenPrice(), 2));
        assertTrue(MathUtils.numbersEqual(candles.get(1).getOpenPrice(), 3));
        assertTrue(MathUtils.numbersEqual(candles.get(2).getOpenPrice(), 4));
        assertTrue(MathUtils.numbersEqual(candles.get(3).getOpenPrice(), 5));
        assertTrue(MathUtils.numbersEqual(candles.get(4).getOpenPrice(), 6));
    }

    @Test
    public void getLastCandles_returnsNumberOfCandlesLowerThanLimit_whenThereAreLessCandlesThanLimited() {
        final String ticker = TICKER;
        int limit = 10;

        OffsetDateTime from1 = DateUtils.getDate(2020, 9, 8);
        OffsetDateTime to1 = DateUtils.atEndOfDay(from1);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(from1.withHour(1), from1.withHour(2), from1.withHour(3));
        mockCandlesSimple(ticker, from1, to1, CandleInterval.ONE_MIN, prices1, times1);

        OffsetDateTime from2 = DateUtils.getDate(2020, 9, 9);
        OffsetDateTime to2 = DateUtils.atEndOfDay(from2);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(from2.withHour(1), from2.withHour(2));
        mockCandlesSimple(ticker, from2, to2, CandleInterval.ONE_MIN, prices2, times2);

        OffsetDateTime from3 = DateUtils.getDate(2020, 9, 10);
        OffsetDateTime to3 = DateUtils.atEndOfDay(from3);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(from3.withHour(1));
        mockCandlesSimple(ticker, from3, to3, CandleInterval.ONE_MIN, prices3, times3);

        when(tinkoffService.getCurrentDateTime())
                .thenReturn(DateUtils.getDateTime(2020, 9, 10, 2, 0, 0));

        List<Candle> candles = service.getLastCandles(ticker, limit);

        assertEquals(6, candles.size());
        assertTrue(MathUtils.numbersEqual(candles.get(0).getOpenPrice(), 1));
        assertTrue(MathUtils.numbersEqual(candles.get(1).getOpenPrice(), 2));
        assertTrue(MathUtils.numbersEqual(candles.get(2).getOpenPrice(), 3));
        assertTrue(MathUtils.numbersEqual(candles.get(3).getOpenPrice(), 4));
        assertTrue(MathUtils.numbersEqual(candles.get(4).getOpenPrice(), 5));
        assertTrue(MathUtils.numbersEqual(candles.get(5).getOpenPrice(), 6));
    }

    @Test
    public void getLastCandles_returnsNoCandles_whenThereIsBigEmptyIntervalAfterCandles() {
        final String ticker = TICKER;
        int limit = 5;

        OffsetDateTime from1 = DateUtils.getDate(2020, 9, 8);
        OffsetDateTime to1 = DateUtils.atEndOfDay(from1);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(from1.withHour(1), from1.withHour(2), from1.withHour(3));
        mockCandlesSimple(ticker, from1, to1, CandleInterval.ONE_MIN, prices1, times1);

        OffsetDateTime from2 = DateUtils.getDate(2020, 9, 9);
        OffsetDateTime to2 = DateUtils.atEndOfDay(from2);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(from2.withHour(1), from2.withHour(2));
        mockCandlesSimple(ticker, from2, to2, CandleInterval.ONE_MIN, prices2, times2);

        OffsetDateTime from3 = DateUtils.getDate(2020, 9, 10);
        OffsetDateTime to3 = DateUtils.atEndOfDay(from3);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(from3.withHour(1));
        mockCandlesSimple(ticker, from3, to3, CandleInterval.ONE_MIN, prices3, times3);

        when(tinkoffService.getCurrentDateTime())
                .thenReturn(to3.plusDays(tradingProperties.getConsecutiveEmptyDaysLimit() + 1));

        List<Candle> candles = service.getLastCandles(ticker, limit);

        assertTrue(candles.isEmpty());
    }

    @Test
    public void getLastCandles_returnsCandlesOnlyAfterManyEmptyDays_whenThereIsBigEmptyIntervalBetweenCandles() {
        final String ticker = TICKER;
        int limit = 5;

        OffsetDateTime from1 = DateUtils.getDate(2020, 9, 1);
        OffsetDateTime to1 = DateUtils.atEndOfDay(from1);
        List<Integer> prices1 = Arrays.asList(1, 2, 3);
        List<OffsetDateTime> times1 = Arrays.asList(from1.withHour(1), from1.withHour(2), from1.withHour(3));
        mockCandlesSimple(ticker, from1, to1, CandleInterval.ONE_MIN, prices1, times1);

        OffsetDateTime from2 = DateUtils.getDate(2020, 9, 10);
        OffsetDateTime to2 = DateUtils.atEndOfDay(from2);
        List<Integer> prices2 = Arrays.asList(4, 5);
        List<OffsetDateTime> times2 = Arrays.asList(from2.withHour(1), from2.withHour(2));
        mockCandlesSimple(ticker, from2, to2, CandleInterval.ONE_MIN, prices2, times2);

        OffsetDateTime from3 = DateUtils.getDate(2020, 9, 11);
        OffsetDateTime to3 = DateUtils.atEndOfDay(from3);
        List<Integer> prices3 = Collections.singletonList(6);
        List<OffsetDateTime> times3 = Collections.singletonList(from3.withHour(1));
        mockCandlesSimple(ticker, from3, to3, CandleInterval.ONE_MIN, prices3, times3);

        when(tinkoffService.getCurrentDateTime())
                .thenReturn(to3.plusDays(tradingProperties.getConsecutiveEmptyDaysLimit()));

        List<Candle> candles = service.getLastCandles(ticker, limit);

        assertEquals(3, candles.size());
        assertTrue(MathUtils.numbersEqual(candles.get(0).getOpenPrice(), 4));
        assertTrue(MathUtils.numbersEqual(candles.get(1).getOpenPrice(), 5));
        assertTrue(MathUtils.numbersEqual(candles.get(2).getOpenPrice(), 6));
    }

    // endregion

    // region mocks

    private void mockAnyCandles() {
        when(tinkoffService.getMarketCandles(eq(TICKER),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(CandleInterval.class)))
                .thenReturn(Collections.emptyList());
    }

    private void mockCandlesSimple(String ticker,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   Integer... openPrices) {

        List<Integer> prices = Arrays.asList(openPrices);
        List<OffsetDateTime> times = Collections.nCopies(openPrices.length, from);

        mockCandlesSimple(ticker, from, to, candleInterval, prices, times);

    }

    private void mockCandlesSimple(String ticker,
                                   OffsetDateTime date,
                                   CandleInterval candleInterval,
                                   Integer... openPrices) {

        OffsetDateTime from = DateUtils.atStartOfDay(date);
        OffsetDateTime to = DateUtils.atEndOfDay(date);

        List<Integer> prices = Arrays.asList(openPrices);
        List<OffsetDateTime> times = Collections.nCopies(openPrices.length, from);

        mockCandlesSimple(ticker, from, to, candleInterval, prices, times);

    }

    private void mockCandlesSimple(String ticker,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   Integer openPrice,
                                   OffsetDateTime time) {

        final List<Integer> openPrices = Collections.singletonList(openPrice);
        final List<OffsetDateTime> times = Collections.singletonList(time);

        mockCandlesSimple(ticker, from, to, candleInterval, openPrices, times);

    }

    private void mockCandlesSimple(String ticker,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   List<Integer> openPrices,
                                   List<OffsetDateTime> times) {

        List<Candle> candles = createCandlesSimple(openPrices, times);

        when(tinkoffService.getMarketCandles(eq(ticker), eq(from), eq(to), eq(candleInterval)))
                .thenReturn(candles);

    }

    private List<Candle> createCandlesSimple(List<Integer> openPrices, List<OffsetDateTime> times) {
        Assert.isTrue(openPrices.size() == times.size(),
                "times and openPrices must have same size");

        List<Candle> candles = new ArrayList<>(openPrices.size());
        for (int i = 0; i < openPrices.size(); i++) {
            candles.add(createCandleSimple(openPrices.get(i), times.get(i)));
        }

        return candles;
    }

    private Candle createCandleSimple(Integer openPrice, OffsetDateTime time) {

        return Candle.builder()
                .openPrice(BigDecimal.valueOf(openPrice))
                .time(time)
                .build();

    }

    // endregion

}