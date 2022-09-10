package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.Asset;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.Share;

import java.util.Collections;
import java.util.List;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class ExtInstrumentsServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private ExtInstrumentsService extInstrumentsService;

    // region getFigiByTicker tests

    @Test
    @DirtiesContext
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
    @DirtiesContext
    void getFigiByTicker_returnsCachedValue() {
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
        extInstrumentsService.getFigiByTicker(ticker3);

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of());
        final String result = extInstrumentsService.getFigiByTicker(ticker3);

        Assertions.assertEquals(figi3, result);
    }

    @Test
    @DirtiesContext
    void getFigiByTicker_throwsIllegalArgumentException_whenNoAssets() {
        final String ticker = "ticker";

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(Collections.emptyList());

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> extInstrumentsService.getFigiByTicker(ticker),
                "Not found instrument for ticker '" + ticker + "'"
        );
    }

    @Test
    @DirtiesContext
    void getFigiByTicker_throwsIllegalArgumentException_whenNoInstrument() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";
        final String ticker4 = "ticker4";

        final String figi2 = "figi2";
        final String figi1 = "figi1";
        final String figi3 = "figi3";

        final AssetInstrument assetInstrument11 = TestData.createAssetInstrument(figi1, ticker1);
        final AssetInstrument assetInstrument21 = TestData.createAssetInstrument(figi2, ticker2);
        final AssetInstrument assetInstrument22 = TestData.createAssetInstrument(figi3, ticker3);

        final Asset asset1 = Asset.newBuilder()
                .addInstruments(assetInstrument11)
                .build();
        final Asset asset2 = Asset.newBuilder()
                .addInstruments(assetInstrument21)
                .addInstruments(assetInstrument22)
                .build();

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of(asset1, asset2));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> extInstrumentsService.getFigiByTicker(ticker4),
                "Not found instrument for ticker '" + ticker4 + "'"
        );
    }

    // endregion

    // region getTickerByFigi tests

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsTicker_whenInstrumentExists() {
        final String ticker = "ticker";
        final String figi = "figi";

        Mocker.mockTickerByFigi(instrumentsService, ticker, figi);
        final String result = extInstrumentsService.getTickerByFigi(figi);

        Assertions.assertEquals(ticker, result);
    }

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsCachedValue() {
        final String ticker = "ticker";
        final String figi = "figi";

        Mocker.mockTickerByFigi(instrumentsService, ticker, figi);
        extInstrumentsService.getTickerByFigi(figi);

        Mockito.when(instrumentsService.getInstrumentByFigiSync(figi)).thenReturn(null);
        final String result = extInstrumentsService.getTickerByFigi(figi);

        Assertions.assertEquals(ticker, result);
    }

    @Test
    @DirtiesContext
    void getTickerByFigi_throwsIllegalArgumentException_whenNoInstrument() {
        final String figi = "figi";

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> extInstrumentsService.getTickerByFigi(figi),
                "Not found instrument for figi '" + figi + "'"
        );
    }

    // endregion

    // region getShare tests

    @Test
    void getShare_returnsShare_whenShareExists() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final Share share1 = Share.newBuilder().setTicker(ticker1).build();
        final Share share2 = Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final Share result = extInstrumentsService.getShare(ticker2);

        Assertions.assertSame(share2, result);
    }

    @Test
    void getShare_returnsNull_whenNoShare() {
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