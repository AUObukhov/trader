package ru.obukhov.trader.bot.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class ScheduledBotUnitTest extends BaseMockedTest {

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
    private BotConfig botConfig;

    @Mock
    private TradingProperties tradingProperties;

    private ScheduledBot bot;

    @BeforeEach
    void setUp() {
        this.bot = new ScheduledBot(
                strategy,
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                botConfig,
                tradingProperties
        );
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenDisabled() {
        Mockito.when(botConfig.isEnabled()).thenReturn(false);

        bot.tick();

        verifyNoOrdersMade();
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenNotWorkTime() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().plusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenNoNoTickers() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
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
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            List<Order> orders1 = List.of(new Order());
            Mockito.when(ordersService.getOrders(ticker1)).thenReturn(orders1);
            List<Order> orders2 = List.of(new Order());
            Mockito.when(ordersService.getOrders(ticker2)).thenReturn(orders2);

            bot.tick();

            Mockito.verifyNoMoreInteractions(strategy, operationsService, marketService, portfolioService);
            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNothing_whenGetOrdersThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            mockCandles(ticker1, List.of(new Candle()));
            Mockito.when(ordersService.getOrders(ticker1)).thenThrow(new IllegalArgumentException());

            prepareEmptyMockedData(ticker2);
            mockCandles(ticker2, List.of(new Candle()));
            Mockito.when(ordersService.getOrders(ticker2)).thenThrow(new IllegalArgumentException());

            bot.tick();

            Mockito.verifyNoMoreInteractions(strategy, operationsService, marketService, portfolioService);
            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetInstrumentThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            Mockito.when(marketService.getInstrument(ticker1)).thenThrow(new IllegalArgumentException());
            mockCandles(ticker1, List.of(new Candle()));

            prepareEmptyMockedData(ticker2);
            Mockito.when(marketService.getInstrument(ticker2)).thenThrow(new IllegalArgumentException());
            mockCandles(ticker2, List.of(new Candle()));

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetAvailableBalanceThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            MarketInstrument instrument1 = prepareEmptyMockedData(ticker1);
            Mockito.when(portfolioService.getAvailableBalance(instrument1.getCurrency()))
                    .thenThrow(new IllegalArgumentException());
            mockCandles(ticker1, List.of(new Candle()));

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetPositionThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            mockCandles(ticker1, List.of(new Candle()));
            Mockito.when(portfolioService.getPosition(ticker1)).thenThrow(new IllegalArgumentException());
            Mockito.when(portfolioService.getPosition(ticker2)).thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetLastCandlesThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            Mockito.when(marketService.getLastCandles(
                    Mockito.eq(ticker1),
                    Mockito.anyInt(),
                    Mockito.any(CandleResolution.class)
            )).thenThrow(new IllegalArgumentException());
            prepareEmptyMockedData(ticker2);
            Mockito.when(marketService.getLastCandles(
                    Mockito.eq(ticker2),
                    Mockito.anyInt(),
                    Mockito.any(CandleResolution.class)
            )).thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenGetOperationsThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            mockCandles(ticker1, List.of(new Candle()));
            Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker1)))
                    .thenThrow(new IllegalArgumentException());

            prepareEmptyMockedData(ticker2);
            mockCandles(ticker2, List.of(new Candle()));
            Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker2)))
                    .thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenDecideThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            mockCandles(ticker1, List.of(new Candle()));
            prepareEmptyMockedData(ticker2);
            mockCandles(ticker2, List.of(new Candle()));

            Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenThrow(new IllegalArgumentException());

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings({"unused", "java:S2699"})
    void tick_catchesException_whenPlaceMarketOrderThrowsException() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            mockCandles(ticker1, List.of(new Candle()));
            prepareEmptyMockedData(ticker2);
            mockCandles(ticker2, List.of(new Candle()));

            Decision decision = new Decision(DecisionAction.BUY, 5);
            Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(decision);

            Mockito.when(ordersService.placeMarketOrder(ticker1, decision.getLots(), OperationType.BUY))
                    .thenThrow(new IllegalArgumentException());

            bot.tick();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_whenCurrentCandlesIsEmpty() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            prepareEmptyMockedData(ticker2);

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_doesNoOrder_whenDecisionIsWait() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);

            prepareEmptyMockedData(ticker1);
            mockCandles(ticker1, List.of(new Candle()));
            prepareEmptyMockedData(ticker2);
            mockCandles(ticker2, List.of(new Candle()));

            Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(Decision.WAIT_DECISION);

            bot.tick();

            verifyNoOrdersMade();
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);
            mockData(ticker1);
            mockData(ticker2);

            Decision decision = new Decision(DecisionAction.BUY, 5);
            Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(decision);

            bot.tick();

            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(ticker1, decision.getLots(), OperationType.BUY);
            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(ticker2, decision.getLots(), OperationType.BUY);
        }
    }

    @Test
    @SuppressWarnings("unused")
    void tick_andPlacesSellOrder_whenDecisionIsSell() {
        OffsetDateTime mockedNow = DateUtils.getDateTime(2020, 9, 23, 6, 0, 0);
        try (MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            Mockito.when(botConfig.isEnabled()).thenReturn(true);
            Mockito.when(tradingProperties.getWorkStartTime()).thenReturn(mockedNow.toOffsetTime().minusHours(1));
            Mockito.when(tradingProperties.getWorkDuration()).thenReturn(Duration.ofHours(8));

            String ticker1 = "ticker1";
            String ticker2 = "ticker2";
            mockTickers(ticker1, ticker2);
            mockData(ticker1);
            mockData(ticker2);

            Decision decision = new Decision(DecisionAction.SELL, 5);
            Mockito.when(strategy.decide(Mockito.any(DecisionData.class))).thenReturn(decision);

            bot.tick();

            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(ticker1, decision.getLots(), OperationType.SELL);
            Mockito.verify(ordersService, Mockito.times(1))
                    .placeMarketOrder(ticker2, decision.getLots(), OperationType.SELL);
        }
    }

    private void mockTickers(String... tickers) {
        Mockito.when(botConfig.getTickers()).thenReturn(Set.of(tickers));
    }

    private void mockData(String ticker) {
        MarketInstrument instrument = TestDataHelper.createAndMockInstrument(marketService, ticker);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(instrument.getCurrency()))
                .thenReturn(balance);

        final PortfolioPosition position = TestDataHelper.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getPosition(ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(new Operation());
        Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final List<Candle> currentCandles = List.of(new Candle());
        mockCandles(ticker, currentCandles);
    }

    private MarketInstrument prepareEmptyMockedData(String ticker) {
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(Collections.emptyList());

        MarketInstrument instrument = TestDataHelper.createAndMockInstrument(marketService, ticker);

        Mockito.when(portfolioService.getAvailableBalance(instrument.getCurrency()))
                .thenReturn(BigDecimal.ZERO);

        Mockito.when(portfolioService.getPosition(ticker))
                .thenReturn(TestDataHelper.createPortfolioPosition(ticker, 0));

        mockCandles(ticker, Collections.emptyList());

        Mockito.when(operationsService.getOperations(Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(Collections.emptyList());

        return instrument;
    }

    private void mockCandles(String ticker, List<Candle> candles) {
        Mockito.when(marketService.getLastCandles(
                Mockito.eq(ticker),
                Mockito.anyInt(),
                Mockito.any(CandleResolution.class)
        )).thenReturn(candles);
    }

    private void verifyNoOrdersMade() {
        Mockito.verify(ordersService, Mockito.never())
                .placeMarketOrder(Mockito.any(), Mockito.anyInt(), Mockito.any());
    }

}