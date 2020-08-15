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
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.obukhov.investor.util.DateUtils.getDate;

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