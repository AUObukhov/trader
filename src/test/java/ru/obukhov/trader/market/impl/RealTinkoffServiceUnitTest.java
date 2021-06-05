package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Candles;
import ru.tinkoff.invest.openapi.model.rest.Currencies;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.LimitOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.MarketOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.Orderbook;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;

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

        Mockito.lenient().when(applicationContext.getBean(RealTinkoffService.class)).thenReturn(realTinkoffService);
        realTinkoffService.setApplicationContext(applicationContext);
    }

    // region MarketContext methods tests

    @Test
    void getMarketStocks_returnsStocks() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketStocks())
                .thenReturn(TestDataHelper.createInstrumentsFuture(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketStocks();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketBonds_returnsBonds() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketBonds())
                .thenReturn(TestDataHelper.createInstrumentsFuture(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketBonds();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketEtfs_returnsEtfs() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketEtfs())
                .thenReturn(TestDataHelper.createInstrumentsFuture(instrument1, instrument2));

        final List<MarketInstrument> result = realTinkoffService.getMarketEtfs();

        Assertions.assertEquals(2, result.size());
        Assertions.assertSame(instrument1, result.get(0));
        Assertions.assertSame(instrument2, result.get(1));
    }

    @Test
    void getMarketCurrencies_returnsCurrencies() {
        final MarketInstrument instrument1 = new MarketInstrument();
        final MarketInstrument instrument2 = new MarketInstrument();
        Mockito.when(marketContext.getMarketCurrencies())
                .thenReturn(TestDataHelper.createInstrumentsFuture(instrument1, instrument2));

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
        Mockito.when(marketContext.getMarketOrderbook(figi, depth))
                .thenReturn(CompletableFuture.completedFuture(optionalOrderbook));

        final Orderbook result = realTinkoffService.getMarketOrderbook(ticker, depth);
        Assertions.assertSame(orderbook, result);
    }

    @Test
    void getMarketOrderbook_returnsNull_whenGetsNoOrderbook() {
        final String ticker = "ticker";
        final String figi = "figi";
        final int depth = 10;

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        Mockito.when(marketContext.getMarketOrderbook(figi, depth))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final Orderbook result = realTinkoffService.getMarketOrderbook(ticker, depth);
        Assertions.assertNull(result);
    }

    // endregion

    // region getMarketCandles tests

    @Test
    void getMarketCandles_returnsMappedCandles() {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 2, 0, 0, 0);
        final Interval interval = Interval.of(from, to);
        final CandleResolution candleInterval = CandleResolution._1MIN;

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));
        final ru.tinkoff.invest.openapi.model.rest.Candle tinkoffCandle1 = TestDataHelper.createTinkoffCandle(
                candleInterval,
                1000,
                1500,
                2000,
                500,
                from.plusMinutes(1)
        );
        final ru.tinkoff.invest.openapi.model.rest.Candle tinkoffCandle2 = TestDataHelper.createTinkoffCandle(
                candleInterval,
                1500,
                2000,
                2500,
                1000,
                from.plusMinutes(1)
        );
        final Candles tinkoffCandles = new Candles().candles(List.of(tinkoffCandle1, tinkoffCandle2));
        final Optional<Candles> optionalCandles = Optional.of(tinkoffCandles);
        Mockito.when(marketContext.getMarketCandles(figi, from, to, candleInterval))
                .thenReturn(CompletableFuture.completedFuture(optionalCandles));

        final List<Candle> candles = realTinkoffService.getMarketCandles(ticker, interval, candleInterval);

        Assertions.assertEquals(tinkoffCandles.getCandles().size(), candles.size());
        AssertUtils.assertEquals(tinkoffCandle1, candles.get(0));
        AssertUtils.assertEquals(tinkoffCandle2, candles.get(1));
    }

    @Test
    void getMarketCandles_returnsEmptyList_whenGetsNoCandles() {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 2, 0, 0, 0);
        final Interval interval = Interval.of(from, to);
        final CandleResolution candleInterval = CandleResolution._1MIN;

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final Optional<Candles> optionalCandles = Optional.of(new Candles());
        Mockito.when(marketContext.getMarketCandles(figi, from, to, candleInterval))
                .thenReturn(CompletableFuture.completedFuture(optionalCandles));

        final List<Candle> candles = realTinkoffService.getMarketCandles(ticker, interval, candleInterval);

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

    @Test
    void getOperations_returnsOperations() {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 1, 2, 0, 0, 0);

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final Operation operation1 = new Operation();
        final Operation operation2 = new Operation();
        final Operations operations = new Operations().operations(List.of(operation1, operation2));
        Mockito.when(operationsContext.getOperations(from, to, figi, null))
                .thenReturn(CompletableFuture.completedFuture(operations));

        final List<Operation> result = realTinkoffService.getOperations(Interval.of(from, to), ticker);

        Assertions.assertSame(operations.getOperations(), result);
    }

    // endregion

    // region OrdersContext methods tests

    @Test
    void getOrders() {
        final List<Order> orders = List.of(new Order(), new Order());
        Mockito.when(ordersContext.getOrders(null)).thenReturn(CompletableFuture.completedFuture(orders));

        final List<Order> result = realTinkoffService.getOrders();

        Assertions.assertSame(orders, result);
    }

    @Test
    void placeLimitOrder() {
        final String ticker = "ticker";
        final String figi = "figi";

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final LimitOrderRequest orderRequest = new LimitOrderRequest();

        final PlacedLimitOrder placedOrder = new PlacedLimitOrder();
        Mockito.when(ordersContext.placeLimitOrder(figi, orderRequest, null))
                .thenReturn(CompletableFuture.completedFuture(placedOrder));
        final PlacedLimitOrder result = realTinkoffService.placeLimitOrder(ticker, orderRequest);

        Assertions.assertSame(placedOrder, result);
    }

    @Test
    void placeMarketOrder() {
        final String ticker = "ticker";
        final String figi = "figi";

        mockInstrument(new MarketInstrument().ticker(ticker).figi(figi));

        final MarketOrderRequest orderRequest = new MarketOrderRequest();

        final PlacedMarketOrder placedOrder = new PlacedMarketOrder();
        Mockito.when(ordersContext.placeMarketOrder(figi, orderRequest, null))
                .thenReturn(CompletableFuture.completedFuture(placedOrder));

        final PlacedMarketOrder result = realTinkoffService.placeMarketOrder(ticker, orderRequest);

        Assertions.assertSame(placedOrder, result);
    }

    @Test
    void cancelOrder() {
        final String orderId = "orderId";

        final CompletableFuture<Void> futureSpy = Mockito.spy(CompletableFuture.completedFuture(null));
        Mockito.when(ordersContext.cancelOrder(orderId, null)).thenReturn(futureSpy);

        realTinkoffService.cancelOrder(orderId);

        Mockito.verify(futureSpy, Mockito.times(1)).join();
    }

    // endregion

    // region PortfolioContext methods tests

    @Test
    void getPortfolioPositions_returnsAndMapsPositions() {
        final ru.tinkoff.invest.openapi.model.rest.PortfolioPosition tinkoffPosition1 =
                new ru.tinkoff.invest.openapi.model.rest.PortfolioPosition()
                        .ticker("ticker1")
                        .balance(BigDecimal.valueOf(1000))
                        .blocked(null)
                        .expectedYield(TestDataHelper.createMoneyAmount(Currency.RUB, 100))
                        .lots(10)
                        .averagePositionPrice(TestDataHelper.createMoneyAmount(Currency.RUB, 110))
                        .averagePositionPriceNoNkd(TestDataHelper.createMoneyAmount(Currency.RUB, 110))
                        .name("name1");

        final ru.tinkoff.invest.openapi.model.rest.PortfolioPosition tinkoffPosition2 =
                new ru.tinkoff.invest.openapi.model.rest.PortfolioPosition()
                        .ticker("ticker2")
                        .balance(BigDecimal.valueOf(2000))
                        .blocked(BigDecimal.valueOf(100))
                        .expectedYield(TestDataHelper.createMoneyAmount(Currency.RUB, 200))
                        .lots(5)
                        .averagePositionPrice(TestDataHelper.createMoneyAmount(Currency.RUB, 440))
                        .averagePositionPriceNoNkd(TestDataHelper.createMoneyAmount(Currency.RUB, 440))
                        .name("name2");
        final Portfolio portfolio = new Portfolio().positions(List.of(tinkoffPosition1, tinkoffPosition2));
        Mockito.when(portfolioContext.getPortfolio(null))
                .thenReturn(CompletableFuture.completedFuture(portfolio));

        final Collection<PortfolioPosition> result = realTinkoffService.getPortfolioPositions();

        Assertions.assertEquals(portfolio.getPositions().size(), result.size());
        Iterator<PortfolioPosition> resultIterator = result.iterator();
        AssertUtils.assertEquals(tinkoffPosition1, resultIterator.next());
        AssertUtils.assertEquals(tinkoffPosition2, resultIterator.next());
    }

    @Test
    void getPortfolioCurrencies() {
        final CurrencyPosition currency1 = new CurrencyPosition()
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(10000))
                .blocked(BigDecimal.valueOf(1000));
        final CurrencyPosition currency2 = new CurrencyPosition()
                .currency(Currency.USD)
                .balance(BigDecimal.valueOf(1000))
                .blocked(null);
        final Currencies currencies = new Currencies().currencies(List.of(currency1, currency2));
        Mockito.when(portfolioContext.getPortfolioCurrencies(null))
                .thenReturn(CompletableFuture.completedFuture(currencies));

        final List<CurrencyPosition> result = realTinkoffService.getPortfolioCurrencies();

        Assertions.assertSame(currencies.getCurrencies(), result);
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
                .thenReturn(TestDataHelper.createInstrumentsFuture(instrument));
    }

}