package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.web.model.exchange.GetShareResponse;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

class InstrumentsControllerIntegrationTest extends ControllerIntegrationTest {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShare_returnsShare() throws Exception {
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
                .setIpoDate(DATE_TIME_MAPPER.map(ipoDate1))
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
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.map(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.map(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setIpoDate(DATE_TIME_MAPPER.map(ipoDate2))
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
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.map(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.map(first1DayCandleDate2))
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

        performAndExpectResponse(requestBuilder, new GetShareResponse(expectedShare));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShare_returnsShareIgnoreCase() throws Exception {
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
                .setIpoDate(DATE_TIME_MAPPER.map(ipoDate1))
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
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.map(first1MinCandleDate1))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.map(first1DayCandleDate1))
                .build();
        final ru.tinkoff.piapi.contract.v1.Share tinkoffShare2 = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi2)
                .setTicker(ticker2)
                .setLot(lotSize2)
                .setCurrency(currency2.name().toLowerCase())
                .setName(name2)
                .setIpoDate(DATE_TIME_MAPPER.map(ipoDate2))
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
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.map(first1MinCandleDate2))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.map(first1DayCandleDate2))
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

        performAndExpectResponse(requestBuilder, new GetShareResponse(expectedShare));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShare_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

}