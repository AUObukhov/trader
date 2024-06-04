package ru.obukhov.trader.test.utils.model.bond;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.RiskLevel;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestBonds {

    private static final Bond ROSTELECOM_BOND = Bond.builder()
            .figi("BBG00J7HHGH1")
            .ticker("RU000A0ZYG52")
            .classCode("TQCB")
            .isin("RU000A0ZYG52")
            .lot(1)
            .currency(Currencies.RUB)
            .klong(DecimalUtils.setDefaultScale(2))
            .kshort(DecimalUtils.setDefaultScale(2))
            .dlong(DecimalUtils.setDefaultScale(0.3))
            .dshort(DecimalUtils.setDefaultScale(0.3))
            .dlongMin(DecimalUtils.setDefaultScale(0.1633))
            .dshortMin(DecimalUtils.setDefaultScale(0.1402))
            .shortEnabledFlag(false)
            .name("Ростелеком выпуск 3")
            .exchange("MOEX")
            .couponQuantityPerYear(2)
            .maturityDate(DateTimeTestData.newDateTime(2027, 11, 9, 3))
            .nominal(DecimalUtils.setDefaultScale(1000))
            .initialNominal(DecimalUtils.setDefaultScale(1000))
            .stateRegDate(DateTimeTestData.newDateTime(2017, 11, 16, 3))
            .placementDate(DateTimeTestData.newDateTime(2017, 11, 21, 3))
            .placementPrice(DecimalUtils.setDefaultScale(1000))
            .aciValue(DecimalUtils.setDefaultScale(17.9))
            .countryOfRisk("RU")
            .countryOfRiskName("Российская Федерация")
            .sector("telecom")
            .issueKind("documentary")
            .issueSize(10000000L)
            .issueSizePlan(10000000L)
            .tradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .otcFlag(false)
            .buyAvailableFlag(true)
            .sellAvailableFlag(true)
            .floatingCouponFlag(false)
            .perpetualFlag(false)
            .amortizationFlag(false)
            .minPriceIncrement(DecimalUtils.setDefaultScale(0.01))
            .apiTradeAvailableFlag(true)
            .uid("00486cd8-5915-4c0b-8017-b81b9d1805d4")
            .realExchange(RealExchange.REAL_EXCHANGE_MOEX)
            .positionUid("cf3e5dbf-3338-4ee5-96fd-375925e16daa")
            .forIisFlag(true)
            .forQualInvestorFlag(false)
            .weekendFlag(false)
            .blockedTcaFlag(false)
            .subordinatedFlag(false)
            .first1MinCandleDate(DateTimeTestData.newDateTime(2018, 3, 14, 9))
            .first1DayCandleDate(DateTimeTestData.newDateTime(2017, 11, 21, 3))
            .riskLevel(RiskLevel.RISK_LEVEL_LOW)
            .build();

    private static final Bond KAZAKHSTAN_BOND = Bond.builder()
            .figi("TCS00A1050H0")
            .ticker("RU000A1050H0")
            .classCode("TQCB")
            .isin("RU000A1050H0")
            .lot(1)
            .currency("rub")
            .klong(DecimalUtils.ZERO)
            .kshort(DecimalUtils.ZERO)
            .dlong(DecimalUtils.ZERO)
            .dshort(DecimalUtils.ZERO)
            .dlongMin(DecimalUtils.ZERO)
            .dshortMin(DecimalUtils.ZERO)
            .shortEnabledFlag(false)
            .name("ЕАБР")
            .exchange("MOEX")
            .couponQuantityPerYear(2)
            .maturityDate(DateTimeTestData.newDateTime(2025, 7, 29, 3))
            .nominal(DecimalUtils.setDefaultScale(1000))
            .initialNominal(DecimalUtils.setDefaultScale(1000))
            .stateRegDate(DateTimeTestData.newDateTime(2022, 7, 28, 3))
            .placementDate(DateTimeTestData.newDateTime(2022, 8, 2, 3))
            .placementPrice(DecimalUtils.setDefaultScale(1000))
            .aciValue(DecimalUtils.setDefaultScale(45.08))
            .countryOfRisk("KZ")
            .countryOfRiskName("Республика Казахстан")
            .sector("financial")
            .issueKind("non_documentary")
            .issueSize(10000000L)
            .issueSizePlan(10000000L)
            .tradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .otcFlag(false)
            .buyAvailableFlag(true)
            .sellAvailableFlag(true)
            .floatingCouponFlag(false)
            .perpetualFlag(false)
            .amortizationFlag(false)
            .minPriceIncrement(DecimalUtils.setDefaultScale(0.01))
            .apiTradeAvailableFlag(true)
            .uid("cdb57948-f0b0-4420-8dd0-c8747fdc6735")
            .realExchange(RealExchange.REAL_EXCHANGE_MOEX)
            .positionUid("4cf921a7-0a5d-4e53-a2e2-b92649376ab4")
            .forIisFlag(true)
            .forQualInvestorFlag(false)
            .weekendFlag(false)
            .blockedTcaFlag(false)
            .subordinatedFlag(false)
            .first1MinCandleDate(DateTimeTestData.newDateTime(2022, 8, 2, 14, 45))
            .first1DayCandleDate(DateTimeTestData.newDateTime(2022, 8, 2, 10))
            .riskLevel(RiskLevel.RISK_LEVEL_UNSPECIFIED)
            .build();

    public static final TestBond ROSTELECOM = readBond("RU000A0ZYG52.json");
    public static final TestBond KAZAKHSTAN = readBond("RU000A1050H0.json");

    private static TestBond readBond(final String fileName) {
        final Bond bond = ResourceUtils.getResourceAsObject("bonds/" + fileName, Bond.class);
        return new TestBond(bond);
    }

}