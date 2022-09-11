package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;

class ShareMapperUnitTest {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    private final ShareMapper shareMapper = Mappers.getMapper(ShareMapper.class);

    @Test
    void map() {
        final String figi = "BBG000B9XRY4";
        final String ticker = "AAPL";
        final int lotSize = 1;
        final Currency currency = Currency.USD;
        final String name = "Apple";
        final OffsetDateTime ipoDate = DateTimeTestData.createDateTime(1980, 12, 12, 3);
        final long issueSize = 16530166000L;
        final String country = "Соединенные Штаты Америки";
        final Sector sector = Sector.IT;
        final long issueSizePlan = 50400000000L;
        final double nominal = 0.00001;
        final SecurityTradingStatus tradingStatus = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final boolean buyAvailable = true;
        final boolean sellAvailable = true;
        final boolean apiTradeAvailable = true;
        final double minPriceIncrement = 0.01;
        final OffsetDateTime first1MinCandleDate = DateTimeTestData.createDateTime(2018, 1, 10, 10, 34);
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.createDateTime(1988, 9, 12, 3);

        final ru.tinkoff.piapi.contract.v1.Share share = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi)
                .setTicker(ticker)
                .setLot(lotSize)
                .setCurrency(currency.name().toLowerCase())
                .setName(name)
                .setIpoDate(DATE_TIME_MAPPER.map(ipoDate))
                .setIssueSize(issueSize)
                .setCountryOfRiskName(country)
                .setSector(sector.name().toLowerCase())
                .setIssueSizePlan(issueSizePlan)
                .setNominal(MONEY_VALUE_MAPPER.doubleToMoneyValue(nominal))
                .setTradingStatus(tradingStatus)
                .setBuyAvailableFlag(buyAvailable)
                .setSellAvailableFlag(sellAvailable)
                .setApiTradeAvailableFlag(apiTradeAvailable)
                .setMinPriceIncrement(QUOTATION_MAPPER.fromDouble(minPriceIncrement))
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.map(first1MinCandleDate))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.map(first1DayCandleDate))
                .build();

        final Share result = shareMapper.map(share);

        Assertions.assertEquals(figi, result.figi());
        Assertions.assertEquals(ticker, result.ticker());
        Assertions.assertEquals(lotSize, result.lotSize());
        Assertions.assertEquals(currency, result.currency());
        Assertions.assertEquals(name, result.name());
        Assertions.assertEquals(ipoDate, result.ipoDate());
        Assertions.assertEquals(issueSize, result.issueSize());
        Assertions.assertEquals(country, result.country());
        Assertions.assertEquals(sector, result.sector());
        Assertions.assertEquals(issueSizePlan, result.issueSizePlan());
        AssertUtils.assertEquals(nominal, result.nominal());
        Assertions.assertEquals(tradingStatus, result.tradingStatus());
        Assertions.assertEquals(buyAvailable, result.buyAvailable());
        Assertions.assertEquals(sellAvailable, result.sellAvailable());
        Assertions.assertEquals(apiTradeAvailable, result.apiTradeAvailable());
        AssertUtils.assertEquals(minPriceIncrement, result.minPriceIncrement());
        Assertions.assertEquals(first1MinCandleDate, result.first1MinCandleDate());
        Assertions.assertEquals(first1DayCandleDate, result.first1DayCandleDate());
    }

    @Test
    void map_whenValueIsNull() {
        final Share share = shareMapper.map(null);

        Assertions.assertNull(share);
    }

}