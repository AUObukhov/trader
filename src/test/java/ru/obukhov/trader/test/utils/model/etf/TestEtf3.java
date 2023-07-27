package ru.obukhov.trader.test.utils.model.etf;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestEtf3 {

    public static final String FIGI = "BBG000PNJ9F5";
    public static final String TICKER = "EZA";
    public static final String CLASS_CODE = "SPBXM";
    public static final String ISIN = "US4642867802";
    public static final int LOT = 1;
    public static final String CURRENCY = "usd";
    public static final Quotation KLONG = QuotationUtils.newNormalizedQuotation(0L, 0);
    public static final Quotation KSHORT = QuotationUtils.newNormalizedQuotation(0L, 0);
    public static final Quotation DLONG = QuotationUtils.newNormalizedQuotation(0L, 0);
    public static final Quotation DSHORT = QuotationUtils.newNormalizedQuotation(0L, 0);
    public static final Quotation DLONG_MIN = QuotationUtils.newNormalizedQuotation(0L, 0);
    public static final Quotation DSHORT_MIN = QuotationUtils.newNormalizedQuotation(0L, 0);
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "iShares MSCI South Africa ETF";
    public static final String EXCHANGE = "spb_etf";
    public static final Quotation FIXED_COMMISSION = QuotationUtils.newNormalizedQuotation(0, 640000000);
    public static final String FOCUS_TYPE = "equity";
    public static final Timestamp RELEASED_DATE = Timestamp.newBuilder().setSeconds(1044576000L).setNanos(0).build();
    public static final Quotation NUM_SHARES = QuotationUtils.newNormalizedQuotation(9600000L, 0);
    public static final String COUNTRY_OF_RISK = "ZA";
    public static final String COUNTRY_OF_RISK_NAME = "Южно-Африканская Республика";
    public static final String SECTOR = "";
    public static final String REBALANCING_FREQUENCY = "quarterly";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_BREAK_IN_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newNormalizedQuotation(0L, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "5375cdb7-4ad8-4139-aef1-6003dc331d07";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_RTS;
    public static final String POSITION_UID = "76f544ad-ed9a-46a0-9b68-f28d961f0bff";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = true;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1668169800L).setNanos(0).build();
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1668150000L).setNanos(0).build();

    public static final ru.tinkoff.piapi.contract.v1.Etf ETF = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
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

}