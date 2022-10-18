package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.bond.TestBond1;
import ru.obukhov.trader.test.utils.model.bond.TestBond2;
import ru.obukhov.trader.test.utils.model.bond.TestBond3;
import ru.obukhov.trader.test.utils.model.bond.TestBond4;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency2;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency3;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency4;
import ru.obukhov.trader.test.utils.model.etf.TestEtf1;
import ru.obukhov.trader.test.utils.model.etf.TestEtf2;
import ru.obukhov.trader.test.utils.model.etf.TestEtf3;
import ru.obukhov.trader.test.utils.model.etf.TestEtf4;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay2;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay3;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.obukhov.trader.test.utils.model.share.TestShare4;
import ru.obukhov.trader.test.utils.model.share.TestShare5;
import ru.tinkoff.piapi.contract.v1.Asset;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

//@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class ExtInstrumentsServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private ExtInstrumentsService extInstrumentsService;

    // region getSingleFigiByTicker tests

    @Test
    @DirtiesContext
    void getSingleFigiByTicker_returnsFigi_whenAssetAndInstrumentFound() {
        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;

        final String ticker3 = TestShare4.TICKER;
        final String figi3 = TestShare4.FIGI;

        final String figi4 = TestShare5.FIGI;

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

        final String result = extInstrumentsService.getSingleFigiByTicker(ticker2);

        Assertions.assertEquals(figi2, result);
    }

    @Test
    @DirtiesContext
    void getSingleFigiByTicker_returnsCachedValue() {
        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;

        final String ticker3 = TestShare4.TICKER;
        final String figi3 = TestShare4.FIGI;

        final String figi4 = TestShare5.FIGI;

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
        extInstrumentsService.getSingleFigiByTicker(ticker2);

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of());
        final String result = extInstrumentsService.getSingleFigiByTicker(ticker2);

        Assertions.assertEquals(figi2, result);
    }

    @Test
    @DirtiesContext
    void getSingleFigiByTicker_throwsIllegalArgumentException_whenMultipleInstrumentsFound() {
        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;

        final String ticker3 = TestShare4.TICKER;
        final String figi3 = TestShare4.FIGI;

        final String figi4 = TestShare5.FIGI;

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

        final Executable executable = () -> extInstrumentsService.getSingleFigiByTicker(ticker3);
        final String expectedMessage = "Expected single instrument for ticker '" + ticker3 + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getSingleFigiByTicker_throwsIllegalArgumentException_whenNoAssets() {
        final String ticker = TestShare1.TICKER;

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(Collections.emptyList());

        final Executable executable = () -> extInstrumentsService.getSingleFigiByTicker(ticker);
        final String expectedMessage = "Expected single instrument for ticker '" + ticker + "'. Found 0";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getSingleFigiByTicker_throwsIllegalArgumentException_whenNoInstrument() {
        final AssetInstrument assetInstrument11 = TestData.createAssetInstrument(TestShare1.FIGI, TestShare1.TICKER);
        final AssetInstrument assetInstrument21 = TestData.createAssetInstrument(TestShare2.FIGI, TestShare2.TICKER);
        final AssetInstrument assetInstrument22 = TestData.createAssetInstrument(TestShare3.FIGI, TestShare3.TICKER);
        final String ticker4 = TestShare4.TICKER;

        final Asset asset1 = Asset.newBuilder()
                .addInstruments(assetInstrument11)
                .build();
        final Asset asset2 = Asset.newBuilder()
                .addInstruments(assetInstrument21)
                .addInstruments(assetInstrument22)
                .build();

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of(asset1, asset2));

        final Executable executable = () -> extInstrumentsService.getSingleFigiByTicker(ticker4);
        final String expectedMessage = "Expected single instrument for ticker '" + ticker4 + "'. Found 0";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getFigiesByTicker tests

    @Test
    @DirtiesContext
    void getFigiesByTicker_returnsFirstFigi_whenAssetAndInstrumentFound() {
        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;

        final String ticker3 = TestShare4.TICKER;
        final String figi3 = TestShare4.FIGI;

        final String figi4 = TestShare5.FIGI;

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

        final List<String> result = extInstrumentsService.getFigiesByTicker(ticker3);

        final List<String> expectedResult = List.of(figi3, figi4);
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DirtiesContext
    void getFigiesByTicker_returnsCachedValue() {
        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;

        final String ticker3 = TestShare4.TICKER;
        final String figi3 = TestShare4.FIGI;

        final String figi4 = TestShare5.FIGI;

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
        extInstrumentsService.getFigiesByTicker(ticker3);

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of());
        final List<String> result = extInstrumentsService.getFigiesByTicker(ticker3);

        final List<String> expectedResult = List.of(figi3, figi4);
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    @DirtiesContext
    void getFigiesByTicker_returnsEmptyList_whenNoAssets() {
        final String ticker = TestShare1.TICKER;

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(Collections.emptyList());

        final List<String> result = extInstrumentsService.getFigiesByTicker(ticker);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DirtiesContext
    void getFigiesByTicker_returnsEmptyList_whenNoInstrument() {
        final AssetInstrument assetInstrument11 = TestData.createAssetInstrument(TestShare1.FIGI, TestShare1.TICKER);
        final AssetInstrument assetInstrument21 = TestData.createAssetInstrument(TestShare2.FIGI, TestShare2.TICKER);
        final AssetInstrument assetInstrument22 = TestData.createAssetInstrument(TestShare3.FIGI, TestShare3.TICKER);
        final String ticker4 = TestShare4.TICKER;

        final Asset asset1 = Asset.newBuilder()
                .addInstruments(assetInstrument11)
                .build();
        final Asset asset2 = Asset.newBuilder()
                .addInstruments(assetInstrument21)
                .addInstruments(assetInstrument22)
                .build();

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of(asset1, asset2));

        final List<String> result = extInstrumentsService.getFigiesByTicker(ticker4);

        Assertions.assertTrue(result.isEmpty());
    }

    // endregion

    // region getTickerByFigi tests

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsTicker_whenInstrumentFound() {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;

        Mocker.mockTickerByFigi(instrumentsService, ticker, figi);
        final String result = extInstrumentsService.getTickerByFigi(figi);

        Assertions.assertEquals(ticker, result);
    }

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsCachedValue() {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;

        Mocker.mockTickerByFigi(instrumentsService, ticker, figi);
        extInstrumentsService.getTickerByFigi(figi);

        Mockito.when(instrumentsService.getInstrumentByFigiSync(figi)).thenReturn(null);
        final String result = extInstrumentsService.getTickerByFigi(figi);

        Assertions.assertEquals(ticker, result);
    }

    @Test
    @DirtiesContext
    void getTickerByFigi_throwsIllegalArgumentException_whenNoInstrument() {
        final String figi = TestShare1.FIGI;

        final Executable executable = () -> extInstrumentsService.getTickerByFigi(figi);
        final String expectedMessage = "Not found instrument for figi '" + figi + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getExchange tests

    @Test
    void getExchange_returnsExchange_whenShareTicker() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final Exchange result = extInstrumentsService.getExchange(TestShare2.TICKER);

        Assertions.assertEquals(TestShare2.EXCHANGE, result);
    }

    @Test
    void getExchange_throwIllegalArgumentException_whenMultipleSharesFound() {
        final String ticker = TestShare4.TICKER;
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE,
                TestShare3.TINKOFF_SHARE,
                TestShare4.TINKOFF_SHARE,
                TestShare5.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final String expectedMessage = "Expected maximum of one share for ticker '" + ticker + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getExchange(ticker), expectedMessage);
    }

    @Test
    void getExchange_returnsExchange_whenCurrencyTicker() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final Exchange result = extInstrumentsService.getExchange(TestCurrency2.TICKER);

        Assertions.assertEquals(TestCurrency2.EXCHANGE, result);
    }

    @Test
    void getExchange_throwIllegalArgumentException_whenMultipleCurrenciesFound() {
        final String ticker = TestCurrency3.TICKER;
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final String expectedMessage = "Expected maximum of one currency for ticker '" + ticker + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getExchange(ticker), expectedMessage);
    }

    @Test
    void getExchange_returnsExchange_whenEtfTicker() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.TINKOFF_ETF, TestEtf3.TINKOFF_ETF);
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final Exchange result = extInstrumentsService.getExchange(TestEtf3.TICKER);

        Assertions.assertEquals(TestEtf3.EXCHANGE, result);
    }

    @Test
    void getExchange_throwIllegalArgumentException_whenMultipleEtfsFound() {
        final String ticker = TestEtf3.TICKER;
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(
                TestEtf1.TINKOFF_ETF,
                TestEtf2.TINKOFF_ETF,
                TestEtf3.TINKOFF_ETF,
                TestEtf4.TINKOFF_ETF
        );
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final String expectedMessage = "Expected maximum of one etf for ticker '" + ticker + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getExchange(ticker), expectedMessage);
    }

    @Test
    void getExchange_returnsExchange_whenBondTicker() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final Exchange result = extInstrumentsService.getExchange(TestBond2.TICKER);

        Assertions.assertEquals(TestBond2.EXCHANGE, result);
    }

    @Test
    void getExchange_throwIllegalArgumentException_whenMultipleBondsFound() {
        final String ticker = TestBond3.TICKER;
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final String expectedMessage = "Expected maximum of one bond for ticker '" + ticker + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getExchange(ticker), expectedMessage);
    }

    @Test
    void getExchange_throwIllegalArgumentException_whenInstrumentNotFound() {
        final String ticker = TestShare2.TICKER;

        final String expectedMessage = "Not found instrument for ticker '" + ticker + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getExchange(ticker), expectedMessage);
    }

    // endregion

    // region getShares tests

    @Test
    void getShares_returnsShare_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare2.TICKER);

        final List<Share> expectedShares = List.of(TestShare2.SHARE);
        Assertions.assertEquals(expectedShares, result);
    }

    @Test
    void getShares_returnsShareIgnoreCase_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare2.TICKER.toLowerCase());

        final List<Share> expectedShares = List.of(TestShare2.SHARE);
        Assertions.assertEquals(expectedShares, result);
    }

    @Test
    void getShares_returnsEmptyList_whenNoShares() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare4.TICKER);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getShares_returnsMultipleShares_whenMultipleShares() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare4.TINKOFF_SHARE,
                TestShare5.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare5.TICKER);

        final List<Share> expectedShares = List.of(TestShare4.SHARE, TestShare5.SHARE);
        Assertions.assertEquals(expectedShares, result);
    }

    // endregion

    // region getSingleShare tests

    @Test
    void getSingleShare_returnsShare_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final Share result = extInstrumentsService.getSingleShare(TestShare2.TICKER);

        Assertions.assertEquals(TestShare2.SHARE, result);
    }

    @Test
    void getSingleShare_returnsShareIgnoreCase_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final Share result = extInstrumentsService.getSingleShare(TestShare2.TICKER.toLowerCase());

        Assertions.assertEquals(TestShare2.SHARE, result);
    }

    @Test
    void getSingleShare_throwIllegalArgumentException_whenNoShare() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare2.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final String ticker = TestShare3.TICKER;
        final Executable executable = () -> extInstrumentsService.getSingleShare(ticker);
        final String expectedMessage = "Expected single share for ticker '" + ticker + "'. Found 0";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getSingleShare_throwIllegalArgumentException_whenMultipleShares() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.TINKOFF_SHARE,
                TestShare4.TINKOFF_SHARE,
                TestShare5.TINKOFF_SHARE
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final String ticker = TestShare4.TICKER;
        final Executable executable = () -> extInstrumentsService.getSingleShare(ticker);
        final String expectedMessage = "Expected single share for ticker '" + ticker + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getEtfs tests

    @Test
    void getEtfs_returnsEtf_whenSingleEtfFound() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.TINKOFF_ETF, TestEtf3.TINKOFF_ETF);
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER);

        final List<Etf> expectedEtfs = List.of(TestEtf3.ETF);
        Assertions.assertEquals(expectedEtfs, result);
    }

    @Test
    void getEtfs_returnsEtfIgnoreCase_whenSingleEtfFound() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.TINKOFF_ETF, TestEtf3.TINKOFF_ETF);
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER.toLowerCase());

        final List<Etf> expectedEtfs = List.of(TestEtf3.ETF);
        Assertions.assertEquals(expectedEtfs, result);
    }

    @Test
    void getEtfs_returnsEmptyList_whenNoEtfs() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.TINKOFF_ETF, TestEtf2.TINKOFF_ETF);
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getEtfs_returnsMultipleEtfs_whenMultipleEtfs() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(
                TestEtf1.TINKOFF_ETF,
                TestEtf3.TINKOFF_ETF,
                TestEtf4.TINKOFF_ETF
        );
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER);

        final List<Etf> expectedEtfs = List.of(TestEtf3.ETF, TestEtf4.ETF);

        Assertions.assertEquals(expectedEtfs, result);
    }

    // endregion

    // region getSingleEtf tests

    @Test
    void getSingleEtf_returnsEtf_whenEtfFound() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.TINKOFF_ETF, TestEtf3.TINKOFF_ETF);
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final Etf result = extInstrumentsService.getSingleEtf(TestEtf3.TICKER);

        Assertions.assertEquals(TestEtf3.ETF, result);
    }

    @Test
    void getSingleEtf_returnsEtfIgnoreCase_whenEtfFound() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.TINKOFF_ETF, TestEtf3.TINKOFF_ETF);
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final Etf result = extInstrumentsService.getSingleEtf(TestEtf3.TICKER.toLowerCase());

        Assertions.assertEquals(TestEtf3.ETF, result);
    }

    @Test
    void getSingleEtf_throwsIllegalArgumentException_whenNoEtfs() {
        final String ticker1 = "FXIT";
        final String ticker2 = "TECH";
        final String ticker3 = "DRIV";
        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final String expectedMessage = "Expected single etf for ticker '" + ticker3 + "'. Found 0";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getSingleEtf(ticker3), expectedMessage);
    }

    @Test
    void getSingleEtf_throwsIllegalArgumentException_whenMultipleEtfs() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(
                TestEtf1.TINKOFF_ETF,
                TestEtf3.TINKOFF_ETF,
                TestEtf4.TINKOFF_ETF
        );
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final String expectedMessage = "Expected single etf for ticker '" + TestEtf3.TICKER + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getSingleEtf(TestEtf3.TICKER), expectedMessage);
    }

    // endregion

    // region getBonds tests

    @Test
    void getBonds_returnsBond_whenSingleBondFound() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final List<Bond> result = extInstrumentsService.getBonds(TestBond1.TICKER);

        final List<Bond> expectedBonds = List.of(TestBond1.BOND);
        Assertions.assertEquals(expectedBonds, result);
    }

    @Test
    void getBonds_returnsBondIgnoreCase_whenSingleBondFound() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final List<Bond> result = extInstrumentsService.getBonds(TestBond1.TICKER.toLowerCase());

        final List<Bond> expectedBonds = List.of(TestBond1.BOND);
        Assertions.assertEquals(expectedBonds, result);
    }

    @Test
    void getBonds_returnsEmptyList_whenNoBonds() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(TestBond1.TINKOFF_BOND, TestBond3.TINKOFF_BOND, TestBond4.TINKOFF_BOND);
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final List<Bond> result = extInstrumentsService.getBonds(TestBond2.TICKER);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getBonds_returnsMultipleBonds_whenMultipleBonds() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final List<Bond> result = extInstrumentsService.getBonds(TestBond4.TICKER);

        final List<Bond> expectedBonds = List.of(TestBond3.BOND, TestBond4.BOND);

        Assertions.assertEquals(expectedBonds, result);
    }

    // endregion

    // region getSingleBond tests

    @Test
    void getSingleBond_returnsBond_whenBondFound() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final Bond result = extInstrumentsService.getSingleBond(TestBond2.TICKER);

        Assertions.assertEquals(TestBond2.BOND, result);
    }

    @Test
    void getSingleBond_returnsBondIgnoreCase_whenBondFound() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final Bond result = extInstrumentsService.getSingleBond(TestBond2.TICKER.toLowerCase());

        Assertions.assertEquals(TestBond2.BOND, result);
    }

    @Test
    void getSingleBond_throwsIllegalArgumentException_whenNoBonds() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final String ticker = TestBond2.TICKER;

        final String expectedMessage = "Expected single bond for ticker '" + ticker + "'. Found 0";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getSingleBond(ticker), expectedMessage);
    }

    @Test
    void getSingleBond_throwsIllegalArgumentException_whenMultipleBonds() {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final String ticker = TestBond3.TICKER;

        final String expectedMessage = "Expected single bond for ticker '" + ticker + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getSingleBond(ticker), expectedMessage);
    }

    // endregion

    // region getCurrencies tests

    @Test
    void getCurrencies_returnsCurrency_whenSingleCurrencyFound() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final List<CurrencyInstrument> result = extInstrumentsService.getCurrencies(TestCurrency1.TICKER);

        final List<CurrencyInstrument> expectedCurrencies = List.of(TestCurrency1.CURRENCY);
        Assertions.assertEquals(expectedCurrencies, result);
    }

    @Test
    void getCurrencies_returnsCurrencyIgnoreCase_whenSingleCurrencyFound() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final List<CurrencyInstrument> result = extInstrumentsService.getCurrencies(TestCurrency1.TICKER.toLowerCase());

        final List<CurrencyInstrument> expectedCurrencies = List.of(TestCurrency1.CURRENCY);
        Assertions.assertEquals(expectedCurrencies, result);
    }

    @Test
    void getCurrencies_returnsEmptyList_whenNoCurrencies() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final List<CurrencyInstrument> result = extInstrumentsService.getCurrencies(TestCurrency2.TICKER);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getCurrencies_returnsMultipleCurrencies_whenMultipleCurrencies() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final List<CurrencyInstrument> result = extInstrumentsService.getCurrencies(TestCurrency3.TICKER);

        final List<CurrencyInstrument> expectedCurrencies = List.of(TestCurrency3.CURRENCY, TestCurrency4.CURRENCY);

        Assertions.assertEquals(expectedCurrencies, result);
    }

    // endregion

    // region getSingleCurrency tests

    @Test
    void getSingleCurrency_returnsCurrency_whenCurrencyFound() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final CurrencyInstrument result = extInstrumentsService.getSingleCurrency(TestCurrency2.TICKER);

        Assertions.assertEquals(TestCurrency2.CURRENCY, result);
    }

    @Test
    void getSingleCurrency_returnsCurrencyIgnoreCase_whenCurrencyFound() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final CurrencyInstrument result = extInstrumentsService.getSingleCurrency(TestCurrency2.TICKER.toLowerCase());

        Assertions.assertEquals(TestCurrency2.CURRENCY, result);
    }

    @Test
    void getSingleCurrency_throwsIllegalArgumentException_whenNoCurrencies() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final String ticker = TestCurrency2.TICKER;

        final String expectedMessage = "Expected single currency for ticker '" + ticker + "'. Found 0";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getSingleCurrency(ticker), expectedMessage);
    }

    @Test
    void getSingleCurrency_throwsIllegalArgumentException_whenMultipleCurrencies() {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final String ticker = TestCurrency3.TICKER;

        final String expectedMessage = "Expected single currency for ticker '" + ticker + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getSingleCurrency(ticker), expectedMessage);
    }

    // endregion

    // region getTradingSchedule tests

    @Test
    void getTradingSchedule() {
        final Exchange exchange = Exchange.MOEX;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3);

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange.getValue())
                .addDays(TestTradingDay1.TINKOFF_TRADING_DAY)
                .addDays(TestTradingDay2.TINKOFF_TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange.getValue(), from.toInstant(), to.toInstant())).thenReturn(tradingSchedule);

        final List<TradingDay> result = extInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    // region getTradingSchedules tests

    @Test
    void getTradingSchedules() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 8, 3);

        final Exchange exchange1 = Exchange.MOEX;
        final Exchange exchange2 = Exchange.SPB;

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule1 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange1.getValue())
                .addDays(TestTradingDay1.TINKOFF_TRADING_DAY)
                .addDays(TestTradingDay2.TINKOFF_TRADING_DAY)
                .build();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule2 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange2.getValue())
                .addDays(TestTradingDay3.TINKOFF_TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingSchedulesSync(from.toInstant(), to.toInstant()))
                .thenReturn(List.of(tradingSchedule1, tradingSchedule2));

        final List<TradingSchedule> result = extInstrumentsService.getTradingSchedules(Interval.of(from, to));

        final List<TradingDay> expectedTradingDays1 = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);
        final List<TradingDay> expectedTradingDays2 = List.of(TestTradingDay3.TRADING_DAY);
        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, expectedTradingDays1);
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, expectedTradingDays2);
        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

}