package ru.obukhov.investor.common.service.impl;

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
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static ru.obukhov.investor.common.util.DateUtils.getDate;

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

        when(marketService.getCandles(ticker, interval, candleInterval)).thenReturn(candles);

        final List<Candle> candlesResponse = service.getCandles(ticker, interval, candleInterval);

        Assert.assertSame(candles, candlesResponse);
    }

}