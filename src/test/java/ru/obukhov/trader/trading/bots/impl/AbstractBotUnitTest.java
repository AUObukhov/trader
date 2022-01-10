package ru.obukhov.trader.trading.bots.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AbstractBotUnitTest {

    @Mock
    private MarketService marketService;
    @Mock
    private OperationsService operationsService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private PortfolioService portfolioService;
    @Mock
    private TinkoffService tinkoffService;
    @Mock
    private TradingStrategy strategy;

    @InjectMocks
    private TestBot bot;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void processTicker_doesNothing_andReturnsEmptyList_whenThereAreOrders(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";

        final List<Order> orders = List.of(new Order());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders);

        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(CandleResolution._1MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertTrue(candles.isEmpty());

        Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void processTicker_doesNoOrder_whenThereAreUncompletedOrders(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";

        Mocker.mockEmptyOrder(ordersService, ticker);

        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(CandleResolution._1MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void processTicker_doesNoOrder_whenCurrentCandlesIsEmpty(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";

        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(CandleResolution._1MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void processTicker_doesNoOrder_whenFirstOfCurrentCandlesHasPreviousStartTime(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";

        final OffsetDateTime previousStartTime = OffsetDateTime.now();
        final Candle candle = new Candle().setTime(previousStartTime);
        mockCandles(ticker, List.of(candle));

        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(CandleResolution._1MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, previousStartTime);

        Assertions.assertNotNull(candles);

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void processTicker_doesNoOrder_whenDecisionIsWait(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";
        final int lotSize = 10;

        Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockCandles(ticker, List.of(candle));

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(CandleResolution._1MIN)
                .commission(0.003)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        Mockito.verify(strategy, Mockito.times(1))
                .decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class));
        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void processTicker_returnsCandles_andPlacesBuyOrder_whenDecisionIsBuy(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";
        final int lotSize = 10;

        final MarketInstrument instrument = Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(brokerAccountId, instrument.getCurrency()))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getPosition(brokerAccountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(new Operation());
        Mockito.when(operationsService.getOperations(Mockito.eq(brokerAccountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle().setTime(OffsetDateTime.now()));
        mockCandles(ticker, currentCandles);

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final Decision decision = new Decision(DecisionAction.BUY, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(CandleResolution._1MIN)
                .commission(0.003)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        AssertUtils.assertListsAreEqual(currentCandles, candles);

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.BUY);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void processTicker_returnsCandles_andPlacesSellOrder_whenDecisionIsSell(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";
        final int lotSize = 10;

        final MarketInstrument instrument = Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(brokerAccountId, instrument.getCurrency()))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getPosition(brokerAccountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(new Operation());
        Mockito.when(operationsService.getOperations(Mockito.eq(brokerAccountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle().setTime(OffsetDateTime.now()));
        mockCandles(ticker, currentCandles);

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final Decision decision = new Decision(DecisionAction.SELL, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(CandleResolution._1MIN)
                .commission(0.003)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        AssertUtils.assertListsAreEqual(currentCandles, candles);

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.SELL);
    }

    private void mockCandles(final String ticker, final List<Candle> candles) {
        Mockito.when(marketService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleResolution.class)))
                .thenReturn(candles);
    }

    private void verifyNoOrdersMade() {
        Mockito.verify(ordersService, Mockito.never())
                .placeMarketOrder(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.any());
    }

    private static final class TestStrategyCache implements StrategyCache {
    }

    private static class TestBot extends AbstractBot {
        public TestBot(
                final MarketService marketService,
                final OperationsService operationsService,
                final OrdersService ordersService,
                final PortfolioService portfolioService,
                final TinkoffService tinkoffService,
                final TradingStrategy strategy
        ) {
            super(marketService, operationsService, ordersService, portfolioService, tinkoffService, strategy, new TestStrategyCache());
        }
    }

}