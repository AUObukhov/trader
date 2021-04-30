package ru.obukhov.trader.bot.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.Strategy;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

class AbstractBotUnitTest extends BaseMockedTest {

    @Mock
    private Strategy strategy;
    @Mock
    private MarketService marketService;
    @Mock
    private OperationsService operationsService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private PortfolioService portfolioService;

    private AbstractBot bot;

    @BeforeEach
    void setUp() {
        bot = new TestBot(strategy, marketService, operationsService, ordersService, portfolioService);
    }

    @Test
    void processTicker_doesNothing_and_returnsEmptyDecisionData_whenThereAreOrders() {
        String ticker = "ticker";

        List<Order> orders = List.of(new Order());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders);

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData.getBalance());
        Assertions.assertNull(decisionData.getPosition());
        Assertions.assertNull(decisionData.getCurrentCandles());
        Assertions.assertNull(decisionData.getLastOperations());
        Assertions.assertNull(decisionData.getInstrument());

        Mockito.verifyNoMoreInteractions(strategy, operationsService, marketService, portfolioService);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNothing_andReturnNullDecisionData_whenGetOrdersThrowsException() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        Mockito.when(ordersService.getOrders(ticker)).thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);

        Mockito.verifyNoMoreInteractions(strategy, operationsService, marketService, portfolioService);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_andReturnsNull_whenGetInstrumentThrowsException() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        Mockito.when(marketService.getInstrument(ticker)).thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_andReturnsNull_whenGetAvailableBalanceThrowsException() {
        String ticker = "ticker";

        MarketInstrument instrument = prepareEmptyMockedData(ticker);
        Mockito.when(portfolioService.getAvailableBalance(instrument.getCurrency()))
                .thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_andReturnsNull_whenGetPositionThrowsException() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        Mockito.when(portfolioService.getPosition(ticker)).thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_andReturnsNull_whenGetLastCandlesThrowsException() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        Mockito.when(marketService.getLastCandles(
                Mockito.eq(ticker),
                Mockito.anyInt(),
                Mockito.any(CandleResolution.class)
        )).thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_andReturnsNull_whenGetOperationsThrowsException() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_andReturnsNull_whenDecideThrowsException() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        mockCandles(ticker, List.of(new Candle()));

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_returnsNull_whenPlaceMarketOrderThrowsException() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        mockCandles(ticker, List.of(new Candle()));

        Decision decision = new Decision(DecisionAction.BUY, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(decision);

        Mockito.when(ordersService.placeMarketOrder(ticker, decision.getLots(), OperationType.BUY))
                .thenThrow(new IllegalArgumentException());

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNull(decisionData);
    }

    @Test
    void processTicker_doesNoOrder_whenCurrentCandlesIsNull() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        mockCandles(ticker, null);

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNotNull(decisionData);

        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_whenCurrentCandlesIsEmpty() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNotNull(decisionData);

        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_whenDecisionIsWait() {
        String ticker = "ticker";

        prepareEmptyMockedData(ticker);
        mockCandles(ticker, List.of(new Candle()));

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(Decision.WAIT_DECISION);

        DecisionData decisionData = bot.processTicker(ticker);

        Assertions.assertNotNull(decisionData);

        Mockito.verify(strategy, Mockito.times(1)).decide(decisionData);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy() {
        String ticker = "ticker";

        MarketInstrument instrument = TestDataHelper.createAndMockInstrument(marketService, ticker);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(instrument.getCurrency()))
                .thenReturn(balance);

        final PortfolioPosition position = TestDataHelper.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getPosition(ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(new Operation());
        Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle());
        mockCandles(ticker, currentCandles);

        Decision decision = new Decision(DecisionAction.BUY, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(decision);

        DecisionData decisionData = bot.processTicker(ticker);

        AssertUtils.assertEquals(balance, decisionData.getBalance());
        Assertions.assertEquals(position, decisionData.getPosition());
        AssertUtils.assertListsAreEqual(currentCandles, decisionData.getCurrentCandles());
        AssertUtils.assertListsAreEqual(operations, decisionData.getLastOperations());
        Assertions.assertEquals(instrument, decisionData.getInstrument());

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(ticker, decision.getLots(), OperationType.BUY);
    }

    @Test
    void processTicker_returnsFilledData_andPlacesSellOrder_whenDecisionIsSell() {
        String ticker = "ticker";

        MarketInstrument instrument = TestDataHelper.createAndMockInstrument(marketService, ticker);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(instrument.getCurrency()))
                .thenReturn(balance);

        final PortfolioPosition position = TestDataHelper.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getPosition(ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(new Operation());
        Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle());
        mockCandles(ticker, currentCandles);

        Decision decision = new Decision(DecisionAction.SELL, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(decision);

        DecisionData decisionData = bot.processTicker(ticker);

        AssertUtils.assertEquals(balance, decisionData.getBalance());
        Assertions.assertEquals(position, decisionData.getPosition());
        AssertUtils.assertListsAreEqual(currentCandles, decisionData.getCurrentCandles());
        AssertUtils.assertListsAreEqual(operations, decisionData.getLastOperations());
        Assertions.assertEquals(instrument, decisionData.getInstrument());

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(ticker, decision.getLots(), OperationType.SELL);
    }

    private MarketInstrument prepareEmptyMockedData(String ticker) {
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(Collections.emptyList());

        MarketInstrument instrument = TestDataHelper.createAndMockInstrument(marketService, ticker);

        Mockito.when(portfolioService.getAvailableBalance(instrument.getCurrency()))
                .thenReturn(BigDecimal.ZERO);

        Mockito.when(portfolioService.getPosition(ticker))
                .thenReturn(TestDataHelper.createPortfolioPosition(ticker, 0));

        mockCandles(ticker, Collections.emptyList());

        Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(Collections.emptyList());

        return instrument;
    }

    private void mockCandles(String ticker, List<Candle> candles) {
        Mockito.when(marketService.getLastCandles(
                Mockito.eq(ticker),
                Mockito.anyInt(),
                Mockito.any(CandleResolution.class)
        )).thenReturn(candles);
    }

    private void verifyNoOrdersMade() {
        Mockito.verify(ordersService, Mockito.never())
                .placeMarketOrder(Mockito.any(), Mockito.anyInt(), Mockito.any());
    }

    private static class TestBot extends AbstractBot {

        public TestBot(
                Strategy strategy,
                MarketService marketService,
                OperationsService operationsService,
                OrdersService ordersService,
                PortfolioService portfolioService
        ) {
            super(strategy, marketService, operationsService, ordersService, portfolioService);
        }
    }

}