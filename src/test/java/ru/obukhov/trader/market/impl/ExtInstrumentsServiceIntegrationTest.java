package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.obukhov.trader.test.utils.model.share.TestShare4;
import ru.obukhov.trader.test.utils.model.share.TestShare5;
import ru.tinkoff.piapi.contract.v1.Asset;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class ExtInstrumentsServiceIntegrationTest extends IntegrationTest {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    @Autowired
    private ExtInstrumentsService extInstrumentsService;

    // region getFigiByTicker tests

    @Test
    @DirtiesContext
    void getFigiByTicker_returnsFirstFigi_whenAssetAndInstrumentFound() {
        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;

        final String ticker3 = TestShare4.TICKER;
        final String figi3 = TestShare4.FIGI;

        final String figi4 = TestShare5.FIGI;

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
        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;

        final String ticker3 = TestShare4.TICKER;
        final String figi3 = TestShare4.FIGI;

        final String figi4 = TestShare5.FIGI;

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
        final String ticker = TestShare1.TICKER;

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(Collections.emptyList());

        final Executable executable = () -> extInstrumentsService.getFigiByTicker(ticker);
        final String expectedMessage = "Not found instrument for ticker '" + ticker + "'";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getFigiByTicker_throwsIllegalArgumentException_whenNoInstrument() {
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

        final Executable executable = () -> extInstrumentsService.getFigiByTicker(ticker4);
        final String expectedMessage = "Not found instrument for ticker '" + ticker4 + "'";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
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
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
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
        final String expectedMessage = "Expected single share for ticker " + ticker + ". Found 0";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
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
        final String expectedMessage = "Expected single share for ticker " + ticker + ". Found 2";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getEtfs tests

    @Test
    void getEtfs_returnsEtf_whenSingleEtfFound() {
        final String figi1 = "BBG005HLTYH9";
        final String ticker1 = "FXIT";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.RUB;
        final String name1 = "FinEx Акции компаний IT-сектора США";
        final OffsetDateTime releasedDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3, 0, 0);
        final double numShares1 = 692000;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 1;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 35);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3);

        final String figi2 = "TECH0A101X68";
        final String ticker2 = "TECH";
        final int lotSize2 = 100;
        final Currency currency2 = Currency.USD;
        final String name2 = "Тинькофф NASDAQ 2";
        final OffsetDateTime releasedDate2 = DateTimeTestData.createDateTime(2020, 7, 13, 3);
        final String country2 = "Соединенные Штаты Америки";
        final Sector sector2 = Sector.IT;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate1))
                .setNumShares(QUOTATION_MAPPER.fromDouble(numShares1))
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate2))
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final List<Etf> result = extInstrumentsService.getEtfs(ticker2);

        Assertions.assertEquals(1, result.size());
        final Etf resultEtf = result.get(0);
        Assertions.assertEquals(figi2, resultEtf.figi());
        Assertions.assertEquals(ticker2, resultEtf.ticker());
        Assertions.assertEquals(lotSize2, resultEtf.lotSize());
        Assertions.assertEquals(currency2, resultEtf.currency());
        Assertions.assertEquals(name2, resultEtf.name());
        Assertions.assertEquals(releasedDate2, resultEtf.releasedDate());
        Assertions.assertNull(resultEtf.numShares());
        Assertions.assertEquals(country2, resultEtf.country());
        Assertions.assertEquals(sector2, resultEtf.sector());
        Assertions.assertEquals(tradingStatus2, resultEtf.tradingStatus());
        Assertions.assertEquals(buyAvailable2, resultEtf.buyAvailable());
        Assertions.assertEquals(sellAvailable2, resultEtf.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, resultEtf.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, resultEtf.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, resultEtf.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, resultEtf.first1DayCandleDate());
    }

    @Test
    void getEtfs_returnsEtfIgnoreCase_whenSingleEtfFound() {
        final String figi1 = "BBG005HLTYH9";
        final String ticker1 = "fxit";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.RUB;
        final String name1 = "FinEx Акции компаний IT-сектора США";
        final OffsetDateTime releasedDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3, 0, 0);
        final double numShares1 = 692000;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 1;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 35);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3);

        final String figi2 = "TECH0A101X68";
        final String ticker2 = "tech";
        final int lotSize2 = 100;
        final Currency currency2 = Currency.USD;
        final String name2 = "Тинькофф NASDAQ 2";
        final OffsetDateTime releasedDate2 = DateTimeTestData.createDateTime(2020, 7, 13, 3);
        final Double numShares2 = null;
        final String country2 = "Соединенные Штаты Америки";
        final Sector sector2 = Sector.IT;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate1))
                .setNumShares(QUOTATION_MAPPER.fromDouble(numShares1))
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate2))
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final List<Etf> result = extInstrumentsService.getEtfs(ticker2);

        Assertions.assertEquals(1, result.size());
        final Etf resultEtf = result.get(0);
        Assertions.assertEquals(figi2, resultEtf.figi());
        Assertions.assertEquals(ticker2, resultEtf.ticker());
        Assertions.assertEquals(lotSize2, resultEtf.lotSize());
        Assertions.assertEquals(currency2, resultEtf.currency());
        Assertions.assertEquals(name2, resultEtf.name());
        Assertions.assertEquals(releasedDate2, resultEtf.releasedDate());
        AssertUtils.assertEquals(numShares2, resultEtf.numShares());
        Assertions.assertEquals(country2, resultEtf.country());
        Assertions.assertEquals(sector2, resultEtf.sector());
        Assertions.assertEquals(tradingStatus2, resultEtf.tradingStatus());
        Assertions.assertEquals(buyAvailable2, resultEtf.buyAvailable());
        Assertions.assertEquals(sellAvailable2, resultEtf.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, resultEtf.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, resultEtf.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, resultEtf.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, resultEtf.first1DayCandleDate());
    }

    @Test
    void getEtfs_returnsEmptyList_whenNoEtfs() {
        final String ticker1 = "FXIT";
        final String ticker2 = "FXRB";
        final String ticker3 = "TECH";
        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final List<Etf> result = extInstrumentsService.getEtfs(ticker3);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getEtfs_returnsMultipleEtfs_whenMultipleEtfs() {
        final String figi1 = "BBG005HLTYH9";
        final String ticker1 = "fxit";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.RUB;
        final String name1 = "FinEx Акции компаний IT-сектора США";
        final OffsetDateTime releasedDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3, 0, 0);
        final double numShares1 = 692000;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 1;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 35);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3);

        final String figi2 = "TECH0A101X68";
        final String ticker2 = "tech";
        final int lotSize2 = 100;
        final Currency currency2 = Currency.USD;
        final String name2 = "Тинькофф NASDAQ 2";
        final OffsetDateTime releasedDate2 = DateTimeTestData.createDateTime(2020, 7, 13, 3);
        final String country2 = "Соединенные Штаты Америки";
        final Sector sector2 = Sector.IT;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final String figi3 = "BBG111111111";
        final String ticker3 = "tech";
        final int lotSize3 = 100;
        final Currency currency3 = Currency.USD;
        final String name3 = "Тинькофф Technology";
        final OffsetDateTime releasedDate3 = DateTimeTestData.createDateTime(2020, 7, 13, 3);
        final String country3 = "Соединенные Штаты Америки";
        final Sector sector3 = Sector.IT;
        final SecurityTradingStatus tradingStatus3 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable3 = true;
        final boolean sellAvailable3 = true;
        final boolean apiTradeAvailable3 = true;
        final double minPriceIncrement3 = 0.000100000;
        final OffsetDateTime first1MinCandleDate3 = DateTimeTestData.createDateTime(2020, 8, 26, 10);
        final OffsetDateTime first1DayCandleDate3 = DateTimeTestData.createDateTime(2020, 8, 26, 10);

        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate1))
                .setNumShares(QUOTATION_MAPPER.fromDouble(numShares1))
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate2))
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();
        final ru.tinkoff.piapi.contract.v1.Etf etf3 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi3)
                .setTicker(ticker3)
                .setLot(lotSize3)
                .setCurrency(currency3.name().toLowerCase())
                .setName(name3)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate3))
                .setCountryOfRiskName(country3)
                .setSector(sector3.name().toLowerCase())
                .setTradingStatus(tradingStatus3)
                .setBuyAvailableFlag(buyAvailable3)
                .setSellAvailableFlag(sellAvailable3)
                .setApiTradeAvailableFlag(apiTradeAvailable3)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement3))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate3))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate3))
                .build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2, etf3));

        final List<Etf> result = extInstrumentsService.getEtfs(ticker2);

        Assertions.assertEquals(2, result.size());

        final Etf resultEtf1 = result.get(0);
        Assertions.assertEquals(figi2, resultEtf1.figi());
        Assertions.assertEquals(ticker2, resultEtf1.ticker());
        Assertions.assertEquals(lotSize2, resultEtf1.lotSize());
        Assertions.assertEquals(currency2, resultEtf1.currency());
        Assertions.assertEquals(name2, resultEtf1.name());
        Assertions.assertEquals(releasedDate2, resultEtf1.releasedDate());
        Assertions.assertNull(resultEtf1.numShares());
        Assertions.assertEquals(country2, resultEtf1.country());
        Assertions.assertEquals(sector2, resultEtf1.sector());
        Assertions.assertEquals(tradingStatus2, resultEtf1.tradingStatus());
        Assertions.assertEquals(buyAvailable2, resultEtf1.buyAvailable());
        Assertions.assertEquals(sellAvailable2, resultEtf1.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, resultEtf1.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, resultEtf1.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, resultEtf1.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, resultEtf1.first1DayCandleDate());

        final Etf resultEtf2 = result.get(1);
        Assertions.assertEquals(figi3, resultEtf2.figi());
        Assertions.assertEquals(ticker3, resultEtf2.ticker());
        Assertions.assertEquals(lotSize3, resultEtf2.lotSize());
        Assertions.assertEquals(currency3, resultEtf2.currency());
        Assertions.assertEquals(name3, resultEtf2.name());
        Assertions.assertEquals(releasedDate3, resultEtf2.releasedDate());
        Assertions.assertNull(resultEtf2.numShares());
        Assertions.assertEquals(country3, resultEtf2.country());
        Assertions.assertEquals(sector3, resultEtf2.sector());
        Assertions.assertEquals(tradingStatus3, resultEtf2.tradingStatus());
        Assertions.assertEquals(buyAvailable3, resultEtf2.buyAvailable());
        Assertions.assertEquals(sellAvailable3, resultEtf2.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable3, resultEtf2.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement3, resultEtf2.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate3, resultEtf2.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate3, resultEtf2.first1DayCandleDate());
    }

    // endregion

    // region getSingleEtf tests

    @Test
    void getSingleEtf_returnsEtf_whenEtfFound() {
        final String figi1 = "BBG005HLTYH9";
        final String ticker1 = "FXIT";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.RUB;
        final String name1 = "FinEx Акции компаний IT-сектора США";
        final OffsetDateTime releasedDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3, 0, 0);
        final double numShares1 = 692000;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 1;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 35);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3);

        final String figi2 = "TECH0A101X68";
        final String ticker2 = "TECH";
        final int lotSize2 = 100;
        final Currency currency2 = Currency.USD;
        final String name2 = "Тинькофф NASDAQ 2";
        final OffsetDateTime releasedDate2 = DateTimeTestData.createDateTime(2020, 7, 13, 3);
        final Double numShares2 = null;
        final String country2 = "Соединенные Штаты Америки";
        final Sector sector2 = Sector.IT;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate1))
                .setNumShares(QUOTATION_MAPPER.fromDouble(numShares1))
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate2))
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final Etf result = extInstrumentsService.getSingleEtf(ticker2);

        Assertions.assertEquals(figi2, result.figi());
        Assertions.assertEquals(ticker2, result.ticker());
        Assertions.assertEquals(lotSize2, result.lotSize());
        Assertions.assertEquals(currency2, result.currency());
        Assertions.assertEquals(name2, result.name());
        Assertions.assertEquals(releasedDate2, result.releasedDate());
        AssertUtils.assertEquals(numShares2, result.numShares());
        Assertions.assertEquals(country2, result.country());
        Assertions.assertEquals(sector2, result.sector());
        Assertions.assertEquals(tradingStatus2, result.tradingStatus());
        Assertions.assertEquals(buyAvailable2, result.buyAvailable());
        Assertions.assertEquals(sellAvailable2, result.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, result.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, result.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, result.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, result.first1DayCandleDate());
    }

    @Test
    void getSingleEtf_returnsEtfIgnoreCase_whenEtfFound() {
        final String figi1 = "BBG005HLTYH9";
        final String ticker1 = "fxit";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.RUB;
        final String name1 = "FinEx Акции компаний IT-сектора США";
        final OffsetDateTime releasedDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3, 0, 0);
        final double numShares1 = 692000;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 1;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 35);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(2013, 10, 31, 3);

        final String figi2 = "BBG004730N88";
        final String ticker2 = "SBER";
        final int lotSize2 = 10;
        final Currency currency2 = Currency.RUB;
        final String name2 = "Сбер Банк";
        final OffsetDateTime releasedDate2 = DateTimeTestData.createDateTime(2007, 7, 11, 3);
        final double numShares2 = 21586948000L;
        final String country2 = "Российская Федерация";
        final Sector sector2 = Sector.FINANCIAL;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate1))
                .setNumShares(QUOTATION_MAPPER.fromDouble(numShares1))
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(releasedDate2))
                .setNumShares(QUOTATION_MAPPER.fromDouble(numShares2))
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final Etf result = extInstrumentsService.getSingleEtf(ticker2);

        Assertions.assertEquals(figi2, result.figi());
        Assertions.assertEquals(ticker2, result.ticker());
        Assertions.assertEquals(lotSize2, result.lotSize());
        Assertions.assertEquals(currency2, result.currency());
        Assertions.assertEquals(name2, result.name());
        Assertions.assertEquals(releasedDate2, result.releasedDate());
        AssertUtils.assertEquals(numShares2, result.numShares());
        Assertions.assertEquals(country2, result.country());
        Assertions.assertEquals(sector2, result.sector());
        Assertions.assertEquals(tradingStatus2, result.tradingStatus());
        Assertions.assertEquals(buyAvailable2, result.buyAvailable());
        Assertions.assertEquals(sellAvailable2, result.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, result.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, result.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, result.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, result.first1DayCandleDate());
    }

    @Test
    void getEtf_returnsNull_whenNoEtf() {
        final String ticker1 = "FXIT";
        final String ticker2 = "TECH";
        final String ticker3 = "DRIV";
        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final String expectedMessage = "Expected single etf for ticker " + ticker3 + ". Found 0";
        Assertions.assertThrows(IllegalArgumentException.class, () -> extInstrumentsService.getSingleShare(ticker3), expectedMessage);
    }

    @Test
    void getSingleEtf_throwIllegalArgumentException_whenMultipleEtfs() {
        final String ticker1 = "FXIT";
        final String ticker2 = "TECH";
        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf3 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2, etf3));

        final String expectedMessage = "Expected single etf for ticker " + ticker2 + ". Found 2";
        Assertions.assertThrows(IllegalArgumentException.class, () -> extInstrumentsService.getSingleEtf(ticker2), expectedMessage);
    }

    // endregion

}