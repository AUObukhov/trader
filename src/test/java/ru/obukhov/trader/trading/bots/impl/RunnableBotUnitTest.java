package ru.obukhov.trader.trading.bots.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RunnableBotUnitTest {

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
    private RealTinkoffService realTinkoffService;
    @Mock
    private TradingStrategy strategy;
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private BotConfig botConfig;
    @Mock
    private MarketProperties marketProperties;

    @InjectMocks
    private TinkoffServices tinkoffServices;

    @Test
    @SuppressWarnings("unused")
    void run_doesNothing_whenDisabled() throws IOException {
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(false);

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNothing_whenNotWorkTime() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().plusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNothing_whenThereAreOrders() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker);

        final List<Order> orders1 = List.of(TestData.createOrder());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders1);

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenGetLastCandlesThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        Mockito.when(marketService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleInterval.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNothing_whenGetOrdersThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker);

        Mockito.when(ordersService.getOrders(ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenGetInstrumentThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mockito.when(marketInstrumentsService.getShare(ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        verifyNoOrdersMade();
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenGetAvailableBalanceThrowsException(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(brokerAccountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        final Share share = Share.newBuilder()
                .setTicker(ticker)
                .setLot(lotSize)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(portfolioService.getAvailableBalance(Mockito.eq(brokerAccountId), Mockito.anyString()))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenGetPositionThrowsException(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(brokerAccountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        final Share share = Share.newBuilder()
                .setTicker(ticker)
                .setLot(lotSize)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(portfolioService.getPosition(brokerAccountId, ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenGetOperationsThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        final Share share = Share.newBuilder()
                .setTicker(ticker)
                .setLot(lotSize)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(operationsService.getOperations(Mockito.anyString(), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenDecideThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        final Share share = Share.newBuilder()
                .setTicker(ticker)
                .setLot(lotSize)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings({"unused", "java:S2699"})
    void run_catchesException_whenPlaceMarketOrderThrowsException(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(brokerAccountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        final Share share = Share.newBuilder()
                .setTicker(ticker)
                .setLot(lotSize)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(decision);

        Mockito.when(ordersService.placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.OPERATION_TYPE_BUY))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenThereAreOrders() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        mockBotConfig(null, ticker);

        Mocker.mockEmptyOrder(ordersService, ticker);

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenCurrentCandlesIsEmpty() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void run_doesNoOrder_whenDecisionIsWait() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        mockBotConfig(null, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        final Share share = Share.newBuilder()
                .setTicker(ticker)
                .setLot(lotSize)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        createRunnableBot().run();

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void run_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        final int lotSize = 10;
        mockBotConfig(brokerAccountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        mockData(brokerAccountId, ticker, lotSize);

        final Decision decision = new Decision(DecisionAction.BUY, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        createRunnableBot().run();

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.OPERATION_TYPE_BUY);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void run_andPlacesSellOrder_whenDecisionIsSell(@Nullable final String brokerAccountId) throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        final int lotSize = 10;
        mockBotConfig(brokerAccountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        mockData(brokerAccountId, ticker, lotSize);

        final Decision decision = new Decision(DecisionAction.SELL, 5L);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        createRunnableBot().run();

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.OPERATION_TYPE_SELL);
    }

    private RunnableBot createRunnableBot() {
        return new RunnableBot(tinkoffServices, strategy, schedulingProperties, botConfig, marketProperties);
    }

    private void mockBotConfig(
            @Nullable final String brokerAccountId,
            final String ticker,
            final CandleInterval candleInterval,
            final double commission
    ) {
        if (brokerAccountId != null) {
            Mockito.when(botConfig.getBrokerAccountId()).thenReturn(brokerAccountId);
        }
        Mockito.when(botConfig.getTicker()).thenReturn(ticker);
        Mockito.when(botConfig.getCandleInterval()).thenReturn(candleInterval);
        Mockito.when(botConfig.getCommission()).thenReturn(commission);
    }

    private void mockBotConfig(@Nullable final String brokerAccountId, final String ticker, final CandleInterval candleInterval) {
        if (brokerAccountId != null) {
            Mockito.when(botConfig.getBrokerAccountId()).thenReturn(brokerAccountId);
        }
        Mockito.when(botConfig.getTicker()).thenReturn(ticker);
        Mockito.when(botConfig.getCandleInterval()).thenReturn(candleInterval);
    }

    private void mockBotConfig(@Nullable final String brokerAccountId, final String ticker) {
        if (brokerAccountId != null) {
            Mockito.when(botConfig.getBrokerAccountId()).thenReturn(brokerAccountId);
        }
        Mockito.when(botConfig.getTicker()).thenReturn(ticker);
    }

    private void mockData(@Nullable final String brokerAccountId, final String ticker, final int lotSize) throws IOException {
        final Share share = Share.newBuilder()
                .setTicker(ticker)
                .setLot(lotSize)
                .build();
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(brokerAccountId, share.getCurrency()))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getPosition(brokerAccountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.createOperation());
        Mockito.when(operationsService.getOperations(Mockito.eq(brokerAccountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockCandles(ticker, List.of(candle));
    }

    private void mockCandles(final String ticker, List<Candle> candles) throws IOException {
        Mockito.when(marketService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleInterval.class)))
                .thenReturn(candles);
    }

    private void verifyNoOrdersMade() throws IOException {
        Mockito.verify(ordersService, Mockito.never())
                .placeMarketOrder(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.any());
    }

}