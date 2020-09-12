package ru.obukhov.investor.service.impl;

import com.google.common.collect.Ordering;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.obukhov.investor.util.DateUtils.getDate;
import static ru.obukhov.investor.util.DateUtils.getTime;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

@RunWith(MockitoJUnitRunner.class)
public class InvestServiceImplTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    @Mock
    private MarketService marketService;

    private InvestServiceImpl service;

    @Before
    public void setUp() {
        service = new InvestServiceImpl(marketService);
    }

    @Test
    public void getCandles_returnsCandlesFromMarketService() {
        String ticker = TICKER;
        OffsetDateTime from = getDate(2020, 1, 1);
        OffsetDateTime to = getDate(2020, 2, 1);
        CandleInterval candleInterval = CandleInterval.ONE_MIN;

        List<Candle> candles = new ArrayList<>();
        mockCandles(ticker, from, to, candleInterval, candles);

        List<Candle> candlesResponse = service.getCandles(ticker, from, to, candleInterval);

        Assert.assertSame(candles, candlesResponse);
    }

    // region getDailySaldos tests

    @Test
    public void getDailySaldos_unitesSaldosByTime() {
        String ticker = TICKER;

        List<Candle> candles = new ArrayList<>();
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getTime(10, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getTime(11, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getTime(11, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getTime(12, 0, 0))
                .build());
        mockCandles(ticker,
                getDate(2020, 1, 1),
                getDate(2020, 1, 4),
                CandleInterval.ONE_MIN,
                candles);

        Map<LocalTime, BigDecimal> saldos = service.getDailySaldos(ticker,
                getDate(2020, 1, 1),
                getDate(2020, 1, 4),
                CandleInterval.ONE_MIN);

        assertEquals(3, saldos.size());

        final BigDecimal average1 = saldos.get(LocalTime.of(10, 0));
        assertTrue(numbersEqual(average1, 100));

        final BigDecimal average2 = saldos.get(LocalTime.of(11, 0));
        assertTrue(numbersEqual(average2, 150));

        final BigDecimal average3 = saldos.get(LocalTime.of(12, 0));
        assertTrue(numbersEqual(average3, 200));
    }

    @Test
    public void getDailySaldos_sortsSaldosByDays() {
        String ticker = TICKER;

        List<Candle> candles = new ArrayList<>();
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getTime(10, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getTime(11, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getTime(11, 0, 0))
                .build());
        mockCandles(ticker,
                getDate(2020, 1, 1),
                getDate(2020, 1, 4),
                CandleInterval.ONE_MIN,
                candles);

        Map<LocalTime, BigDecimal> saldos = service.getDailySaldos(ticker,
                getDate(2020, 1, 1),
                getDate(2020, 1, 4),
                CandleInterval.ONE_MIN);

        assertEquals(3, saldos.size());

        assertTrue(Ordering.<LocalTime>natural().isOrdered(saldos.keySet()));
    }

    // endregion

    // region getWeeklySaldos tests

    @Test
    public void getWeeklySaldos_unitesSaldosByDayOfWeek() {
        String ticker = TICKER;

        List<Candle> candles = new ArrayList<>();

        // Monday
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 24))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 24))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getDate(2020, 8, 24))
                .build());

        // Tuesday
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 25))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 25))
                .build());

        // Wednesday
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getDate(2020, 8, 26))
                .build());

        mockCandles(ticker,
                getDate(2020, 8, 24),
                getDate(2020, 8, 30),
                CandleInterval.DAY,
                candles);

        Map<DayOfWeek, BigDecimal> saldos = service.getWeeklySaldos(ticker,
                getDate(2020, 8, 24),
                getDate(2020, 8, 30));

        assertEquals(3, saldos.size());

        final BigDecimal mondayAverage = saldos.get(DayOfWeek.MONDAY);
        assertTrue(numbersEqual(mondayAverage, 200));

        final BigDecimal tuesdatAverage = saldos.get(DayOfWeek.TUESDAY);
        assertTrue(numbersEqual(tuesdatAverage, 150));

        final BigDecimal wednesdayAverage = saldos.get(DayOfWeek.WEDNESDAY);
        assertTrue(numbersEqual(wednesdayAverage, 300));
    }

    @Test
    public void getWeeklySaldos_sortsSaldosByDays() {
        String ticker = TICKER;

        List<Candle> candles = new ArrayList<>();

        // Tuesday
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 25))
                .build());

        // Monday
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 24))
                .build());

        // Friday
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 28))
                .build());

        // Wednesday
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 26))
                .build());

        mockCandles(ticker,
                getDate(2020, 8, 24),
                getDate(2020, 8, 30),
                CandleInterval.DAY,
                candles);

        Map<DayOfWeek, BigDecimal> saldos = service.getWeeklySaldos(ticker,
                getDate(2020, 8, 24),
                getDate(2020, 8, 30));

        assertEquals(4, saldos.size());

        assertTrue(Ordering.<DayOfWeek>natural().isOrdered(saldos.keySet()));
    }

    // endregion

    // region mocks

    private void mockCandles(String ticker,
                             OffsetDateTime from,
                             OffsetDateTime to,
                             CandleInterval candleInterval,
                             List<Candle> candles) {

        TemporalUnit temporalUnit = DateUtils.getPeriodUnitByCandleInterval(candleInterval);

        when(marketService.getCandles(eq(ticker), eq(from), eq(to), eq(candleInterval), eq(temporalUnit)))
                .thenReturn(candles);
    }

    // endregion

}