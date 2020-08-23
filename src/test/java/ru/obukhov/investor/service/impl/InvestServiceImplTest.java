package ru.obukhov.investor.service.impl;

import com.google.common.collect.Ordering;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetStatisticsRequest;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
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
public class InvestServiceImplTest extends BaseMockedTest {

    private static final String TOKEN = "token";
    private static final String TICKER = "ticker";

    @Mock
    private ApplicationContext appContext;
    @Mock
    private ConnectionService connectionService;
    @Mock
    private MarketService marketService;

    private InvestServiceImpl service;

    @Before
    public void setUp() {
        service = new InvestServiceImpl(appContext, connectionService);
    }

    @Test
    public void getCandles_returnsCandlesFromMarketService() {
        GetCandlesRequest request = GetCandlesRequest.builder()
                .token(TOKEN)
                .ticker(TICKER)
                .from(getDate(2020, 1, 1))
                .to(getDate(2020, 2, 1))
                .candleInterval(CandleInterval.ONE_MIN)
                .build();

        mockMarketService(request.getToken());
        List<Candle> candles = new ArrayList<>();
        mockCandles(request.getTicker(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval(),
                candles);

        List<Candle> candlesResponse = service.getCandles(request);

        Assert.assertSame(candles, candlesResponse);
    }

    @Test
    public void getSaldos_unitesSaldosByTime() {
        GetStatisticsRequest request = GetStatisticsRequest.builder()
                .token(TOKEN)
                .ticker(TICKER)
                .from(getDate(2020, 1, 1))
                .to(getDate(2020, 1, 4))
                .build();

        mockMarketService(request.getToken());

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
        mockCandles(request.getTicker(),
                getDate(2020, 1, 1),
                getDate(2020, 1, 4),
                CandleInterval.ONE_MIN,
                candles);

        Map<LocalTime, BigDecimal> saldos = service.getSaldos(request);

        assertEquals(3, saldos.size());

        final BigDecimal average1 = saldos.get(LocalTime.of(10, 0));
        assertTrue(numbersEqual(average1, 100));

        final BigDecimal average2 = saldos.get(LocalTime.of(11, 0));
        assertTrue(numbersEqual(average2, 150));

        final BigDecimal average3 = saldos.get(LocalTime.of(12, 0));
        assertTrue(numbersEqual(average3, 200));
    }

    @Test
    public void getSaldos_sortsSaldosByDays() {
        GetStatisticsRequest request = GetStatisticsRequest.builder()
                .token(TOKEN)
                .ticker(TICKER)
                .from(getDate(2020, 1, 1))
                .to(getDate(2020, 1, 4))
                .build();

        mockMarketService(request.getToken());

        List<Candle> candles = new ArrayList<>();
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getTime(12, 0, 0))
                .build());
        candles.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getTime(12, 0, 0))
                .build());
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
        mockCandles(request.getTicker(),
                getDate(2020, 1, 1),
                getDate(2020, 1, 4),
                CandleInterval.ONE_MIN,
                candles);

        Map<LocalTime, BigDecimal> saldos = service.getSaldos(request);

        assertEquals(3, saldos.size());

        assertTrue(Ordering.<LocalTime>natural().isOrdered(saldos.keySet()));
    }

    private void mockMarketService(String token) {
        when(appContext.getBean(eq(MarketService.class), eq(connectionService), eq(token)))
                .thenReturn(marketService);
    }

    private void mockCandles(String ticker,
                             OffsetDateTime from,
                             OffsetDateTime to,
                             CandleInterval candleInterval,
                             List<Candle> candles) {

        when(marketService.getCandles(eq(ticker), eq(from), eq(to), eq(candleInterval)))
                .thenReturn(candles);
    }

}