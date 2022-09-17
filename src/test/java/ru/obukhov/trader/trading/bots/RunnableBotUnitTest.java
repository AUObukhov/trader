package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.RealContext;
import ru.obukhov.trader.market.impl.RealExtOrdersService;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.PortfolioPositionBuilder;
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
import java.time.Duration;
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
    private RealExtOrdersService ordersService;
    @Mock
    private RealContext realContext;
    @Mock
    private TradingStrategy strategy;
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private BotConfig botConfig;
    @Mock
    private MarketProperties marketProperties;

    @InjectMocks
    private ServicesContainer services;

    @Test
    void run_doesNothing_whenDisabled() {
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(false);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenNotWorkTime() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().plusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenThereAreOrders() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare1.TICKER;

        Mockito.when(botConfig.ticker()).thenReturn(ticker);

        final List<Order> orders = List.of(TestData.createOrder());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders);

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetLastCandlesThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare1.TICKER;

        mockBotConfig(ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        Mockito.when(extMarketDataService.getLastCandles(
                        Mockito.eq(ticker),
                        Mockito.anyInt(),
                        Mockito.eq(CandleInterval.CANDLE_INTERVAL_1_MIN),
                        Mockito.eq(currentDateTime))
                )
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenGetOrdersThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare1.TICKER;

        Mockito.when(botConfig.ticker()).thenReturn(ticker);

        Mockito.when(ordersService.getOrders(ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(extOperationsService, extMarketDataService, extOperationsService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetInstrumentThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare1.TICKER;

        mockBotConfig(ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mockito.when(extInstrumentsService.getSingleShare(ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetAvailableBalanceThrowsException() {
        final String accountId = TestData.ACCOUNT_ID1;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;

        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        Mockito.when(extOperationsService.getAvailableBalance(Mockito.eq(accountId), Mockito.any(Currency.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetPositionThrowsException() {
        final String accountId = TestData.ACCOUNT_ID1;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;

        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        Mockito.when(extOperationsService.getSecurity(accountId, ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetOperationsThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;

        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        Mockito.when(extOperationsService.getOperations(Mockito.anyString(), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenDecideThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;

        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    @SuppressWarnings("java:S2699")
    void run_catchesException_whenPlaceMarketOrderThrowsException() {
        final String accountId = TestData.ACCOUNT_ID1;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;

        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(decision);

        Mockito.when(ordersService.postOrder(
                accountId,
                ticker,
                decision.getQuantityLots(),
                null,
                OrderDirection.ORDER_DIRECTION_BUY,
                OrderType.ORDER_TYPE_MARKET,
                null
        )).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();
    }

    @Test
    void run_doesNoOrder_whenThereAreOrders() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare1.TICKER;
        Mockito.when(botConfig.ticker()).thenReturn(ticker);

        Mocker.mockEmptyOrder(ordersService, ticker);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenCurrentCandlesIsEmpty() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenDecisionIsWait() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;
        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy() {
        final String accountId = TestData.ACCOUNT_ID1;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;
        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());
        mockData(accountId, ticker, TestShare2.CURRENCY);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        createRunnableBot().run();

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
    void run_andPlacesSellOrder_whenDecisionIsSell() {
        final String accountId = TestData.ACCOUNT_ID1;
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realContext.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = TestShare2.TICKER;
        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        Mocker.mockShare(extInstrumentsService, TestShare2.createShare());
        mockData(accountId, ticker, TestShare2.CURRENCY);

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        createRunnableBot().run();

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

    private RunnableBot createRunnableBot() {
        return new RunnableBot(services, realContext, strategy, schedulingProperties, botConfig, marketProperties);
    }

    private void mockBotConfig(
            final String accountId,
            final String ticker,
            final CandleInterval candleInterval,
            final double commission
    ) {
        Mockito.when(botConfig.accountId()).thenReturn(accountId);
        Mockito.when(botConfig.ticker()).thenReturn(ticker);
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
        Mockito.when(botConfig.commission()).thenReturn(commission);
    }

    private void mockBotConfig(final String accountId, final String ticker, final CandleInterval candleInterval) {
        Mockito.when(botConfig.accountId()).thenReturn(accountId);
        Mockito.when(botConfig.ticker()).thenReturn(ticker);
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
    }

    private void mockBotConfig(final String ticker, final CandleInterval candleInterval) {
        Mockito.when(botConfig.ticker()).thenReturn(ticker);
        Mockito.when(botConfig.candleInterval()).thenReturn(candleInterval);
    }

    private void mockData(final String accountId, final String ticker, final Currency currency) {
        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(extOperationsService.getAvailableBalance(accountId, currency))
                .thenReturn(balance);

        final PortfolioPosition portfolioPosition = new PortfolioPositionBuilder()
                .setTicker(ticker)
                .setQuantityLots(0)
                .build();
        Mockito.when(extOperationsService.getSecurity(accountId, ticker))
                .thenReturn(portfolioPosition);

        final List<Operation> operations = List.of(TestData.createOperation());
        Mockito.when(extOperationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockCandles(ticker, List.of(candle));
    }

    private void mockCandles(final String ticker, final List<Candle> candles) {
        Mockito.when(extMarketDataService.getLastCandles(
                Mockito.eq(ticker),
                Mockito.anyInt(),
                Mockito.any(CandleInterval.class),
                Mockito.any(OffsetDateTime.class)
        )).thenReturn(candles);
    }

}