package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.obukhov.trader.test.utils.model.share.TestShare4;
import ru.obukhov.trader.test.utils.model.share.TestShare5;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;
import java.util.List;

class InstrumentsControllerIntegrationTest extends ControllerIntegrationTest {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    // region getShares tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShares_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShares_returnsEmptyResponse_whenNoShares() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .param("ticker", TestShare3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, List.of());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShares_returnsMultipleShares_whenMultipleShares() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare4.createTinkoffShare(), TestShare5.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .param("ticker", TestShare4.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        final List<Share> expectedShares = List.of(TestShare4.createShare(), TestShare5.createShare());
        performAndExpectResponse(requestBuilder, expectedShares);
    }

    // endregion

    // region getShare tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShare() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", TestShare2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.createShare());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShareIgnoreCase() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", TestShare2.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.createShare());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsBadRequest_whenNoShare() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final String ticker3 = TestShare3.TICKER;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker3)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single share for ticker " + ticker3 + ". Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsServerError_whenMultipleShares() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare4.createTinkoffShare(),
                TestShare5.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final String ticker = TestShare4.TICKER.toLowerCase();
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single share for ticker " + ticker + ". Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

    // region getEtfs tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtfs_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etfs")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtfs_returnsEmptyResponse_whenNoEtfs() throws Exception {
        final String ticker1 = "FXIT";
        final String ticker2 = "FXRB";
        final String ticker3 = "TECH";
        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etfs")
                .param("ticker", ticker3)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, List.of());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtfs_returnsMultipleEtfs_whenMultipleEtfs() throws Exception {
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

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etfs")
                .param("ticker", ticker2)
                .contentType(MediaType.APPLICATION_JSON);

        final Etf expectedEtf1 = Etf.builder()
                .figi(figi2)
                .ticker(ticker2)
                .lotSize(lotSize2)
                .currency(currency2)
                .name(name2)
                .releasedDate(releasedDate2)
                .country(country2)
                .sector(sector2)
                .tradingStatus(tradingStatus2)
                .buyAvailable(buyAvailable2)
                .sellAvailable(sellAvailable2)
                .apiTradeAvailable(apiTradeAvailable2)
                .minPriceIncrement(DecimalUtils.setDefaultScale(minPriceIncrement2))
                .first1MinCandleDate(first1MinCandleDate2)
                .first1DayCandleDate(first1DayCandleDate2)
                .build();
        final Etf expectedEtf2 = Etf.builder()
                .figi(figi3)
                .ticker(ticker3)
                .lotSize(lotSize3)
                .currency(currency3)
                .name(name3)
                .releasedDate(releasedDate3)
                .country(country3)
                .sector(sector3)
                .tradingStatus(tradingStatus3)
                .buyAvailable(buyAvailable3)
                .sellAvailable(sellAvailable3)
                .apiTradeAvailable(apiTradeAvailable3)
                .minPriceIncrement(DecimalUtils.setDefaultScale(minPriceIncrement3))
                .first1MinCandleDate(first1MinCandleDate3)
                .first1DayCandleDate(first1DayCandleDate3)
                .build();

        performAndExpectResponse(requestBuilder, List.of(expectedEtf1, expectedEtf2));
    }

    // endregion

    // region getSingleEtf tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsEtf() throws Exception {
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

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", ticker2)
                .contentType(MediaType.APPLICATION_JSON);

        final Etf expectedEtf = Etf.builder()
                .figi(figi2)
                .ticker(ticker2)
                .lotSize(lotSize2)
                .currency(currency2)
                .name(name2)
                .releasedDate(releasedDate2)
                .country(country2)
                .sector(sector2)
                .tradingStatus(tradingStatus2)
                .buyAvailable(buyAvailable2)
                .sellAvailable(sellAvailable2)
                .apiTradeAvailable(apiTradeAvailable2)
                .minPriceIncrement(DecimalUtils.setDefaultScale(minPriceIncrement2))
                .first1MinCandleDate(first1MinCandleDate2)
                .first1DayCandleDate(first1DayCandleDate2)
                .build();

        performAndExpectResponse(requestBuilder, expectedEtf);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsEtfIgnoreCase() throws Exception {
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

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", "TECH")
                .contentType(MediaType.APPLICATION_JSON);

        final Etf expectedEtf = Etf.builder()
                .figi(figi2)
                .ticker(ticker2)
                .lotSize(lotSize2)
                .currency(currency2)
                .name(name2)
                .releasedDate(releasedDate2)
                .country(country2)
                .sector(sector2)
                .tradingStatus(tradingStatus2)
                .buyAvailable(buyAvailable2)
                .sellAvailable(sellAvailable2)
                .apiTradeAvailable(apiTradeAvailable2)
                .minPriceIncrement(DecimalUtils.setDefaultScale(minPriceIncrement2))
                .first1MinCandleDate(first1MinCandleDate2)
                .first1DayCandleDate(first1DayCandleDate2)
                .build();

        performAndExpectResponse(requestBuilder, expectedEtf);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsServerError_whenNoEtf() throws Exception {
        final String ticker1 = "FXIT";
        final String ticker2 = "TECH";
        final String ticker3 = "DRIV";
        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", ticker3)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single etf for ticker " + ticker3 + ". Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsServerError_whenMultipleEtfs() throws Exception {
        final String ticker1 = "FXIT";
        final String ticker2 = "TECH";
        final ru.tinkoff.piapi.contract.v1.Etf etf1 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker1).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf2 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();
        final ru.tinkoff.piapi.contract.v1.Etf etf3 = ru.tinkoff.piapi.contract.v1.Etf.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(List.of(etf1, etf2, etf3));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", ticker2)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single etf for ticker " + ticker2 + ". Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

}