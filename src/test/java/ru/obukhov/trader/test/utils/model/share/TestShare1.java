package ru.obukhov.trader.test.utils.model.share;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Sector;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.contract.v1.ShareType;

public class TestShare1 {

    public static final String FIGI = "BBG000B9XRY4";
    public static final String TICKER = "AAPL";
    public static final String CLASS_CODE = "SPBXM";
    public static final String ISIN = "US0378331005";
    public static final int LOT = 1;
    public static final String CURRENCY = Currency.USD;
    public static final Quotation KLONG = QuotationUtils.newNormalizedQuotation(2L, 0);
    public static final Quotation KSHORT = QuotationUtils.newNormalizedQuotation(2L, 0);
    public static final Quotation DLONG = QuotationUtils.newNormalizedQuotation(1L, 0);
    public static final Quotation DSHORT = QuotationUtils.newNormalizedQuotation(1L, 0);
    public static final Quotation DLONG_MIN = QuotationUtils.newNormalizedQuotation(1L, 0);
    public static final Quotation DSHORT_MIN = QuotationUtils.newNormalizedQuotation(1L, 0);
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "Apple";
    public static final String EXCHANGE = "SPB";
    public static final Timestamp IPO_DATE = TimestampUtils.newTimestamp(345427200L, 0);
    public static final long ISSUE_SIZE = 16530166000L;
    public static final String COUNTRY_OF_RISK = "US";
    public static final String COUNTRY_OF_RISK_NAME = "Соединенные Штаты Америки";
    public static final Sector SECTOR = Sector.IT;
    public static final long ISSUE_SIZE_PLAN = 50400000000L;
    public static final MoneyValue NOMINAL = MoneyValue.newBuilder().setCurrency(Currency.USD).setUnits(0L).setNano(10000).build();
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final boolean DIV_YIELD_FLAG = true;
    public static final ShareType SHARE_TYPE = ShareType.SHARE_TYPE_COMMON;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newNormalizedQuotation(0L, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "a9eb4238-eba9-488c-b102-b6140fd08e38";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_RTS;
    public static final String POSITION_UID = "5c5e6656-c4d3-4391-a7ee-e81a76f1804e";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = true;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = TimestampUtils.newTimestamp(1516692840L, 0);
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = TimestampUtils.newTimestamp(590025600L, 0);

    public static final ru.tinkoff.piapi.contract.v1.Share SHARE = ru.tinkoff.piapi.contract.v1.Share.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setClassCode(CLASS_CODE)
            .setIsin(ISIN)
            .setLot(LOT)
            .setCurrency(CURRENCY)
            .setKlong(KLONG)
            .setKshort(KSHORT)
            .setDlong(DLONG)
            .setDshort(DSHORT)
            .setDlongMin(DLONG_MIN)
            .setDshortMin(DSHORT_MIN)
            .setShortEnabledFlag(SHORT_ENABLED_FLAG)
            .setName(NAME)
            .setExchange(EXCHANGE)
            .setIpoDate(IPO_DATE)
            .setIssueSize(ISSUE_SIZE)
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setSector(SECTOR.name().toLowerCase())
            .setIssueSizePlan(ISSUE_SIZE_PLAN)
            .setNominal(NOMINAL)
            .setTradingStatus(TRADING_STATUS)
            .setOtcFlag(OTC_FLAG)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
            .setDivYieldFlag(DIV_YIELD_FLAG)
            .setShareType(SHARE_TYPE)
            .setMinPriceIncrement(MIN_PRICE_INCREMENT)
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .setUid(UID)
            .setRealExchange(REAL_EXCHANGE)
            .setPositionUid(POSITION_UID)
            .setForIisFlag(FOR_IIS_FLAG)
            .setForQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .setWeekendFlag(WEEKEND_FLAG)
            .setBlockedTcaFlag(BLOCKED_TCA_FLAG)
            .setFirst1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .setFirst1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();

    public static final String STRING = "{\"figi\":\"BBG000B9XRY4\",\"ticker\":\"AAPL\",\"classCode\":\"SPBXM\",\"isin\":\"US0378331005\",\"lot\":1,\"currency\":\"usd\",\"klong\":{\"units\":2,\"nano\":0},\"kshort\":{\"units\":2,\"nano\":0},\"dlong\":{\"units\":1,\"nano\":0},\"dshort\":{\"units\":1,\"nano\":0},\"dlongMin\":{\"units\":1,\"nano\":0},\"dshortMin\":{\"units\":1,\"nano\":0},\"shortEnabledFlag\":false,\"name\":\"Apple\",\"exchange\":\"SPB\",\"ipoDate\":{\"seconds\":345427200,\"nanos\":0},\"issueSize\":16530166000,\"countryOfRisk\":\"US\",\"countryOfRiskName\":\"Соединенные Штаты Америки\",\"sector\":\"it\",\"issueSizePlan\":50400000000,\"nominal\":{\"currency\":\"usd\",\"units\":0,\"nano\":10000},\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\",\"otcFlag\":false,\"buyAvailableFlag\":true,\"sellAvailableFlag\":true,\"divYieldFlag\":true,\"shareType\":\"SHARE_TYPE_COMMON\",\"minPriceIncrement\":{\"units\":0,\"nano\":10000000},\"apiTradeAvailableFlag\":true,\"uid\":\"a9eb4238-eba9-488c-b102-b6140fd08e38\",\"realExchange\":\"REAL_EXCHANGE_RTS\",\"positionUid\":\"5c5e6656-c4d3-4391-a7ee-e81a76f1804e\",\"forIisFlag\":true,\"forQualInvestorFlag\":true,\"weekendFlag\":false,\"blockedTcaFlag\":false,\"first1MinCandleDate\":{\"seconds\":1516692840,\"nanos\":0},\"first1DayCandleDate\":{\"seconds\":590025600,\"nanos\":0}}";

}