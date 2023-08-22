package ru.obukhov.trader.test.utils.model.share;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.ShareType;

public class TestShare3 {

    public static final String FIGI = "BBG006L8G4H1";
    public static final String TICKER = "YNDX";
    public static final String CLASS_CODE = "TQBR";
    public static final String ISIN = "NL0009805522";
    public static final int LOT = 1;
    public static final String CURRENCY = Currencies.RUB;
    public static final Quotation KLONG = QuotationUtils.newQuotation(2L);
    public static final Quotation KSHORT = QuotationUtils.newQuotation(2L);
    public static final Quotation DLONG = QuotationUtils.newQuotation(0L, 250000000);
    public static final Quotation DSHORT = QuotationUtils.newQuotation(0L, 204300000);
    public static final Quotation DLONG_MIN = QuotationUtils.newQuotation(0L, 134000000);
    public static final Quotation DSHORT_MIN = QuotationUtils.newQuotation(0L, 97400000);
    public static final boolean SHORT_ENABLED_FLAG = true;
    public static final String NAME = "Яндекс";
    public static final String EXCHANGE = "MOEX_PLUS";
    public static final Timestamp IPO_DATE = TimestampUtils.newTimestamp(1306195200L, 0);
    public static final long ISSUE_SIZE = 326342270L;
    public static final String COUNTRY_OF_RISK = "RU";
    public static final String COUNTRY_OF_RISK_NAME = "Российская Федерация";
    public static final String SECTOR = "telecom";
    public static final long ISSUE_SIZE_PLAN = 0L;
    public static final MoneyValue NOMINAL = TestData.createMoneyValue(0.01, Currencies.EUR);
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final boolean DIV_YIELD_FLAG = false;
    public static final ShareType SHARE_TYPE = ShareType.SHARE_TYPE_COMMON;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0L, 200000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "10e17a87-3bce-4a1f-9dfc-720396f98a3c";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "cb51e157-1f73-4c62-baac-93f11755056a";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = true;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = TimestampUtils.newTimestamp(1520447880L, 0);
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = TimestampUtils.newTimestamp(1401865200L, 0);

    public static final Share SHARE = Share.newBuilder()
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
            .setSector(SECTOR)
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

}