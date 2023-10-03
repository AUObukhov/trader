package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderStates;
import ru.obukhov.trader.test.utils.model.share.TestShares;
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
import ru.tinkoff.piapi.core.models.Position;

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
    private ExtOrdersService ordersService;
    @Mock
    private Context context;
    @Mock
    private TradingStrategy strategy;

    @InjectMocks
    private TestBot bot;

    @Test
    void processBotConfig_doesNothing_andReturnsEmptyList_whenThereAreOrders() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String figi = TestShares.APPLE.share().figi();

        final List<OrderState> orders = List.of(TestOrderStates.ORDER_STATE1.orderState());
        Mockito.when(ordersService.getOrders(accountId)).thenReturn(orders);

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                null,
                null
        );

        final List<Candle> allCandles = TestData.newCandles(List.of(1, 2, 3), OffsetDateTime.now());
        final List<Candle> candles = bot.processBotConfig(botConfig, allCandles);

        Assertions.assertTrue(candles.isEmpty());

        Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_doesNoOrder_whenThereAreUncompletedOrders() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final String figi = TestShares.APPLE.share().figi();

        Mocker.mockEmptyOrder(ordersService, accountId);

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                null,
                null
        );

        final List<Candle> allCandles = TestData.newCandles(List.of(1, 2, 3), OffsetDateTime.now());
        final List<Candle> candles = bot.processBotConfig(botConfig, allCandles);

        Assertions.assertNotNull(candles);

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_doesNoOrder_whenDecisionIsWait() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mocker.mockShare(extInstrumentsService, share);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );

        final List<Candle> allCandles = List.of(
                new Candle().setTime(currentDateTime.minusMinutes(1)),
                new Candle().setTime(currentDateTime),
                new Candle().setTime(currentDateTime.plusMinutes(1))
        );
        final List<Candle> candles = bot.processBotConfig(botConfig, allCandles);

        Assertions.assertNotNull(candles);

        Mockito.verify(strategy, Mockito.times(1))
                .decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class));
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_returnsCandles_andPlacesBuyOrder_whenDecisionIsBuy() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.APPLE.share();
        final String figi = share.figi();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        Mocker.mockShare(extInstrumentsService, share);

        final BigDecimal balance = DecimalUtils.setDefaultScale(10000);
        Mockito.when(extOperationsService.getAvailableBalance(accountId, share.currency()))
                .thenReturn(balance);

        final Position portfolioPosition = new PositionBuilder().setFigi(figi).build();

        Mockito.when(extOperationsService.getSecurity(accountId, figi))
                .thenReturn(portfolioPosition);

        final List<Operation> operations = List.of(TestData.newOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi)))
                .thenReturn(operations);

        final Candle candle = new Candle().setTime(currentDateTime.minusMinutes(1));
        final List<Candle> allCandles = List.of(
                candle,
                new Candle().setTime(currentDateTime),
                new Candle().setTime(currentDateTime.plusMinutes(1))
        );

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );

        final List<Candle> actualCandles = bot.processBotConfig(botConfig, allCandles);

        final List<Candle> expectedResult = List.of(candle);
        AssertUtils.assertEquals(expectedResult, actualCandles);

        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(
                        accountId,
                        figi,
                        decision.getQuantity(),
                        null,
                        OrderDirection.ORDER_DIRECTION_BUY,
                        OrderType.ORDER_TYPE_MARKET,
                        null
                );
    }

    @Test
    void processBotConfig_returnsCandles_andPlacesSellOrder_whenDecisionIsSell() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mocker.mockShare(extInstrumentsService, share);

        final BigDecimal balance = DecimalUtils.setDefaultScale(10000);
        Mockito.when(extOperationsService.getAvailableBalance(accountId, share.currency()))
                .thenReturn(balance);

        final Position position = new PositionBuilder().setFigi(figi).build();
        Mockito.when(extOperationsService.getSecurity(accountId, figi))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.newOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi)))
                .thenReturn(operations);

        final Candle candle = new Candle().setTime(currentDateTime.minusMinutes(1));
        final List<Candle> allCandles = List.of(
                candle,
                new Candle().setTime(currentDateTime),
                new Candle().setTime(currentDateTime.plusMinutes(1))
        );

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );

        final List<Candle> actualCandles = bot.processBotConfig(botConfig, allCandles);

        final List<Candle> expectedResult = List.of(candle);
        AssertUtils.assertEquals(expectedResult, actualCandles);

        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(
                        accountId,
                        figi,
                        decision.getQuantity(),
                        null,
                        OrderDirection.ORDER_DIRECTION_SELL,
                        OrderType.ORDER_TYPE_MARKET,
                        null
                );
    }

    private static final class TestStrategyCache implements StrategyCache {
    }

    private static class TestBot extends Bot {
        public TestBot(
                final ExtMarketDataService extMarketDataService,
                final ExtInstrumentsService extInstrumentsService,
                final ExtOperationsService operationsService,
                final ExtOrdersService ordersService,
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