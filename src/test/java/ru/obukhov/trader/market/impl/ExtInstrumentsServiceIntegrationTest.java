package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
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
    private static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

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

    // region getShares tests

    @Test
    void getShares_returnsShare_whenSingleShareExists() {
        final String figi1 = "BBG000B9XRY4";
        final String ticker1 = "AAPL";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.USD;
        final String name1 = "Apple";
        final OffsetDateTime ipoDate1 = DateTimeTestData.createDateTime(1980, 12, 12, 3);
        final long issueSize1 = 16530166000L;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final long issueSizePlan1 = 50400000000L;
        final double nominal1 = 0.00001;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 0.01;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 1, 10, 10, 34);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(1988, 9, 12, 3);

        final String figi2 = "BBG004730N88";
        final String ticker2 = "SBER";
        final int lotSize2 = 10;
        final Currency currency2 = Currency.RUB;
        final String name2 = "Сбер Банк";
        final OffsetDateTime ipoDate2 = DateTimeTestData.createDateTime(2007, 7, 11, 3);
        final long issueSize2 = 21586948000L;
        final String country2 = "Российская Федерация";
        final Sector sector2 = Sector.FINANCIAL;
        final long issueSizePlan2 = 21586948000L;
        final double nominal2 = 3;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate1))
                .setIssueSize(issueSize1)
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan1)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal1))
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate2))
                .setIssueSize(issueSize2)
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan2)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal2))
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final List<Share> result = extInstrumentsService.getShares(ticker2);

        Assertions.assertEquals(1, result.size());
        final Share resultShare = result.get(0);
        Assertions.assertEquals(figi2, resultShare.figi());
        Assertions.assertEquals(ticker2, resultShare.ticker());
        Assertions.assertEquals(lotSize2, resultShare.lotSize());
        Assertions.assertEquals(currency2, resultShare.currency());
        Assertions.assertEquals(name2, resultShare.name());
        Assertions.assertEquals(ipoDate2, resultShare.ipoDate());
        Assertions.assertEquals(issueSize2, resultShare.issueSize());
        Assertions.assertEquals(country2, resultShare.country());
        Assertions.assertEquals(sector2, resultShare.sector());
        Assertions.assertEquals(issueSizePlan2, resultShare.issueSizePlan());
        AssertUtils.assertEquals(nominal2, resultShare.nominal());
        Assertions.assertEquals(tradingStatus2, resultShare.tradingStatus());
        Assertions.assertEquals(buyAvailable2, resultShare.buyAvailable());
        Assertions.assertEquals(sellAvailable2, resultShare.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, resultShare.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, resultShare.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, resultShare.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, resultShare.first1DayCandleDate());
    }

    @Test
    void getShares_returnsShareIgnoreCase_whenSingleShareExists() {
        final String figi1 = "BBG000B9XRY4";
        final String ticker1 = "aapl";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.USD;
        final String name1 = "Apple";
        final OffsetDateTime ipoDate1 = DateTimeTestData.createDateTime(1980, 12, 12, 3);
        final long issueSize1 = 16530166000L;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final long issueSizePlan1 = 50400000000L;
        final double nominal1 = 0.00001;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 0.01;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 1, 10, 10, 34);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(1988, 9, 12, 3);

        final String figi2 = "BBG004730N88";
        final String ticker2 = "sber";
        final int lotSize2 = 10;
        final Currency currency2 = Currency.RUB;
        final String name2 = "Сбер Банк";
        final OffsetDateTime ipoDate2 = DateTimeTestData.createDateTime(2007, 7, 11, 3);
        final long issueSize2 = 21586948000L;
        final String country2 = "Российская Федерация";
        final Sector sector2 = Sector.FINANCIAL;
        final long issueSizePlan2 = 21586948000L;
        final double nominal2 = 3;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate1))
                .setIssueSize(issueSize1)
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan1)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal1))
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate2))
                .setIssueSize(issueSize2)
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan2)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal2))
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final List<Share> result = extInstrumentsService.getShares("SBER");

        Assertions.assertEquals(1, result.size());
        final Share resultShare = result.get(0);
        Assertions.assertEquals(figi2, resultShare.figi());
        Assertions.assertEquals(ticker2, resultShare.ticker());
        Assertions.assertEquals(lotSize2, resultShare.lotSize());
        Assertions.assertEquals(currency2, resultShare.currency());
        Assertions.assertEquals(name2, resultShare.name());
        Assertions.assertEquals(ipoDate2, resultShare.ipoDate());
        Assertions.assertEquals(issueSize2, resultShare.issueSize());
        Assertions.assertEquals(country2, resultShare.country());
        Assertions.assertEquals(sector2, resultShare.sector());
        Assertions.assertEquals(issueSizePlan2, resultShare.issueSizePlan());
        AssertUtils.assertEquals(nominal2, resultShare.nominal());
        Assertions.assertEquals(tradingStatus2, resultShare.tradingStatus());
        Assertions.assertEquals(buyAvailable2, resultShare.buyAvailable());
        Assertions.assertEquals(sellAvailable2, resultShare.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, resultShare.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, resultShare.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, resultShare.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, resultShare.first1DayCandleDate());
    }

    @Test
    void getShares_returnsEmptyList_whenNoShares() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";
        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final List<Share> result = extInstrumentsService.getShares(ticker3);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getShares_returnsMultipleShares_whenMultipleShares() {
        final String figi1 = "BBG000G25P51";
        final String ticker = "DIOD";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.USD;
        final String name1 = "Diodes Inc";
        final long issueSize1 = 49590347;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final long issueSizePlan1 = 70000000;
        final double nominal1 = 0.666667000;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 0.010000000;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2019, 4, 4, 16, 30);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(1989, 5, 24, 3);

        final String figi2 = "BBG000R0L782";
        final int lotSize2 = 100;
        final Currency currency2 = Currency.RUB;
        final String name2 = "ДИОД";
        final OffsetDateTime ipoDate2 = DateTimeTestData.createDateTime(2010, 1, 20, 3);
        final long issueSize2 = 91500000L;
        final String country2 = "Российская Федерация";
        final Sector sector2 = Sector.HEALTH_CARE;
        final long issueSizePlan2 = 91500000;
        final double nominal2 = 0.010000000;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = false;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 8, 4, 40);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 9, 10);

        final String ticker2 = "AAPL";

        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setIssueSize(issueSize1)
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan1)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal1))
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate2))
                .setIssueSize(issueSize2)
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan2)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal2))
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share share3 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2, share3));

        final List<Share> result = extInstrumentsService.getShares(ticker);

        Assertions.assertEquals(2, result.size());

        final Share resultShare1 = result.get(0);
        Assertions.assertEquals(figi1, resultShare1.figi());
        Assertions.assertEquals(ticker, resultShare1.ticker());
        Assertions.assertEquals(lotSize1, resultShare1.lotSize());
        Assertions.assertEquals(currency1, resultShare1.currency());
        Assertions.assertEquals(name1, resultShare1.name());
        Assertions.assertNull(resultShare1.ipoDate());
        Assertions.assertEquals(issueSize1, resultShare1.issueSize());
        Assertions.assertEquals(country1, resultShare1.country());
        Assertions.assertEquals(sector1, resultShare1.sector());
        Assertions.assertEquals(issueSizePlan1, resultShare1.issueSizePlan());
        AssertUtils.assertEquals(nominal1, resultShare1.nominal());
        Assertions.assertEquals(tradingStatus1, resultShare1.tradingStatus());
        Assertions.assertEquals(buyAvailable1, resultShare1.buyAvailable());
        Assertions.assertEquals(sellAvailable1, resultShare1.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable1, resultShare1.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement1, resultShare1.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate1, resultShare1.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate1, resultShare1.first1DayCandleDate());

        final Share resultShare2 = result.get(1);
        Assertions.assertEquals(figi2, resultShare2.figi());
        Assertions.assertEquals(ticker, resultShare2.ticker());
        Assertions.assertEquals(lotSize2, resultShare2.lotSize());
        Assertions.assertEquals(currency2, resultShare2.currency());
        Assertions.assertEquals(name2, resultShare2.name());
        Assertions.assertEquals(ipoDate2, resultShare2.ipoDate());
        Assertions.assertEquals(issueSize2, resultShare2.issueSize());
        Assertions.assertEquals(country2, resultShare2.country());
        Assertions.assertEquals(sector2, resultShare2.sector());
        Assertions.assertEquals(issueSizePlan2, resultShare2.issueSizePlan());
        AssertUtils.assertEquals(nominal2, resultShare2.nominal());
        Assertions.assertEquals(tradingStatus2, resultShare2.tradingStatus());
        Assertions.assertEquals(buyAvailable2, resultShare2.buyAvailable());
        Assertions.assertEquals(sellAvailable2, resultShare2.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, resultShare2.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, resultShare2.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, resultShare2.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, resultShare2.first1DayCandleDate());
    }

    // endregion

    // region getSingleShare tests

    @Test
    void getSingleShare_returnsShare_whenSingleShareExists() {
        final String figi1 = "BBG000B9XRY4";
        final String ticker1 = "AAPL";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.USD;
        final String name1 = "Apple";
        final OffsetDateTime ipoDate1 = DateTimeTestData.createDateTime(1980, 12, 12, 3);
        final long issueSize1 = 16530166000L;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final long issueSizePlan1 = 50400000000L;
        final double nominal1 = 0.00001;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 0.01;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 1, 10, 10, 34);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(1988, 9, 12, 3);

        final String figi2 = "BBG004730N88";
        final String ticker2 = "SBER";
        final int lotSize2 = 10;
        final Currency currency2 = Currency.RUB;
        final String name2 = "Сбер Банк";
        final OffsetDateTime ipoDate2 = DateTimeTestData.createDateTime(2007, 7, 11, 3);
        final long issueSize2 = 21586948000L;
        final String country2 = "Российская Федерация";
        final Sector sector2 = Sector.FINANCIAL;
        final long issueSizePlan2 = 21586948000L;
        final double nominal2 = 3;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate1))
                .setIssueSize(issueSize1)
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan1)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal1))
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate2))
                .setIssueSize(issueSize2)
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan2)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal2))
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final Share result = extInstrumentsService.getSingleShare(ticker2);

        Assertions.assertEquals(figi2, result.figi());
        Assertions.assertEquals(ticker2, result.ticker());
        Assertions.assertEquals(lotSize2, result.lotSize());
        Assertions.assertEquals(currency2, result.currency());
        Assertions.assertEquals(name2, result.name());
        Assertions.assertEquals(ipoDate2, result.ipoDate());
        Assertions.assertEquals(issueSize2, result.issueSize());
        Assertions.assertEquals(country2, result.country());
        Assertions.assertEquals(sector2, result.sector());
        Assertions.assertEquals(issueSizePlan2, result.issueSizePlan());
        AssertUtils.assertEquals(nominal2, result.nominal());
        Assertions.assertEquals(tradingStatus2, result.tradingStatus());
        Assertions.assertEquals(buyAvailable2, result.buyAvailable());
        Assertions.assertEquals(sellAvailable2, result.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, result.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, result.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, result.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, result.first1DayCandleDate());
    }

    @Test
    void getSingleShare_returnsShareIgnoreCase_whenSingleShareExists() {
        final String figi1 = "BBG000B9XRY4";
        final String ticker1 = "aapl";
        final int lotSize1 = 1;
        final Currency currency1 = Currency.USD;
        final String name1 = "Apple";
        final OffsetDateTime ipoDate1 = DateTimeTestData.createDateTime(1980, 12, 12, 3);
        final long issueSize1 = 16530166000L;
        final String country1 = "Соединенные Штаты Америки";
        final Sector sector1 = Sector.IT;
        final long issueSizePlan1 = 50400000000L;
        final double nominal1 = 0.00001;
        final SecurityTradingStatus tradingStatus1 = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable1 = true;
        final boolean sellAvailable1 = true;
        final boolean apiTradeAvailable1 = true;
        final double minPriceIncrement1 = 0.01;
        final OffsetDateTime first1MinCandleDate1 = DateTimeTestData.createDateTime(2018, 1, 10, 10, 34);
        final OffsetDateTime first1DayCandleDate1 = DateTimeTestData.createDateTime(1988, 9, 12, 3);

        final String figi2 = "BBG004730N88";
        final String ticker2 = "sber";
        final int lotSize2 = 10;
        final Currency currency2 = Currency.RUB;
        final String name2 = "Сбер Банк";
        final OffsetDateTime ipoDate2 = DateTimeTestData.createDateTime(2007, 7, 11, 3);
        final long issueSize2 = 21586948000L;
        final String country2 = "Российская Федерация";
        final Sector sector2 = Sector.FINANCIAL;
        final long issueSizePlan2 = 21586948000L;
        final double nominal2 = 3;
        final SecurityTradingStatus tradingStatus2 = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
        final boolean buyAvailable2 = true;
        final boolean sellAvailable2 = true;
        final boolean apiTradeAvailable2 = true;
        final double minPriceIncrement2 = 0.010000000;
        final OffsetDateTime first1MinCandleDate2 = DateTimeTestData.createDateTime(2018, 3, 7, 21, 33);
        final OffsetDateTime first1DayCandleDate2 = DateTimeTestData.createDateTime(2000, 1, 4, 10);

        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
                .setLot(lotSize1)
                .setCurrency(currency1.name().toLowerCase())
                .setName(name1)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate1))
                .setIssueSize(issueSize1)
                .setCountryOfRiskName(country1)
                .setSector(sector1.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan1)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal1))
                .setTradingStatus(tradingStatus1)
                .setBuyAvailableFlag(buyAvailable1)
                .setSellAvailableFlag(sellAvailable1)
                .setApiTradeAvailableFlag(apiTradeAvailable1)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement1))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(ipoDate2))
                .setIssueSize(issueSize2)
                .setCountryOfRiskName(country2)
                .setSector(sector2.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan2)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal2))
                .setTradingStatus(tradingStatus2)
                .setBuyAvailableFlag(buyAvailable2)
                .setSellAvailableFlag(sellAvailable2)
                .setApiTradeAvailableFlag(apiTradeAvailable2)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement2))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(first1DayCandleDate2))
                .build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final Share result = extInstrumentsService.getSingleShare("SBER");

        Assertions.assertEquals(figi2, result.figi());
        Assertions.assertEquals(ticker2, result.ticker());
        Assertions.assertEquals(lotSize2, result.lotSize());
        Assertions.assertEquals(currency2, result.currency());
        Assertions.assertEquals(name2, result.name());
        Assertions.assertEquals(ipoDate2, result.ipoDate());
        Assertions.assertEquals(issueSize2, result.issueSize());
        Assertions.assertEquals(country2, result.country());
        Assertions.assertEquals(sector2, result.sector());
        Assertions.assertEquals(issueSizePlan2, result.issueSizePlan());
        AssertUtils.assertEquals(nominal2, result.nominal());
        Assertions.assertEquals(tradingStatus2, result.tradingStatus());
        Assertions.assertEquals(buyAvailable2, result.buyAvailable());
        Assertions.assertEquals(sellAvailable2, result.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable2, result.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement2, result.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate2, result.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate2, result.first1DayCandleDate());
    }

    @Test
    void getSingleShare_throwIllegalArgumentException_whenNoShare() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";
        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final String expectedMessage = "Expected single share for ticker " + ticker3 + ". Found 0";
        Assertions.assertThrows(IllegalArgumentException.class, () -> extInstrumentsService.getSingleShare(ticker3), expectedMessage);
    }

    @Test
    void getSingleShare_throwIllegalArgumentException_whenMultipleShares() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";
        final ru.tinkoff.piapi.contract.v1.Share share1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Share share2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker2).build();
        final ru.tinkoff.piapi.contract.v1.Share share3 = ru.tinkoff.piapi.contract.v1.Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2, share3));

        final String expectedMessage = "Expected single share for ticker " + ticker2 + ". Found 2";
        Assertions.assertThrows(IllegalArgumentException.class, () -> extInstrumentsService.getSingleShare(ticker3), expectedMessage);
    }

    // endregion

}