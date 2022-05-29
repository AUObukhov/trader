package ru.obukhov.trader.trading.bots.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AbstractBotUnitTest {

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
    private TinkoffService tinkoffService;
    @Mock
    private TradingStrategy strategy;

    @InjectMocks
    private TestBot bot;

    @Test
    void processTicker_doesNothing_andReturnsEmptyList_whenThereAreOrders() throws IOException {
        final String accountId = "2000124699";
        final String ticker = "ticker";

        final List<Order> orders = List.of(TestData.createOrder());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders);

        final BotConfig botConfig = BotConfig.builder()
                .accountId(accountId)
                .ticker(ticker)
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertTrue(candles.isEmpty());

        Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_whenThereAreUncompletedOrders() throws IOException {
        final String accountId = "2000124699";

        final String ticker = "ticker";

        Mocker.mockEmptyOrder(ordersService, ticker);

        final BotConfig botConfig = BotConfig.builder()
                .accountId(accountId)
                .ticker(ticker)
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_whenCurrentCandlesIsEmpty() throws IOException {
        final String accountId = "2000124699";
        final String ticker = "ticker";

        final BotConfig botConfig = BotConfig.builder()
                .accountId(accountId)
                .ticker(ticker)
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_whenFirstOfCurrentCandlesHasPreviousStartTime() throws IOException {
        final String accountId = "2000124699";
        final String ticker = "ticker";

        final OffsetDateTime previousStartTime = OffsetDateTime.now();
        final Candle candle = new Candle().setTime(previousStartTime);
        mockCandles(ticker, List.of(candle));

        final BotConfig botConfig = BotConfig.builder()
                .accountId(accountId)
                .ticker(ticker)
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, previousStartTime);

        Assertions.assertNotNull(candles);

        verifyNoOrdersMade();
    }

    @Test
    void processTicker_doesNoOrder_whenDecisionIsWait() throws IOException {
        final String accountId = "2000124699";
        final String ticker = "ticker";
        final int lotSize = 10;

        final Share share = TestData.createShare(ticker, Currency.RUB, lotSize);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockCandles(ticker, List.of(candle));

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        final BotConfig botConfig = BotConfig.builder()
                .accountId(accountId)
                .ticker(ticker)
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.003)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        Mockito.verify(strategy, Mockito.times(1))
                .decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class));
        verifyNoOrdersMade();
    }

    @Test
    void processTicker_returnsCandles_andPlacesBuyOrder_whenDecisionIsBuy() throws IOException {
        final String accountId = "2000124699";
        final String ticker = "ticker";
        final Currency currency = Currency.USD;
        final int lotSize = 10;

        final Share share = TestData.createShare(ticker, currency, lotSize);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(accountId, currency))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getSecurity(accountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.createOperation());
        Mockito.when(operationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle().setTime(OffsetDateTime.now()));
        mockCandles(ticker, currentCandles);

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = BotConfig.builder()
                .accountId(accountId)
                .ticker(ticker)
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.003)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        AssertUtils.assertEquals(currentCandles, candles);

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(accountId, ticker, decision.getQuantityLots(), OperationType.OPERATION_TYPE_BUY);
    }

    @Test
    void processTicker_returnsCandles_andPlacesSellOrder_whenDecisionIsSell() throws IOException {
        final String accountId = "2000124699";
        final String ticker = "ticker";
        final Currency currency = Currency.USD;
        final int lotSize = 10;

        final Share share = TestData.createShare(ticker, currency, lotSize);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(accountId, currency))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getSecurity(accountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.createOperation());
        Mockito.when(operationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle().setTime(OffsetDateTime.now()));
        mockCandles(ticker, currentCandles);

        Mockito.when(tinkoffService.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = BotConfig.builder()
                .accountId(accountId)
                .ticker(ticker)
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.003)
                .build();

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        AssertUtils.assertEquals(currentCandles, candles);

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(accountId, ticker, decision.getQuantityLots(), OperationType.OPERATION_TYPE_SELL);
    }

    private void mockCandles(final String ticker, final List<Candle> candles) throws IOException {
        Mockito.when(marketService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleInterval.class)))
                .thenReturn(candles);
    }

    private void verifyNoOrdersMade() throws IOException {
        Mockito.verify(ordersService, Mockito.never())
                .placeMarketOrder(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.any());
    }

    private static final class TestStrategyCache implements StrategyCache {
    }

    private static class TestBot extends AbstractBot {
        public TestBot(
                final MarketService marketService,
                final MarketInstrumentsService marketInstrumentsService,
                final MarketOperationsService operationsService,
                final MarketOrdersService ordersService,
                final PortfolioService portfolioService,
                final TinkoffService tinkoffService,
                final TradingStrategy strategy
        ) {
            super(
                    marketService,
                    marketInstrumentsService,
                    operationsService,
                    ordersService,
                    portfolioService,
                    tinkoffService,
                    strategy,
                    new TestStrategyCache()
            );
        }
    }

}