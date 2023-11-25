package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
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
    void processBotConfig_doesNothing_whenThereAreOrders() {
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

        final OffsetDateTime now = OffsetDateTime.now();
        final Interval interval = Interval.of(now.minusMinutes(3), now);
        bot.processBotConfig(botConfig, interval);

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

        final OffsetDateTime now = OffsetDateTime.now();
        final Interval interval = Interval.of(now.minusMinutes(3), now);
        bot.processBotConfig(botConfig, interval);

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_doesNoOrder_whenDecisionIsWait() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final Share share = TestShares.SBER.share();
        final String figi = share.figi();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        Mocker.mockShare(extInstrumentsService, share);

        final BigDecimal balance = DecimalUtils.setDefaultScale(10000);
        Mockito.when(extOperationsService.getAvailableBalance(accountId, share.currency()))
                .thenReturn(balance);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.anyLong(), Mockito.nullable(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );

        Mockito.when(extMarketDataService.getLastPrice(figi, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        bot.processBotConfig(botConfig, interval);

        Mockito.verify(strategy, Mockito.times(1))
                .decide(Mockito.any(DecisionData.class), Mockito.anyLong(), Mockito.nullable(StrategyCache.class));
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void processBotConfig_placesBuyOrder_whenDecisionIsBuy() {
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

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.anyLong(), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        Mockito.when(extMarketDataService.getLastPrice(figi, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        bot.processBotConfig(botConfig, interval);

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
    void processBotConfig_placesSellOrder_whenDecisionIsSell() {
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

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.anyLong(), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        Mockito.when(extMarketDataService.getLastPrice(figi, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        final BotConfig botConfig = new BotConfig(
                accountId,
                figi,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.setDefaultScale(0.003),
                null,
                null
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );
        bot.processBotConfig(botConfig, interval);

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