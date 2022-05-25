package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.trading.bots.impl.FakeBot;
import ru.tinkoff.piapi.contract.v1.Asset;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@UtilityClass
public class Mocker {

    public static void mockEmptyOrder(final MarketOrdersService ordersService, final String ticker) throws IOException {
        final Order order = TestData.createOrder();
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

    public static void mockFigiByTicker(final InstrumentsService instrumentsService, final String figi, final String ticker) {
        final AssetInstrument assetInstrument = AssetInstrument.newBuilder()
                .setFigi(figi)
                .setTicker(ticker)
                .build();
        final Asset asset = Asset.newBuilder()
                .addInstruments(assetInstrument)
                .build();
        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of(asset));
    }

    public static void mockTickerByFigi(final InstrumentsService instrumentsService, final String ticker, final String figi) {
        final Instrument instrument = Instrument.newBuilder().setTicker(ticker).build();
        Mockito.when(instrumentsService.getInstrumentByFigiSync(figi)).thenReturn(instrument);
    }
}