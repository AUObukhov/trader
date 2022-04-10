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
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.trading.bots.impl.FakeBot;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@UtilityClass
public class Mocker {

    public static MarketInstrument createAndMockInstrument(final TinkoffService tinkoffService, final String ticker, final int lotSize)
            throws IOException {
        final MarketInstrument instrument = createMarketInstrument(ticker, lotSize);
        Mockito.when(tinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);
        return instrument;
    }

    public static MarketInstrument createAndMockInstrument(final FakeBot fakeBot, final String ticker, final int lotSize) throws IOException {
        final MarketInstrument instrument = createMarketInstrument(ticker, lotSize);
        Mockito.when(fakeBot.searchMarketInstrument(ticker)).thenReturn(instrument);
        return instrument;
    }

    public static MarketInstrument createAndMockInstrument(final MarketService marketService, final String ticker, final int lotSize)
            throws IOException {
        final MarketInstrument instrument = createMarketInstrument(ticker, lotSize);
        Mockito.when(marketService.getInstrument(ticker)).thenReturn(instrument);
        return instrument;
    }

    public static MarketInstrument createAndMockInstrument(final MarketService marketService, final String ticker, final String figi)
            throws IOException {
        final MarketInstrument instrument = createMarketInstrument(ticker, figi);
        Mockito.when(marketService.getInstrument(ticker)).thenReturn(instrument);
        return instrument;
    }

    private static MarketInstrument createMarketInstrument(final String ticker, final int lotSize) {
        return new MarketInstrument()
                .figi(StringUtils.EMPTY)
                .ticker(ticker)
                .lot(lotSize)
                .currency(Currency.RUB)
                .name(StringUtils.EMPTY)
                .type(InstrumentType.STOCK);
    }

    private static MarketInstrument createMarketInstrument(final String ticker, final String figi) {
        return new MarketInstrument()
                .figi(figi)
                .ticker(ticker)
                .lot(1)
                .currency(Currency.RUB)
                .name(StringUtils.EMPTY)
                .type(InstrumentType.STOCK);
    }

    public static void mockEmptyOrder(final OrdersService ordersService, final String ticker) throws IOException {
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
    ) throws IOException {
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