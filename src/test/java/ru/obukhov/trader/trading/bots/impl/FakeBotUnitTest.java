package ru.obukhov.trader.trading.bots.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@ExtendWith(MockitoExtension.class)
class FakeBotUnitTest {

    @Mock
    private MarketService marketService;
    @Mock
    private MarketInstrumentsService marketInstrumentsService;
    @Mock
    private MarketOperationsService operationsService;
    @Mock
    private MarketOrdersService ordersService;
    @Mock
    private PortfolioService portfolioService;
    @Mock
    private TradingStrategy strategy;
    @Mock
    private FakeTinkoffService fakeTinkoffService;

    @InjectMocks
    private FakeBot fakeBot;

    @Test
    void getShare() {
        final String ticker = "ticker";
        final Share expectedShare = Share.newBuilder()
                .setTicker(ticker)
                .setLot(10)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(expectedShare);

        final Share share = fakeBot.getShare(ticker);

        Assertions.assertEquals(expectedShare, share);
    }

    @Test
    void getCurrentDateTime() {
        final OffsetDateTime expectedCurrentDateTime = OffsetDateTime.now();
        Mockito.when(fakeTinkoffService.getCurrentDateTime()).thenReturn(expectedCurrentDateTime);

        final OffsetDateTime currentDateTime = fakeBot.getCurrentDateTime();

        Assertions.assertEquals(expectedCurrentDateTime, currentDateTime);
    }

    @Test
    void nextMinute() {
        final OffsetDateTime expectedNextMinute = OffsetDateTime.now();
        Mockito.when(fakeTinkoffService.nextMinute()).thenReturn(expectedNextMinute);

        final OffsetDateTime nextMinute = fakeBot.nextMinute();

        Assertions.assertEquals(expectedNextMinute, nextMinute);
    }

    @Test
    void getInvestments() {
        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;
        final SortedMap<OffsetDateTime, BigDecimal> expectedInvestments = new TreeMap<>();
        expectedInvestments.put(OffsetDateTime.now(), BigDecimal.TEN);
        Mockito.when(fakeTinkoffService.getInvestments(accountId, currency)).thenReturn(expectedInvestments);

        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeBot.getInvestments(accountId, currency);

        AssertUtils.assertMapsAreEqual(expectedInvestments, investments);
    }

    @Test
    void getCurrentBalance() {
        final String accountId = "2000124699";
        final Currency currency = Currency.RUB;
        final BigDecimal expectedBalance = BigDecimal.TEN;
        Mockito.when(fakeTinkoffService.getCurrentBalance(accountId, currency)).thenReturn(expectedBalance);

        final BigDecimal balance = fakeBot.getCurrentBalance(accountId, currency);

        AssertUtils.assertEquals(expectedBalance, balance);
    }

    @Test
    void getOperations() throws IOException {
        final String accountId = "2000124699";
        final Interval interval = Interval.of(OffsetDateTime.now(), OffsetDateTime.now());
        final String ticker = "ticker";
        final List<Operation> expectedOperations = new ArrayList<>();
        Mockito.when(fakeTinkoffService.getOperations(accountId, interval, ticker)).thenReturn(expectedOperations);

        final List<Operation> operations = fakeBot.getOperations(accountId, interval, ticker);

        AssertUtils.assertEquals(expectedOperations, operations);
    }

    @Test
    void getPortfolioPositions() {
        final String accountId = "2000124699";
        final List<PortfolioPosition> expectedPositions = new ArrayList<>();
        Mockito.when(fakeTinkoffService.getPortfolioPositions(accountId)).thenReturn(expectedPositions);

        final List<PortfolioPosition> positions = fakeBot.getPortfolioPositions(accountId);

        AssertUtils.assertEquals(expectedPositions, positions);
    }

    @Test
    void getCurrentPrice() throws IOException {
        final String ticker = "ticker";
        final BigDecimal expectedCurrentPrice = BigDecimal.TEN;
        Mockito.when(fakeTinkoffService.getCurrentPrice(ticker)).thenReturn(expectedCurrentPrice);

        final BigDecimal currentPrice = fakeBot.getCurrentPrice(ticker);

        AssertUtils.assertEquals(expectedCurrentPrice, currentPrice);
    }

}