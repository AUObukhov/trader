package ru.obukhov.trader.trading.bots.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
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
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ScheduledBotUnitTest {

    @Mock
    private TradingStrategy strategy;

    @Mock
    private MarketService marketService;

    @Mock
    private OperationsService operationsService;

    @Mock
    private OrdersService ordersService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private ScheduledBotProperties scheduledBotProperties;

    @Mock
    private TradingProperties tradingProperties;

    private ScheduledBot bot;

    @BeforeEach
    void setUp() {
        Mockito.when(scheduledBotProperties.getCandleResolution()).thenReturn(CandleResolution._1MIN);
        bot = new ScheduledBot(
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                strategy,
                scheduledBotProperties,
                tradingProperties
        );
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenDisabled() {
        Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(false);

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenNotWorkTime() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().plusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenNoNoTickers() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));
            mockTickers();

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenThereAreOrders() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final List<Order> orders1 = List.of(new Order());
            Mockito.when(ordersService.getOrders(ticker1)).thenReturn(orders1);
            final List<Order> orders2 = List.of(new Order());
            Mockito.when(ordersService.getOrders(ticker2)).thenReturn(orders2);

            bot.tick();

            Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
            Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetLastCandlesThrowsException() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            Mockito.when(marketService.getLastCandles(Mockito.eq(ticker1), Mockito.anyInt(), Mockito.any(CandleResolution.class)))
                    .thenThrow(new IllegalArgumentException());
            Mockito.when(marketService.getLastCandles(Mockito.eq(ticker2), Mockito.anyInt(), Mockito.any(CandleResolution.class)))
                    .thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenGetOrdersThrowsException() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            Mockito.when(ordersService.getOrders(ticker1)).thenThrow(new IllegalArgumentException());
            Mockito.when(ordersService.getOrders(ticker2)).thenThrow(new IllegalArgumentException());

            bot.tick();

            Mockito.verifyNoMoreInteractions(operationsService, marketService, portfolioService);
            Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetInstrumentThrowsException() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final Candle candle1 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle1));
            final Candle candle2 = new Candle().setTime(mockedNow);
            mockCandles(ticker2, List.of(candle2));

            Mockito.when(marketService.getInstrument(ticker1)).thenThrow(new IllegalArgumentException());
            Mockito.when(marketService.getInstrument(ticker2)).thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetAvailableBalanceThrowsException(@Nullable final String brokerAccountId) {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final Candle candle1 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle1));
            final Candle candle2 = new Candle().setTime(mockedNow);
            mockCandles(ticker2, List.of(candle2));

            final int lotSize = 10;

            Mocker.createAndMockInstrument(marketService, ticker1, lotSize);
            Mocker.createAndMockInstrument(marketService, ticker2, lotSize);

            Mockito.when(portfolioService.getAvailableBalance(Mockito.eq(brokerAccountId), Mockito.any(Currency.class)))
                    .thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetPositionThrowsException(@Nullable final String brokerAccountId) {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final Candle candle1 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle1));
            final Candle candle2 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle2));

            final int lotSize = 10;

            Mocker.createAndMockInstrument(marketService, ticker1, lotSize);
            Mocker.createAndMockInstrument(marketService, ticker2, lotSize);

            Mockito.when(portfolioService.getPosition(brokerAccountId, ticker1)).thenThrow(new IllegalArgumentException());
            Mockito.when(portfolioService.getPosition(brokerAccountId, ticker2)).thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetOperationsThrowsException() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final Candle candle1 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle1));
            final Candle candle2 = new Candle().setTime(mockedNow);
            mockCandles(ticker2, List.of(candle2));

            final int lotSize = 10;

            Mocker.createAndMockInstrument(marketService, ticker1, lotSize);
            Mocker.createAndMockInstrument(marketService, ticker2, lotSize);

            Mockito.when(operationsService.getOperations(Mockito.anyString(), Mockito.any(Interval.class), Mockito.eq(ticker1)))
                    .thenThrow(new IllegalArgumentException());
            Mockito.when(operationsService.getOperations(Mockito.anyString(), Mockito.any(Interval.class), Mockito.eq(ticker2)))
                    .thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenDecideThrowsException() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final Candle candle1 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle1));
            final Candle candle2 = new Candle().setTime(mockedNow);
            mockCandles(ticker2, List.of(candle2));

            final int lotSize = 10;

            Mocker.createAndMockInstrument(marketService, ticker1, lotSize);
            Mocker.createAndMockInstrument(marketService, ticker2, lotSize);

            Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                    .thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings({"unused", "java:S2699"})
    void tick_catchesException_whenPlaceMarketOrderThrowsException(@Nullable final String brokerAccountId) {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final Candle candle1 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle1));
            final Candle candle2 = new Candle().setTime(mockedNow);
            mockCandles(ticker2, List.of(candle2));

            final int lotSize = 10;

            Mocker.createAndMockInstrument(marketService, ticker1, lotSize);
            Mocker.createAndMockInstrument(marketService, ticker2, lotSize);

            final Decision decision = new Decision(DecisionAction.BUY, 5);
            Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                    .thenReturn(decision);

            Mockito.when(ordersService.placeMarketOrder(brokerAccountId, ticker1, decision.getLots(), OperationType.BUY))
                    .thenThrow(new IllegalArgumentException());

            bot.tick();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenThereAreOrders() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            Mocker.mockEmptyOrder(ordersService, ticker1);
            Mocker.mockEmptyOrder(ordersService, ticker2);

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenCurrentCandlesIsEmpty() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenDecisionIsWait() {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            final Candle candle1 = new Candle().setTime(mockedNow);
            mockCandles(ticker1, List.of(candle1));
            final Candle candle2 = new Candle().setTime(mockedNow);
            mockCandles(ticker2, List.of(candle2));

            final int lotSize = 10;

            Mocker.createAndMockInstrument(marketService, ticker1, lotSize);
            Mocker.createAndMockInstrument(marketService, ticker2, lotSize);

            Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                    .thenReturn(new Decision(DecisionAction.WAIT));

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy(@Nullable final String brokerAccountId) {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(scheduledBotProperties.getBrokerAccountId()).thenReturn(brokerAccountId);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            final int lotSize = 10;
            mockTickers(ticker1, ticker2);
            mockData(brokerAccountId, ticker1, lotSize);
            mockData(brokerAccountId, ticker2, lotSize);

            final Decision decision = new Decision(DecisionAction.BUY, 5);
            Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                    .thenReturn(decision);

            bot.tick();

            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(brokerAccountId, ticker1, decision.getLots(), OperationType.BUY);
            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(brokerAccountId, ticker2, decision.getLots(), OperationType.BUY);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    @SuppressWarnings("unused")
    void tick_andPlacesSellOrder_whenDecisionIsSell(@Nullable final String brokerAccountId) {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 6);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            Mockito.when(scheduledBotProperties.isEnabled()).thenReturn(true);
            Mockito.when(scheduledBotProperties.getBrokerAccountId()).thenReturn(brokerAccountId);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            final String ticker1 = "ticker1";
            final String ticker2 = "ticker2";
            final int lotSize = 10;
            mockTickers(ticker1, ticker2);
            mockData(brokerAccountId, ticker1, lotSize);
            mockData(brokerAccountId, ticker2, lotSize);

            final Decision decision = new Decision(DecisionAction.SELL, 5);
            Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.nullable(StrategyCache.class)))
                    .thenReturn(decision);

            bot.tick();

            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(brokerAccountId, ticker1, decision.getLots(), OperationType.SELL);
            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(brokerAccountId, ticker2, decision.getLots(), OperationType.SELL);
        }
    }

    private void mockTickers(String... tickers) {
        Mockito.when(scheduledBotProperties.getTickers()).thenReturn(Set.of(tickers));
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