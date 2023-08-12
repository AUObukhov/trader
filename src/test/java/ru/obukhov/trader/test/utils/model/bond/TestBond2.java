package ru.obukhov.trader.test.utils.model.bond;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.RiskLevel;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestBond2 {

    public static final String FIGI = "TCS00A1050H0";
    public static final String TICKER = "RU000A1050H0";
    public static final String CLASS_CODE = "TQCB";
    public static final String ISIN = "RU000A1050H0";
    public static final int LOT = 1;
    public static final String CURRENCY = Currencies.RUB;
    public static final Quotation KLONG = QuotationUtils.newNormalizedQuotation(0, 0);
    public static final Quotation KSHORT = QuotationUtils.newNormalizedQuotation(0, 0);
    public static final Quotation DLONG = QuotationUtils.newNormalizedQuotation(0, 0);
    public static final Quotation DSHORT = QuotationUtils.newNormalizedQuotation(0, 0);
    public static final Quotation DLONG_MIN = QuotationUtils.newNormalizedQuotation(0, 0);
    public static final Quotation DSHORT_MIN = QuotationUtils.newNormalizedQuotation(0, 0);
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "ЕАБР";
    public static final String EXCHANGE = "MOEX";
    public static final int COUPON_QUANTITY_PER_YEAR = 2;
    public static final Timestamp MATURITY_DATE = Timestamp.newBuilder().setSeconds(1753747200L).setNanos(0).build();
    public static final MoneyValue NOMINAL = MoneyValue.newBuilder().setCurrency(Currencies.RUB).setUnits(1000).setNano(0).build();
    public static final MoneyValue INITIAL_NOMINAL = MoneyValue.newBuilder().setCurrency(Currencies.RUB).setUnits(1000).setNano(0).build();
    public static final Timestamp STATE_REG_DATE = Timestamp.newBuilder().setSeconds(1658966400L).setNanos(0).build();
    public static final Timestamp PLACEMENT_DATE = Timestamp.newBuilder().setSeconds(1659398400L).setNanos(0).build();
    public static final MoneyValue PLACEMENT_PRICE = MoneyValue.newBuilder().setCurrency(Currencies.RUB).setUnits(1000).setNano(0).build();
    public static final MoneyValue ACI_VALUE = MoneyValue.newBuilder().setCurrency(Currencies.RUB).setUnits(45).setNano(80000000).build();
    public static final String COUNTRY_OF_RISK = "KZ";
    public static final String COUNTRY_OF_RISK_NAME = "Республика Казахстан";
    public static final String SECTOR = "financial";
    public static final String ISSUE_KIND = "non_documentary";
    public static final long ISSUE_SIZE = 10000000;
    public static final long ISSUE_SIZE_PLAN = 10000000;
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final boolean FLOATING_COUPON_FLAG = false;
    public static final boolean PERPETUAL_FLAG = false;
    public static final boolean AMORTIZATION_FLAG = false;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newNormalizedQuotation(0, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "cdb57948-f0b0-4420-8dd0-c8747fdc6735";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "4cf921a7-0a5d-4e53-a2e2-b92649376ab4";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final boolean SUBORDINATED_FLAG = false;

    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1659440700L).setNanos(0).build();
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = Timestamp.newBuilder().setSeconds(1659423600L).setNanos(0).build();
    public static final RiskLevel RISK_LEVEL = RiskLevel.RISK_LEVEL_UNSPECIFIED;

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

    public static final String STRING = "{\"figi\":\"TCS00A1050H0\",\"ticker\":\"RU000A1050H0\",\"classCode\":\"TQCB\",\"isin\":\"RU000A1050H0\",\"lot\":1,\"currency\":\"rub\",\"klong\":{\"units\":0,\"nano\":0},\"kshort\":{\"units\":0,\"nano\":0},\"dlong\":{\"units\":0,\"nano\":0},\"dshort\":{\"units\":0,\"nano\":0},\"dlongMin\":{\"units\":0,\"nano\":0},\"dshortMin\":{\"units\":0,\"nano\":0},\"shortEnabledFlag\":false,\"name\":\"ЕАБР\",\"exchange\":\"MOEX\",\"couponQuantityPerYear\":2,\"maturityDate\":{\"seconds\":1753747200,\"nanos\":0},\"nominal\":{\"currency\":\"rub\",\"units\":1000,\"nano\":0},\"initialNominal\":{\"currency\":\"rub\",\"units\":1000,\"nano\":0},\"stateRegDate\":{\"seconds\":1658966400,\"nanos\":0},\"placementDate\":{\"seconds\":1659398400,\"nanos\":0},\"placementPrice\":{\"currency\":\"rub\",\"units\":1000,\"nano\":0},\"aciValue\":{\"currency\":\"rub\",\"units\":45,\"nano\":80000000},\"countryOfRisk\":\"KZ\",\"countryOfRiskName\":\"Республика Казахстан\",\"sector\":\"financial\",\"issueKind\":\"non_documentary\",\"issueSize\":10000000,\"issueSizePlan\":10000000,\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\",\"otcFlag\":false,\"buyAvailableFlag\":true,\"sellAvailableFlag\":true,\"floatingCouponFlag\":false,\"perpetualFlag\":false,\"amortizationFlag\":false,\"minPriceIncrement\":{\"units\":0,\"nano\":10000000},\"apiTradeAvailableFlag\":true,\"uid\":\"cdb57948-f0b0-4420-8dd0-c8747fdc6735\",\"realExchange\":\"REAL_EXCHANGE_MOEX\",\"positionUid\":\"4cf921a7-0a5d-4e53-a2e2-b92649376ab4\",\"forIisFlag\":true,\"forQualInvestorFlag\":false,\"weekendFlag\":false,\"blockedTcaFlag\":false,\"subordinatedFlag\":false,\"first1MinCandleDate\":{\"seconds\":1659440700,\"nanos\":0},\"first1DayCandleDate\":{\"seconds\":1659423600,\"nanos\":0},\"riskLevel\":\"RISK_LEVEL_UNSPECIFIED\"}";

}