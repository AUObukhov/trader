package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderStates;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionsData;
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
import java.util.Map;

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
    void processBotConfig_doesNothing_whenThereAreOrders() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();

        final List<OrderState> orders = List.of(TestOrderStates.ORDER_STATE1.orderState());
        Mockito.when(ordersService.getOrders(accountId)).thenReturn(orders);

        final BotConfig botConfig = new BotConfig(
                accountId,
                List.of(figi1, figi2),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                null,
                null
        );

        final OffsetDateTime now = DateUtils.now();
        final Interval interval = Interval.of(now.minusMinutes(3), now);
        bot.processBotConfig(botConfig, interval);

        Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_doesNoOrder_whenThereAreUncompletedOrders() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();

        Mocker.mockEmptyOrder(ordersService, accountId);

        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, null, null, null);

        final OffsetDateTime now = DateUtils.now();
        final Interval interval = Interval.of(now.minusMinutes(3), now);
        bot.processBotConfig(botConfig, interval);

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_doesNoOrder_whenDecisionIsWait() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final OffsetDateTime currentDateTime = DateUtils.now();

        Mocker.mockShares(extInstrumentsService, share1, share2);
        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.currency(), share2.currency());
        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        final Decision decision = new Decision(DecisionAction.WAIT);
        final Map<String, Decision> decisions = Map.of(
                figi1, decision,
                figi2, decision
        );
        Mockito.when(strategy.decide(Mockito.any(DecisionsData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decisions);

        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission = DecimalUtils.setDefaultScale(0.003);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, commission, null, null);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        bot.processBotConfig(botConfig, interval);

        Mockito.verify(strategy, Mockito.times(1))
                .decide(Mockito.any(DecisionsData.class), Mockito.nullable(StrategyCache.class));
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_placesBuyOrder_whenDecisionIsBuy() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final OffsetDateTime currentDateTime = DateUtils.now();

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);
        Mocker.mockShares(extInstrumentsService, share1, share2);
        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.currency(), share2.currency());
        Mocker.mockSecurity(extOperationsService, accountId);

        final List<Operation> operations = List.of(TestData.newOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi1)))
                .thenReturn(operations);
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi2)))
                .thenReturn(operations);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        final Map<String, Decision> decisions = Map.of(figi1, decision, figi2, decision);
        Mockito.when(strategy.decide(Mockito.any(DecisionsData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decisions);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission = DecimalUtils.setDefaultScale(0.003);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, commission, null, null);
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        bot.processBotConfig(botConfig, interval);

        final Long quantity = decision.getQuantity();
        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(accountId, figi1, quantity, null, OrderDirection.ORDER_DIRECTION_BUY, OrderType.ORDER_TYPE_MARKET, null);
        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(accountId, figi2, quantity, null, OrderDirection.ORDER_DIRECTION_BUY, OrderType.ORDER_TYPE_MARKET, null);
    }

    @Test
    void processBotConfig_placesSellOrder_whenDecisionIsSell() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final Share share1 = TestShares.APPLE.share();
        final Share share2 = TestShares.SBER.share();

        final String figi1 = share1.figi();
        final String figi2 = share2.figi();

        final OffsetDateTime currentDateTime = DateUtils.now();

        Mocker.mockShares(extInstrumentsService, share1, share2);
        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.currency(), share2.currency());
        Mocker.mockSecurity(extOperationsService, accountId);

        final List<Operation> operations = List.of(TestData.newOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi1)))
                .thenReturn(operations);
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi2)))
                .thenReturn(operations);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        final Map<String, Decision> decisions = Map.of(figi1, decision, figi2, decision);
        Mockito.when(strategy.decide(Mockito.any(DecisionsData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decisions);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        final List<String> figies = List.of(figi1, figi2);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BigDecimal commission = DecimalUtils.setDefaultScale(0.003);
        final BotConfig botConfig = new BotConfig(accountId, figies, candleInterval, commission, null, null);
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );
        bot.processBotConfig(botConfig, interval);

        final Long quantity = decision.getQuantity();
        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(accountId, figi2, quantity, null, OrderDirection.ORDER_DIRECTION_SELL, OrderType.ORDER_TYPE_MARKET, null);
        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(accountId, figi1, quantity, null, OrderDirection.ORDER_DIRECTION_SELL, OrderType.ORDER_TYPE_MARKET, null);
    }

    private static class TestBot extends Bot {
        public TestBot(
                final ExtMarketDataService extMarketDataService,
                final ExtInstrumentsService extInstrumentsService,
                final ExtOperationsService operationsService,
                final ExtOrdersService ordersService,
                final ExtUsersService usersService,
                final Context context,
                final TradingStrategy strategy
        ) {
            super(
                    extMarketDataService,
                    extInstrumentsService,
                    operationsService,
                    ordersService,
                    usersService,
                    context,
                    strategy
            );
        }
    }

}