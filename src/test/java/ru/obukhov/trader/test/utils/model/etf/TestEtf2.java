package ru.obukhov.trader.test.utils.model.etf;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Etf;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestEtf2 {

    public static final String FIGI = "BBG005HLSZ23";
    public static final String TICKER = "FXUS";
    public static final String CLASS_CODE = "TQTF";
    public static final String ISIN = "IE00BD3QHZ91";
    public static final int LOT = 1;
    public static final String CURRENCY = "rub";
    public static final Quotation KLONG = QuotationUtils.ZERO;
    public static final Quotation KSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG = QuotationUtils.ZERO;
    public static final Quotation DSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG_MIN = QuotationUtils.ZERO;
    public static final Quotation DSHORT_MIN = QuotationUtils.ZERO;
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "FinEx Акции американских компаний";
    public static final String EXCHANGE = "moex_close";
    public static final Quotation FIXED_COMMISSION = QuotationUtils.newQuotation(0L, 900000000);
    public static final String FOCUS_TYPE = "equity";
    public static final Timestamp RELEASED_DATE = Timestamp.newBuilder().setSeconds(1383177600L).setNanos(0).build();
    public static final Quotation NUM_SHARES = QuotationUtils.newQuotation(330000L, 0);
    public static final String COUNTRY_OF_RISK = "US";
    public static final String COUNTRY_OF_RISK_NAME = "Соединенные Штаты Америки";
    public static final String SECTOR = "other";
    public static final String REBALANCING_FREQUENCY = "quarterly";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0L, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "46a525d0-d33b-4100-a9d3-152045e5fef7";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "37f20627-aef5-45f4-b184-e597ba3a28e5";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = true;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1520450220L).setNanos(0).build();
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1383177600L).setNanos(0).build();

    public static final Etf ETF = Etf.newBuilder()
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
            .setFixedCommission(FIXED_COMMISSION)
            .setFocusType(FOCUS_TYPE)
            .setReleasedDate(RELEASED_DATE)
            .setNumShares(NUM_SHARES)
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setSector(SECTOR)
            .setRebalancingFreq(REBALANCING_FREQUENCY)
            .setTradingStatus(TRADING_STATUS)
            .setOtcFlag(OTC_FLAG)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
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

    public static final String JSON_STRING = "{\"figi\":\"BBG005HLSZ23\"," +
            "\"ticker\":\"FXUS\"," +
            "\"classCode\":\"TQTF\"," +
            "\"isin\":\"IE00BD3QHZ91\"," +
            "\"lot\":1," +
            "\"currency\":\"rub\"," +
            "\"klong\":0," +
            "\"kshort\":0," +
            "\"dlong\":0," +
            "\"dshort\":0," +
            "\"dlongMin\":0," +
            "\"dshortMin\":0," +
            "\"shortEnabledFlag\":false," +
            "\"name\":\"FinEx Акции американских компаний\"," +
            "\"exchange\":\"moex_close\"," +
            "\"fixedCommission\":0.9," +
            "\"focusType\":\"equity\"," +
            "\"releasedDate\":{\"seconds\":1383177600,\"nanos\":0}," +
            "\"numShares\":330000," +
            "\"countryOfRisk\":\"US\"," +
            "\"countryOfRiskName\":\"Соединенные Штаты Америки\"," +
            "\"sector\":\"other\"," +
            "\"rebalancingFreq\":\"quarterly\"," +
            "\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\"," +
            "\"otcFlag\":false," +
            "\"buyAvailableFlag\":true," +
            "\"sellAvailableFlag\":true," +
            "\"minPriceIncrement\":0.01," +
            "\"apiTradeAvailableFlag\":true," +
            "\"uid\":\"46a525d0-d33b-4100-a9d3-152045e5fef7\"," +
            "\"realExchange\":\"REAL_EXCHANGE_MOEX\"," +
            "\"positionUid\":\"37f20627-aef5-45f4-b184-e597ba3a28e5\"," +
            "\"forIisFlag\":true," +
            "\"forQualInvestorFlag\":true," +
            "\"weekendFlag\":false," +
            "\"blockedTcaFlag\":false," +
            "\"first1MinCandleDate\":{\"seconds\":1520450220,\"nanos\":0}," +
            "\"first1DayCandleDate\":{\"seconds\":1383177600,\"nanos\":0}}";

}