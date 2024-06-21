package ru.obukhov.trader.test.utils.model.bond;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

public record TestBond(Bond bond, ru.tinkoff.piapi.contract.v1.Bond tinkoffBond, String jsonString) {

    TestBond(final Bond bond) {
        this(bond, buildTinkoffBond(bond), buildJsonString(bond));
    }

    private static ru.tinkoff.piapi.contract.v1.Bond buildTinkoffBond(final Bond bond) {
        final MoneyValueMapper moneyValueMapper = Mappers.getMapper(MoneyValueMapper.class);
        final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);
        return ru.tinkoff.piapi.contract.v1.Bond.newBuilder()
                .setFigi(bond.figi())
                .setTicker(bond.ticker())
                .setClassCode(bond.classCode())
                .setIsin(bond.isin())
                .setLot(bond.lot())
                .setCurrency(bond.currency())
                .setKlong(quotationMapper.fromBigDecimal(bond.klong()))
                .setKshort(quotationMapper.fromBigDecimal(bond.kshort()))
                .setDlong(quotationMapper.fromBigDecimal(bond.dlong()))
                .setDshort(quotationMapper.fromBigDecimal(bond.dshort()))
                .setDlongMin(quotationMapper.fromBigDecimal(bond.dlongMin()))
                .setDshortMin(quotationMapper.fromBigDecimal(bond.dshortMin()))
                .setShortEnabledFlag(bond.shortEnabledFlag())
                .setName(bond.name())
                .setExchange(bond.exchange())
                .setCouponQuantityPerYear(bond.couponQuantityPerYear())
                .setMaturityDate(DateTimeTestData.newTimestamp(bond.maturityDate()))
                .setNominal(moneyValueMapper.map(bond.nominal(), bond.currency()))
                .setInitialNominal(moneyValueMapper.map(bond.initialNominal(), bond.currency()))
                .setStateRegDate(DateTimeTestData.newTimestamp(bond.stateRegDate()))
                .setPlacementDate(DateTimeTestData.newTimestamp(bond.placementDate()))
                .setPlacementPrice(moneyValueMapper.map(bond.placementPrice(), bond.currency()))
                .setAciValue(moneyValueMapper.map(bond.aciValue(), bond.currency()))
                .setCountryOfRisk(bond.countryOfRisk())
                .setCountryOfRiskName(bond.countryOfRiskName())
                .setSector(bond.sector())
                .setIssueKind(bond.issueKind())
                .setIssueSize(bond.issueSize())
                .setIssueSizePlan(bond.issueSizePlan())
                .setTradingStatus(bond.tradingStatus())
                .setOtcFlag(bond.otcFlag())
                .setBuyAvailableFlag(bond.buyAvailableFlag())
                .setSellAvailableFlag(bond.sellAvailableFlag())
                .setFloatingCouponFlag(bond.floatingCouponFlag())
                .setPerpetualFlag(bond.perpetualFlag())
                .setAmortizationFlag(bond.amortizationFlag())
                .setMinPriceIncrement(quotationMapper.fromBigDecimal(bond.minPriceIncrement()))
                .setApiTradeAvailableFlag(bond.apiTradeAvailableFlag())
                .setUid(bond.uid())
                .setRealExchange(bond.realExchange())
                .setPositionUid(bond.positionUid())
                .setForIisFlag(bond.forIisFlag())
                .setForQualInvestorFlag(bond.forQualInvestorFlag())
                .setWeekendFlag(bond.weekendFlag())
                .setBlockedTcaFlag(bond.blockedTcaFlag())
                .setSubordinatedFlag(bond.subordinatedFlag())
                .setFirst1MinCandleDate(DateTimeTestData.newTimestamp(bond.first1MinCandleDate()))
                .setFirst1DayCandleDate(DateTimeTestData.newTimestamp(bond.first1DayCandleDate()))
                .setRiskLevel(bond.riskLevel())
                .build();
    }

    private static String buildJsonString(final Bond bond) {
        return "{\"figi\":\"" + bond.figi() + "\"," +
                "\"ticker\":\"" + bond.ticker() + "\"," +
                "\"classCode\":\"" + bond.classCode() + "\"," +
                "\"isin\":\"" + bond.isin() + "\"," +
                "\"lot\":" + bond.lot() + "," +
                "\"currency\":\"" + bond.currency() + "\"," +
                "\"klong\":" + DecimalUtils.toPrettyStringSafe(bond.klong()) + "," +
                "\"kshort\":" + DecimalUtils.toPrettyStringSafe(bond.kshort()) + "," +
                "\"dlong\":" + DecimalUtils.toPrettyStringSafe(bond.dlong()) + "," +
                "\"dshort\":" + DecimalUtils.toPrettyStringSafe(bond.dshort()) + "," +
                "\"dlongMin\":" + DecimalUtils.toPrettyStringSafe(bond.dlongMin()) + "," +
                "\"dshortMin\":" + DecimalUtils.toPrettyStringSafe(bond.dshortMin()) + "," +
                "\"shortEnabledFlag\":" + bond.shortEnabledFlag() + "," +
                "\"name\":\"" + bond.name() + "\"," +
                "\"exchange\":\"" + bond.exchange() + "\"," +
                "\"couponQuantityPerYear\":" + bond.couponQuantityPerYear() + "," +
                "\"maturityDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(bond.maturityDate()) + "\"," +
                "\"nominal\":" + bond.nominal() + "," +
                "\"initialNominal\":" + bond.initialNominal() + "," +
                "\"stateRegDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(bond.stateRegDate()) + "\"," +
                "\"placementDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(bond.placementDate()) + "\"," +
                "\"placementPrice\":" + bond.placementPrice() + "," +
                "\"aciValue\":" + bond.aciValue() + "," +
                "\"countryOfRisk\":\"" + bond.countryOfRisk() + "\"," +
                "\"countryOfRiskName\":\"" + bond.countryOfRiskName() + "\"," +
                "\"sector\":\"" + bond.sector() + "\"," +
                "\"issueKind\":\"" + bond.issueKind() + "\"," +
                "\"issueSize\":" + bond.issueSize() + "," +
                "\"issueSizePlan\":" + bond.issueSizePlan() + "," +
                "\"tradingStatus\":\"" + bond.tradingStatus() + "\"," +
                "\"otcFlag\":" + bond.otcFlag() + "," +
                "\"buyAvailableFlag\":" + bond.buyAvailableFlag() + "," +
                "\"sellAvailableFlag\":" + bond.sellAvailableFlag() + "," +
                "\"floatingCouponFlag\":" + bond.floatingCouponFlag() + "," +
                "\"perpetualFlag\":" + bond.perpetualFlag() + "," +
                "\"amortizationFlag\":" + bond.amortizationFlag() + "," +
                "\"minPriceIncrement\":" + DecimalUtils.toPrettyStringSafe(bond.minPriceIncrement()) + "," +
                "\"apiTradeAvailableFlag\":" + bond.apiTradeAvailableFlag() + "," +
                "\"uid\":\"" + bond.uid() + "\"," +
                "\"realExchange\":\"" + bond.realExchange() + "\"," +
                "\"positionUid\":\"" + bond.positionUid() + "\"," +
                "\"forIisFlag\":" + bond.forIisFlag() + "," +
                "\"forQualInvestorFlag\":" + bond.forQualInvestorFlag() + "," +
                "\"weekendFlag\":" + bond.weekendFlag() + "," +
                "\"blockedTcaFlag\":" + bond.blockedTcaFlag() + "," +
                "\"subordinatedFlag\":" + bond.subordinatedFlag() + "," +
                "\"first1MinCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(bond.first1MinCandleDate()) + "\"," +
                "\"first1DayCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(bond.first1DayCandleDate()) + "\"," +
                "\"riskLevel\":\"" + bond.riskLevel() + "\"}";
    }

    public String getFigi() {
        return bond.figi();
    }

}