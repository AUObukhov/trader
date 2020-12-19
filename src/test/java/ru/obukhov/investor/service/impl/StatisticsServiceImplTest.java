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
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
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
public class StatisticsServiceImplTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    @Mock
    private MarketService marketService;

    private StatisticsServiceImpl service;

    @Before
    public void setUp() {
        service = new StatisticsServiceImpl(marketService);
    }

    @Test
    public void getCandles_returnsCandlesFromMarketService() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 1, 1);
        final OffsetDateTime to = getDate(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleInterval candleInterval = CandleInterval.ONE_MIN;

        final List<Candle> candles = new ArrayList<>();

        mockCandles(ticker, interval, candleInterval, candles);

        final List<Candle> candlesResponse = service.getCandles(ticker, interval, candleInterval);

        Assert.assertSame(candles, candlesResponse);
    }

    // region getDailySaldos tests

    @Test
    public void getDailySaldos_unitesSaldosByTime() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 4);
        final Interval interval = Interval.of(from, to);

        final CandleInterval candleInterval = CandleInterval.ONE_MIN;

        final List<Candle> candles = new ArrayList<>();
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

        mockCandles(ticker, interval, candleInterval, candles);

        final Map<LocalTime, BigDecimal> saldos = (Map) service.getDailySaldos(ticker, interval, candleInterval);

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
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 1, 1);
        final OffsetDateTime to = getDate(2020, 1, 4);
        final Interval interval = Interval.of(from, to);

        final CandleInterval candleInterval = CandleInterval.ONE_MIN;

        final List<Candle> candles = new ArrayList<>();
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

        mockCandles(ticker, interval, candleInterval, candles);

        final Map<LocalTime, BigDecimal> saldos = (Map) service.getDailySaldos(ticker, interval, candleInterval);

        assertEquals(3, saldos.size());

        assertTrue(Ordering.<LocalTime>natural().isOrdered(saldos.keySet()));
    }

    // endregion

    // region getWeeklySaldos tests

    @Test
    public void getWeeklySaldos_unitesSaldosByDayOfWeek() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 24);
        final OffsetDateTime to = getDate(2020, 8, 30);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();

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

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<DayOfWeek, BigDecimal> saldos = (Map) service.getWeeklySaldos(ticker, interval);

        assertEquals(3, saldos.size());

        final BigDecimal mondayAverage = saldos.get(DayOfWeek.MONDAY);
        assertTrue(numbersEqual(mondayAverage, 200));

        final BigDecimal tuesdayAverage = saldos.get(DayOfWeek.TUESDAY);
        assertTrue(numbersEqual(tuesdayAverage, 150));

        final BigDecimal wednesdayAverage = saldos.get(DayOfWeek.WEDNESDAY);
        assertTrue(numbersEqual(wednesdayAverage, 300));
    }

    @Test
    public void getWeeklySaldos_sortsSaldosByDays() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 24);
        final OffsetDateTime to = getDate(2020, 8, 30);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();

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

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<DayOfWeek, BigDecimal> saldos = (Map) service.getWeeklySaldos(ticker, interval);

        assertEquals(4, saldos.size());

        assertTrue(Ordering.natural().isOrdered(saldos.keySet()));
    }

    // endregion

    // region getMonthlySaldos

    @Test
    public void getMonthlySaldos_unitesSaldosByDayOfMonth() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 1);
        final OffsetDateTime to = getDate(2020, 8, 3);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();

        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getDate(2020, 8, 1))
                .build());

        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 2))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 2))
                .build());

        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getDate(2020, 8, 3))
                .build());

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<Integer, BigDecimal> saldos = (Map) service.getMonthlySaldos(ticker, interval);

        assertEquals(3, saldos.size());

        final BigDecimal average1 = saldos.get(1);
        assertTrue(numbersEqual(average1, 200));

        final BigDecimal average2 = saldos.get(2);
        assertTrue(numbersEqual(average2, 150));

        final BigDecimal average3 = saldos.get(3);
        assertTrue(numbersEqual(average3, 300));
    }

    @Test
    public void getMonthlySaldos_sortsSaldosByDays() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 1);
        final OffsetDateTime to = getDate(2020, 8, 4);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 2))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 4))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 3))
                .build());

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<Integer, BigDecimal> saldos = (Map) service.getMonthlySaldos(ticker, interval);

        assertEquals(4, saldos.size());

        assertTrue(Ordering.natural().isOrdered(saldos.keySet()));
    }

    // endregion

    // region getYearlySaldos

    @Test
    public void getYearlySaldos_unitesSaldosMonth() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 1);
        final OffsetDateTime to = getDate(2020, 8, 3);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();

        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getDate(2020, 8, 1))
                .build());

        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getDate(2020, 9, 2))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getDate(2020, 9, 2))
                .build());

        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getDate(2020, 10, 3))
                .build());

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<Integer, BigDecimal> saldos = (Map) service.getMonthlySaldos(ticker, interval);

        assertEquals(3, saldos.size());

        final BigDecimal average1 = saldos.get(1);
        assertTrue(numbersEqual(average1, 200));

        final BigDecimal average2 = saldos.get(2);
        assertTrue(numbersEqual(average2, 150));

        final BigDecimal average3 = saldos.get(3);
        assertTrue(numbersEqual(average3, 300));
    }

    @Test
    public void getYearlySaldos_sortsSaldosByMonths() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 1);
        final OffsetDateTime to = getDate(2020, 8, 4);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();

        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 9, 2))
                .build());

        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 8, 1))
                .build());

        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 11, 4))
                .build());

        candles.add(Candle.builder()
                .saldo(BigDecimal.TEN)
                .time(getDate(2020, 10, 3))
                .build());

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<Integer, BigDecimal> saldos = (Map) service.getMonthlySaldos(ticker, interval);

        assertEquals(4, saldos.size());

        assertTrue(Ordering.natural().isOrdered(saldos.keySet()));
    }

    // endregion

    // region mocks

    private void mockCandles(String ticker,
                             Interval interval,
                             CandleInterval candleInterval,
                             List<Candle> candles) {

        when(marketService.getCandles(eq(ticker), eq(interval), eq(candleInterval)))
                .thenReturn(candles);
    }

    // endregion

}