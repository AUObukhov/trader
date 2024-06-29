package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Periods;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.MapUtils;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccount;
import ru.obukhov.trader.test.utils.model.bond.TestBond;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.dividend.TestDividend;
import ru.obukhov.trader.test.utils.model.etf.TestEtf;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.trading.bots.FakeBot;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusResponse;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.UsersService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

@UtilityClass
public class Mocker {

    public static void mockEmptyOrder(final ExtOrdersService ordersService, final String figi) {
        final OrderState order = OrderState.builder().build();
        Mockito.when(ordersService.getOrders(figi)).thenReturn(List.of(order));
    }

    public static MockedStatic<OffsetDateTime> mockNow(final OffsetDateTime mockedNow) {
        final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mockito.mockStatic(OffsetDateTime.class, Mockito.CALLS_REAL_METHODS);
        offsetDateTimeStaticMock.when(OffsetDateTime::now).thenReturn(mockedNow);
        return offsetDateTimeStaticMock;
    }

    public static MockedStatic<Instant> mockNow(final Instant mockedNow) {
        final MockedStatic<Instant> instantStaticMock = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS);
        instantStaticMock.when(Instant::now).thenReturn(mockedNow);
        return instantStaticMock;
    }

    public static void mockTinkoffOperations(
            final FakeBot fakeBot,
            final String accountId,
            final String figi,
            final Interval interval,
            final Operation... operations
    ) {
        Mockito.when(fakeBot.getOperations(accountId, interval, List.of(figi)))
                .thenReturn(Map.of(figi, List.of(operations)));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static MockedStatic<Runtime> mockRuntime(final Runtime runtime) {
        final MockedStatic<Runtime> runtimeStaticMock = Mockito.mockStatic(Runtime.class, Mockito.CALLS_REAL_METHODS);
        runtimeStaticMock.when(Runtime::getRuntime).thenReturn(runtime);
        return runtimeStaticMock;
    }

    public static void mockTickerByFigi(final InstrumentsService instrumentsService, final String ticker, final String figi) {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = ru.tinkoff.piapi.contract.v1.Instrument.newBuilder().setTicker(ticker).build();
        Mockito.when(instrumentsService.getInstrumentByFigiSync(figi)).thenReturn(instrument);
    }

    public static void verifyNoOrdersMade(final ExtOrdersService ordersService) {
        Mockito.verify(ordersService, Mockito.never())
                .postOrder(
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.isNull(),
                        Mockito.any(OrderDirection.class),
                        Mockito.any(OrderType.class),
                        Mockito.isNull()
                );
    }

    public static void mockAccounts(final UsersService usersService, final TestAccount... accounts) {
        final List<ru.tinkoff.piapi.contract.v1.Account> tinkoffAccounts = Arrays.stream(accounts).map(TestAccount::tinkoffAccount).toList();
        Mockito.when(usersService.getAccountsSync()).thenReturn(tinkoffAccounts);
    }

    public static void mockShares(final ExtInstrumentsService extInstrumentsService, final TestShare... testShares) {
        final List<String> figies = Arrays.stream(testShares).map(TestShare::getFigi).toList();
        final List<Share> shares = Arrays.stream(testShares).map(TestShare::share).toList();
        Mockito.when(extInstrumentsService.getShares(figies)).thenReturn(shares);
    }

    public static void mockTradingStatus(final MarketDataService marketDataService, final String figi, final SecurityTradingStatus status) {
        final GetTradingStatusResponse response = GetTradingStatusResponse.newBuilder()
                .setTradingStatus(status)
                .build();
        Mockito.when(marketDataService.getTradingStatusSync(figi)).thenReturn(response);
    }

    public static void mockInstrument(final InstrumentsService instrumentsService, final TestInstrument instrument) {
        Mockito.when(instrumentsService.getInstrumentByFigiSync(instrument.getFigi())).thenReturn(instrument.tinkoffInstrument());
    }

    public static void mockInstrument(final InstrumentsService instrumentsService, final TestShare share) {
        Mockito.when(instrumentsService.getInstrumentByFigiSync(share.getFigi())).thenReturn(share.tinkoffInstrument());
    }

    public static void mockInstrument(final InstrumentsService instrumentsService, final TestCurrency currency) {
        Mockito.when(instrumentsService.getInstrumentByFigiSync(currency.getFigi())).thenReturn(currency.tinkoffInstrument());
    }

    public static void mockInstrument(final ExtInstrumentsService instrumentsService, final TestShare share) {
        Mockito.when(instrumentsService.getInstrument(share.getFigi())).thenReturn(share.instrument());
    }

    public void mockShare(
            final InstrumentsService instrumentsService,
            final MarketDataService marketDataService,
            final TestShare share,
            final OffsetDateTime to
    ) {
        mockInstrument(instrumentsService, share);
        mockCandles(marketDataService, share.getFigi(), share.candles());
        mockDividends(instrumentsService, share, to);
    }

    public static void mockShare(final InstrumentsService instrumentsService, final TestShare share) {
        Mockito.when(instrumentsService.getShareByFigiSync(share.getFigi())).thenReturn(share.tinkoffShare());
    }

    public void mockAllShares(
            final InstrumentsService instrumentsService,
            final MarketDataService marketDataService,
            final List<TestShare> testShares,
            final OffsetDateTime mockedNow
    ) {
        testShares.forEach(share -> mockShare(instrumentsService, marketDataService, share, mockedNow));
        mockAllShares(instrumentsService, testShares);
    }

    public static void mockAllShares(final InstrumentsService instrumentsService, final List<TestShare> shares) {
        final List<ru.tinkoff.piapi.contract.v1.Share> tinkoffShares = shares.stream().map(TestShare::tinkoffShare).toList();
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(tinkoffShares);
    }

    public static void mockAllShares(final InstrumentsService instrumentsService, final TestShare... shares) {
        final List<ru.tinkoff.piapi.contract.v1.Share> tinkoffShares = Arrays.stream(shares).map(TestShare::tinkoffShare).toList();
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(tinkoffShares);
    }

    public static void mockEtf(final InstrumentsService instrumentsService, final TestEtf etf) {
        Mockito.when(instrumentsService.getEtfByFigiSync(etf.getFigi())).thenReturn(etf.tinkoffEtf());
    }

    public void mockCurrency(
            final InstrumentsService instrumentsService,
            final MarketDataService marketDataService,
            final TestCurrency currency
    ) {
        mockInstrument(instrumentsService, currency);
        mockCandles(marketDataService, currency.getFigi(), currency.candles());
    }

    public static void mockCurrency(final InstrumentsService instrumentsService, final TestCurrency currency) {
        Mockito.when(instrumentsService.getCurrencyByFigiSync(currency.getFigi()))
                .thenReturn(currency.tinkoffCurrency());
    }

    public static void mockAllCurrencies(final InstrumentsService instrumentsService, final TestCurrency... currencies) {
        final List<ru.tinkoff.piapi.contract.v1.Currency> tinkoffCurrencies = Arrays.stream(currencies).map(TestCurrency::tinkoffCurrency).toList();
        Mockito.when(instrumentsService.getAllCurrenciesSync())
                .thenReturn(tinkoffCurrencies);
    }

    public static void mockBond(final InstrumentsService instrumentsService, final TestBond bond) {
        Mockito.when(instrumentsService.getBondByFigiSync(bond.getFigi())).thenReturn(bond.tinkoffBond());
    }

    public static void mockTradingSchedules(
            final ExtInstrumentsService instrumentsService,
            final List<String> figies,
            final OffsetTime startTime,
            final OffsetTime endTime
    ) {
        for (final String figi : figies) {
            mockTradingSchedule(instrumentsService, figi, startTime, endTime);
        }
    }

    public static void mockTradingSchedule(
            final ExtInstrumentsService instrumentsService,
            final String figi,
            final OffsetTime startTime,
            final OffsetTime endTime
    ) {
        Mockito.when(instrumentsService.getTradingScheduleByFigi(
                Mockito.eq(figi),
                Mockito.any(Interval.class)
        )).thenAnswer(invocation -> {
            final Interval interval = invocation.getArgument(1);
            return interval.splitIntoIntervals(Periods.DAY).stream()
                    .map(dayInterval -> intervalToTradingDay(dayInterval, startTime, endTime))
                    .toList();
        });
    }

    public static void mockDividends(
            final InstrumentsService instrumentsService,
            final TestShare share,
            final OffsetDateTime to
    ) {
        final List<ru.tinkoff.piapi.contract.v1.Dividend> tinkoffDividends = share.dividends().stream()
                .map(TestDividend::tinkoffDividend)
                .toList();
        Mockito.when(instrumentsService.getDividendsSync(share.getFigi(), share.getFirst1DayCandleDate().toInstant(), to.toInstant()))
                .thenReturn(tinkoffDividends);
    }

    public static void mockSecurity(final ExtOperationsService extOperationsService, final String accountId) {
        Mockito.when(extOperationsService.getSecurity(Mockito.eq(accountId), Mockito.anyString()))
                .thenAnswer(invocationOnMock -> {
                    final String figi = invocationOnMock.getArgument(1);
                    return new PositionBuilder().setFigi(figi).build();
                });
    }

    private static TradingDay intervalToTradingDay(final Interval interval, final OffsetTime startTime, final OffsetTime endTime) {
        final OffsetDateTime startDateTime = DateUtils.setTime(interval.getFrom(), startTime);
        OffsetDateTime to = interval.getTo();
        to = to.equals(DateUtils.toStartOfDay(to)) ? to.minusDays(1) : to;
        final OffsetDateTime endDateTime = DateUtils.setTime(to, endTime);
        final boolean isTradingDay = DateUtils.isWorkDay(startDateTime);
        return TestData.newTradingDay(isTradingDay, startDateTime, endDateTime);
    }

    public static void mockLastPricesDouble(
            final MarketDataService marketDataService,
            final SequencedMap<String, Double> figiesToPrices
    ) {
        final List<String> figies = figiesToPrices.keySet().stream().toList();
        final List<LastPrice> lastPrices = figiesToPrices.entrySet().stream()
                .map(entry -> TestData.newLastPrice(entry.getKey(), entry.getValue()))
                .toList();

        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);
    }

    public static void mockLastPricesBigDecimal(
            final MarketDataService marketDataService,
            final SequencedMap<String, BigDecimal> figiesToPrices
    ) {
        final List<String> figies = figiesToPrices.keySet().stream().toList();
        final List<LastPrice> lastPrices = figiesToPrices.entrySet().stream()
                .map(entry -> TestData.newLastPrice(entry.getKey(), entry.getValue()))
                .toList();

        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);
    }

    public static void mockCurrenciesLastPrices(
            final InstrumentsService instrumentsService,
            final MarketDataService marketDataService,
            final SequencedMap<TestCurrency, Double> prices
    ) {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = prices.keySet().stream().map(TestCurrency::tinkoffCurrency).toList();
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final SequencedMap<String, Double> currenciesLastPrices = new LinkedHashMap<>(prices.size(), 1);
        for (Map.Entry<TestCurrency, Double> entry : prices.entrySet()) {
            currenciesLastPrices.put(entry.getKey().getFigi(), entry.getValue());
        }
        Mocker.mockLastPricesDouble(marketDataService, currenciesLastPrices);
    }

    public static void mockSharesLastPrices(
            final InstrumentsService instrumentsService,
            final MarketDataService marketDataService,
            final SequencedMap<TestShare, Double> prices
    ) {
        final List<ru.tinkoff.piapi.contract.v1.Share> tinkoffShares = prices.keySet().stream().map(TestShare::tinkoffShare).toList();
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(tinkoffShares);

        final SequencedMap<String, Double> lastPrices = new LinkedHashMap<>(3, 1);
        for (final Map.Entry<TestShare, Double> entry : prices.entrySet()) {
            lastPrices.put(entry.getKey().getFigi(), entry.getValue());
        }
        Mocker.mockLastPricesDouble(marketDataService, lastPrices);
    }

    public static void mockAvailableBalances(
            final ExtOperationsService extOperationsService,
            final String accountId,
            final int balance,
            final String... currencies
    ) {
        final BigDecimal decimalBalance = DecimalUtils.setDefaultScale(balance);
        final Map<String, BigDecimal> balances = Arrays.stream(currencies).collect(MapUtils.newMapValueCollector(currency -> decimalBalance));
        Mockito.when(extOperationsService.getAvailableBalances(accountId))
                .thenReturn(balances);
    }

    public static void mockEmptyCandles(
            final MarketDataService marketDataService,
            final String figi,
            final CandleInterval candleInterval
    ) {
        Mockito.when(marketDataService.getCandlesSync(
                Mockito.eq(figi),
                Mockito.any(Instant.class),
                Mockito.any(Instant.class),
                Mockito.eq(candleInterval)
        )).thenReturn(Collections.emptyList());
    }

    private void mockCandles(
            final MarketDataService marketDataService,
            final String figi,
            final Map<CandleInterval, List<HistoricCandle>> candles
    ) {
        for (Map.Entry<CandleInterval, List<HistoricCandle>> entry : candles.entrySet()) {
            new CandleMocker(marketDataService, figi, entry.getKey())
                    .add(entry.getValue())
                    .mock();
        }
    }

}