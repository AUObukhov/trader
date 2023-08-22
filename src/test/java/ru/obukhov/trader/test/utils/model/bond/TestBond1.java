package ru.obukhov.trader.test.utils.model.bond;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.RiskLevel;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestBond1 {

    public static final String FIGI = "BBG00J7HHGH1";
    public static final String TICKER = "RU000A0ZYG52";
    public static final String CLASS_CODE = "TQCB";
    public static final String ISIN = "RU000A0ZYG52";
    public static final int LOT = 1;
    public static final String CURRENCY = Currencies.RUB;
    public static final Quotation KLONG = QuotationUtils.newQuotation(2L);
    public static final Quotation KSHORT = QuotationUtils.newQuotation(2L);
    public static final Quotation DLONG = QuotationUtils.newQuotation(0L, 300000000);
    public static final Quotation DSHORT = QuotationUtils.newQuotation(0L, 300000000);
    public static final Quotation DLONG_MIN = QuotationUtils.newQuotation(0L, 163300000);
    public static final Quotation DSHORT_MIN = QuotationUtils.newQuotation(0L, 140200000);
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "Ростелеком выпуск 3";
    public static final String EXCHANGE = "MOEX";
    public static final int COUPON_QUANTITY_PER_YEAR = 2;
    public static final Timestamp MATURITY_DATE = Timestamp.newBuilder().setSeconds(1825718400L).setNanos(0).build();
    public static final MoneyValue NOMINAL = TestData.createMoneyValue(1000, Currencies.RUB);
    public static final MoneyValue INITIAL_NOMINAL = TestData.createMoneyValue(1000, Currencies.RUB);
    public static final Timestamp STATE_REG_DATE = Timestamp.newBuilder().setSeconds(1510790400L).setNanos(0).build();
    public static final Timestamp PLACEMENT_DATE = Timestamp.newBuilder().setSeconds(1511222400L).setNanos(0).build();
    public static final MoneyValue PLACEMENT_PRICE = TestData.createMoneyValue(1000, Currencies.RUB);
    public static final MoneyValue ACI_VALUE = TestData.createMoneyValue(17.9, Currencies.RUB);
    public static final String COUNTRY_OF_RISK = "RU";
    public static final String COUNTRY_OF_RISK_NAME = "Российская Федерация";
    public static final String SECTOR = "telecom";
    public static final String ISSUE_KIND = "documentary";
    public static final long ISSUE_SIZE = 10000000;
    public static final long ISSUE_SIZE_PLAN = 10000000;
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final boolean FLOATING_COUPON_FLAG = false;
    public static final boolean PERPETUAL_FLAG = false;
    public static final boolean AMORTIZATION_FLAG = false;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "00486cd8-5915-4c0b-8017-b81b9d1805d4";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "cf3e5dbf-3338-4ee5-96fd-375925e16daa";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final boolean SUBORDINATED_FLAG = false;

    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1521007200L).setNanos(0).build();
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1511222400L).setNanos(0).build();
    public static final RiskLevel RISK_LEVEL = RiskLevel.RISK_LEVEL_LOW;

    public static final Bond BOND = Bond.newBuilder()
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
            .setCouponQuantityPerYear(COUPON_QUANTITY_PER_YEAR)
            .setMaturityDate(MATURITY_DATE)
            .setNominal(NOMINAL)
            .setInitialNominal(INITIAL_NOMINAL)
            .setStateRegDate(STATE_REG_DATE)
            .setPlacementDate(PLACEMENT_DATE)
            .setPlacementPrice(PLACEMENT_PRICE)
            .setAciValue(ACI_VALUE)
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setSector(SECTOR)
            .setIssueKind(ISSUE_KIND)
            .setIssueSize(ISSUE_SIZE)
            .setIssueSizePlan(ISSUE_SIZE_PLAN)
            .setTradingStatus(TRADING_STATUS)
            .setOtcFlag(OTC_FLAG)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
            .setFloatingCouponFlag(FLOATING_COUPON_FLAG)
            .setPerpetualFlag(PERPETUAL_FLAG)
            .setAmortizationFlag(AMORTIZATION_FLAG)
            .setMinPriceIncrement(MIN_PRICE_INCREMENT)
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .setUid(UID)
            .setRealExchange(REAL_EXCHANGE)
            .setPositionUid(POSITION_UID)
            .setForIisFlag(FOR_IIS_FLAG)
            .setForQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .setWeekendFlag(WEEKEND_FLAG)
            .setBlockedTcaFlag(BLOCKED_TCA_FLAG)
            .setSubordinatedFlag(SUBORDINATED_FLAG)
            .setFirst1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .setFirst1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .setRiskLevel(RISK_LEVEL)
            .build();

    public static final String JSON_STRING = "{\"figi\":\"BBG00J7HHGH1\"," +
            "\"ticker\":\"RU000A0ZYG52\"," +
            "\"classCode\":\"TQCB\"," +
            "\"isin\":\"RU000A0ZYG52\"," +
            "\"lot\":1,\"currency\":\"rub\"," +
            "\"klong\":2," +
            "\"kshort\":2," +
            "\"dlong\":0.3," +
            "\"dshort\":0.3," +
            "\"dlongMin\":0.1633," +
            "\"dshortMin\":0.1402," +
            "\"shortEnabledFlag\":false," +
            "\"name\":\"Ростелеком выпуск 3\"," +
            "\"exchange\":\"MOEX\"," +
            "\"couponQuantityPerYear\":2," +
            "\"maturityDate\":{\"seconds\":1825718400,\"nanos\":0}," +
            "\"nominal\":{\"currency\":\"rub\",\"units\":1000,\"nano\":0}," +
            "\"initialNominal\":{\"currency\":\"rub\",\"units\":1000,\"nano\":0}," +
            "\"stateRegDate\":{\"seconds\":1510790400,\"nanos\":0}," +
            "\"placementDate\":{\"seconds\":1511222400,\"nanos\":0}," +
            "\"placementPrice\":{\"currency\":\"rub\",\"units\":1000,\"nano\":0}," +
            "\"aciValue\":{\"currency\":\"rub\",\"units\":17,\"nano\":900000000}," +
            "\"countryOfRisk\":\"RU\"," +
            "\"countryOfRiskName\":\"Российская Федерация\"," +
            "\"sector\":\"telecom\"," +
            "\"issueKind\":\"documentary\"," +
            "\"issueSize\":10000000," +
            "\"issueSizePlan\":10000000," +
            "\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\"," +
            "\"otcFlag\":false," +
            "\"buyAvailableFlag\":true," +
            "\"sellAvailableFlag\":true," +
            "\"floatingCouponFlag\":false," +
            "\"perpetualFlag\":false," +
            "\"amortizationFlag\":false," +
            "\"minPriceIncrement\":0.01," +
            "\"apiTradeAvailableFlag\":true," +
            "\"uid\":\"00486cd8-5915-4c0b-8017-b81b9d1805d4\"," +
            "\"realExchange\":\"REAL_EXCHANGE_MOEX\"," +
            "\"positionUid\":\"cf3e5dbf-3338-4ee5-96fd-375925e16daa\"," +
            "\"forIisFlag\":true," +
            "\"forQualInvestorFlag\":false," +
            "\"weekendFlag\":false," +
            "\"blockedTcaFlag\":false," +
            "\"subordinatedFlag\":false," +
            "\"first1MinCandleDate\":{\"seconds\":1521007200,\"nanos\":0}," +
            "\"first1DayCandleDate\":{\"seconds\":1511222400,\"nanos\":0}," +
            "\"riskLevel\":\"RISK_LEVEL_LOW\"}";

}