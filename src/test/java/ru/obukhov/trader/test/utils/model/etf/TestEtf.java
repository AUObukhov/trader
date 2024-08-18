package ru.obukhov.trader.test.utils.model.etf;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

public record TestEtf(Etf etf, ru.tinkoff.piapi.contract.v1.Etf tEtf) {

    TestEtf(final Etf etf) {
        this(etf, buildTEtf(etf));
    }

    private static ru.tinkoff.piapi.contract.v1.Etf buildTEtf(final Etf etf) {
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);

        return ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
                .setFigi(etf.figi())
                .setTicker(etf.ticker())
                .setClassCode(etf.classCode())
                .setIsin(etf.isin())
                .setLot(etf.lot())
                .setCurrency(etf.currency())
                .setKlong(quotationMapper.fromBigDecimal(etf.klong()))
                .setKshort(quotationMapper.fromBigDecimal(etf.kshort()))
                .setDlong(quotationMapper.fromBigDecimal(etf.dlong()))
                .setDshort(quotationMapper.fromBigDecimal(etf.dshort()))
                .setDlongMin(quotationMapper.fromBigDecimal(etf.dlongMin()))
                .setDshortMin(quotationMapper.fromBigDecimal(etf.dshortMin()))
                .setShortEnabledFlag(etf.shortEnabledFlag())
                .setName(etf.name())
                .setExchange(etf.exchange())
                .setFixedCommission(quotationMapper.fromBigDecimal(etf.fixedCommission()))
                .setFocusType(etf.focusType())
                .setReleasedDate(dateTimeMapper.offsetDateTimeToTimestamp(etf.releasedDate()))
                .setNumShares(quotationMapper.fromBigDecimal(etf.numShares()))
                .setCountryOfRisk(etf.countryOfRisk())
                .setCountryOfRiskName(etf.countryOfRiskName())
                .setSector(etf.sector())
                .setRebalancingFreq(etf.rebalancingFreq())
                .setTradingStatus(etf.tradingStatus())
                .setOtcFlag(etf.otcFlag())
                .setBuyAvailableFlag(etf.buyAvailableFlag())
                .setSellAvailableFlag(etf.sellAvailableFlag())
                .setMinPriceIncrement(quotationMapper.fromBigDecimal(etf.minPriceIncrement()))
                .setApiTradeAvailableFlag(etf.apiTradeAvailableFlag())
                .setUid(etf.uid())
                .setRealExchange(etf.realExchange())
                .setPositionUid(etf.positionUid())
                .setForIisFlag(etf.forIisFlag())
                .setForQualInvestorFlag(etf.forQualInvestorFlag())
                .setWeekendFlag(etf.weekendFlag())
                .setBlockedTcaFlag(etf.blockedTcaFlag())
                .setLiquidityFlag(etf.liquidityFlag())
                .setFirst1MinCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(etf.first1MinCandleDate()))
                .setFirst1DayCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(etf.first1DayCandleDate()))
                .build();
    }

    public String getFigi() {
        return etf.figi();
    }

}