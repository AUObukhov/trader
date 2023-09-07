package ru.obukhov.trader.test.utils.model.share;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

public record TestShare(Share share, ru.tinkoff.piapi.contract.v1.Share tinkoffShare, String jsonString) {

    TestShare(final Share share) {
        this(share, buildTinkoffAccount(share), buildJsonString(share));
    }

    private static ru.tinkoff.piapi.contract.v1.Share buildTinkoffAccount(final Share share) {
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        final MoneyValueMapper moneyValueMapper = Mappers.getMapper(MoneyValueMapper.class);
        final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);
        return ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(share.figi())
                .setTicker(share.ticker())
                .setClassCode(share.classCode())
                .setIsin(share.isin())
                .setLot(share.lot())
                .setCurrency(share.currency())
                .setKlong(quotationMapper.fromBigDecimal(share.klong()))
                .setKshort(quotationMapper.fromBigDecimal(share.kshort()))
                .setDlong(quotationMapper.fromBigDecimal(share.dlong()))
                .setDshort(quotationMapper.fromBigDecimal(share.dshort()))
                .setDlongMin(quotationMapper.fromBigDecimal(share.dlongMin()))
                .setDshortMin(quotationMapper.fromBigDecimal(share.dshortMin()))
                .setShortEnabledFlag(share.shortEnabledFlag())
                .setName(share.name())
                .setExchange(share.exchange())
                .setIpoDate(dateTimeMapper.offsetDateTimeToTimestamp(share.ipoDate()))
                .setIssueSize(share.issueSize())
                .setCountryOfRisk(share.countryOfRisk())
                .setCountryOfRiskName(share.countryOfRiskName())
                .setSector(share.sector())
                .setIssueSizePlan(share.issueSizePlan())
                .setNominal(moneyValueMapper.map(share.nominal(), share.currency()))
                .setTradingStatus(share.tradingStatus())
                .setOtcFlag(share.otcFlag())
                .setBuyAvailableFlag(share.buyAvailableFlag())
                .setSellAvailableFlag(share.sellAvailableFlag())
                .setDivYieldFlag(share.divYieldFlag())
                .setShareType(share.shareType())
                .setMinPriceIncrement(quotationMapper.fromBigDecimal(share.minPriceIncrement()))
                .setApiTradeAvailableFlag(share.apiTradeAvailableFlag())
                .setUid(share.uid())
                .setRealExchange(share.realExchange())
                .setPositionUid(share.positionUid())
                .setForIisFlag(share.forIisFlag())
                .setForQualInvestorFlag(share.forQualInvestorFlag())
                .setWeekendFlag(share.weekendFlag())
                .setBlockedTcaFlag(share.blockedTcaFlag())
                .setFirst1MinCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(share.first1MinCandleDate()))
                .setFirst1DayCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(share.first1DayCandleDate()))
                .build();
    }

    private static String buildJsonString(final Share share) {
        return "{\"figi\":\"" + share.figi() + "\"," +
                "\"ticker\":\"" + share.ticker() + "\"," +
                "\"classCode\":\"" + share.classCode() + "\"," +
                "\"isin\":\"" + share.isin() + "\"," +
                "\"lot\":" + share.lot() + "," +
                "\"currency\":\"" + share.currency() + "\"," +
                "\"klong\":" + share.klong() + "," +
                "\"kshort\":" + share.kshort() + "," +
                "\"dlong\":" + share.dlong() + "," +
                "\"dshort\":" + share.dshort() + "," +
                "\"dlongMin\":" + share.dlongMin() + "," +
                "\"dshortMin\":" + share.dshortMin() + "," +
                "\"shortEnabledFlag\":" + share.shortEnabledFlag() + "," +
                "\"name\":\"" + share.name() + "\"," +
                "\"exchange\":\"" + share.exchange() + "\"," +
                "\"ipoDate\":" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(share.ipoDate()) + "," +
                "\"issueSize\":" + share.issueSize() + "," +
                "\"countryOfRisk\":\"" + share.countryOfRisk() + "\"," +
                "\"countryOfRiskName\":\"" + share.countryOfRiskName() + "\"," +
                "\"sector\":\"" + share.sector() + "\"," +
                "\"issueSizePlan\":" + share.issueSizePlan() + "," +
                "\"nominal\":" + share.nominal() + "," +
                "\"tradingStatus\":\"" + share.tradingStatus() + "\"," +
                "\"otcFlag\":" + share.otcFlag() + "," +
                "\"buyAvailableFlag\":" + share.buyAvailableFlag() + "," +
                "\"sellAvailableFlag\":" + share.sellAvailableFlag() + "," +
                "\"divYieldFlag\":" + share.divYieldFlag() + "," +
                "\"shareType\":\"" + share.shareType() + "\"," +
                "\"minPriceIncrement\":" + share.minPriceIncrement() + "," +
                "\"apiTradeAvailableFlag\":" + share.apiTradeAvailableFlag() + "," +
                "\"uid\":\"" + share.uid() + "\"," +
                "\"realExchange\":\"" + share.realExchange() + "\"," +
                "\"positionUid\":\"" + share.positionUid() + "\"," +
                "\"forIisFlag\":" + share.forIisFlag() + "," +
                "\"forQualInvestorFlag\":" + share.forQualInvestorFlag() + "," +
                "\"weekendFlag\":" + share.weekendFlag() + "," +
                "\"blockedTcaFlag\":" + share.blockedTcaFlag() + "," +
                "\"first1MinCandleDate\":" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(share.first1MinCandleDate()) + "," +
                "\"first1DayCandleDate\":" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(share.first1DayCandleDate()) + "}";
    }

}