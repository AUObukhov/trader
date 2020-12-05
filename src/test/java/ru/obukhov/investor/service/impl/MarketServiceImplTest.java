package ru.obukhov.investor.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.Assert;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentType;

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
import static ru.obukhov.investor.service.impl.MarketServiceImpl.CONSECUTIVE_EMPTY_DAYS_LIMIT;
import static ru.obukhov.investor.util.DateUtils.getDate;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

@RunWith(MockitoJUnitRunner.class)
public class MarketServiceImplTest extends BaseMockedTest {

    private static final String FIGI = "figi";
    private static final String TICKER = "ticker";

    @Mock
    private TinkoffService tinkoffService;

    private MarketService service;

    @Before
    public void setUp() {
        this.service = new MarketServiceImpl(tinkoffService);

        when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        mockAnyCandles();
    }

    // region getCandles tests

    @Test
    public void getCandles_skipsCandlesByDays_whenFromIsReached() {
        final String figi = FIGI;
        final CandleInterval candleInterval = CandleInterval.ONE_MIN;
        final String ticker = TICKER;

        mockCandlesSimple(figi,
                getDate(2020, 1, 5),
                candleInterval,
                10);

        mockCandlesSimple(figi,
                getDate(2020, 1, 7),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(figi,
                getDate(2020, 1, 11),
                candleInterval,
                3, 4);

        mockCandlesSimple(figi,
                getDate(2020, 1, 12),
                candleInterval,
                5);

        mockInstrument(figi, ticker);

        final OffsetDateTime from = getDate(2020, 1, 6);
        final OffsetDateTime to = getDate(2020, 1, 13);
        List<Candle> candles = service.getCandlesByTicker(ticker, from, to, candleInterval);

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
        final String figi = FIGI;
        final CandleInterval candleInterval = CandleInterval.ONE_MIN;
        final String ticker = TICKER;

        mockCandlesSimple(figi,
                getDate(2020, 1, 1),
                candleInterval,
                10);

        mockCandlesSimple(figi,
                getDate(2020, 1, 10),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(figi,
                getDate(2020, 1, 18),
                candleInterval,
                3, 4);

        mockCandlesSimple(figi,
                getDate(2020, 1, 19),
                candleInterval,
                5);

        mockInstrument(figi, ticker);

        final OffsetDateTime from = getDate(2020, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 21);
        List<Candle> candles = service.getCandlesByTicker(ticker, from, to, candleInterval);

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
        final String figi = FIGI;
        final CandleInterval candleInterval = CandleInterval.MONTH;
        final String ticker = TICKER;

        mockCandlesSimple(figi,
                getDate(2016, 1, 1),
                getDate(2017, 1, 1),
                candleInterval,
                10);

        mockCandlesSimple(figi,
                getDate(2017, 1, 1),
                getDate(2018, 1, 1),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(figi,
                getDate(2018, 1, 1),
                getDate(2019, 1, 1),
                candleInterval,
                3, 4);

        mockCandlesSimple(figi,
                getDate(2019, 1, 1),
                getDate(2020, 1, 1),
                candleInterval,
                5);

        mockInstrument(figi, ticker);

        final OffsetDateTime from = getDate(2017, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 1);
        List<Candle> candles = service.getCandlesByTicker(ticker, from, to, candleInterval);

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
        final String figi = FIGI;
        final CandleInterval candleInterval = CandleInterval.MONTH;
        final String ticker = TICKER;

        mockCandlesSimple(figi,
                getDate(2015, 1, 1),
                getDate(2016, 1, 1),
                candleInterval,
                10);

        mockCandlesSimple(figi,
                getDate(2017, 1, 1),
                getDate(2018, 1, 1),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(figi,
                getDate(2018, 1, 1),
                getDate(2019, 1, 1),
                candleInterval,
                3, 4);

        mockCandlesSimple(figi,
                getDate(2019, 1, 1),
                getDate(2020, 1, 1),
                candleInterval,
                5);

        mockInstrument(figi, ticker);

        final OffsetDateTime from = getDate(2010, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 1);
        List<Candle> candles = service.getCandlesByTicker(ticker, from, to, candleInterval);

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
        final String ticker = TICKER;

        mockInstrument(FIGI, ticker);

        service.getLastCandleByTicker(ticker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLastCandle_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String figi = FIGI;
        final String ticker = TICKER;
        final OffsetDateTime from = DateUtils.getLastWorkDay().minusDays(CONSECUTIVE_EMPTY_DAYS_LIMIT + 1);
        final OffsetDateTime to = from.plusDays(1);

        mockInstrument(figi, ticker);
        mockCandlesSimple(figi, from, to, CandleInterval.ONE_MIN, 10);

        service.getLastCandleByTicker(ticker);
    }

    @Test
    public void getLastCandle_returnsCandle_whenCandleExistsInMaxDayToSearch() {
        final String figi = FIGI;
        final String ticker = TICKER;
        final OffsetDateTime earliestDayToSearch = OffsetDateTime.now().minusDays(CONSECUTIVE_EMPTY_DAYS_LIMIT);
        final OffsetDateTime from = DateUtils.atStartOfDay(earliestDayToSearch);
        final OffsetDateTime to = DateUtils.atEndOfDay(earliestDayToSearch);
        final int openPrice = 10;

        mockInstrument(figi, ticker);
        mockCandlesSimple(figi, from, to, CandleInterval.ONE_MIN, openPrice, earliestDayToSearch);

        Candle candle = service.getLastCandleByTicker(ticker);

        assertNotNull(candle);
        assertTrue(MathUtils.numbersEqual(candle.getOpenPrice(), openPrice));
    }

    // endregion

    // region getLastCandle with to tests

    @Test(expected = IllegalArgumentException.class)
    public void getLastCandleTo_throwsIllegalArgumentException_whenNoCandles() {
        final String ticker = TICKER;
        final OffsetDateTime to = OffsetDateTime.now().minusDays(10);

        mockInstrument(FIGI, ticker);

        service.getLastCandleByFigi(ticker, to);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLastCandleTo_throwsIllegalArgumentException_whenNoCandlesInMaxDaysToSearch() {
        final String figi = FIGI;
        final String ticker = TICKER;
        final OffsetDateTime to = DateUtils.getDate(2020, 1, 10);
        final OffsetDateTime candlesTo = to.minusDays(CONSECUTIVE_EMPTY_DAYS_LIMIT + 1);
        final OffsetDateTime candlesFrom = candlesTo.minusDays(1);

        mockInstrument(figi, ticker);
        mockCandlesSimple(figi, candlesFrom, candlesTo, CandleInterval.ONE_MIN, 10);

        service.getLastCandleByFigi(ticker, to);
    }

    @Test
    public void getLastCandleTo_returnsCandle_whenCandleExistsInMaxDayToSearch() {
        final String figi = FIGI;
        final String ticker = TICKER;
        final OffsetDateTime to = DateUtils.atEndOfDay(DateUtils.getDate(2020, 1, 10));
        final OffsetDateTime candlesTo = to.minusDays(CONSECUTIVE_EMPTY_DAYS_LIMIT - 1);
        final OffsetDateTime candlesFrom = DateUtils.atStartOfDay(candlesTo);
        final int openPrice = 10;

        mockInstrument(figi, ticker);
        mockCandlesSimple(figi, candlesFrom, CandleInterval.ONE_MIN, openPrice);

        Candle candle = service.getLastCandleByFigi(figi, to);

        assertNotNull(candle);
        assertTrue(MathUtils.numbersEqual(candle.getOpenPrice(), openPrice));
    }

    // endregion

    // region mocks

    private void mockAnyCandles() {
        when(tinkoffService.getMarketCandles(eq(FIGI),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(CandleInterval.class)))
                .thenReturn(Collections.emptyList());
    }

    private void mockCandlesSimple(String figi,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   Integer... openPrices) {

        List<Integer> prices = Arrays.asList(openPrices);
        List<OffsetDateTime> times = Collections.nCopies(openPrices.length, from);

        mockCandlesSimple(figi, from, to, candleInterval, prices, times);

    }

    private void mockCandlesSimple(String figi,
                                   OffsetDateTime date,
                                   CandleInterval candleInterval,
                                   Integer... openPrices) {

        OffsetDateTime from = DateUtils.atStartOfDay(date);
        OffsetDateTime to = DateUtils.atEndOfDay(date);

        List<Integer> prices = Arrays.asList(openPrices);
        List<OffsetDateTime> times = Collections.nCopies(openPrices.length, from);

        mockCandlesSimple(figi, from, to, candleInterval, prices, times);

    }

    private void mockCandlesSimple(String figi,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   Integer openPrice,
                                   OffsetDateTime time) {

        final List<Integer> openPrices = Collections.singletonList(openPrice);
        final List<OffsetDateTime> times = Collections.singletonList(time);

        mockCandlesSimple(figi, from, to, candleInterval, openPrices, times);

    }

    private void mockCandlesSimple(String figi,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   List<Integer> openPrices,
                                   List<OffsetDateTime> times) {

        List<Candle> candles = createCandlesSimple(openPrices, times);

        when(tinkoffService.getMarketCandles(eq(figi), eq(from), eq(to), eq(candleInterval)))
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

    private void mockInstrument(String figi, String ticker) {

        Instrument instrument = new Instrument(figi,
                ticker,
                null,
                null,
                0,
                null,
                "testInstrument",
                InstrumentType.Stock);

        when(tinkoffService.searchMarketInstrumentByTicker(eq(ticker))).thenReturn(instrument);

    }

    // endregion

}