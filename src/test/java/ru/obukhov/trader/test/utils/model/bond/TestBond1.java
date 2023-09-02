package ru.obukhov.trader.test.utils.model.bond;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.RiskLevel;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestBond1 {

    private static final MoneyValueMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyValueMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public static final String FIGI = "BBG00J7HHGH1";
    public static final String TICKER = "RU000A0ZYG52";
    public static final String CLASS_CODE = "TQCB";
    public static final String ISIN = "RU000A0ZYG52";
    public static final int LOT = 1;
    public static final String CURRENCY = Currencies.RUB;
    public static final BigDecimal KLONG = DecimalUtils.setDefaultScale(2);
    public static final BigDecimal KSHORT = DecimalUtils.setDefaultScale(2);
    public static final BigDecimal DLONG = DecimalUtils.setDefaultScale(0.3);
    public static final BigDecimal DSHORT = DecimalUtils.setDefaultScale(0.3);
    public static final BigDecimal DLONG_MIN = DecimalUtils.setDefaultScale(0.1633);
    public static final BigDecimal DSHORT_MIN = DecimalUtils.setDefaultScale(0.1402);
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "Ростелеком выпуск 3";
    public static final String EXCHANGE = "MOEX";
    public static final int COUPON_QUANTITY_PER_YEAR = 2;
    public static final OffsetDateTime MATURITY_DATE = DateTimeTestData.createDateTime(2027, 11, 9, 3);
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal INITIAL_NOMINAL = DecimalUtils.setDefaultScale(1000);
    public static final OffsetDateTime STATE_REG_DATE = DateTimeTestData.createDateTime(2017, 11, 16, 3);
    public static final OffsetDateTime PLACEMENT_DATE = DateTimeTestData.createDateTime(2017, 11, 21, 3);
    public static final BigDecimal PLACEMENT_PRICE = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal ACI_VALUE = DecimalUtils.setDefaultScale(17.9);
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
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "00486cd8-5915-4c0b-8017-b81b9d1805d4";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "cf3e5dbf-3338-4ee5-96fd-375925e16daa";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final boolean SUBORDINATED_FLAG = false;

    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 3, 14, 9);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2017, 11, 21, 3);
    public static final RiskLevel RISK_LEVEL = RiskLevel.RISK_LEVEL_LOW;

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
            .setKlong(QUOTATION_MAPPER.fromBigDecimal(KLONG))
            .setKshort(QUOTATION_MAPPER.fromBigDecimal(KSHORT))
            .setDlong(QUOTATION_MAPPER.fromBigDecimal(DLONG))
            .setDshort(QUOTATION_MAPPER.fromBigDecimal(DSHORT))
            .setDlongMin(QUOTATION_MAPPER.fromBigDecimal(DLONG_MIN))
            .setDshortMin(QUOTATION_MAPPER.fromBigDecimal(DSHORT_MIN))
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
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(MIN_PRICE_INCREMENT))
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

}