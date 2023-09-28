package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.bots.FakeBot;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Etf;
import ru.tinkoff.piapi.contract.v1.GetTradingStatusResponse;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static void mockCurrentDateTime(final Context context) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        Mockito.when(context.getCurrentDateTime()).thenReturn(currentDateTime);
    }

    public static void mockTinkoffOperations(
            final FakeBot fakeBot,
            final String accountId,
            final String figi,
            final Interval interval,
            final Operation... operations
    ) {
        Mockito.when(fakeBot.getOperations(accountId, interval, figi))
                .thenReturn(List.of(operations));
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

    public static void mockShare(final ExtInstrumentsService extInstrumentsService, final Share share) {
        Mockito.when(extInstrumentsService.getShare(share.figi())).thenReturn(share);
    }

    public static void mockTradingStatus(final MarketDataService marketDataService, final String figi, final SecurityTradingStatus status) {
        final GetTradingStatusResponse response = GetTradingStatusResponse.newBuilder()
                .setTradingStatus(status)
                .build();
        Mockito.when(marketDataService.getTradingStatusSync(figi)).thenReturn(response);
    }

    public static void mockInstrument(
            final InstrumentsService instrumentsService,
            final ru.tinkoff.piapi.contract.v1.Instrument instrument
    ) {
        Mockito.when(instrumentsService.getInstrumentByFigiSync(instrument.getFigi())).thenReturn(instrument);
    }

    public static void mockInstrument(
            final ExtInstrumentsService instrumentsService,
            final Instrument instrument
    ) {
        Mockito.when(instrumentsService.getInstrument(instrument.figi())).thenReturn(instrument);
    }

    public static void mockShare(final InstrumentsService instrumentsService, final ru.tinkoff.piapi.contract.v1.Share share) {
        Mockito.when(instrumentsService.getShareByFigiSync(share.getFigi())).thenReturn(share);
    }

    public static void mockAllShares(final InstrumentsService instrumentsService, final ru.tinkoff.piapi.contract.v1.Share... shares) {
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(shares));
    }

    public static void mockEtf(final InstrumentsService instrumentsService, final Etf etf) {
        Mockito.when(instrumentsService.getEtfByFigiSync(etf.getFigi())).thenReturn(etf);
    }

    public static void mockCurrency(
            final InstrumentsService instrumentsService,
            final ru.tinkoff.piapi.contract.v1.Currency currency
    ) {
        Mockito.when(instrumentsService.getCurrencyByFigiSync(currency.getFigi()))
                .thenReturn(currency);
    }

    public static void mockAllCurrencies(
            final InstrumentsService instrumentsService,
            final ru.tinkoff.piapi.contract.v1.Currency... currencies
    ) {
        Mockito.when(instrumentsService.getAllCurrenciesSync())
                .thenReturn(List.of(currencies));
    }

    public static void mockBond(final InstrumentsService instrumentsService, final Bond bond) {
        Mockito.when(instrumentsService.getBondByFigiSync(bond.getFigi())).thenReturn(bond);
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
            return interval.splitIntoDailyIntervals().stream()
                    .map(dayInterval -> intervalToTradingDay(dayInterval, startTime, endTime))
                    .toList();
        });
    }

    private static TradingDay intervalToTradingDay(final Interval interval, final OffsetTime startTime, final OffsetTime endTime) {
        final OffsetDateTime startDateTime = DateUtils.setTime(interval.getFrom(), startTime);
        final OffsetDateTime endDateTime = DateUtils.setTime(interval.getTo(), endTime);
        final boolean isTradingDay = DateUtils.isWorkDay(startDateTime);
        return TestData.newTradingDay(isTradingDay, startDateTime, endDateTime);
    }

    public static void mockLastPricesDouble(final MarketDataService marketDataService, final Map<String, Double> figiesToPrices) {
        final List<String> figies = figiesToPrices.keySet().stream().toList();
        final List<LastPrice> lastPrices = figiesToPrices.entrySet().stream()
                .map(entry -> TestData.newLastPrice(entry.getKey(), entry.getValue()))
                .toList();

        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);
    }

    public static void mockLastPricesBigDecimal(
            final MarketDataService marketDataService,
            final Map<String, BigDecimal> figiesToPrices
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
            final Map<ru.tinkoff.piapi.contract.v1.Currency, Double> prices
    ) {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = prices.keySet().stream().toList();
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final Map<String, Double> currenciesLastPrices = new LinkedHashMap<>(prices.size(), 1);
        for (Map.Entry<ru.tinkoff.piapi.contract.v1.Currency, Double> entry : prices.entrySet()) {
            currenciesLastPrices.put(entry.getKey().getFigi(), entry.getValue());
        }
        Mocker.mockLastPricesDouble(marketDataService, currenciesLastPrices);
    }

    public static void mockSharesLastPrices(
            final InstrumentsService instrumentsService,
            final MarketDataService marketDataService,
            final Map<ru.tinkoff.piapi.contract.v1.Share, Double> prices
    ) {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = prices.keySet().stream().toList();
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final Map<String, Double> lastPrices = new LinkedHashMap<>(3, 1);
        for (final Map.Entry<ru.tinkoff.piapi.contract.v1.Share, Double> entry : prices.entrySet()) {
            lastPrices.put(entry.getKey().getFigi(), entry.getValue());
        }
        Mocker.mockLastPricesDouble(marketDataService, lastPrices);
    }

}