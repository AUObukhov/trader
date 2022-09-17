package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.RealExtOrdersService;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BotUnitTest {

    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @Mock
    private ExtOperationsService extOperationsService;
    @Mock
    private RealExtOrdersService ordersService;
    @Mock
    private Context context;
    @Mock
    private TradingStrategy strategy;

    @InjectMocks
    private TestBot bot;

    @Test
    void processTicker_doesNothing_andReturnsEmptyList_whenThereAreOrders() {
        final String accountId = "2000124699";
        final String ticker = TestShare1.TICKER;

        final List<Order> orders = List.of(TestData.createOrder());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders);

        final BotConfig botConfig = new BotConfig(
                accountId,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                null,
                null
        );

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertTrue(candles.isEmpty());

        Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processTicker_doesNoOrder_whenThereAreUncompletedOrders() {
        final String accountId = "2000124699";

        final String ticker = TestShare1.TICKER;

        Mocker.mockEmptyOrder(ordersService, ticker);

        final BotConfig botConfig = new BotConfig(
                accountId,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                null,
                null
        );

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processTicker_doesNoOrder_whenCurrentCandlesIsEmpty() {
        final String accountId = "2000124699";
        final String ticker = TestShare1.TICKER;

        final BotConfig botConfig = new BotConfig(
                accountId,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                null,
                null
        );

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processTicker_doesNoOrder_whenFirstOfCurrentCandlesHasPreviousStartTime() {
        final String accountId = "2000124699";
        final String ticker = TestShare1.TICKER;

        final OffsetDateTime previousStartTime = OffsetDateTime.now();
        final Candle candle = new Candle().setTime(previousStartTime);
        mockCandles(ticker, List.of(candle));
        Mocker.mockCurrentDateTime(context);

        final BotConfig botConfig = new BotConfig(
                accountId,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                null,
                null
        );

        final List<Candle> candles = bot.processBotConfig(botConfig, previousStartTime);

        Assertions.assertNotNull(candles);

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processTicker_doesNoOrder_whenDecisionIsWait() {
        final String accountId = "2000124699";
        final String ticker = TestShare2.TICKER;

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockCandles(ticker, List.of(candle));

        Mockito.when(context.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        final BotConfig botConfig = new BotConfig(
                accountId,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                null,
                null
        );

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        Assertions.assertNotNull(candles);

        Mockito.verify(strategy, Mockito.times(1))
                .decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class));
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processTicker_returnsCandles_andPlacesBuyOrder_whenDecisionIsBuy() {
        final String accountId = "2000124699";
        final String ticker = TestShare1.TICKER;

        Mocker.mockShare(extInstrumentsService, TestShare1.createShare());

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(extOperationsService.getAvailableBalance(accountId, TestShare1.CURRENCY))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(extOperationsService.getSecurity(accountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.createOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle().setTime(OffsetDateTime.now()));
        mockCandles(ticker, currentCandles);

        Mockito.when(context.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = new BotConfig(
                accountId,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                null,
                null
        );

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        AssertUtils.assertEquals(currentCandles, candles);

        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(
                        accountId,
                        ticker,
                        decision.getQuantityLots(),
                        null,
                        OrderDirection.ORDER_DIRECTION_BUY,
                        OrderType.ORDER_TYPE_MARKET,
                        null
                );
    }

    @Test
    void processTicker_returnsCandles_andPlacesSellOrder_whenDecisionIsSell() {
        final String accountId = "2000124699";
        final String ticker = TestShare2.TICKER;

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(extOperationsService.getAvailableBalance(accountId, TestShare2.CURRENCY))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(extOperationsService.getSecurity(accountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.createOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle().setTime(OffsetDateTime.now()));
        mockCandles(ticker, currentCandles);

        Mockito.when(context.getCurrentDateTime()).thenReturn(OffsetDateTime.now());

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = new BotConfig(
                accountId,
                ticker,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                null,
                null
        );

        final List<Candle> candles = bot.processBotConfig(botConfig, null);

        AssertUtils.assertEquals(currentCandles, candles);

        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(
                        accountId,
                        ticker,
                        decision.getQuantityLots(),
                        null,
                        OrderDirection.ORDER_DIRECTION_SELL,
                        OrderType.ORDER_TYPE_MARKET,
                        null
                );
    }

    private void mockCandles(final String ticker, final List<Candle> candles) {
        Mockito.when(extMarketDataService.getLastCandles(
                Mockito.eq(ticker),
                Mockito.anyInt(),
                Mockito.any(CandleInterval.class),
                Mockito.any(OffsetDateTime.class))
        ).thenReturn(candles);
    }

    private static final class TestStrategyCache implements StrategyCache {
    }

    private static class TestBot extends Bot {
        public TestBot(
                final ExtMarketDataService extMarketDataService,
                final ExtInstrumentsService extInstrumentsService,
                final ExtOperationsService operationsService,
                final RealExtOrdersService ordersService,
                final Context context,
                final TradingStrategy strategy
        ) {
            super(
                    extMarketDataService,
                    extInstrumentsService,
                    operationsService,
                    ordersService,
                    context,
                    strategy,
                    new TestStrategyCache()
            );
        }
    }

}