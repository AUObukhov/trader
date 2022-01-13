package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.OrdersService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.trading.bots.impl.FakeBot;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.Order;

import java.time.OffsetDateTime;
import java.util.List;

@UtilityClass
public class Mocker {

    public static MarketInstrument createAndMockInstrument(final TinkoffService tinkoffService, final String ticker, final int lotSize) {
        final MarketInstrument instrument = createMarketInstrument(ticker, lotSize);
        Mockito.when(tinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);
        return instrument;
    }

    public static MarketInstrument createAndMockInstrument(final FakeBot fakeBot, final String ticker, final int lotSize) {
        final MarketInstrument instrument = createMarketInstrument(ticker, lotSize);
        Mockito.when(fakeBot.searchMarketInstrument(ticker)).thenReturn(instrument);
        return instrument;
    }

    public static MarketInstrument createAndMockInstrument(final MarketService marketService, final String ticker, final int lotSize) {
        final MarketInstrument instrument = createMarketInstrument(ticker, lotSize);
        Mockito.when(marketService.getInstrument(ticker)).thenReturn(instrument);
        return instrument;
    }

    private static MarketInstrument createMarketInstrument(String ticker, int lotSize) {
        return new MarketInstrument()
                .figi(StringUtils.EMPTY)
                .ticker(ticker)
                .lot(lotSize)
                .currency(Currency.RUB)
                .name(StringUtils.EMPTY)
                .type(InstrumentType.STOCK);
    }

    public static void mockEmptyOrder(final OrdersService ordersService, final String ticker) {
        final Order order = new Order();
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(List.of(order));
    }

    public static MockedStatic<OffsetDateTime> mockNow(final OffsetDateTime mockedNow) {
        final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mockito.mockStatic(OffsetDateTime.class, Mockito.CALLS_REAL_METHODS);
        offsetDateTimeStaticMock.when(OffsetDateTime::now).thenReturn(mockedNow);
        return offsetDateTimeStaticMock;
    }

    public static void mockTinkoffOperations(
            final FakeBot fakeBot,
            @Nullable final String brokerAccountId,
            final String ticker,
            final Interval interval,
            final Operation... operations
    ) {
        Mockito.when(fakeBot.getOperations(brokerAccountId, interval, ticker))
                .thenReturn(List.of(operations));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static MockedStatic<Runtime> mockRuntime(final Runtime runtime) {
        final MockedStatic<Runtime> runtimeStaticMock = Mockito.mockStatic(Runtime.class, Mockito.CALLS_REAL_METHODS);
        runtimeStaticMock.when(Runtime::getRuntime).thenReturn(runtime);
        return runtimeStaticMock;
    }

}