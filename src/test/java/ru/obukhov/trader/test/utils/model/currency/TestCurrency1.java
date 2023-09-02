package ru.obukhov.trader.test.utils.model.currency;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestCurrency1 {

    private static final MoneyValueMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyValueMapper.class);
    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public static final String FIGI = "BBG0013HGFT4";
    public static final String TICKER = "USD000UTSTOM";
    public static final String CLASS_CODE = "CETS";
    public static final String ISIN = "";
    public static final int LOT = 1000;
    public static final String CURRENCY_VALUE = Currencies.RUB;
    public static final BigDecimal KLONG = DecimalUtils.setDefaultScale(2L);
    public static final BigDecimal KSHORT = DecimalUtils.setDefaultScale(2L);
    public static final BigDecimal DLONG = DecimalUtils.setDefaultScale(0.5);
    public static final BigDecimal DSHORT = DecimalUtils.setDefaultScale(0.5);
    public static final BigDecimal DLONG_MIN = DecimalUtils.setDefaultScale(0.2929);
    public static final BigDecimal DSHORT_MIN = DecimalUtils.setDefaultScale(0.2247);
    public static final boolean SHORT_ENABLED_FLAG = true;
    public static final String NAME = "Доллар США";
    public static final String EXCHANGE = "FX";
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1);
    public static final String COUNTRY_OF_RISK = "";
    public static final String COUNTRY_OF_RISK_NAME = "";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final String ISO_CURRENCY_NAME = "usd";
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.0025);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "a22a1263-8e1b-4546-a1aa-416463f104d3";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "6e97aa9b-50b6-4738-bce7-17313f2b2cc2";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 3, 7, 19, 16);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2000, 5, 16, 3);

    public static final Currency CURRENCY = Currency.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .classCode(CLASS_CODE)
            .isin(ISIN)
            .lot(LOT)
            .currency(CURRENCY_VALUE)
            .klong(KLONG)
            .kshort(KSHORT)
            .dlong(DLONG)
            .dshort(DSHORT)
            .dlongMin(DLONG_MIN)
            .dshortMin(DSHORT_MIN)
            .shortEnabledFlag(SHORT_ENABLED_FLAG)
            .name(NAME)
            .exchange(EXCHANGE)
            .nominal(NOMINAL)
            .countryOfRisk(COUNTRY_OF_RISK)
            .countryOfRiskName(COUNTRY_OF_RISK_NAME)
            .tradingStatus(TRADING_STATUS)
            .buyAvailableFlag(BUY_AVAILABLE_FLAG)
            .sellAvailableFlag(SELL_AVAILABLE_FLAG)
            .isoCurrencyName(ISO_CURRENCY_NAME)
            .minPriceIncrement(MIN_PRICE_INCREMENT)
            .apiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .uid(UID)
            .realExchange(REAL_EXCHANGE)
            .positionUid(POSITION_UID)
            .forIisFlag(FOR_IIS_FLAG)
            .forQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .weekendFlag(WEEKEND_FLAG)
            .blockedTcaFlag(BLOCKED_TCA_FLAG)
            .first1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();

    public static final ru.tinkoff.piapi.contract.v1.Currency TINKOFF_CURRENCY = ru.tinkoff.piapi.contract.v1.Currency.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setClassCode(CLASS_CODE)
            .setIsin(ISIN)
            .setLot(LOT)
            .setCurrency(CURRENCY_VALUE)
            .setKlong(QUOTATION_MAPPER.fromBigDecimal(KLONG))
            .setKshort(QUOTATION_MAPPER.fromBigDecimal(KSHORT))
            .setDlong(QUOTATION_MAPPER.fromBigDecimal(DLONG))
            .setDshort(QUOTATION_MAPPER.fromBigDecimal(DSHORT))
            .setDlongMin(QUOTATION_MAPPER.fromBigDecimal(DLONG_MIN))
            .setDshortMin(QUOTATION_MAPPER.fromBigDecimal(DSHORT_MIN))
            .setShortEnabledFlag(SHORT_ENABLED_FLAG)
            .setName(NAME)
            .setExchange(EXCHANGE)
            .setNominal(MONEY_VALUE_MAPPER.map(NOMINAL, ISO_CURRENCY_NAME))
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setTradingStatus(TRADING_STATUS)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
            .setIsoCurrencyName(ISO_CURRENCY_NAME)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(MIN_PRICE_INCREMENT))
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .setUid(UID)
            .setRealExchange(REAL_EXCHANGE)
            .setPositionUid(POSITION_UID)
            .setForIisFlag(FOR_IIS_FLAG)
            .setForQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .setWeekendFlag(WEEKEND_FLAG)
            .setBlockedTcaFlag(BLOCKED_TCA_FLAG)
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final String JSON_STRING = "{" +
            "\"figi\":\"BBG0013HGFT4\"," +
            "\"ticker\":\"USD000UTSTOM\"," +
            "\"classCode\":\"CETS\"," +
            "\"isin\":\"\"," +
            "\"lot\":1000," +
            "\"currency\":\"rub\"," +
            "\"klong\":2," +
            "\"kshort\":2," +
            "\"dlong\":0.5," +
            "\"dshort\":0.5," +
            "\"dlongMin\":0.2929," +
            "\"dshortMin\":0.2247," +
            "\"shortEnabledFlag\":true," +
            "\"name\":\"Доллар США\"," +
            "\"exchange\":\"FX\"," +
            "\"nominal\":1," +
            "\"countryOfRisk\":\"\"," +
            "\"countryOfRiskName\":\"\"," +
            "\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\"," +
            "\"otcFlag\":false," +
            "\"buyAvailableFlag\":true," +
            "\"sellAvailableFlag\":true," +
            "\"isoCurrencyName\":\"usd\"," +
            "\"minPriceIncrement\":0.0025," +
            "\"apiTradeAvailableFlag\":true," +
            "\"uid\":\"a22a1263-8e1b-4546-a1aa-416463f104d3\"," +
            "\"realExchange\":\"REAL_EXCHANGE_MOEX\"," +
            "\"positionUid\":\"6e97aa9b-50b6-4738-bce7-17313f2b2cc2\"," +
            "\"forIisFlag\":true," +
            "\"forQualInvestorFlag\":false," +
            "\"weekendFlag\":false," +
            "\"blockedTcaFlag\":false," +
            "\"first1MinCandleDate\":\"2018-03-07T19:16:00+03:00\"," +
            "\"first1DayCandleDate\":\"2000-05-16T03:00:00+03:00\"}";

}