package ru.obukhov.investor.common.service.impl;

import com.google.common.collect.Ordering;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.market.impl.StatisticsServiceImpl;
import ru.obukhov.investor.market.interfaces.MarketService;
import ru.obukhov.investor.market.model.Candle;
import ru.obukhov.investor.test.utils.AssertUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.obukhov.investor.common.util.DateUtils.getDate;
import static ru.obukhov.investor.common.util.DateUtils.getTime;

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
                .closePrice(BigDecimal.valueOf(200))
                .openPrice(BigDecimal.valueOf(100))
                .time(getTime(10, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(300))
                .openPrice(BigDecimal.valueOf(200))
                .time(getTime(11, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(300))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(300))
                .openPrice(BigDecimal.valueOf(100))
                .time(getTime(11, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(200))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(100))
                .time(getTime(12, 0, 0))
                .build());

        mockCandles(ticker, interval, candleInterval, candles);

        final Map<LocalTime, BigDecimal> saldos = (Map) service.getDailySaldos(ticker, interval, candleInterval);

        assertEquals(3, saldos.size());

        final BigDecimal average1 = saldos.get(LocalTime.of(10, 0));
        AssertUtils.assertEquals(100, average1);

        final BigDecimal average2 = saldos.get(LocalTime.of(11, 0));
        AssertUtils.assertEquals(150, average2);

        final BigDecimal average3 = saldos.get(LocalTime.of(12, 0));
        AssertUtils.assertEquals(200, average3);
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
                .closePrice(BigDecimal.valueOf(20))
                .openPrice(BigDecimal.valueOf(10))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(30))
                .openPrice(BigDecimal.valueOf(20))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(40))
                .openPrice(BigDecimal.valueOf(30))
                .time(getTime(10, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(50))
                .openPrice(BigDecimal.valueOf(40))
                .time(getTime(11, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(60))
                .openPrice(BigDecimal.valueOf(50))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(70))
                .openPrice(BigDecimal.valueOf(60))
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
                .closePrice(BigDecimal.valueOf(200))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 24))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 24))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 24))
                .build());

        // Tuesday
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(300))
                .time(getDate(2020, 8, 25))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(500))
                .openPrice(BigDecimal.valueOf(300))
                .time(getDate(2020, 8, 25))
                .build());

        // Wednesday
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(500))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 26))
                .build());

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<DayOfWeek, BigDecimal> saldos = (Map) service.getWeeklySaldos(ticker, interval);

        assertEquals(3, saldos.size());

        final BigDecimal mondayAverage = saldos.get(DayOfWeek.MONDAY);
        AssertUtils.assertEquals(200, mondayAverage);

        final BigDecimal tuesdayAverage = saldos.get(DayOfWeek.TUESDAY);
        AssertUtils.assertEquals(150, tuesdayAverage);

        final BigDecimal wednesdayAverage = saldos.get(DayOfWeek.WEDNESDAY);
        AssertUtils.assertEquals(300, wednesdayAverage);
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
                .closePrice(BigDecimal.valueOf(20))
                .openPrice(BigDecimal.valueOf(10))
                .time(getDate(2020, 8, 25))
                .build());

        // Monday
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(30))
                .openPrice(BigDecimal.valueOf(20))
                .time(getDate(2020, 8, 24))
                .build());

        // Friday
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(40))
                .openPrice(BigDecimal.valueOf(30))
                .time(getDate(2020, 8, 28))
                .build());

        // Wednesday
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(50))
                .openPrice(BigDecimal.valueOf(40))
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
                .closePrice(BigDecimal.valueOf(200))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(300))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(300))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 2))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 2))
                .build());

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(500))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 8, 3))
                .build());

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<Integer, BigDecimal> saldos = (Map) service.getMonthlySaldos(ticker, interval);

        assertEquals(3, saldos.size());

        final BigDecimal average1 = saldos.get(1);
        AssertUtils.assertEquals(200, average1);

        final BigDecimal average2 = saldos.get(2);
        AssertUtils.assertEquals(150, average2);

        final BigDecimal average3 = saldos.get(3);
        AssertUtils.assertEquals(300, average3);
    }

    @Test
    public void getMonthlySaldos_sortsSaldosByDays() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 1);
        final OffsetDateTime to = getDate(2020, 8, 4);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(20))
                .openPrice(BigDecimal.valueOf(10))
                .time(getDate(2020, 8, 2))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(30))
                .openPrice(BigDecimal.valueOf(20))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(40))
                .openPrice(BigDecimal.valueOf(30))
                .time(getDate(2020, 8, 4))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(50))
                .openPrice(BigDecimal.valueOf(40))
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
                .closePrice(BigDecimal.valueOf(200))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(300))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(100))
                .time(getDate(2020, 8, 1))
                .build());

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(300))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 9, 2))
                .build());
        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(400))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 9, 2))
                .build());

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(500))
                .openPrice(BigDecimal.valueOf(200))
                .time(getDate(2020, 10, 3))
                .build());

        mockCandles(ticker, interval, CandleInterval.DAY, candles);

        final Map<Integer, BigDecimal> saldos = (Map) service.getMonthlySaldos(ticker, interval);

        assertEquals(3, saldos.size());

        final BigDecimal average1 = saldos.get(1);
        AssertUtils.assertEquals(200, average1);

        final BigDecimal average2 = saldos.get(2);
        AssertUtils.assertEquals(150, average2);

        final BigDecimal average3 = saldos.get(3);
        AssertUtils.assertEquals(300, average3);
    }

    @Test
    public void getYearlySaldos_sortsSaldosByMonths() {
        final String ticker = TICKER;

        final OffsetDateTime from = getDate(2020, 8, 1);
        final OffsetDateTime to = getDate(2020, 8, 4);
        final Interval interval = Interval.of(from, to);

        final List<Candle> candles = new ArrayList<>();

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(20))
                .openPrice(BigDecimal.valueOf(10))
                .time(getDate(2020, 9, 2))
                .build());

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(30))
                .openPrice(BigDecimal.valueOf(20))
                .time(getDate(2020, 8, 1))
                .build());

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(40))
                .openPrice(BigDecimal.valueOf(30))
                .time(getDate(2020, 11, 4))
                .build());

        candles.add(Candle.builder()
                .closePrice(BigDecimal.valueOf(50))
                .openPrice(BigDecimal.valueOf(40))
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