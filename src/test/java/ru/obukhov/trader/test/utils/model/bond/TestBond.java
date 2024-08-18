package ru.obukhov.trader.test.utils.model.bond;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

public record TestBond(Bond bond, ru.tinkoff.piapi.contract.v1.Bond tBond) {

    TestBond(final Bond bond) {
        this(bond, buildTBond(bond));
    }

    private static ru.tinkoff.piapi.contract.v1.Bond buildTBond(final Bond bond) {
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
                .setLiquidityFlag(bond.liquidityFlag())
                .setFirst1MinCandleDate(DateTimeTestData.newTimestamp(bond.first1MinCandleDate()))
                .setFirst1DayCandleDate(DateTimeTestData.newTimestamp(bond.first1DayCandleDate()))
                .setRiskLevel(bond.riskLevel())
                .build();
    }

    public String getFigi() {
        return bond.figi();
    }

}