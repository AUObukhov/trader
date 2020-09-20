package ru.obukhov.investor.service.impl;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentType;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.obukhov.investor.util.DateUtils.getDate;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

@RunWith(MockitoJUnitRunner.class)
public class MarketServiceImplTest extends BaseMockedTest {

    private static final String FIGI = "figi";
    private static final String TICKER = "ticker";

    @Mock
    private MarketContext marketContext;

    private MarketService service;

    @Before
    public void setUp() {
        this.service = new MarketServiceImpl(null, marketContext);

        mockAnyCandles();
    }

    @Test
    public void getCandles_skipsCandlesByDays_whenFromIsReached() {
        final String figi = FIGI;
        final CandleInterval candleInterval = CandleInterval.ONE_MIN;
        final String ticker = TICKER;

        mockCandlesSimple(figi,
                getDate(2020, 1, 4),
                getDate(2020, 1, 5),
                candleInterval,
                10);

        mockCandlesSimple(figi,
                getDate(2020, 1, 5),
                getDate(2020, 1, 6),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(figi,
                getDate(2020, 1, 11),
                getDate(2020, 1, 12),
                candleInterval,
                3, 4);

        mockCandlesSimple(figi,
                getDate(2020, 1, 12),
                getDate(2020, 1, 13),
                candleInterval,
                5);

        mockInstrument(figi, ticker);

        final OffsetDateTime from = getDate(2020, 1, 5);
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
        final String figi = FIGI;
        final CandleInterval candleInterval = CandleInterval.ONE_MIN;
        final String ticker = TICKER;

        mockCandlesSimple(figi,
                getDate(2020, 1, 3),
                getDate(2020, 1, 4),
                candleInterval,
                10);

        mockCandlesSimple(figi,
                getDate(2020, 1, 10),
                getDate(2020, 1, 11),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(figi,
                getDate(2020, 1, 11),
                getDate(2020, 1, 12),
                candleInterval,
                3, 4);

        mockCandlesSimple(figi,
                getDate(2020, 1, 12),
                getDate(2020, 1, 13),
                candleInterval,
                5);

        mockInstrument(figi, ticker);

        final OffsetDateTime from = getDate(2020, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 15);
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
        List<Candle> candles = service.getCandles(ticker, from, to, candleInterval);

        assertEquals(6, candles.size());
        assertTrue(numbersEqual(BigDecimal.valueOf(0), candles.get(0).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(1), candles.get(1).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(2), candles.get(2).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(3), candles.get(3).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(4), candles.get(4).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(5), candles.get(5).getOpenPrice()));
    }

    private void mockAnyCandles() {
        Answer<CompletableFuture<Optional<HistoricalCandles>>> answer = invocation ->
                createCandlesFuture(invocation.getArgument(0, String.class),
                        invocation.getArgument(3, CandleInterval.class),
                        Collections.emptyList());
        when(marketContext.getMarketCandles(eq(FIGI),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(CandleInterval.class)))
                .thenAnswer(answer);
    }

    private void mockCandlesSimple(String figi,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   Integer... openPrices) {

        List<ru.tinkoff.invest.openapi.models.market.Candle> candles = createCandlesSimple(from, openPrices);

        when(marketContext.getMarketCandles(eq(figi), eq(from), eq(to), eq(candleInterval)))
                .thenReturn(createCandlesFuture(figi, candleInterval, candles));

    }

    private List<ru.tinkoff.invest.openapi.models.market.Candle> createCandlesSimple(OffsetDateTime time,
                                                                                     Integer... openPrices) {

        return Arrays.stream(openPrices)
                .map(p -> createCandleSimple(p, time))
                .collect(Collectors.toList());

    }

    private ru.tinkoff.invest.openapi.models.market.Candle createCandleSimple(Integer openPrice, OffsetDateTime time) {

        return new ru.tinkoff.invest.openapi.models.market.Candle(
                null,
                null,
                BigDecimal.valueOf(openPrice),
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                time);

    }

    private CompletableFuture<Optional<HistoricalCandles>> createCandlesFuture(
            String figi,
            CandleInterval candleInterval,
            List<ru.tinkoff.invest.openapi.models.market.Candle> candles) {

        HistoricalCandles historicalCandles = new HistoricalCandles(figi, candleInterval, candles);
        Optional<HistoricalCandles> optionalHistoricalCandles = Optional.of(historicalCandles);
        return CompletableFuture.completedFuture(optionalHistoricalCandles);

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
        List<Instrument> instruments = ImmutableList.of(instrument);
        InstrumentsList instrumentsList = new InstrumentsList(instruments.size(), instruments);
        CompletableFuture<InstrumentsList> completableFuture = CompletableFuture.completedFuture(instrumentsList);

        when(marketContext.searchMarketInstrumentsByTicker(eq(ticker))).thenReturn(completableFuture);

    }

}