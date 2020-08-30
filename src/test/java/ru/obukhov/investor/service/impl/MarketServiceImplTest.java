package ru.obukhov.investor.service.impl;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.MarketService;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentType;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.obukhov.investor.util.DateUtils.getDate;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

@RunWith(MockitoJUnitRunner.class)
public class MarketServiceImplTest extends BaseMockedTest {

    private static final String TOKEN = "token";
    private static final String FIGI = "figi";
    private static final String TICKER = "ticker";

    @Mock
    private ConnectionService connectionService;
    @Mock
    private OpenApi openApi;
    @Mock
    private MarketContext marketContext;

    private MarketService service;

    @Before
    public void setUp() {
        when(connectionService.getApi(eq(TOKEN))).thenReturn(openApi);
        when(openApi.getMarketContext()).thenReturn(marketContext);

        this.service = new MarketServiceImpl(connectionService, TOKEN);
    }

    @Test
    public void getCandles_returnsAllCandles() {
        final String figi = FIGI;
        final CandleInterval candleInterval = CandleInterval.ONE_MIN;
        final String ticker = TICKER;

        mockCandlesSimple(figi,
                getDate(2020, 1, 1),
                getDate(2020, 1, 2),
                candleInterval,
                0, 1, 2);

        mockCandlesSimple(figi,
                getDate(2020, 1, 2),
                getDate(2020, 1, 3),
                candleInterval,
                3, 4);

        mockCandlesSimple(figi,
                getDate(2020, 1, 3),
                getDate(2020, 1, 4),
                candleInterval,
                5);


        mockInstrument(figi, ticker);

        final OffsetDateTime from = getDate(2020, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 4);
        List<Candle> candles = service.getCandles(ticker, from, to, candleInterval, ChronoUnit.DAYS);

        assertEquals(6, candles.size());
        assertTrue(numbersEqual(BigDecimal.valueOf(0), candles.get(0).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(1), candles.get(1).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(2), candles.get(2).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(3), candles.get(3).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(4), candles.get(4).getOpenPrice()));
        assertTrue(numbersEqual(BigDecimal.valueOf(5), candles.get(5).getOpenPrice()));
    }

    private void mockCandlesSimple(String figi,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval candleInterval,
                                   Integer... openPrices) {

        List<ru.tinkoff.invest.openapi.models.market.Candle> candles = createCandlesSimple(openPrices);

        when(marketContext.getMarketCandles(eq(figi), eq(from), eq(to), eq(candleInterval)))
                .thenReturn(createCandlesFuture(figi, candleInterval, candles));

    }

    private List<ru.tinkoff.invest.openapi.models.market.Candle> createCandlesSimple(Integer... openPrices) {

        return Arrays.stream(openPrices)
                .map(this::createCandleSimple)
                .collect(Collectors.toList());

    }

    private ru.tinkoff.invest.openapi.models.market.Candle createCandleSimple(Integer openPrice) {

        return new ru.tinkoff.invest.openapi.models.market.Candle(
                null,
                null,
                BigDecimal.valueOf(openPrice),
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ONE,
                OffsetDateTime.now());

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