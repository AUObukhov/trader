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
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.Candle;
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
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ScheduledBotUnitTest {

    @Mock
    private MarketService marketService;
    @Mock
    private OperationsService operationsService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private PortfolioService portfolioService;
    @Mock
    private RealTinkoffService realTinkoffService;
    @Mock
    private TradingStrategy strategy;
    @Mock
    private SchedulingProperties schedulingProperties;
    @Mock
    private ScheduledBotProperties scheduledBotProperties;
    @Mock
    private MarketProperties marketProperties;

    @InjectMocks
    private ScheduledBot bot;

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenDisabled() {
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(false);

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenNotWorkTime() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().plusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenThereAreOrders() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        final List<Order> orders1 = List.of(new Order());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders1);

        bot.tick();

        Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetLastCandlesThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        Mockito.when(marketService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleResolution.class)))
                .thenThrow(new IllegalArgumentException());

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenGetOrdersThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        Mockito.when(ordersService.getOrders(ticker)).thenThrow(new IllegalArgumentException());

        bot.tick();

        Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetInstrumentThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mockito.when(marketService.getInstrument(ticker)).thenThrow(new IllegalArgumentException());

        bot.tick();

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetAvailableBalanceThrowsException(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        Mockito.when(portfolioService.getAvailableBalance(Mockito.eq(brokerAccountId), Mockito.any(Currency.class)))
                .thenThrow(new IllegalArgumentException());

        bot.tick();

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetPositionThrowsException(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        Mockito.when(portfolioService.getPosition(brokerAccountId, ticker)).thenThrow(new IllegalArgumentException());

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetOperationsThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        Mockito.when(operationsService.getOperations(Mockito.anyString(), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenThrow(new IllegalArgumentException());
        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenDecideThrowsException() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenThrow(new IllegalArgumentException());

        bot.tick();

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings({"unused", "java:S2699"})
    void tick_catchesException_whenPlaceMarketOrderThrowsException(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        final Decision decision = new Decision(DecisionAction.BUY, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(decision);

        Mockito.when(ordersService.placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.BUY))
                .thenThrow(new IllegalArgumentException());

        bot.tick();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenThereAreOrders() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        Mocker.mockEmptyOrder(ordersService, ticker);

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenCurrentCandlesIsEmpty() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenDecisionIsWait() {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        mockBotConfig(null, ticker, CandleResolution._1MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final int lotSize = 10;

        Mocker.createAndMockInstrument(marketService, ticker, lotSize);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        bot.tick();

        verifyNoOrdersMade();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        final int lotSize = 10;
        mockBotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.0);
        mockData(brokerAccountId, ticker, lotSize);

        final Decision decision = new Decision(DecisionAction.BUY, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        bot.tick();

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.BUY);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_andPlacesSellOrder_whenDecisionIsSell(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        final int lotSize = 10;
        mockBotConfig(brokerAccountId, ticker, CandleResolution._1MIN, 0.0);
        mockData(brokerAccountId, ticker, lotSize);

        final Decision decision = new Decision(DecisionAction.SELL, 5);
        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                .thenReturn(decision);

        bot.tick();

        Mockito.verify(ordersService, Mockito.times(1))
                .placeMarketOrder(brokerAccountId, ticker, decision.getLots(), OperationType.SELL);
    }

    private void mockBotConfig(
            @Nullable final String brokerAccountId,
            final String ticker,
            final CandleResolution candleResolution,
            final double commission
    ) {
        final BotConfig botConfig = BotConfig.builder()
                .brokerAccountId(brokerAccountId)
                .ticker(ticker)
                .candleResolution(candleResolution)
                .commission(commission)
                .build();

        Mockito.when(scheduledBotProperties.getBotConfig()).thenReturn(botConfig);
    }

    private void mockData(@Nullable final String brokerAccountId, final String ticker, final int lotSize) {
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

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockCandles(ticker, List.of(candle));
    }

    private void mockCandles(final String ticker, List<Candle> candles) {
        Mockito.when(marketService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleResolution.class)))
                .thenReturn(candles);
    }

    private void verifyNoOrdersMade() {
        Mockito.verify(ordersService, Mockito.never())
                .placeMarketOrder(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.any());
    }

}