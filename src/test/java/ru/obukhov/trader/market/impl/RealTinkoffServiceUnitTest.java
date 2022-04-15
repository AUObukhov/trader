package ru.obukhov.trader.market.impl;

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
import ru.obukhov.trader.market.model.BrokerAccountType;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.client.service.interfaces.MarketClient;
import ru.obukhov.trader.web.client.service.interfaces.OperationsClient;
import ru.obukhov.trader.web.client.service.interfaces.OrdersClient;
import ru.obukhov.trader.web.client.service.interfaces.PortfolioClient;
import ru.obukhov.trader.web.client.service.interfaces.UserClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RealTinkoffServiceUnitTest {

    @Mock
    private MarketClient marketClient;
    @Mock
    private OperationsClient operationsClient;
    @Mock
    private OrdersClient ordersClient;
    @Mock
    private PortfolioClient portfolioClient;
    @Mock
    private UserClient userClient;

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
    void getMarketStocks_returnsStocks() throws IOException {
        final MarketInstrument instrument1 = TestData.createMarketInstrument();
        final MarketInstrument instrument2 = TestData.createMarketInstrument();
        Mockito.when(marketClient.getMarketStocks()).thenReturn(List.of(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketStocks();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketBonds_returnsBonds() throws IOException {
        final MarketInstrument instrument1 = TestData.createMarketInstrument();
        final MarketInstrument instrument2 = TestData.createMarketInstrument();
        Mockito.when(marketClient.getMarketBonds()).thenReturn(List.of(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketBonds();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketEtfs_returnsEtfs() throws IOException {
        final MarketInstrument instrument1 = TestData.createMarketInstrument();
        final MarketInstrument instrument2 = TestData.createMarketInstrument();
        Mockito.when(marketClient.getMarketEtfs()).thenReturn(List.of(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketEtfs();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketCurrencies_returnsCurrencies() throws IOException {
        final MarketInstrument instrument1 = TestData.createMarketInstrument();
        final MarketInstrument instrument2 = TestData.createMarketInstrument();
        Mockito.when(marketClient.getMarketCurrencies()).thenReturn(List.of(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketCurrencies();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    // region getMarketOrderbook tests

    @Test
    void getMarketOrderbook_returnsOrderbook() throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";
        final int depth = 10;

        mockInstrument(TestData.createMarketInstrument(ticker, figi));

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

        mockInstrument(TestData.createMarketInstrument(ticker, figi));

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
        final CandleInterval candleInterval = CandleInterval._1MIN;

        mockInstrument(TestData.createMarketInstrument(ticker, figi));
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
        final Candles expectedCandles = new Candles(null, null, List.of(tinkoffCandle1, tinkoffCandle2));
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
        final CandleInterval candleInterval = CandleInterval._1MIN;

        mockInstrument(TestData.createMarketInstrument(ticker, figi));

        final Candles expectedCandles = new Candles(null, null, List.of());
        Mockito.when(marketClient.getMarketCandles(figi, from, to, candleInterval)).thenReturn(expectedCandles);

        final List<Candle> candles = realTinkoffService.getMarketCandles(ticker, interval, candleInterval);

        Assertions.assertTrue(candles.isEmpty());
    }

    // endregion

    // region searchMarketInstrument tests

    @Test
    void searchMarketInstrument_returnsNull_whenGetsNoInstruments() throws IOException {
        final String ticker = "ticker";

        Mockito.when(marketClient.searchMarketInstrumentsByTicker(ticker)).thenReturn(List.of());

        final MarketInstrument result = realTinkoffService.searchMarketInstrument(ticker);

        Assertions.assertNull(result);
    }

    @Test
    void searchMarketInstrument_returnsFirstInstrument_whenGetsMultipleInstruments() throws IOException {
        final String ticker = "ticker";
        final MarketInstrument instrument1 = TestData.createMarketInstrument(ticker);
        final MarketInstrument instrument2 = TestData.createMarketInstrument(ticker);

        Mockito.when(marketClient.searchMarketInstrumentsByTicker(ticker)).thenReturn(List.of(instrument1, instrument2));

        final MarketInstrument result = realTinkoffService.searchMarketInstrument(ticker);

        Assertions.assertSame(instrument1, result);
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

        mockInstrument(TestData.createMarketInstrument(ticker, figi));

        final Operation operation1 = TestData.createOperation();
        final Operation operation2 = TestData.createOperation();
        final List<Operation> operations = List.of(operation1, operation2);
        Mockito.when(operationsClient.getOperations(brokerAccountId, from, to, figi)).thenReturn(operations);

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

        mockInstrument(TestData.createMarketInstrument(ticker, figi));

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

        mockInstrument(TestData.createMarketInstrument(ticker, figi));

        final MarketOrderRequest orderRequest = new MarketOrderRequest(1, OperationType.BUY);

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
        final PortfolioPosition tinkoffPosition1 = new PortfolioPosition()
                .setTicker("ticker1")
                .setBalance(BigDecimal.valueOf(1000))
                .setBlocked(null)
                .setExpectedYield(TestData.createMoneyAmount(Currency.USD, 100))
                .setCount(10)
                .setAveragePositionPrice(TestData.createMoneyAmount(Currency.USD, 110))
                .setAveragePositionPriceNoNkd(TestData.createMoneyAmount(Currency.USD, 110))
                .setName("name1");

        final PortfolioPosition tinkoffPosition2 = new PortfolioPosition()
                .setTicker("ticker2")
                .setBalance(BigDecimal.valueOf(2000))
                .setBlocked(BigDecimal.valueOf(100))
                .setExpectedYield(TestData.createMoneyAmount(Currency.USD, 200))
                .setCount(5)
                .setAveragePositionPrice(TestData.createMoneyAmount(Currency.USD, 440))
                .setAveragePositionPriceNoNkd(TestData.createMoneyAmount(Currency.USD, 440))
                .setName("name2");
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
        final CurrencyPosition currency1 = TestData.createCurrencyPosition(Currency.RUB, 10000, 1000);
        final CurrencyPosition currency2 = TestData.createCurrencyPosition(Currency.USD, 1000);
        final List<CurrencyPosition> currencies = List.of(currency1, currency2);
        Mockito.when(portfolioClient.getPortfolioCurrencies(brokerAccountId)).thenReturn(currencies);

        final List<CurrencyPosition> result = realTinkoffService.getPortfolioCurrencies(brokerAccountId);

        Assertions.assertSame(currencies, result);
    }

    // endregion

    // region UserContext methods tests

    @Test
    void getUserAccounts() throws IOException {
        final UserAccount userAccount1 = new UserAccount();
        userAccount1.setBrokerAccountType(BrokerAccountType.TINKOFF_IIS);
        userAccount1.setBrokerAccountId("2008941383");

        final UserAccount userAccount2 = new UserAccount();
        userAccount2.setBrokerAccountType(BrokerAccountType.TINKOFF);
        userAccount2.setBrokerAccountId("2000124699");

        final List<UserAccount> userAccounts = List.of(userAccount1, userAccount2);

        Mockito.when(userClient.getAccounts()).thenReturn(userAccounts);

        final List<UserAccount> result = realTinkoffService.getAccounts();

        Assertions.assertSame(userAccounts, result);
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

    private void mockInstrument(MarketInstrument instrument) throws IOException {
        Mockito.when(marketClient.searchMarketInstrumentsByTicker(instrument.ticker()))
                .thenReturn(List.of(instrument));
    }

}