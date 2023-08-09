package ru.obukhov.trader.test.utils.model.share;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.contract.v1.ShareType;

public class TestShare2 {

    public static final String FIGI = "BBG004730N88";
    public static final String TICKER = "SBER";
    public static final String CLASS_CODE = "TQBR";
    public static final String ISIN = "RU0009029540";
    public static final int LOT = 10;
    public static final String CURRENCY = Currencies.RUB;
    public static final Quotation KLONG = QuotationUtils.newNormalizedQuotation(2L, 0);
    public static final Quotation KSHORT = QuotationUtils.newNormalizedQuotation(2L, 0);
    public static final Quotation DLONG = QuotationUtils.newNormalizedQuotation(0L, 200000000);
    public static final Quotation DSHORT = QuotationUtils.newNormalizedQuotation(0L, 199900000);
    public static final Quotation DLONG_MIN = QuotationUtils.newNormalizedQuotation(0L, 105600000);
    public static final Quotation DSHORT_MIN = QuotationUtils.newNormalizedQuotation(0L, 95400000);
    public static final boolean SHORT_ENABLED_FLAG = true;
    public static final String NAME = "Сбер Банк";
    public static final String EXCHANGE = "MOEX_EVENING_WEEKEND";
    public static final Timestamp IPO_DATE = TimestampUtils.newTimestamp(1184112000L, 0);
    public static final long ISSUE_SIZE = 21586948000L;
    public static final String COUNTRY_OF_RISK = "RU";
    public static final String COUNTRY_OF_RISK_NAME = "Российская Федерация";
    public static final String SECTOR = "financial";
    public static final long ISSUE_SIZE_PLAN = 21586948000L;
    public static final MoneyValue NOMINAL = MoneyValue.newBuilder().setCurrency(Currencies.RUB).setUnits(3).build();
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final boolean DIV_YIELD_FLAG = true;
    public static final ShareType SHARE_TYPE = ShareType.SHARE_TYPE_COMMON;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newNormalizedQuotation(0L, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "e6123145-9665-43e0-8413-cd61b8aa9b13";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "41eb2102-5333-4713-bf15-72b204c4bf7";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = true;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = TimestampUtils.newTimestamp(1520447580L, 0);
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = TimestampUtils.newTimestamp(946969200L, 0);

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