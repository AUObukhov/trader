package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
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

@AutoConfigureMockMvc
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
        final String expectedMessage = "Expected single instrument with ticker '" + ticker3 + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getSingleFigiByTicker_throwsIllegalArgumentException_whenNoAssets() {
        final String ticker = TestShare1.TICKER;

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(Collections.emptyList());

        final Executable executable = () -> extInstrumentsService.getSingleFigiByTicker(ticker);
        final String expectedMessage = "Expected single instrument with ticker '" + ticker + "'. Found 0";
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
        final String expectedMessage = "Expected single instrument with ticker '" + ticker4 + "'. Found 0";
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

    // region getShares tests

    @Test
    void getShares_returnsShare_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare2.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare2.TICKER);

        final List<Share> expectedShares = List.of(TestShare2.createShare());
        Assertions.assertEquals(expectedShares, result);
    }

    @Test
    void getShares_returnsShareIgnoreCase_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare2.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare2.TICKER.toLowerCase());

        final List<Share> expectedShares = List.of(TestShare2.createShare());
        Assertions.assertEquals(expectedShares, result);
    }

    @Test
    void getShares_returnsEmptyList_whenNoShares() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare2.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare4.TICKER);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getShares_returnsMultipleShares_whenMultipleShares() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare4.createTinkoffShare(),
                TestShare5.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final List<Share> result = extInstrumentsService.getShares(TestShare5.TICKER);

        final List<Share> expectedShares = List.of(TestShare4.createShare(), TestShare5.createShare());
        Assertions.assertEquals(expectedShares, result);
    }

    // endregion

    // region getSingleShare tests

    @Test
    void getSingleShare_returnsShare_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare2.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final Share result = extInstrumentsService.getSingleShare(TestShare2.TICKER);

        Assertions.assertEquals(TestShare2.createShare(), result);
    }

    @Test
    void getSingleShare_returnsShareIgnoreCase_whenSingleShareFound() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare2.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final Share result = extInstrumentsService.getSingleShare(TestShare2.TICKER.toLowerCase());

        Assertions.assertEquals(TestShare2.createShare(), result);
    }

    @Test
    void getSingleShare_throwIllegalArgumentException_whenNoShare() {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare2.createTinkoffShare()
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
                TestShare1.createTinkoffShare(),
                TestShare4.createTinkoffShare(),
                TestShare5.createTinkoffShare()
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
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf3.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER);

        final List<Etf> expectedEtfs = List.of(TestEtf3.createEtf());
        Assertions.assertEquals(expectedEtfs, result);
    }

    @Test
    void getEtfs_returnsEtfIgnoreCase_whenSingleEtfFound() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf3.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER.toLowerCase());

        final List<Etf> expectedEtfs = List.of(TestEtf3.createEtf());
        Assertions.assertEquals(expectedEtfs, result);
    }

    @Test
    void getEtfs_returnsEmptyList_whenNoEtfs() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf2.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getEtfs_returnsMultipleEtfs_whenMultipleEtfs() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(
                TestEtf1.createTinkoffEtf(),
                TestEtf3.createTinkoffEtf(),
                TestEtf4.createTinkoffEtf()
        );
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final List<Etf> result = extInstrumentsService.getEtfs(TestEtf3.TICKER);

        final List<Etf> expectedEtfs = List.of(TestEtf3.createEtf(), TestEtf4.createEtf());

        Assertions.assertEquals(expectedEtfs, result);
    }

    // endregion

    // region getSingleEtf tests

    @Test
    void getSingleEtf_returnsEtf_whenEtfFound() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf3.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final Etf result = extInstrumentsService.getSingleEtf(TestEtf3.TICKER);

        Assertions.assertEquals(TestEtf3.createEtf(), result);
    }

    @Test
    void getSingleEtf_returnsEtfIgnoreCase_whenEtfFound() {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf3.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final Etf result = extInstrumentsService.getSingleEtf(TestEtf3.TICKER.toLowerCase());

        Assertions.assertEquals(TestEtf3.createEtf(), result);
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
                TestEtf1.createTinkoffEtf(),
                TestEtf3.createTinkoffEtf(),
                TestEtf4.createTinkoffEtf()
        );
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final String expectedMessage = "Expected single etf for ticker '" + TestEtf3.TICKER + "'. Found 2";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extInstrumentsService.getSingleEtf(TestEtf3.TICKER), expectedMessage);
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
                .addDays(TestTradingDay1.createTinkoffTradingDay())
                .addDays(TestTradingDay2.createTinkoffTradingDay())
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange.getValue(), from.toInstant(), to.toInstant())).thenReturn(tradingSchedule);

        final List<TradingDay> result = extInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.createTradingDay(), TestTradingDay2.createTradingDay());

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
                .addDays(TestTradingDay1.createTinkoffTradingDay())
                .addDays(TestTradingDay2.createTinkoffTradingDay())
                .build();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule2 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange2.getValue())
                .addDays(TestTradingDay3.createTinkoffTradingDay())
                .build();
        Mockito.when(instrumentsService.getTradingSchedulesSync(from.toInstant(), to.toInstant()))
                .thenReturn(List.of(tradingSchedule1, tradingSchedule2));

        final List<TradingSchedule> result = extInstrumentsService.getTradingSchedules(Interval.of(from, to));

        final List<TradingDay> expectedTradingDays1 = List.of(TestTradingDay1.createTradingDay(), TestTradingDay2.createTradingDay());
        final List<TradingDay> expectedTradingDays2 = List.of(TestTradingDay3.createTradingDay());
        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, expectedTradingDays1);
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, expectedTradingDays2);
        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

}