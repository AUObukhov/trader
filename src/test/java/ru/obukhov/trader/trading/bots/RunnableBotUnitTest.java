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
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderStates;
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
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RunnableBotUnitTest {

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
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private BotConfig botConfig;

    @InjectMocks
    private ServicesContainer services;

    @Test
    void run_doesNothing_whenDisabled() {
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(false);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenWrongTradingStatus() {
        final String figi = TestShare1.FIGI;

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenThereAreOrders() {
        final String figi = TestShare1.FIGI;

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final List<OrderState> orders = List.of(TestOrderStates.ORDER_STATE1.orderState());
        Mockito.when(ordersService.getOrders(figi)).thenReturn(orders);

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetLastCandlesThrowsException() {
        final String figi = TestShare1.FIGI;
        final OffsetDateTime currentTimestamp = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(figi, CandleInterval.CANDLE_INTERVAL_1_MIN);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        Mockito.when(extMarketDataService.getLastCandles(
                        Mockito.eq(figi),
                        Mockito.anyInt(),
                        Mockito.eq(CandleInterval.CANDLE_INTERVAL_1_MIN),
                        Mockito.eq(currentTimestamp))
                )
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenGetOrdersThrowsException() {
        final String figi = TestShare1.FIGI;

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);
        Mockito.when(ordersService.getOrders(figi)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetInstrumentThrowsException() {
        final String figi = TestShare1.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(figi, CandleInterval.CANDLE_INTERVAL_1_MIN);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockLastCandles(figi, List.of(candle1));
        Mockito.when(extInstrumentsService.getShare(figi)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetAvailableBalanceThrowsException() {
        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare2.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, figi, CandleInterval.CANDLE_INTERVAL_1_MIN);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockLastCandles(figi, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        Mockito.when(extOperationsService.getAvailableBalance(Mockito.eq(accountId), Mockito.anyString()))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetPositionThrowsException() {
        final String figi = TestShare2.FIGI;
        final String accountId = TestData.ACCOUNT_ID1;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, figi, CandleInterval.CANDLE_INTERVAL_1_MIN);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockLastCandles(figi, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        Mockito.when(extOperationsService.getSecurity(accountId, figi)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetOperationsThrowsException() {
        final String figi = TestShare2.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(null, figi, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockLastCandles(figi, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        Mockito.when(extOperationsService.getOperations(Mockito.anyString(), Mockito.any(Interval.class), Mockito.eq(figi)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenDecideThrowsException() {
        final String figi = TestShare2.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(null, figi, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockLastCandles(figi, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_catchesException_whenPlaceMarketOrderThrowsException() {
        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare2.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, figi, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockLastCandles(figi, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(decision);

        Mockito.when(ordersService.postOrder(
                accountId,
                figi,
                decision.getQuantity(),
                null,
                OrderDirection.ORDER_DIRECTION_BUY,
                OrderType.ORDER_TYPE_MARKET,
                null
        )).thenThrow(new IllegalArgumentException());

        Assertions.assertDoesNotThrow(() -> createRunnableBot().run());
    }

    @Test
    void run_doesNoOrder_whenThereAreOrders() {
        final String figi = TestShare1.FIGI;

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);
        Mocker.mockEmptyOrder(ordersService, figi);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenCurrentCandlesIsEmpty() {
        final String figi = TestShare1.FIGI;

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenDecisionIsWait() {
        final String figi = TestShare2.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(null, figi, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockLastCandles(figi, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy() {
        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare2.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, figi, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);
        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);
        mockData(accountId, figi, TestShare2.CURRENCY);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        createRunnableBot().run();

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
    void run_andPlacesSellOrder_whenDecisionIsSell() {
        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare2.FIGI;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, figi, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);
        Mocker.mockShare(extInstrumentsService, TestShare2.SHARE);
        mockData(accountId, figi, TestShare2.CURRENCY);

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        createRunnableBot().run();

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

    private RunnableBot createRunnableBot() {
        return new RunnableBot(services, context, strategy, schedulingProperties, botConfig);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockBotConfig(final String accountId, final String figi, final CandleInterval candleInterval, final double commission) {
        Mockito.when(botConfig.accountId()).thenReturn(accountId);
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
        Mockito.when(botConfig.commission()).thenReturn(DecimalUtils.setDefaultScale(commission));
    }

    @SuppressWarnings("SameParameterValue")
    private void mockBotConfig(final String accountId, final String figi, final CandleInterval candleInterval) {
        Mockito.when(botConfig.accountId()).thenReturn(accountId);
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockBotConfig(final String figi, final CandleInterval candleInterval) {
        Mockito.when(botConfig.figi()).thenReturn(figi);
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockData(final String accountId, final String figi, final String currency) {
        final BigDecimal balance = DecimalUtils.setDefaultScale(10000);
        Mockito.when(extOperationsService.getAvailableBalance(accountId, currency))
                .thenReturn(balance);

        final Position position = new PositionBuilder().setFigi(figi).build();
        Mockito.when(extOperationsService.getSecurity(accountId, figi))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.newOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi)))
                .thenReturn(operations);

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockLastCandles(figi, List.of(candle));
    }

    private void mockLastCandles(final String figi, final List<Candle> candles) {
        Mockito.when(extMarketDataService.getLastCandles(
                Mockito.eq(figi),
                Mockito.anyInt(),
                Mockito.any(CandleInterval.class),
                Mockito.any(OffsetDateTime.class)
        )).thenReturn(candles);
    }

}