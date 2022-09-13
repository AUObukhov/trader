package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

class InstrumentsControllerIntegrationTest extends ControllerIntegrationTest {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
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

        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(tinkoffShare1, tinkoffShare2));

        final String ticker3 = "YNDX";

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .param("ticker", ticker3)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, List.of());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShares_returnsMultipleShares_whenMultipleShares() throws Exception {
        final String figi1 = "BBG000G25P51";
        final String ticker1 = "DIOD";
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
        final String ticker2 = "diod";
        final int lotSize2 = 100;
        final Currency currency2 = Currency.RUB;
        final String name2 = "ДиоД";
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

        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
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
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(tinkoffShare1, tinkoffShare2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .param("ticker", ticker1)
                .contentType(MediaType.APPLICATION_JSON);

        final Share expectedShare1 = Share.builder()
                .figi(figi1)
                .ticker(ticker1)
                .lotSize(lotSize1)
                .currency(currency1)
                .name(name1)
                .issueSize(issueSize1)
                .country(country1)
                .sector(sector1)
                .issueSizePlan(issueSizePlan1)
                .nominal(DecimalUtils.setDefaultScale(nominal1))
                .tradingStatus(tradingStatus1)
                .buyAvailable(buyAvailable1)
                .sellAvailable(sellAvailable1)
                .apiTradeAvailable(apiTradeAvailable1)
                .minPriceIncrement(DecimalUtils.setDefaultScale(minPriceIncrement1))
                .first1MinCandleDate(first1MinCandleDate1)
                .first1DayCandleDate(first1DayCandleDate1)
                .build();
        final Share expectedShare2 = Share.builder()
                .figi(figi2)
                .ticker(ticker2)
                .lotSize(lotSize2)
                .currency(currency2)
                .name(name2)
                .ipoDate(ipoDate2)
                .issueSize(issueSize2)
                .country(country2)
                .sector(sector2)
                .issueSizePlan(issueSizePlan2)
                .nominal(DecimalUtils.setDefaultScale(nominal2))
                .tradingStatus(tradingStatus2)
                .buyAvailable(buyAvailable2)
                .sellAvailable(sellAvailable2)
                .apiTradeAvailable(apiTradeAvailable2)
                .minPriceIncrement(DecimalUtils.setDefaultScale(minPriceIncrement2))
                .first1MinCandleDate(first1MinCandleDate2)
                .first1DayCandleDate(first1DayCandleDate2)
                .build();

        performAndExpectResponse(requestBuilder, List.of(expectedShare1, expectedShare2));
    }

    // endregion

    // region getShare tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShare() throws Exception {
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

        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(tinkoffShare1, tinkoffShare2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker2)
                .contentType(MediaType.APPLICATION_JSON);

        final Share expectedShare = new Share(
                figi2,
                ticker2,
                lotSize2,
                currency2,
                name2,
                ipoDate2,
                issueSize2,
                country2,
                sector2,
                issueSizePlan2,
                BigDecimal.valueOf(nominal2),
                tradingStatus2,
                buyAvailable2,
                sellAvailable2,
                apiTradeAvailable2,
                BigDecimal.valueOf(minPriceIncrement2),
                first1MinCandleDate2,
                first1DayCandleDate2
        );

        performAndExpectResponse(requestBuilder, expectedShare);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShareIgnoreCase() throws Exception {
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

        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(tinkoffShare1, tinkoffShare2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", "SBER")
                .contentType(MediaType.APPLICATION_JSON);

        final Share expectedShare = new Share(
                figi2,
                ticker2,
                lotSize2,
                currency2,
                name2,
                ipoDate2,
                issueSize2,
                country2,
                sector2,
                issueSizePlan2,
                BigDecimal.valueOf(nominal2),
                tradingStatus2,
                buyAvailable2,
                sellAvailable2,
                apiTradeAvailable2,
                BigDecimal.valueOf(minPriceIncrement2),
                first1MinCandleDate2,
                first1DayCandleDate2
        );

        performAndExpectResponse(requestBuilder, expectedShare);
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

        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(tinkoffShare1, tinkoffShare2));

        final String ticker3 = "YNDX";

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
    void getSingleShare_returnsServerError_whenNoShare() throws Exception {
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

        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(tinkoffShare1, tinkoffShare2));

        final String ticker3 = "YNDX";

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
        final String figi1 = "BBG000G25P51";
        final String ticker1 = "DIOD";
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
        final String ticker2 = "diod";
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

        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare1 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi1)
                .setTicker(ticker1)
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
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
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

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(tinkoffShare1, tinkoffShare2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker1)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single share for ticker " + ticker1 + ". Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

}