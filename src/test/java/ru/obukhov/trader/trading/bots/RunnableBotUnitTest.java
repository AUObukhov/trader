package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.order_state.TestOrderStates;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionsData;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @ParameterizedTest
    @EnumSource(value = SecurityTradingStatus.class, names = {
            "SECURITY_TRADING_STATUS_UNSPECIFIED",
            "SECURITY_TRADING_STATUS_NOT_AVAILABLE_FOR_TRADING",
            "SECURITY_TRADING_STATUS_OPENING_PERIOD",
            "SECURITY_TRADING_STATUS_CLOSING_PERIOD",
            "SECURITY_TRADING_STATUS_BREAK_IN_TRADING",
            "SECURITY_TRADING_STATUS_CLOSING_AUCTION",
            "SECURITY_TRADING_STATUS_DARK_POOL_AUCTION",
            "SECURITY_TRADING_STATUS_DISCRETE_AUCTION",
            "SECURITY_TRADING_STATUS_OPENING_AUCTION_PERIOD",
            "SECURITY_TRADING_STATUS_TRADING_AT_CLOSING_AUCTION_PRICE",
            "SECURITY_TRADING_STATUS_SESSION_ASSIGNED",
            "SECURITY_TRADING_STATUS_SESSION_CLOSE",
            "SECURITY_TRADING_STATUS_SESSION_OPEN",
            "SECURITY_TRADING_STATUS_DEALER_NORMAL_TRADING",
            "SECURITY_TRADING_STATUS_DEALER_BREAK_IN_TRADING",
            "SECURITY_TRADING_STATUS_DEALER_NOT_AVAILABLE_FOR_TRADING",
            "UNRECOGNIZED",
    })
    void run_doesNothing_whenWrongTradingStatus(final SecurityTradingStatus wrongTradingStatus) {
        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();
        final String figi3 = TestShares.YANDEX.getFigi();

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figies()).thenReturn(List.of(figi1, figi2, figi3));

        mockNormalTradingStatus(figi1, figi3);
        Mockito.when(extMarketDataService.getTradingStatus(figi2)).thenReturn(wrongTradingStatus);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenThereAreOrders() {
        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();
        final String figi3 = TestShares.YANDEX.getFigi();

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figies()).thenReturn(List.of(figi1, figi2, figi3));
        Mockito.when(botConfig.candleInterval()).thenReturn(CandleInterval.CANDLE_INTERVAL_1_MIN);
        mockNormalTradingStatus(figi1, figi2, figi3);

        final List<OrderState> orders = List.of(TestOrderStates.ORDER_STATE1.orderState());
        Mockito.when(ordersService.getOrders(figi1)).thenReturn(Collections.emptyList());
        Mockito.when(ordersService.getOrders(figi2)).thenReturn(orders);
        Mockito.when(ordersService.getOrders(figi3)).thenReturn(Collections.emptyList());

        final OffsetDateTime mockedNow = DateUtils.now();
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            createRunnableBot().run();

            Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
            Mockito.verify(strategy, Mockito.never()).decide(
                    Mockito.any(DecisionsData.class),
                    Mockito.any(BotConfig.class),
                    Mockito.any(Interval.class)
            );
            Mocker.verifyNoOrdersMade(ordersService);
        }
    }

    @Test
    void run_doesNothing_whenGetOrdersThrowsException() {
        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figies()).thenReturn(List.of(figi1, figi2));
        Mockito.when(botConfig.candleInterval()).thenReturn(CandleInterval.CANDLE_INTERVAL_1_MIN);
        mockNormalTradingStatus(figi1, figi2);

        Mockito.when(ordersService.getOrders(figi1)).thenReturn(Collections.emptyList());
        Mockito.when(ordersService.getOrders(figi2)).thenThrow(new IllegalArgumentException());

        final OffsetDateTime mockedNow = DateUtils.now();
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            createRunnableBot().run();

            Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
            Mockito.verify(strategy, Mockito.never()).decide(
                    Mockito.any(DecisionsData.class),
                    Mockito.any(BotConfig.class),
                    Mockito.any(Interval.class)
            );
            Mocker.verifyNoOrdersMade(ordersService);
        }
    }

    @Test
    void run_doesNoOrder_whenGetAvailableBalancesThrowsException() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, CandleInterval.CANDLE_INTERVAL_1_MIN, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockShares(extInstrumentsService, share1, share2);
        Mockito.when(extOperationsService.getAvailableBalances(accountId)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetSecurityThrowsException() {
        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        final String accountId = TestAccounts.TINKOFF.getId();

        final OffsetDateTime currentDateTime = DateTimeTestData.newDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.003, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.getCurrency());
        Mocker.mockShares(extInstrumentsService, share1, share2);

        Mockito.when(extOperationsService.getSecurity(accountId, figi1)).thenReturn(Position.builder().build());
        Mockito.when(extOperationsService.getSecurity(accountId, figi2)).thenThrow(new IllegalArgumentException());

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetOperationsThrowsException() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockShares(extInstrumentsService, share1, share2);

        mockOperations(accountId, figi1);
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi2)))
                .thenThrow(new IllegalArgumentException());

        Mockito.when(context.getCurrentDateTime()).thenReturn(DateTimeTestData.newDateTime(2020, 9, 23, 6));

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetLastPriceThrowsException() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        final OffsetDateTime currentDateTime = DateTimeTestData.newDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.getCurrency(), share2.getCurrency());
        Mocker.mockShares(extInstrumentsService, share1, share2);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenThrow(new IllegalArgumentException());

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenDecideThrowsException() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        final OffsetDateTime currentDateTime = DateTimeTestData.newDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.getCurrency(), share2.getCurrency());
        Mocker.mockShares(extInstrumentsService, share1, share2);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        Mockito.when(strategy.decide(
                Mockito.any(DecisionsData.class),
                Mockito.any(BotConfig.class),
                Mockito.any(Interval.class)
        )).thenThrow(new IllegalArgumentException());

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_catchesException_whenPostOrderThrowsException() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        final OffsetDateTime currentDateTime = DateTimeTestData.newDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);

        Mocker.mockShares(extInstrumentsService, share1, share2);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(
                Mockito.any(DecisionsData.class),
                Mockito.any(BotConfig.class),
                Mockito.any(Interval.class)
        )).thenReturn(Map.of(share1.getFigi(), decision, share2.getFigi(), decision));

        final int balance = 10000;
        final String[] currencies = {share1.getCurrency(), share2.getCurrency()};
        Mocker.mockAvailableBalances(extOperationsService, accountId, balance, currencies);

        final long quantity = decision.getQuantity();
        final OrderDirection orderDirection = OrderDirection.ORDER_DIRECTION_BUY;
        final BigDecimal price = null;
        final OrderType orderType = OrderType.ORDER_TYPE_MARKET;
        final String orderId = null;
        Mockito.when(ordersService.postOrder(accountId, figi2, quantity, price, orderDirection, orderType, orderId))
                .thenThrow(new IllegalArgumentException());

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        Assertions.assertDoesNotThrow(() -> createRunnableBot().run());
    }

    @Test
    void run_doesNoOrder_whenThereAreOrders() {
        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figies()).thenReturn(List.of(figi1, figi2));
        Mockito.when(botConfig.candleInterval()).thenReturn(CandleInterval.CANDLE_INTERVAL_1_MIN);
        mockNormalTradingStatus(figi1, figi2);

        Mocker.mockEmptyOrder(ordersService, figi1);
        Mocker.mockEmptyOrder(ordersService, figi2);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenCurrentCandlesIsEmpty() {
        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        Mockito.when(botConfig.figies()).thenReturn(List.of(figi1, figi2));
        Mockito.when(botConfig.candleInterval()).thenReturn(CandleInterval.CANDLE_INTERVAL_1_MIN);
        mockNormalTradingStatus(figi1, figi2);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenDecisionIsWait() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        final OffsetDateTime currentDateTime = DateTimeTestData.newDateTime(2020, 9, 23, 6);

        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(null, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.getCurrency(), share2.getCurrency());
        Mocker.mockShares(extInstrumentsService, share1, share2);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        final Map<String, Decision> decisions = Map.of(
                figi1, new Decision(DecisionAction.WAIT),
                figi2, new Decision(DecisionAction.WAIT)
        );
        Mockito.when(strategy.decide(
                Mockito.any(DecisionsData.class),
                Mockito.any(BotConfig.class),
                Mockito.any(Interval.class)
        )).thenReturn(decisions);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        final OffsetDateTime currentDateTime = DateTimeTestData.newDateTime(2020, 9, 23, 6);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockShares(extInstrumentsService, share1, share2);

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        Mocker.mockAvailableBalances(extOperationsService, accountId, 10000, share1.getCurrency(), share2.getCurrency());
        Mocker.mockSecurity(extOperationsService, accountId);
        mockOperations(accountId, figi1, figi2);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(
                        Mockito.any(DecisionsData.class),
                        Mockito.any(BotConfig.class),
                        Mockito.any(Interval.class)
                ))
                .thenReturn(Map.of(figi1, decision, figi2, decision));

        createRunnableBot().run();

        final long quantity = decision.getQuantity();
        final OrderDirection orderDirection = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType orderType = OrderType.ORDER_TYPE_MARKET;
        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(accountId, figi2, quantity, null, orderDirection, orderType, null);
    }

    @Test
    void run_andPlacesSellOrder_whenDecisionIsSell() {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;

        final String figi1 = share1.getFigi();
        final String figi2 = share2.getFigi();

        final OffsetDateTime currentDateTime = DateTimeTestData.newDateTime(2020, 9, 23, 6);

        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        mockBotConfig(accountId, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0, figi1, figi2);
        mockNormalTradingStatus(figi1, figi2);
        Mocker.mockShares(extInstrumentsService, share1, share2);
        final int balance = 10000;
        final String[] currencies = {share1.getCurrency(), share2.getCurrency()};
        Mocker.mockAvailableBalances(extOperationsService, accountId, balance, currencies);
        Mocker.mockSecurity(extOperationsService, accountId);
        mockOperations(accountId, figi1, figi2);

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(
                Mockito.any(DecisionsData.class),
                Mockito.any(BotConfig.class),
                Mockito.any(Interval.class)
        )).thenReturn(Map.of(figi1, decision, figi2, decision));

        Mockito.when(extMarketDataService.getPrice(figi1, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(100));
        Mockito.when(extMarketDataService.getPrice(figi2, currentDateTime)).thenReturn(DecimalUtils.setDefaultScale(200));

        createRunnableBot().run();

        final long quantity = decision.getQuantity();
        final OrderDirection orderDirection = OrderDirection.ORDER_DIRECTION_SELL;
        final OrderType orderType = OrderType.ORDER_TYPE_MARKET;
        Mockito.verify(ordersService, Mockito.times(1))
                .postOrder(accountId, figi2, quantity, null, orderDirection, orderType, null);
    }

    private RunnableBot createRunnableBot() {
        return new RunnableBot(services, context, strategy, schedulingProperties, botConfig);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockBotConfig(final String accountId, final CandleInterval candleInterval, final double commission, final String... figies) {
        Mockito.when(botConfig.accountId()).thenReturn(accountId);
        Mockito.when(botConfig.figies()).thenReturn(Arrays.asList(figies));
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
        Mockito.when(botConfig.commission()).thenReturn(DecimalUtils.setDefaultScale(commission));
    }

    @SuppressWarnings("SameParameterValue")
    private void mockBotConfig(final CandleInterval candleInterval, final double commission, final String... figies) {
        Mockito.when(botConfig.figies()).thenReturn(Arrays.asList(figies));
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
        Mockito.when(botConfig.commission()).thenReturn(DecimalUtils.setDefaultScale(commission));
    }

    @SuppressWarnings("SameParameterValue")
    private void mockBotConfig(final String accountId, final CandleInterval candleInterval, final String... figies) {
        Mockito.when(botConfig.accountId()).thenReturn(accountId);
        Mockito.when(botConfig.figies()).thenReturn(Arrays.asList(figies));
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
    }

    private void mockNormalTradingStatus(final String... figies) {
        for (final String figi : figies) {
            Mockito.when(extMarketDataService.getTradingStatus(figi)).thenReturn(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING);
        }
    }

    private void mockOperations(final String accountId, final String... figies) {
        final List<Operation> operations = List.of(TestData.newOperation());
        for (final String figi : figies) {
            Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(figi)))
                    .thenReturn(operations);
        }
    }

}