package ru.obukhov.trader.test.utils.model.etf;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

public record TestEtf(Etf etf, ru.tinkoff.piapi.contract.v1.Etf tinkoffEtf, String jsonString) {

    TestEtf(final Etf etf) {
        this(etf, buildTinkoffEtf(etf), buildJsonString(etf));
    }

    private static ru.tinkoff.piapi.contract.v1.Etf buildTinkoffEtf(final Etf etf) {
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
                .setFirst1MinCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(etf.first1MinCandleDate()))
                .setFirst1DayCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(etf.first1DayCandleDate()))
                .build();
    }

    private static String buildJsonString(final Etf etf) {
        return "{\"figi\":\"" + etf.figi() + "\"," +
                "\"ticker\":\"" + etf.ticker() + "\"," +
                "\"classCode\":\"" + etf.classCode() + "\"," +
                "\"isin\":\"" + etf.isin() + "\"," +
                "\"lot\":" + etf.lot() + "," +
                "\"currency\":\"" + etf.currency() + "\"," +
                "\"klong\":" + DecimalUtils.toPrettyStringSafe(etf.klong()) + "," +
                "\"kshort\":" + DecimalUtils.toPrettyStringSafe(etf.kshort()) + "," +
                "\"dlong\":" + DecimalUtils.toPrettyStringSafe(etf.dlong()) + "," +
                "\"dshort\":" + DecimalUtils.toPrettyStringSafe(etf.dshort()) + "," +
                "\"dlongMin\":" + DecimalUtils.toPrettyStringSafe(etf.dlongMin()) + "," +
                "\"dshortMin\":" + DecimalUtils.toPrettyStringSafe(etf.dshortMin()) + "," +
                "\"shortEnabledFlag\":" + etf.shortEnabledFlag() + "," +
                "\"name\":\"" + etf.name() + "\"," +
                "\"exchange\":\"" + etf.exchange() + "\"," +
                "\"fixedCommission\":" + etf.fixedCommission() + "," +
                "\"focusType\":\"" + etf.focusType() + "\"," +
                "\"releasedDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(etf.releasedDate()) + "\"," +
                "\"numShares\":" + etf.numShares() + "," +
                "\"countryOfRisk\":\"" + etf.countryOfRisk() + "\"," +
                "\"countryOfRiskName\":\"" + etf.countryOfRiskName() + "\"," +
                "\"sector\":\"" + etf.sector() + "\"," +
                "\"rebalancingFreq\":\"" + etf.rebalancingFreq() + "\"," +
                "\"tradingStatus\":\"" + etf.tradingStatus() + "\"," +
                "\"otcFlag\":" + etf.otcFlag() + "," +
                "\"buyAvailableFlag\":" + etf.buyAvailableFlag() + "," +
                "\"sellAvailableFlag\":" + etf.sellAvailableFlag() + "," +
                "\"minPriceIncrement\":" + DecimalUtils.toPrettyStringSafe(etf.minPriceIncrement()) + "," +
                "\"apiTradeAvailableFlag\":" + etf.apiTradeAvailableFlag() + "," +
                "\"uid\":\"" + etf.uid() + "\"," +
                "\"realExchange\":\"" + etf.realExchange() + "\"," +
                "\"positionUid\":\"" + etf.positionUid() + "\"," +
                "\"forIisFlag\":" + etf.forIisFlag() + "," +
                "\"forQualInvestorFlag\":" + etf.forQualInvestorFlag() + "," +
                "\"weekendFlag\":" + etf.weekendFlag() + "," +
                "\"blockedTcaFlag\":" + etf.blockedTcaFlag() + "," +
                "\"first1MinCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(etf.first1MinCandleDate()) + "\"," +
                "\"first1DayCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(etf.first1DayCandleDate()) + "\"}";
    }

    public String getFigi() {
        return etf.figi();
    }

}