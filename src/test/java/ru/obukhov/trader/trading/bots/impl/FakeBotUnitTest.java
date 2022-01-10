package ru.obukhov.trader.trading.bots.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;

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
    private OperationsService operationsService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private PortfolioService portfolioService;
    @Mock
    private TradingStrategy strategy;
    @Mock
    private FakeTinkoffService fakeTinkoffService;

    @InjectMocks
    private FakeBot fakeBot;

    @Test
    void searchMarketInstrument() {
        final String ticker = "ticker";
        final MarketInstrument expectedInstrument = Mocker.createAndMockInstrument(fakeTinkoffService, ticker, 10);

        final MarketInstrument instrument = fakeBot.searchMarketInstrument(ticker);

        Assertions.assertEquals(expectedInstrument, instrument);
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getInvestments(final String brokerAccountId) {
        final Currency currency = Currency.RUB;
        final SortedMap<OffsetDateTime, BigDecimal> expectedInvestments = new TreeMap<>();
        expectedInvestments.put(OffsetDateTime.now(), BigDecimal.TEN);
        Mockito.when(fakeTinkoffService.getInvestments(brokerAccountId, currency)).thenReturn(expectedInvestments);

        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeBot.getInvestments(brokerAccountId, currency);

        AssertUtils.assertMapsAreEqual(expectedInvestments, investments);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getCurrentBalance(final String brokerAccountId) {
        final Currency currency = Currency.RUB;
        final BigDecimal expectedBalance = BigDecimal.TEN;
        Mockito.when(fakeTinkoffService.getCurrentBalance(brokerAccountId, currency)).thenReturn(expectedBalance);

        final BigDecimal balance = fakeBot.getCurrentBalance(brokerAccountId, currency);

        AssertUtils.assertEquals(expectedBalance, balance);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations(final String brokerAccountId) {
        final Interval interval = Interval.of(OffsetDateTime.now(), OffsetDateTime.now());
        final String ticker = "ticker";
        final List<Operation> expectedOperations = new ArrayList<>();
        Mockito.when(fakeTinkoffService.getOperations(brokerAccountId, interval, ticker)).thenReturn(expectedOperations);

        final List<Operation> operations = fakeBot.getOperations(brokerAccountId, interval, ticker);

        AssertUtils.assertListsAreEqual(expectedOperations, operations);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioPositions(final String brokerAccountId) {
        final List<PortfolioPosition> expectedPositions = new ArrayList<>();
        Mockito.when(fakeTinkoffService.getPortfolioPositions(brokerAccountId)).thenReturn(expectedPositions);

        final List<PortfolioPosition> positions = fakeBot.getPortfolioPositions(brokerAccountId);

        AssertUtils.assertListsAreEqual(expectedPositions, positions);
    }

    @Test
    void getCurrentPrice() {
        final String ticker = "ticker";
        final BigDecimal expectedCurrentPrice = BigDecimal.TEN;
        Mockito.when(fakeTinkoffService.getCurrentPrice(ticker)).thenReturn(expectedCurrentPrice);

        final BigDecimal currentPrice = fakeBot.getCurrentPrice(ticker);

        AssertUtils.assertEquals(expectedCurrentPrice, currentPrice);
    }

}