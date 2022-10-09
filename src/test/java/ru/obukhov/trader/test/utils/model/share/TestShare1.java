package ru.obukhov.trader.test.utils.model.share;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestShare1 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public static final String FIGI = "BBG000B9XRY4";
    public static final String TICKER = "AAPL";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY = Currency.USD;
    public static final String NAME = "Apple";
    public static final Exchange EXCHANGE = Exchange.SPB_MORNING_WEEKEND;
    public static final OffsetDateTime IPO_DATE = DateTimeTestData.createDateTime(1980, 12, 12, 3);
    public static final long ISSUE_SIZE = 16530166000L;
    public static final String COUNTRY = "Соединенные Штаты Америки";
    public static final Sector SECTOR = Sector.IT;
    public static final long ISSUE_SIZE_PLAN = 50400000000L;
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(0.00001);
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = true;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 1, 10, 10, 34);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(1988, 9, 12, 3);

    public static final ru.tinkoff.piapi.contract.v1.Share TINKOFF_SHARE = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setLot(LOT_SIZE)
            .setCurrency(CURRENCY.name().toLowerCase())
            .setName(NAME)
            .setExchange(EXCHANGE.getValue())
            .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(IPO_DATE))
            .setIssueSize(ISSUE_SIZE)
            .setCountryOfRiskName(COUNTRY)
            .setSector(SECTOR.name().toLowerCase())
            .setIssueSizePlan(ISSUE_SIZE_PLAN)
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(NOMINAL))
            .setTradingStatus(TRADING_STATUS)
            .setBuyAvailableFlag(BUY_AVAILABLE)
            .setSellAvailableFlag(SELL_AVAILABLE)
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final Share SHARE = Share.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .lotSize(LOT_SIZE)
            .currency(CURRENCY)
            .name(NAME)
            .exchange(EXCHANGE)
            .ipoDate(IPO_DATE)
            .issueSize(ISSUE_SIZE)
            .country(COUNTRY)
            .sector(SECTOR)
            .issueSizePlan(ISSUE_SIZE_PLAN)
            .nominal(NOMINAL)
            .tradingStatus(TRADING_STATUS)
            .buyAvailable(BUY_AVAILABLE)
            .sellAvailable(SELL_AVAILABLE)
            .apiTradeAvailable(API_TRADE_AVAILABLE)
            .minPriceIncrement(MIN_PRICE_INCREMENT)
            .first1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();

}