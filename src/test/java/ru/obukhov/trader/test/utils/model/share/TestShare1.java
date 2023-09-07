package ru.obukhov.trader.test.utils.model.share;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.contract.v1.ShareType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestShare1 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final MoneyValueMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyValueMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public static final String FIGI = "BBG000B9XRY4";
    public static final String TICKER = "AAPL";
    public static final String CLASS_CODE = "SPBXM";
    public static final String ISIN = "US0378331005";
    public static final int LOT = 1;
    public static final String CURRENCY = Currencies.USD;
    public static final BigDecimal KLONG = DecimalUtils.setDefaultScale(2);
    public static final BigDecimal KSHORT = DecimalUtils.setDefaultScale(2);
    public static final BigDecimal DLONG = DecimalUtils.ONE;
    public static final BigDecimal DSHORT = DecimalUtils.ONE;
    public static final BigDecimal DLONG_MIN = DecimalUtils.ONE;
    public static final BigDecimal DSHORT_MIN = DecimalUtils.ONE;
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "Apple";
    public static final String EXCHANGE = "SPB";
    public static final OffsetDateTime IPO_DATE = DateTimeTestData.createDateTime(1980, 12, 12);
    public static final long ISSUE_SIZE = 16530166000L;
    public static final String COUNTRY_OF_RISK = "US";
    public static final String COUNTRY_OF_RISK_NAME = "Соединенные Штаты Америки";
    public static final String SECTOR = "it";
    public static final long ISSUE_SIZE_PLAN = 50400000000L;
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(0.00001);
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final boolean DIV_YIELD_FLAG = true;
    public static final ShareType SHARE_TYPE = ShareType.SHARE_TYPE_COMMON;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "a9eb4238-eba9-488c-b102-b6140fd08e38";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_RTS;
    public static final String POSITION_UID = "5c5e6656-c4d3-4391-a7ee-e81a76f1804e";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = true;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 1, 10, 10, 34);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(1988, 9, 12, 3);

    public static final Share SHARE = Share.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .classCode(CLASS_CODE)
            .isin(ISIN)
            .lot(LOT)
            .currency(CURRENCY)
            .klong(KLONG)
            .kshort(KSHORT)
            .dlong(DLONG)
            .dshort(DSHORT)
            .dlongMin(DLONG_MIN)
            .dshortMin(DSHORT_MIN)
            .shortEnabledFlag(SHORT_ENABLED_FLAG)
            .name(NAME)
            .exchange(EXCHANGE)
            .ipoDate(IPO_DATE)
            .issueSize(ISSUE_SIZE)
            .countryOfRisk(COUNTRY_OF_RISK)
            .countryOfRiskName(COUNTRY_OF_RISK_NAME)
            .sector(SECTOR)
            .issueSizePlan(ISSUE_SIZE_PLAN)
            .nominal(NOMINAL)
            .tradingStatus(TRADING_STATUS)
            .otcFlag(OTC_FLAG)
            .buyAvailableFlag(BUY_AVAILABLE_FLAG)
            .sellAvailableFlag(SELL_AVAILABLE_FLAG)
            .divYieldFlag(DIV_YIELD_FLAG)
            .shareType(SHARE_TYPE)
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

    public static final ru.tinkoff.piapi.contract.v1.Share TINKOFF_SHARE = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setClassCode(CLASS_CODE)
            .setIsin(ISIN)
            .setLot(LOT)
            .setCurrency(CURRENCY)
            .setKlong(QUOTATION_MAPPER.fromBigDecimal(KLONG))
            .setKshort(QUOTATION_MAPPER.fromBigDecimal(KSHORT))
            .setDlong(QUOTATION_MAPPER.fromBigDecimal(DLONG))
            .setDshort(QUOTATION_MAPPER.fromBigDecimal(DSHORT))
            .setDlongMin(QUOTATION_MAPPER.fromBigDecimal(DLONG_MIN))
            .setDshortMin(QUOTATION_MAPPER.fromBigDecimal(DSHORT_MIN))
            .setShortEnabledFlag(SHORT_ENABLED_FLAG)
            .setName(NAME)
            .setExchange(EXCHANGE)
            .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(IPO_DATE))
            .setIssueSize(ISSUE_SIZE)
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setSector(SECTOR)
            .setIssueSizePlan(ISSUE_SIZE_PLAN)
            .setNominal(MONEY_VALUE_MAPPER.map(NOMINAL, CURRENCY))
            .setTradingStatus(TRADING_STATUS)
            .setOtcFlag(OTC_FLAG)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
            .setDivYieldFlag(DIV_YIELD_FLAG)
            .setShareType(SHARE_TYPE)
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

    public static final String JSON_STRING = "{\"figi\":\"BBG000B9XRY4\"," +
            "\"ticker\":\"AAPL\"," +
            "\"classCode\":\"SPBXM\"," +
            "\"isin\":\"US0378331005\"," +
            "\"lot\":1," +
            "\"currency\":\"usd\"," +
            "\"klong\":2," +
            "\"kshort\":2," +
            "\"dlong\":1," +
            "\"dshort\":1," +
            "\"dlongMin\":1," +
            "\"dshortMin\":1," +
            "\"shortEnabledFlag\":false," +
            "\"name\":\"Apple\"," +
            "\"exchange\":\"SPB\"," +
            "\"ipoDate\":{\"seconds\":345427200,\"nanos\":0}," +
            "\"issueSize\":16530166000," +
            "\"countryOfRisk\":\"US\"," +
            "\"countryOfRiskName\":\"Соединенные Штаты Америки\"," +
            "\"sector\":\"it\"," +
            "\"issueSizePlan\":50400000000," +
            "\"nominal\":0.000010000," +
            "\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\"," +
            "\"otcFlag\":false," +
            "\"buyAvailableFlag\":true," +
            "\"sellAvailableFlag\":true," +
            "\"divYieldFlag\":true," +
            "\"shareType\":\"SHARE_TYPE_COMMON\"," +
            "\"minPriceIncrement\":0.01," +
            "\"apiTradeAvailableFlag\":true," +
            "\"uid\":\"a9eb4238-eba9-488c-b102-b6140fd08e38\"," +
            "\"realExchange\":\"REAL_EXCHANGE_RTS\"," +
            "\"positionUid\":\"5c5e6656-c4d3-4391-a7ee-e81a76f1804e\"," +
            "\"forIisFlag\":true," +
            "\"forQualInvestorFlag\":true," +
            "\"weekendFlag\":false," +
            "\"blockedTcaFlag\":false," +
            "\"first1MinCandleDate\":{\"seconds\":1515569640,\"nanos\":0}," +
            "\"first1DayCandleDate\":{\"seconds\":590025600,\"nanos\":0}}";

}