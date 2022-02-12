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
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.MarketInstrumentList;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.Operations;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.market.model.Portfolio;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.UserAccounts;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.tinkoff.invest.openapi.okhttp.MarketContext;
import ru.tinkoff.invest.openapi.okhttp.OpenApi;
import ru.tinkoff.invest.openapi.okhttp.OperationsContext;
import ru.tinkoff.invest.openapi.okhttp.OrdersContext;
import ru.tinkoff.invest.openapi.okhttp.PortfolioContext;
import ru.tinkoff.invest.openapi.okhttp.UserContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
class RealTinkoffServiceUnitTest {

    @Mock
    private MarketContext marketContext;
    @Mock
    private OperationsContext operationsContext;
    @Mock
    private OrdersContext ordersContext;
    @Mock
    private PortfolioContext portfolioContext;
    @Mock
    private UserContext userContext;

    @Mock
    private OpenApi opeApi;
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private RealTinkoffService realTinkoffService;

    @BeforeEach
    private void setUp() {
        Mockito.lenient().when(opeApi.getMarketContext()).thenReturn(marketContext);
        Mockito.lenient().when(opeApi.getOperationsContext()).thenReturn(operationsContext);
        Mockito.lenient().when(opeApi.getOrdersContext()).thenReturn(ordersContext);
        Mockito.lenient().when(opeApi.getPortfolioContext()).thenReturn(portfolioContext);
        Mockito.lenient().when(opeApi.getUserContext()).thenReturn(userContext);

        Mockito.lenient().when(applicationContext.getBean(RealTinkoffService.class)).thenReturn(realTinkoffService);
        realTinkoffService.setApplicationContext(applicationContext);
    }

    // region MarketContext methods tests

    @Test
    void getMarketStocks_returnsStocks() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketStocks()).thenReturn(TestData.createInstrumentsFuture(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketStocks();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketBonds_returnsBonds() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketBonds()).thenReturn(TestData.createInstrumentsFuture(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketBonds();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketEtfs_returnsEtfs() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketEtfs()).thenReturn(TestData.createInstrumentsFuture(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketEtfs();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketCurrencies_returnsCurrencies() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketCurrencies()).thenReturn(TestData.createInstrumentsFuture(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketCurrencies();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    // region getMarketOrderbook tests

    @Test
    void getMarketOrderbook_returnsOrderbook() {
        final String ticker = "ticker";
        final String figi = "figi";
        final int depth = 10;

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final Orderbook orderbook = new Orderbook();
        final Optional<Orderbook> optionalOrderbook = Optional.of(orderbook);
        Mockito.when(marketContext.getMarketOrderbook(figi, depth)).thenReturn(CompletableFuture.completedFuture(optionalOrderbook));

        final Orderbook result = realTinkoffService.getMarketOrderbook(ticker, depth);
        Assertions.assertSame(orderbook, result);
    }

    @Test
    void getMarketOrderbook_returnsNull_whenGetsNoOrderbook() {
        final String ticker = "ticker";
        final String figi = "figi";
        final int depth = 10;

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        Mockito.when(marketContext.getMarketOrderbook(figi, depth)).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final Orderbook result = realTinkoffService.getMarketOrderbook(ticker, depth);
        Assertions.assertNull(result);
    }

    // endregion

    // region getMarketCandles tests

    @Test
    void getMarketCandles_returnsMappedCandles() {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleResolution candleResolution = CandleResolution._1MIN;

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));
        final Candle tinkoffCandle1 = TestData.createTinkoffCandle(
                candleResolution,
                1000,
                1500,
                2000,
                500,
                from.plusMinutes(1)
        );
        final Candle tinkoffCandle2 = TestData.createTinkoffCandle(
                candleResolution,
                1500,
                2000,
                2500,
                1000,
                from.plusMinutes(1)
        );
        final Candles tinkoffCandles = new Candles().candles(List.of(tinkoffCandle1, tinkoffCandle2));
        final Optional<Candles> optionalCandles = Optional.of(tinkoffCandles);
        Mockito.when(marketContext.getMarketCandles(figi, from, to, candleResolution)).thenReturn(CompletableFuture.completedFuture(optionalCandles));

        final List<Candle> candles = realTinkoffService.getMarketCandles(ticker, interval, candleResolution);

        Assertions.assertEquals(tinkoffCandles.getCandles().size(), candles.size());
        Assertions.assertEquals(tinkoffCandle1, candles.get(0));
        Assertions.assertEquals(tinkoffCandle2, candles.get(1));
    }

    @Test
    void getMarketCandles_returnsEmptyList_whenGetsNoCandles() {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleResolution candleResolution = CandleResolution._1MIN;

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final Optional<Candles> optionalCandles = Optional.of(new Candles());
        Mockito.when(marketContext.getMarketCandles(figi, from, to, candleResolution)).thenReturn(CompletableFuture.completedFuture(optionalCandles));

        final List<Candle> candles = realTinkoffService.getMarketCandles(ticker, interval, candleResolution);

        Assertions.assertTrue(candles.isEmpty());
    }

    // endregion

    // region searchMarketInstrument tests

    @Test
    void searchMarketInstrument_returnsNull_whenGetsNoInstruments() {
        final String ticker = "ticker";

        final CompletableFuture<MarketInstrumentList> future = CompletableFuture.completedFuture(new MarketInstrumentList());

        Mockito.when(marketContext.searchMarketInstrumentsByTicker(ticker)).thenReturn(future);

        final MarketInstrument result = realTinkoffService.searchMarketInstrument(ticker);

        Assertions.assertNull(result);
    }

    @Test
    void searchMarketInstrument_returnsFirstInstrument_whenGetsMultipleInstruments() {
        final String ticker = "ticker";
        final MarketInstrument instrument1 = new MarketInstrument().ticker(ticker);
        final MarketInstrument instrument2 = new MarketInstrument().ticker(ticker);

        final List<MarketInstrument> instrumentList = List.of(instrument1, instrument2);
        final MarketInstrumentList marketInstrumentList = new MarketInstrumentList().instruments(instrumentList);
        final CompletableFuture<MarketInstrumentList> future = CompletableFuture.completedFuture(marketInstrumentList);

        Mockito.when(marketContext.searchMarketInstrumentsByTicker(ticker)).thenReturn(future);

        final MarketInstrument result = realTinkoffService.searchMarketInstrument(ticker);

        Assertions.assertSame(instrument1, result);
    }

    // endregion

    // region OperationsContext methods tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOperations_returnsOperations(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final Operation operation1 = new Operation();
        final Operation operation2 = new Operation();
        final Operations operations = new Operations().operations(List.of(operation1, operation2));
        Mockito.when(operationsContext.getOperations(from, to, figi, brokerAccountId)).thenReturn(CompletableFuture.completedFuture(operations));

        final List<Operation> result = realTinkoffService.getOperations(brokerAccountId, Interval.of(from, to), ticker);

        Assertions.assertSame(operations.getOperations(), result);
    }

    // endregion

    // region OrdersContext methods tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOrders(@Nullable final String brokerAccountId) {
        final List<Order> orders = List.of(new Order(), new Order());
        Mockito.when(ordersContext.getOrders(brokerAccountId)).thenReturn(CompletableFuture.completedFuture(orders));

        final List<Order> result = realTinkoffService.getOrders(brokerAccountId);

        Assertions.assertSame(orders, result);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeLimitOrder(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";
        final String figi = "figi";

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final LimitOrderRequest orderRequest = new LimitOrderRequest();

        final PlacedLimitOrder placedOrder = new PlacedLimitOrder();
        Mockito.when(ordersContext.placeLimitOrder(figi, orderRequest, brokerAccountId)).thenReturn(CompletableFuture.completedFuture(placedOrder));
        final PlacedLimitOrder result = realTinkoffService.placeLimitOrder(brokerAccountId, ticker, orderRequest);

        Assertions.assertSame(placedOrder, result);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void placeMarketOrder(@Nullable final String brokerAccountId) {
        final String ticker = "ticker";
        final String figi = "figi";

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final MarketOrderRequest orderRequest = new MarketOrderRequest();

        final PlacedMarketOrder placedOrder = new PlacedMarketOrder();
        Mockito.when(ordersContext.placeMarketOrder(figi, orderRequest, brokerAccountId))
                .thenReturn(CompletableFuture.completedFuture(placedOrder));

        final PlacedMarketOrder result = realTinkoffService.placeMarketOrder(brokerAccountId, ticker, orderRequest);

        Assertions.assertSame(placedOrder, result);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void cancelOrder(@Nullable final String brokerAccountId) {
        final String orderId = "orderId";

        final CompletableFuture<Void> futureSpy = Mockito.spy(CompletableFuture.completedFuture(null));
        Mockito.when(ordersContext.cancelOrder(orderId, brokerAccountId)).thenReturn(futureSpy);

        realTinkoffService.cancelOrder(brokerAccountId, orderId);

        Mockito.verify(futureSpy, Mockito.times(1)).join();
    }

    // endregion

    // region PortfolioContext methods tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioPositions_returnsAndMapsPositions(@Nullable final String brokerAccountId) {
        final PortfolioPosition tinkoffPosition1 = PortfolioPosition.builder()
                .ticker("ticker1")
                .balance(BigDecimal.valueOf(1000))
                .blocked(null)
                .expectedYield(BigDecimal.valueOf(100))
                .count(10)
                .averagePositionPrice(BigDecimal.valueOf(110))
                .averagePositionPriceNoNkd(BigDecimal.valueOf(110))
                .name("name1")
                .build();

        final PortfolioPosition tinkoffPosition2 = PortfolioPosition.builder()
                .ticker("ticker2")
                .balance(BigDecimal.valueOf(2000))
                .blocked(BigDecimal.valueOf(100))
                .expectedYield(BigDecimal.valueOf(200))
                .count(5)
                .averagePositionPrice(BigDecimal.valueOf(440))
                .averagePositionPriceNoNkd(BigDecimal.valueOf(440))
                .name("name2")
                .build();
        final Portfolio portfolio = new Portfolio().positions(List.of(tinkoffPosition1, tinkoffPosition2));
        Mockito.when(portfolioContext.getPortfolio(brokerAccountId)).thenReturn(CompletableFuture.completedFuture(portfolio));

        final Collection<PortfolioPosition> result = realTinkoffService.getPortfolioPositions(brokerAccountId);

        Assertions.assertEquals(portfolio.getPositions().size(), result.size());
        Iterator<PortfolioPosition> resultIterator = result.iterator();
        Assertions.assertEquals(tinkoffPosition1, resultIterator.next());
        Assertions.assertEquals(tinkoffPosition2, resultIterator.next());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPortfolioCurrencies(@Nullable final String brokerAccountId) {
        final CurrencyPosition currency1 = new CurrencyPosition()
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(10000))
                .blocked(BigDecimal.valueOf(1000));
        final CurrencyPosition currency2 = new CurrencyPosition()
                .currency(Currency.USD)
                .balance(BigDecimal.valueOf(1000))
                .blocked(null);
        final Currencies currencies = new Currencies().currencies(List.of(currency1, currency2));
        Mockito.when(portfolioContext.getPortfolioCurrencies(brokerAccountId)).thenReturn(CompletableFuture.completedFuture(currencies));

        final List<CurrencyPosition> result = realTinkoffService.getPortfolioCurrencies(brokerAccountId);

        Assertions.assertSame(currencies.getCurrencies(), result);
    }

    // endregion

    // region UserContext methods tests

    @Test
    void getUserAccounts() {
        final UserAccount userAccount1 = new UserAccount();
        userAccount1.setBrokerAccountType(BrokerAccountType.TINKOFFIIS);
        userAccount1.setBrokerAccountId("2008941383");

        final UserAccount userAccount2 = new UserAccount();
        userAccount2.setBrokerAccountType(BrokerAccountType.TINKOFF);
        userAccount2.setBrokerAccountId("2000124699");

        final UserAccounts userAccounts = new UserAccounts();
        userAccounts.setAccounts(List.of(userAccount1, userAccount2));

        Mockito.when(userContext.getAccounts()).thenReturn(CompletableFuture.completedFuture(userAccounts));

        final List<UserAccount> result = realTinkoffService.getAccounts();

        Assertions.assertSame(userAccounts.getAccounts(), result);
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

    private void mockInstrument(MarketInstrument instrument) {
        Mockito.when(marketContext.searchMarketInstrumentsByTicker(instrument.getTicker()))
                .thenReturn(TestData.createInstrumentsFuture(instrument));
    }

}