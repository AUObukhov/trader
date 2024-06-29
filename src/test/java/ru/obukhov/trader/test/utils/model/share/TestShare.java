package ru.obukhov.trader.test.utils.model.share;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.dividend.TestDividend;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.ShareType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record TestShare(
        Share share,
        ru.tinkoff.piapi.contract.v1.Share tinkoffShare,
        Instrument instrument,
        ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument,
        String jsonString,
        List<TestDividend> dividends,
        Map<CandleInterval, List<HistoricCandle>> candles
) {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final MoneyValueMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyValueMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public TestShare(final Share share, final List<TestDividend> dividends, final Map<CandleInterval, List<HistoricCandle>> candles) {
        this(share, buildTinkoffShare(share), buildInstrument(share), buildTinkoffInstrument(share), buildJsonString(share), dividends, candles);
    }

    private static ru.tinkoff.piapi.contract.v1.Share buildTinkoffShare(final Share share) {
        return ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(share.figi())
                .setTicker(share.ticker())
                .setClassCode(share.classCode())
                .setIsin(share.isin())
                .setLot(share.lot())
                .setCurrency(share.currency())
                .setKlong(mapToQuotation(share.klong()))
                .setKshort(mapToQuotation(share.kshort()))
                .setDlong(mapToQuotation(share.dlong()))
                .setDshort(mapToQuotation(share.dshort()))
                .setDlongMin(mapToQuotation(share.dlongMin()))
                .setDshortMin(mapToQuotation(share.dshortMin()))
                .setShortEnabledFlag(share.shortEnabledFlag())
                .setName(share.name())
                .setExchange(share.exchange())
                .setIpoDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(share.ipoDate()))
                .setIssueSize(share.issueSize())
                .setCountryOfRisk(share.countryOfRisk())
                .setCountryOfRiskName(share.countryOfRiskName())
                .setSector(share.sector())
                .setIssueSizePlan(share.issueSizePlan())
                .setNominal(MONEY_VALUE_MAPPER.map(share.nominal(), share.currency()))
                .setTradingStatus(share.tradingStatus())
                .setOtcFlag(share.otcFlag())
                .setBuyAvailableFlag(share.buyAvailableFlag())
                .setSellAvailableFlag(share.sellAvailableFlag())
                .setDivYieldFlag(share.divYieldFlag())
                .setShareType(share.shareType())
                .setMinPriceIncrement(mapToQuotation(share.minPriceIncrement()))
                .setApiTradeAvailableFlag(share.apiTradeAvailableFlag())
                .setUid(share.uid())
                .setRealExchange(share.realExchange())
                .setPositionUid(share.positionUid())
                .setForIisFlag(share.forIisFlag())
                .setForQualInvestorFlag(share.forQualInvestorFlag())
                .setWeekendFlag(share.weekendFlag())
                .setBlockedTcaFlag(share.blockedTcaFlag())
                .setLiquidityFlag(share.liquidityFlag())
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(share.first1MinCandleDate()))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(share.first1DayCandleDate()))
                .build();
    }

    private static Quotation mapToQuotation(final BigDecimal value) {
        return value == null ? Quotation.getDefaultInstance() : QUOTATION_MAPPER.fromBigDecimal(value);
    }

    private static Instrument buildInstrument(final Share share) {
        return Instrument.builder()
                .figi(share.figi())
                .ticker(share.ticker())
                .classCode(share.classCode())
                .isin(share.isin())
                .lot(share.lot())
                .currency(share.currency())
                .klong(share.klong())
                .kshort(share.kshort())
                .dlong(share.dlong())
                .dshort(share.dshort())
                .dlongMin(share.dlongMin())
                .dshortMin(share.dshortMin())
                .shortEnabledFlag(share.shortEnabledFlag())
                .name(share.name())
                .exchange(share.exchange())
                .countryOfRisk(share.countryOfRisk())
                .countryOfRiskName(share.countryOfRiskName())
                .instrumentType("share")
                .tradingStatus(share.tradingStatus())
                .otcFlag(share.otcFlag())
                .buyAvailableFlag(share.buyAvailableFlag())
                .sellAvailableFlag(share.sellAvailableFlag())
                .minPriceIncrement(share.minPriceIncrement())
                .apiTradeAvailableFlag(share.apiTradeAvailableFlag())
                .uid(share.uid())
                .realExchange(share.realExchange())
                .positionUid(share.positionUid())
                .forIisFlag(share.forIisFlag())
                .forQualInvestorFlag(share.forQualInvestorFlag())
                .weekendFlag(share.weekendFlag())
                .blockedTcaFlag(share.blockedTcaFlag())
                .instrumentKind(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .first1MinCandleDate(share.first1MinCandleDate())
                .first1DayCandleDate(share.first1DayCandleDate())
                .build();
    }

    private static ru.tinkoff.piapi.contract.v1.Instrument buildTinkoffInstrument(final Share share) {
        return ru.tinkoff.piapi.contract.v1.Instrument.newBuilder()
                .setFigi(share.figi())
                .setTicker(share.ticker())
                .setClassCode(share.classCode())
                .setIsin(share.isin())
                .setLot(share.lot())
                .setCurrency(share.currency())
                .setKlong(mapToQuotation(share.klong()))
                .setKshort(mapToQuotation(share.kshort()))
                .setDlong(mapToQuotation(share.dlong()))
                .setDshort(mapToQuotation(share.dshort()))
                .setDlongMin(mapToQuotation(share.dlongMin()))
                .setDshortMin(mapToQuotation(share.dshortMin()))
                .setShortEnabledFlag(share.shortEnabledFlag())
                .setName(share.name())
                .setExchange(share.exchange())
                .setCountryOfRisk(share.countryOfRisk())
                .setCountryOfRiskName(share.countryOfRiskName())
                .setInstrumentType("share")
                .setTradingStatus(share.tradingStatus())
                .setOtcFlag(share.otcFlag())
                .setBuyAvailableFlag(share.buyAvailableFlag())
                .setSellAvailableFlag(share.sellAvailableFlag())
                .setMinPriceIncrement(mapToQuotation(share.minPriceIncrement()))
                .setApiTradeAvailableFlag(share.apiTradeAvailableFlag())
                .setUid(share.uid())
                .setRealExchange(share.realExchange())
                .setPositionUid(share.positionUid())
                .setForIisFlag(share.forIisFlag())
                .setForQualInvestorFlag(share.forQualInvestorFlag())
                .setWeekendFlag(share.weekendFlag())
                .setBlockedTcaFlag(share.blockedTcaFlag())
                .setInstrumentKind(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(share.first1MinCandleDate()))
                .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(share.first1DayCandleDate()))
                .build();
    }

    private static String buildJsonString(final Share share) {
        return "{\"figi\":\"" + share.figi() + "\"," +
                "\"ticker\":\"" + share.ticker() + "\"," +
                "\"classCode\":\"" + share.classCode() + "\"," +
                "\"isin\":\"" + share.isin() + "\"," +
                "\"lot\":" + share.lot() + "," +
                "\"currency\":\"" + share.currency() + "\"," +
                "\"klong\":" + DecimalUtils.toPrettyStringSafe(share.klong()) + "," +
                "\"kshort\":" + DecimalUtils.toPrettyStringSafe(share.kshort()) + "," +
                "\"dlong\":" + DecimalUtils.toPrettyStringSafe(share.dlong()) + "," +
                "\"dshort\":" + DecimalUtils.toPrettyStringSafe(share.dshort()) + "," +
                "\"dlongMin\":" + DecimalUtils.toPrettyStringSafe(share.dlongMin()) + "," +
                "\"dshortMin\":" + DecimalUtils.toPrettyStringSafe(share.dshortMin()) + "," +
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
                "\"minPriceIncrement\":" + DecimalUtils.toPrettyStringSafe(share.minPriceIncrement()) + "," +
                "\"apiTradeAvailableFlag\":" + share.apiTradeAvailableFlag() + "," +
                "\"uid\":\"" + share.uid() + "\"," +
                "\"realExchange\":\"" + share.realExchange() + "\"," +
                "\"positionUid\":\"" + share.positionUid() + "\"," +
                "\"forIisFlag\":" + share.forIisFlag() + "," +
                "\"forQualInvestorFlag\":" + share.forQualInvestorFlag() + "," +
                "\"weekendFlag\":" + share.weekendFlag() + "," +
                "\"blockedTcaFlag\":" + share.blockedTcaFlag() + "," +
                "\"liquidityFlag\":" + share.liquidityFlag() + "," +
                "\"first1MinCandleDate\":" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(share.first1MinCandleDate()) + "," +
                "\"first1DayCandleDate\":" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(share.first1DayCandleDate()) + "}";
    }

    public String getFigi() {
        return share.figi();
    }

    public String getTicker() {
        return share.ticker();
    }

    public Integer getLot() {
        return share.lot();
    }

    public String getCurrency() {
        return share.currency();
    }

    public String getName() {
        return share.name();
    }

    public OffsetDateTime getFirst1MinCandleDate() {
        return share.first1MinCandleDate();
    }

    public OffsetDateTime getFirst1DayCandleDate() {
        return share.first1DayCandleDate();
    }

    public LastPrice getLastPrice() {
        final HistoricCandle lastCandle = candles.get(CandleInterval.CANDLE_INTERVAL_1_MIN).getLast();
        return TestData.newLastPrice(share.figi(), lastCandle.getClose(), lastCandle.getTime());
    }

    public TestShare withForQualInvestorFlag(final boolean forQualInvestorFlag) {
        return new TestShare(share.withForQualInvestorFlag(forQualInvestorFlag), dividends, candles);
    }

    public TestShare withForIisFlag(final boolean forIisFlag) {
        return new TestShare(share.withForIisFlag(forIisFlag), dividends, candles);
    }

    public TestShare withShareType(final ShareType shareType) {
        return new TestShare(share.withShareType(shareType), dividends, candles);
    }

}