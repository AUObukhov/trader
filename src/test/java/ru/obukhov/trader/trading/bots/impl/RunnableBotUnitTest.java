package ru.obukhov.trader.trading.bots.impl;

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
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
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
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RunnableBotUnitTest {

    @Mock
    private ExtMarketDataService extMarketDataService;
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
    void run_doesNothing_whenDisabled() throws IOException {
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(false);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenNotWorkTime() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().plusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenThereAreOrders() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        Mockito.when(botConfig.ticker()).thenReturn(ticker);

        final List<Order> orders = List.of(TestData.createOrder());
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(orders);

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(operationsService, extMarketDataService, portfolioService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetLastCandlesThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        Mockito.when(extMarketDataService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleInterval.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNothing_whenGetOrdersThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        Mockito.when(botConfig.ticker()).thenReturn(ticker);

        Mockito.when(ordersService.getOrders(ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mockito.verifyNoMoreInteractions(operationsService, extMarketDataService, portfolioService);
        Mockito.verify(strategy, Mockito.never()).decide(Mockito.any(), Mockito.any());
        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetInstrumentThrowsException() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        Mockito.when(marketInstrumentsService.getShare(ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetAvailableBalanceThrowsException() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final Share share = TestData.createShare(ticker, Currency.RUB, 10);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(portfolioService.getAvailableBalance(Mockito.eq(accountId), Mockito.any(Currency.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenGetPositionThrowsException() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final Share share = TestData.createShare(ticker, Currency.RUB, 10);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(portfolioService.getSecurity(accountId, ticker)).thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
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

        final Share share = TestData.createShare(ticker, Currency.RUB, 10);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(operationsService.getOperations(Mockito.anyString(), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
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

        final Share share = TestData.createShare(ticker, Currency.RUB, 10);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenThrow(new IllegalArgumentException());

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    @SuppressWarnings("java:S2699")
    void run_catchesException_whenPlaceMarketOrderThrowsException() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);

        final Candle candle1 = new Candle().setTime(currentDateTime);
        mockCandles(ticker, List.of(candle1));

        final Share share = TestData.createShare(ticker, Currency.RUB, 10);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

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
    void run_doesNoOrder_whenThereAreOrders() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        Mockito.when(botConfig.ticker()).thenReturn(ticker);

        Mocker.mockEmptyOrder(ordersService, ticker);

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_doesNoOrder_whenCurrentCandlesIsEmpty() throws IOException {
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
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

        final Share share = TestData.createShare(ticker, Currency.RUB, 10);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        Mockito.when(strategy.decide(Mockito.any(DecisionData.class), Mockito.any(StrategyCache.class)))
                .thenReturn(new Decision(DecisionAction.WAIT));

        createRunnableBot().run();

        Mocker.verifyNoOrdersMade(ordersService);
    }

    @Test
    void run_returnsFilledData_andPlacesBuyOrder_whenDecisionIsBuy() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        final Currency currency = Currency.RUB;
        final int lotSize = 10;
        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        mockData(accountId, ticker, currency, lotSize);

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
    void run_andPlacesSellOrder_whenDecisionIsSell() throws IOException {
        final String accountId = "2000124699";
        final OffsetDateTime currentDateTime = DateTimeTestData.createDateTime(2020, 9, 23, 6);

        Mockito.when(realTinkoffService.getCurrentDateTime()).thenReturn(currentDateTime);
        Mockito.when(schedulingProperties.isEnabled()).thenReturn(true);
        final WorkSchedule workSchedule = new WorkSchedule(currentDateTime.toOffsetTime().minusHours(1), Duration.ofHours(8));
        Mockito.when(marketProperties.getWorkSchedule()).thenReturn(workSchedule);

        final String ticker = "ticker";
        mockBotConfig(accountId, ticker, CandleInterval.CANDLE_INTERVAL_1_MIN, 0.0);
        mockData(accountId, ticker, Currency.RUB, 10);

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
        return new RunnableBot(tinkoffServices, strategy, schedulingProperties, botConfig, marketProperties);
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

    private void mockData(final String accountId, final String ticker, final Currency currency, final int lotSize)
            throws IOException {
        final Share share = TestData.createShare(ticker, currency, lotSize);
        Mockito.when(marketInstrumentsService.getShare(ticker)).thenReturn(share);

        final BigDecimal balance = BigDecimal.valueOf(10000);
        Mockito.when(portfolioService.getAvailableBalance(accountId, currency))
                .thenReturn(balance);

        final PortfolioPosition position = TestData.createPortfolioPosition(ticker, 0);
        Mockito.when(portfolioService.getSecurity(accountId, ticker))
                .thenReturn(position);

        final List<Operation> operations = List.of(TestData.createOperation());
        Mockito.when(operationsService.getOperations(Mockito.eq(accountId), Mockito.any(Interval.class), Mockito.eq(ticker)))
                .thenReturn(operations);

        final Candle candle = new Candle().setTime(OffsetDateTime.now());
        mockCandles(ticker, List.of(candle));
    }

    private void mockCandles(final String ticker, final List<Candle> candles) {
        Mockito.when(extMarketDataService.getLastCandles(Mockito.eq(ticker), Mockito.anyInt(), Mockito.any(CandleInterval.class)))
                .thenReturn(candles);
    }

}