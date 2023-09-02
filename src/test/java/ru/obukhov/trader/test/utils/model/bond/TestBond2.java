package ru.obukhov.trader.test.utils.model.bond;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.RiskLevel;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestBond2 {

    private static final MoneyValueMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyValueMapper.class);

    public static final String FIGI = "TCS00A1050H0";
    public static final String TICKER = "RU000A1050H0";
    public static final String CLASS_CODE = "TQCB";
    public static final String ISIN = "RU000A1050H0";
    public static final int LOT = 1;
    public static final String CURRENCY = Currencies.RUB;
    public static final Quotation KLONG = QuotationUtils.ZERO;
    public static final Quotation KSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG = QuotationUtils.ZERO;
    public static final Quotation DSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG_MIN = QuotationUtils.ZERO;
    public static final Quotation DSHORT_MIN = QuotationUtils.ZERO;
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "ЕАБР";
    public static final String EXCHANGE = "MOEX";
    public static final int COUPON_QUANTITY_PER_YEAR = 2;
    public static final OffsetDateTime MATURITY_DATE = DateTimeTestData.createDateTime(2025, 7, 29, 3);
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal INITIAL_NOMINAL = DecimalUtils.setDefaultScale(1000);
    public static final OffsetDateTime STATE_REG_DATE = DateTimeTestData.createDateTime(2022, 7, 28, 3);
    public static final OffsetDateTime PLACEMENT_DATE = DateTimeTestData.createDateTime(2022, 8, 2, 3);
    public static final BigDecimal PLACEMENT_PRICE = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal ACI_VALUE = DecimalUtils.setDefaultScale(45.08);
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
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "cdb57948-f0b0-4420-8dd0-c8747fdc6735";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "4cf921a7-0a5d-4e53-a2e2-b92649376ab4";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final boolean SUBORDINATED_FLAG = false;

    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2022, 8, 2, 14, 45);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2022, 8, 2, 10);
    public static final RiskLevel RISK_LEVEL = RiskLevel.RISK_LEVEL_UNSPECIFIED;

    public static final Bond BOND = Bond.builder()
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
            .couponQuantityPerYear(COUPON_QUANTITY_PER_YEAR)
            .maturityDate(MATURITY_DATE)
            .nominal(NOMINAL)
            .initialNominal(INITIAL_NOMINAL)
            .stateRegDate(STATE_REG_DATE)
            .placementDate(PLACEMENT_DATE)
            .placementPrice(PLACEMENT_PRICE)
            .aciValue(ACI_VALUE)
            .countryOfRisk(COUNTRY_OF_RISK)
            .countryOfRiskName(COUNTRY_OF_RISK_NAME)
            .sector(SECTOR)
            .issueKind(ISSUE_KIND)
            .issueSize(ISSUE_SIZE)
            .issueSizePlan(ISSUE_SIZE_PLAN)
            .tradingStatus(TRADING_STATUS)
            .otcFlag(OTC_FLAG)
            .buyAvailableFlag(BUY_AVAILABLE_FLAG)
            .sellAvailableFlag(SELL_AVAILABLE_FLAG)
            .floatingCouponFlag(FLOATING_COUPON_FLAG)
            .perpetualFlag(PERPETUAL_FLAG)
            .amortizationFlag(AMORTIZATION_FLAG)
            .minPriceIncrement(MIN_PRICE_INCREMENT)
            .apiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .uid(UID)
            .realExchange(REAL_EXCHANGE)
            .positionUid(POSITION_UID)
            .forIisFlag(FOR_IIS_FLAG)
            .forQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .weekendFlag(WEEKEND_FLAG)
            .blockedTcaFlag(BLOCKED_TCA_FLAG)
            .subordinatedFlag(SUBORDINATED_FLAG)
            .first1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .riskLevel(RISK_LEVEL)
            .build();

    public static final ru.tinkoff.piapi.contract.v1.Bond TINKOFF_BOND = ru.tinkoff.piapi.contract.v1.Bond.newBuilder()
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
            .setMaturityDate(TimestampUtils.newTimestamp(MATURITY_DATE))
            .setNominal(MONEY_VALUE_MAPPER.map(NOMINAL, CURRENCY))
            .setInitialNominal(MONEY_VALUE_MAPPER.map(INITIAL_NOMINAL, CURRENCY))
            .setStateRegDate(TimestampUtils.newTimestamp(STATE_REG_DATE))
            .setPlacementDate(TimestampUtils.newTimestamp(PLACEMENT_DATE))
            .setPlacementPrice(MONEY_VALUE_MAPPER.map(PLACEMENT_PRICE, CURRENCY))
            .setAciValue(MONEY_VALUE_MAPPER.map(ACI_VALUE, CURRENCY))
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
            .setFirst1MinCandleDate(TimestampUtils.newTimestamp(FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(TimestampUtils.newTimestamp(FIRST_1_DAY_CANDLE_DATE))
            .setRiskLevel(RISK_LEVEL)
            .build();

    public static final String JSON_STRING = "{\"figi\":\"TCS00A1050H0\"," +
            "\"ticker\":\"RU000A1050H0\"," +
            "\"classCode\":\"TQCB\"," +
            "\"isin\":\"RU000A1050H0\"," +
            "\"lot\":1," +
            "\"currency\":\"rub\"," +
            "\"klong\":0," +
            "\"kshort\":0," +
            "\"dlong\":0," +
            "\"dshort\":0," +
            "\"dlongMin\":0," +
            "\"dshortMin\":0," +
            "\"shortEnabledFlag\":false," +
            "\"name\":\"ЕАБР\"," +
            "\"exchange\":\"MOEX\"," +
            "\"couponQuantityPerYear\":2," +
            "\"maturityDate\":\"2025-07-29T03:00:00+03:00\"," +
            "\"nominal\":1000.000000000," +
            "\"initialNominal\":1000.000000000," +
            "\"stateRegDate\":\"2022-07-28T03:00:00+03:00\"," +
            "\"placementDate\":\"2022-08-02T03:00:00+03:00\"," +
            "\"placementPrice\":1000.000000000," +
            "\"aciValue\":45.080000000," +
            "\"countryOfRisk\":\"KZ\"," +
            "\"countryOfRiskName\":\"Республика Казахстан\"," +
            "\"sector\":\"financial\"," +
            "\"issueKind\":\"non_documentary\"," +
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
            "\"uid\":\"cdb57948-f0b0-4420-8dd0-c8747fdc6735\"," +
            "\"realExchange\":\"REAL_EXCHANGE_MOEX\"," +
            "\"positionUid\":\"4cf921a7-0a5d-4e53-a2e2-b92649376ab4\"," +
            "\"forIisFlag\":true," +
            "\"forQualInvestorFlag\":false," +
            "\"weekendFlag\":false," +
            "\"blockedTcaFlag\":false," +
            "\"subordinatedFlag\":false," +
            "\"first1MinCandleDate\":\"2022-08-02T14:45:00+03:00\"," +
            "\"first1DayCandleDate\":\"2022-08-02T10:00:00+03:00\"," +
            "\"riskLevel\":\"RISK_LEVEL_UNSPECIFIED\"}";

}