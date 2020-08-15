package ru.obukhov.investor.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import ru.obukhov.investor.BaseTest;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
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
public class InvestServiceImplTest extends BaseTest {

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
                .token("token")
                .ticker("ticker")
                .tickerType(TickerType.ETF)
                .from(getDate(2020, 1, 1))
                .to(getDate(2020, 2, 1))
                .candleInterval(CandleInterval.ONE_MIN)
                .build();

        mockMarketService(request.getToken());
        List<Candle> candles = new ArrayList<>();
        mockCandles(request.getTicker(),
                request.getTickerType(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval(),
                candles);

        List<Candle> candlesResponse = service.getCandles(request);

        Assert.assertSame(candles, candlesResponse);
    }

    @Test
    public void getStatistics() {
        GetStatisticsRequest request = GetStatisticsRequest.builder()
                .token("token")
                .ticker("ticker")
                .from(getDate(2020, 1, 1))
                .to(getDate(2020, 1, 4))
                .tickerType(TickerType.ETF)
                .build();

        mockMarketService(request.getToken());

        List<Candle> candlesDay1 = new ArrayList<>();
        candlesDay1.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getTime(10, 0, 0))
                .build());
        candlesDay1.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getTime(11, 0, 0))
                .build());
        candlesDay1.add(Candle.builder()
                .saldo(BigDecimal.valueOf(100))
                .time(getTime(12, 0, 0))
                .build());
        mockCandles(request.getTicker(),
                request.getTickerType(),
                getDate(2020, 1, 1),
                getDate(2020, 1, 2),
                CandleInterval.ONE_MIN,
                candlesDay1);

        List<Candle> candlesDay2 = new ArrayList<>();
        candlesDay2.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getTime(11, 0, 0))
                .build());
        candlesDay2.add(Candle.builder()
                .saldo(BigDecimal.valueOf(200))
                .time(getTime(12, 0, 0))
                .build());
        mockCandles(request.getTicker(),
                request.getTickerType(),
                getDate(2020, 1, 2),
                getDate(2020, 1, 3),
                CandleInterval.ONE_MIN,
                candlesDay2);

        List<Candle> candlesDay3 = new ArrayList<>();
        candlesDay3.add(Candle.builder()
                .saldo(BigDecimal.valueOf(300))
                .time(getTime(12, 0, 0))
                .build());
        mockCandles(request.getTicker(),
                request.getTickerType(),
                getDate(2020, 1, 3),
                getDate(2020, 1, 4),
                CandleInterval.ONE_MIN,
                candlesDay3);

        Map<LocalTime, BigDecimal> statistics = service.getStatistics(request);

        assertEquals(3, statistics.size());

        final BigDecimal average1 = statistics.get(LocalTime.of(10, 0));
        assertTrue(numbersEqual(average1, 100));

        final BigDecimal average2 = statistics.get(LocalTime.of(11, 0));
        assertTrue(numbersEqual(average2, 150));

        final BigDecimal average3 = statistics.get(LocalTime.of(12, 0));
        assertTrue(numbersEqual(average3, 200));
    }

    private void mockMarketService(String token) {
        when(appContext.getBean(eq(MarketService.class), eq(connectionService), eq(token)))
                .thenReturn(marketService);
    }

    private void mockCandles(String ticker,
                             TickerType tickerType,
                             OffsetDateTime from,
                             OffsetDateTime to,
                             CandleInterval candleInterval,
                             List<Candle> candles) {

        when(marketService.getMarketCandles(eq(ticker), eq(tickerType), eq(from), eq(to), eq(candleInterval)))
                .thenReturn(candles);
    }

}