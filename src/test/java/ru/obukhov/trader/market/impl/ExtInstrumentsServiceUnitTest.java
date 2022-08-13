package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.Asset;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExtInstrumentsServiceUnitTest {

    @Mock
    private InstrumentsService instrumentsService;

    @InjectMocks
    private ExtInstrumentsService extInstrumentsService;

    // region getFigiByTicker tests

    @Test
    void getFigiByTicker_returnsFirstFigi_whenAssetAndInstrumentExists() {
        final String ticker1 = "ticker1";
        final String figi1 = "figi1";

        final String ticker2 = "ticker2";
        final String figi2 = "figi2";

        final String ticker3 = "ticker3";
        final String figi3 = "figi3";

        final String figi4 = "figi4";

        Mocker.mockFigiByTicker(instrumentsService, figi1, ticker1);
        final AssetInstrument assetInstrument1 = TestData.createAssetInstrument(figi1, ticker1);
        final Asset asset1 = Asset.newBuilder()
                .addInstruments(assetInstrument1)
                .build();

        final AssetInstrument assetInstrument2 = TestData.createAssetInstrument(figi2, ticker2);
        final AssetInstrument assetInstrument3 = TestData.createAssetInstrument(figi3, ticker3);
        final AssetInstrument assetInstrument4 = TestData.createAssetInstrument(figi4, ticker3);
        final Asset asset2 = Asset.newBuilder()
                .addInstruments(assetInstrument2)
                .addInstruments(assetInstrument3)
                .addInstruments(assetInstrument4)
                .build();
        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of(asset1, asset2));

        final String result = extInstrumentsService.getFigiByTicker(ticker3);

        Assertions.assertEquals(figi3, result);
    }

    @Test
    void getFigiByTicker_throwsIllegalArgumentException_whenAssetsNotExists() {
        final String ticker = "ticker";

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(Collections.emptyList());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> extInstrumentsService.getFigiByTicker(ticker),
                "Not found instrument for ticker '" + ticker + "'"
        );
    }

    @Test
    void getFigiByTicker_throwsIllegalArgumentException_whenInstrumentNotExists() {
        final String ticker1 = "ticker1";
        final String figi1 = "figi1";

        final String ticker2 = "ticker2";
        final String figi2 = "figi2";

        final String ticker3 = "ticker3";
        final String figi3 = "figi3";

        final String ticker4 = "ticker4";

        Mocker.mockFigiByTicker(instrumentsService, figi1, ticker1);
        final AssetInstrument assetInstrument11 = TestData.createAssetInstrument(figi1, ticker1);
        final Asset asset1 = Asset.newBuilder()
                .addInstruments(assetInstrument11)
                .build();

        final AssetInstrument assetInstrument21 = TestData.createAssetInstrument(figi2, ticker2);
        final AssetInstrument assetInstrument22 = TestData.createAssetInstrument(figi3, ticker3);
        final Asset asset2 = Asset.newBuilder()
                .addInstruments(assetInstrument21)
                .addInstruments(assetInstrument22)
                .build();
        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of(asset1, asset2));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> extInstrumentsService.getFigiByTicker(ticker4),
                "Not found instrument for ticker '" + ticker4 + "'"
        );
    }

    // endregion

    // region getShare tests

    @Test
    void getShare_returnsShare_whenExists() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final Share share1 = Share.newBuilder().setTicker(ticker1).build();
        final Share share2 = Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final Share result = extInstrumentsService.getShare(ticker2);

        Assertions.assertSame(share2, result);
    }

    @Test
    void getShare_returnsNull_whenShareNotExists() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";
        final Share share1 = Share.newBuilder().setTicker(ticker1).build();
        final Share share2 = Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final Share result = extInstrumentsService.getShare(ticker3);

        Assertions.assertNull(result);
    }

    // endregion
}