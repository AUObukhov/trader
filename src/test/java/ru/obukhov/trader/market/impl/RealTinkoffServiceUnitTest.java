package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.client.service.interfaces.MarketClient;
import ru.obukhov.trader.web.client.service.interfaces.OrdersClient;
import ru.obukhov.trader.web.client.service.interfaces.PortfolioClient;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.UsersService;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RealTinkoffServiceUnitTest {

    @Mock
    private
    InstrumentsService instrumentsService;
    @Mock
    private MarketClient marketClient;
    @Mock
    private OrdersClient ordersClient;
    @Mock
    private PortfolioClient portfolioClient;
    @Mock
    private UsersService usersService;
    @Mock
    private OperationsService operationsService;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private RealTinkoffService realTinkoffService;

    @BeforeEach
    private void setUp() {
        Mockito.lenient().when(applicationContext.getBean(RealTinkoffService.class)).thenReturn(realTinkoffService);
        realTinkoffService.setApplicationContext(applicationContext);
    }

    // region MarketContext methods tests

    @Test
    void getAllShares_returnsShares() throws IOException {
        final Share share1 = Share.newBuilder().build();
        final Share share2 = Share.newBuilder().build();
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final List<Share> result = realTinkoffService.getAllShares();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(share1, result.get(0));
        Assertions.assertSame(share2, result.get(1));
    }

    // region getMarketOrderbook tests

    @Test
    void getMarketOrderbook_returnsOrderbook() throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";
        final int depth = 10;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);
        final Orderbook orderbook = new Orderbook(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        Mockito.when(marketClient.getMarketOrderbook(figi, depth)).thenReturn(orderbook);

        final Orderbook result = realTinkoffService.getMarketOrderbook(ticker, depth);
        Assertions.assertSame(orderbook, result);
    }

    @Test
    void getMarketOrderbook_returnsNull_whenGetsNoOrderbook() throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";
        final int depth = 10;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final Orderbook result = realTinkoffService.getMarketOrderbook(ticker, depth);
        Assertions.assertNull(result);
    }

    // endregion

    // region getMarketCandles tests

    @Test
    void getMarketCandles_returnsMappedCandles() throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);
        final Candle tinkoffCandle1 = TestData.createTinkoffCandle(
                candleInterval,
                1000,
                1500,
                2000,
                500,
                from.plusMinutes(1)
        );
        final Candle tinkoffCandle2 = TestData.createTinkoffCandle(
                candleInterval,
                1500,
                2000,
                2500,
                1000,
                from.plusMinutes(1)
        );
        final Candles expectedCandles = new Candles(figi, null, List.of(tinkoffCandle1, tinkoffCandle2));
        Mockito.when(marketClient.getMarketCandles(figi, from, to, candleInterval)).thenReturn(expectedCandles);

        final List<Candle> candles = realTinkoffService.getMarketCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(expectedCandles.candleList().size(), candles.size());
        Assertions.assertEquals(tinkoffCandle1, candles.get(0));
        Assertions.assertEquals(tinkoffCandle2, candles.get(1));
    }

    @Test
    void getMarketCandles_returnsEmptyList_whenGetsNoCandles() throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final Candles expectedCandles = new Candles(null, null, List.of());
        Mockito.when(marketClient.getMarketCandles(figi, from, to, candleInterval)).thenReturn(expectedCandles);

        final List<Candle> candles = realTinkoffService.getMarketCandles(ticker, interval, candleInterval);

        Assertions.assertTrue(candles.isEmpty());
    }

    // endregion

    // region OperationsContext methods tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_returnsOperations(@Nullable final String brokerAccountId) throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final Operation operation1 = TestData.createOperation();
        final Operation operation2 = TestData.createOperation();
        final List<Operation> operations = List.of(operation1, operation2);
        Mockito.when(operationsService.getAllOperationsSync(brokerAccountId, from.toInstant(), to.toInstant(), figi)).thenReturn(operations);

        final List<Operation> result = realTinkoffService.getOperations(brokerAccountId, Interval.of(from, to), ticker);

        Assertions.assertSame(operations, result);
    }

    // endregion

    // region OrdersContext methods tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOrders(@Nullable final String brokerAccountId) throws IOException {
        final List<Order> orders = List.of(TestData.createOrder(), TestData.createOrder());
        Mockito.when(ordersClient.getOrders(brokerAccountId)).thenReturn(orders);

        final List<Order> result = realTinkoffService.getOrders(brokerAccountId);

        Assertions.assertSame(orders, result);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeLimitOrder(@Nullable final String brokerAccountId) throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final LimitOrderRequest orderRequest = new LimitOrderRequest(null, null, null);

        final PlacedLimitOrder placedOrder = new PlacedLimitOrder(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        Mockito.when(ordersClient.placeLimitOrder(brokerAccountId, figi, orderRequest)).thenReturn(placedOrder);
        final PlacedLimitOrder result = realTinkoffService.placeLimitOrder(brokerAccountId, ticker, orderRequest);

        Assertions.assertSame(placedOrder, result);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder(@Nullable final String brokerAccountId) throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final MarketOrderRequest orderRequest = new MarketOrderRequest(1L, OperationType.OPERATION_TYPE_BUY);

        final PlacedMarketOrder placedOrder = new PlacedMarketOrder(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        Mockito.when(ordersClient.placeMarketOrder(brokerAccountId, figi, orderRequest))
                .thenReturn(placedOrder);

        final PlacedMarketOrder result = realTinkoffService.placeMarketOrder(brokerAccountId, ticker, orderRequest);

        Assertions.assertSame(placedOrder, result);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void cancelOrder(@Nullable final String brokerAccountId) throws IOException {
        final String orderId = "orderId";

        realTinkoffService.cancelOrder(brokerAccountId, orderId);

        Mockito.verify(ordersClient, Mockito.times(1)).cancelOrder(brokerAccountId, orderId);
    }

    // endregion

    // region PortfolioContext methods tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioPositions_returnsAndMapsPositions(@Nullable final String brokerAccountId) throws IOException {
        final PortfolioPosition tinkoffPosition1 = TestData.createPortfolioPosition(
                "ticker1",
                1000,
                0,
                Currency.RUB.name(),
                100,
                10,
                110,
                100,
                "name1"
        );

        final PortfolioPosition tinkoffPosition2 = TestData.createPortfolioPosition(
                "ticker2",
                2000,
                100,
                Currency.USD.name(),
                200,
                5,
                440,
                400,
                "name2"
        );

        final List<PortfolioPosition> portfolioPositions = List.of(tinkoffPosition1, tinkoffPosition2);
        Mockito.when(portfolioClient.getPortfolio(brokerAccountId)).thenReturn(portfolioPositions);

        final Collection<PortfolioPosition> result = realTinkoffService.getPortfolioPositions(brokerAccountId);

        Assertions.assertEquals(portfolioPositions.size(), result.size());
        Iterator<PortfolioPosition> resultIterator = result.iterator();
        Assertions.assertEquals(tinkoffPosition1, resultIterator.next());
        Assertions.assertEquals(tinkoffPosition2, resultIterator.next());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies(@Nullable final String brokerAccountId) throws IOException {
        final CurrencyPosition currency1 = TestData.createCurrencyPosition(Currency.RUB.name(), 10000, 1000);
        final CurrencyPosition currency2 = TestData.createCurrencyPosition(Currency.USD.name(), 1000);
        final List<CurrencyPosition> currencies = List.of(currency1, currency2);
        Mockito.when(portfolioClient.getPortfolioCurrencies(brokerAccountId)).thenReturn(currencies);

        final List<CurrencyPosition> result = realTinkoffService.getPortfolioCurrencies(brokerAccountId);

        Assertions.assertSame(currencies, result);
    }

    // endregion

    // region UserContext methods tests

    @Test
    void getUserAccounts() {
        final String id1 = "2008941383";
        final AccountType accountType1 = AccountType.ACCOUNT_TYPE_TINKOFF_IIS;
        final String name1 = "ИИС";
        final AccountStatus accountStatus1 = AccountStatus.ACCOUNT_STATUS_OPEN;
        final Timestamp openedDate1 = DateTimeTestData.createTimestamp(1562889600);
        final OffsetDateTime openedDateTime1 = DateTimeTestData.createDateTime(2019, 7, 12, 3, ZoneOffset.ofHours(3));
        final Timestamp closedDate1 = DateTimeTestData.createTimestamp(-10800);
        final OffsetDateTime closedDateTime1 = DateTimeTestData.createDateTime(1970, 1, 1, ZoneOffset.ofHours(3));
        final AccessLevel accessLevel1 = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

        Account account1 = Account.newBuilder()
                .setId(id1)
                .setType(accountType1)
                .setName(name1)
                .setStatus(accountStatus1)
                .setOpenedDate(openedDate1)
                .setClosedDate(closedDate1)
                .setAccessLevel(accessLevel1)
                .build();

        final String id2 = "2000124699";
        final AccountType accountType2 = AccountType.ACCOUNT_TYPE_TINKOFF;
        final String name2 = "Брокерский счёт";
        final AccountStatus accountStatus2 = AccountStatus.ACCOUNT_STATUS_OPEN;
        final Timestamp openedDate2 = DateTimeTestData.createTimestamp(1527206400);
        final OffsetDateTime openedDateTime2 = DateTimeTestData.createDateTime(2018, 5, 25, 3, ZoneOffset.ofHours(3));
        final Timestamp closedDate2 = DateTimeTestData.createTimestamp(-10800);
        final OffsetDateTime closedDateTime2 = DateTimeTestData.createDateTime(1970, 1, 1, ZoneOffset.ofHours(3));
        final AccessLevel accessLevel2 = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

        Account account2 = Account.newBuilder()
                .setId(id2)
                .setType(accountType2)
                .setName(name2)
                .setStatus(accountStatus2)
                .setOpenedDate(openedDate2)
                .setClosedDate(closedDate2)
                .setAccessLevel(accessLevel2)
                .build();

        Mockito.when(usersService.getAccountsSync())
                .thenReturn(List.of(account1, account2));

        final UserAccount userAccount1 = new UserAccount(id1, accountType1, name1, accountStatus1, openedDateTime1, closedDateTime1, accessLevel1);
        final UserAccount userAccount2 = new UserAccount(id2, accountType2, name2, accountStatus2, openedDateTime2, closedDateTime2, accessLevel2);
        final List<UserAccount> expectedUserAccounts = List.of(userAccount1, userAccount2);

        Mockito.when(usersService.getAccountsSync()).thenReturn(List.of(account1, account2));

        final List<UserAccount> result = realTinkoffService.getAccounts();

        Assertions.assertEquals(expectedUserAccounts, result);
    }

    // endregion

    @Test
    void getCurrentDateTime_returnsCurrentDateTime() {
        final OffsetDateTime now = OffsetDateTime.now();

        final OffsetDateTime currentDateTime = realTinkoffService.getCurrentDateTime();

        final long delay = Duration.between(now, currentDateTime).toMillis();

        Assertions.assertTrue(delay >= 0);

        final int maxDelay = 5;
        Assertions.assertTrue(delay < maxDelay);
    }

}