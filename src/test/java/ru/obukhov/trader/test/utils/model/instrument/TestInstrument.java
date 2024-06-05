package ru.obukhov.trader.test.utils.model.instrument;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

import java.time.OffsetDateTime;

public record TestInstrument(Instrument instrument, ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument, String jsonString) {

    TestInstrument(final Instrument instrument) {
        this(instrument, buildTinkoffInstrument(instrument), buildJsonString(instrument));
    }

    private static ru.tinkoff.piapi.contract.v1.Instrument buildTinkoffInstrument(final Instrument instrument) {
        final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);

        return ru.tinkoff.piapi.contract.v1.Instrument.newBuilder()
                .setFigi(instrument.figi())
                .setTicker(instrument.ticker())
                .setClassCode(instrument.classCode())
                .setIsin(instrument.isin())
                .setLot(instrument.lot())
                .setCurrency(instrument.currency())
                .setKlong(quotationMapper.fromBigDecimal(instrument.klong()))
                .setKshort(quotationMapper.fromBigDecimal(instrument.kshort()))
                .setDlong(quotationMapper.fromBigDecimal(instrument.dlong()))
                .setDshort(quotationMapper.fromBigDecimal(instrument.dshort()))
                .setDlongMin(quotationMapper.fromBigDecimal(instrument.dlongMin()))
                .setDshortMin(quotationMapper.fromBigDecimal(instrument.dshortMin()))
                .setShortEnabledFlag(instrument.shortEnabledFlag())
                .setName(instrument.name())
                .setExchange(instrument.exchange())
                .setCountryOfRisk(instrument.countryOfRisk())
                .setCountryOfRiskName(instrument.countryOfRiskName())
                .setInstrumentType(instrument.instrumentType())
                .setTradingStatus(instrument.tradingStatus())
                .setOtcFlag(instrument.otcFlag())
                .setBuyAvailableFlag(instrument.buyAvailableFlag())
                .setSellAvailableFlag(instrument.sellAvailableFlag())
                .setMinPriceIncrement(quotationMapper.fromBigDecimal(instrument.minPriceIncrement()))
                .setApiTradeAvailableFlag(instrument.apiTradeAvailableFlag())
                .setUid(instrument.uid())
                .setRealExchange(instrument.realExchange())
                .setPositionUid(instrument.positionUid())
                .setForIisFlag(instrument.forIisFlag())
                .setForQualInvestorFlag(instrument.forQualInvestorFlag())
                .setWeekendFlag(instrument.weekendFlag())
                .setBlockedTcaFlag(instrument.blockedTcaFlag())
                .setInstrumentKind(instrument.instrumentKind())
                .setFirst1MinCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(instrument.first1MinCandleDate()))
                .setFirst1DayCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(instrument.first1DayCandleDate()))
                .build();
    }

    private static String buildJsonString(final Instrument instrument) {
        return "{\"figi\":\"" + instrument.figi() + "\"," +
                "\"ticker\":\"" + instrument.ticker() + "\"," +
                "\"classCode\":\"" + instrument.classCode() + "\"," +
                "\"isin\":\"" + instrument.isin() + "\"," +
                "\"lot\":" + instrument.lot() + "," +
                "\"currency\":\"" + instrument.currency() + "\"," +
                "\"klong\":" + instrument.klong() + "," +
                "\"kshort\":" + instrument.kshort() + "," +
                "\"dlong\":" + instrument.dlong() + "," +
                "\"dshort\":" + instrument.dshort() + "," +
                "\"dlongMin\":" + instrument.dlongMin() + "," +
                "\"dshortMin\":" + instrument.dshortMin() + "," +
                "\"shortEnabledFlag\":" + instrument.shortEnabledFlag() + "," +
                "\"name\":\"" + instrument.name() + "\"," +
                "\"exchange\":\"" + instrument.exchange() + "\"," +
                "\"countryOfRisk\":\"" + instrument.countryOfRisk() + "\"," +
                "\"countryOfRiskName\":\"" + instrument.countryOfRiskName() + "\"," +
                "\"instrumentType\":\"" + instrument.instrumentType() + "\"," +
                "\"tradingStatus\":\"" + instrument.tradingStatus() + "\"," +
                "\"otcFlag\":" + instrument.otcFlag() + "," +
                "\"buyAvailableFlag\":" + instrument.buyAvailableFlag() + "," +
                "\"sellAvailableFlag\":" + instrument.sellAvailableFlag() + "," +
                "\"minPriceIncrement\":" + instrument.minPriceIncrement() + "," +
                "\"apiTradeAvailableFlag\":" + instrument.apiTradeAvailableFlag() + "," +
                "\"uid\":\"" + instrument.uid() + "\"," +
                "\"realExchange\":\"" + instrument.realExchange() + "\"," +
                "\"positionUid\":\"" + instrument.positionUid() + "\"," +
                "\"forIisFlag\":" + instrument.forIisFlag() + "," +
                "\"forQualInvestorFlag\":" + instrument.forQualInvestorFlag() + "," +
                "\"weekendFlag\":" + instrument.weekendFlag() + "," +
                "\"blockedTcaFlag\":" + instrument.blockedTcaFlag() + "," +
                "\"instrumentKind\":\"" + instrument.instrumentKind() + "\"," +
                "\"first1MinCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(instrument.first1MinCandleDate()) + "\"," +
                "\"first1DayCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(instrument.first1DayCandleDate()) + "\"}";
    }

    public String getFigi() {
        return instrument.figi();
    }

    public OffsetDateTime getFirst1MinCandleDate() {
        return instrument.first1MinCandleDate();
    }

    public OffsetDateTime getFirst1DayCandleDate() {
        return instrument.first1DayCandleDate();
    }

}