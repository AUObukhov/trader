package ru.obukhov.trader.test.utils.model.currency;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.InstrumentType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record TestCurrency(
        Currency currency,
        ru.tinkoff.piapi.contract.v1.Currency tinkoffCurrency,
        ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument,
        String jsonString,
        Map<CandleInterval, List<HistoricCandle>> candles
) {

    TestCurrency(final Currency currency, final Map<CandleInterval, List<HistoricCandle>> candles) {
        this(currency, buildTinkoffCurrency(currency), buildTinkoffInstrument(currency), buildJsonString(currency), candles);
    }

    private static ru.tinkoff.piapi.contract.v1.Currency buildTinkoffCurrency(final Currency currency) {
        final MoneyValueMapper moneyValueMapper = Mappers.getMapper(MoneyValueMapper.class);
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);
        return ru.tinkoff.piapi.contract.v1.Currency.newBuilder()
                .setFigi(currency.figi())
                .setTicker(currency.ticker())
                .setClassCode(currency.classCode())
                .setIsin(currency.isin())
                .setLot(currency.lot())
                .setCurrency(currency.currency())
                .setKlong(quotationMapper.fromBigDecimal(currency.klong()))
                .setKshort(quotationMapper.fromBigDecimal(currency.kshort()))
                .setDlong(quotationMapper.fromBigDecimal(currency.dlong()))
                .setDshort(quotationMapper.fromBigDecimal(currency.dshort()))
                .setDlongMin(quotationMapper.fromBigDecimal(currency.dlongMin()))
                .setDshortMin(quotationMapper.fromBigDecimal(currency.dshortMin()))
                .setShortEnabledFlag(currency.shortEnabledFlag())
                .setName(currency.name())
                .setExchange(currency.exchange())
                .setNominal(moneyValueMapper.map(currency.nominal(), currency.isoCurrencyName()))
                .setCountryOfRisk(currency.countryOfRisk())
                .setCountryOfRiskName(currency.countryOfRiskName())
                .setTradingStatus(currency.tradingStatus())
                .setOtcFlag(currency.otcFlag())
                .setBuyAvailableFlag(currency.buyAvailableFlag())
                .setSellAvailableFlag(currency.sellAvailableFlag())
                .setIsoCurrencyName(currency.isoCurrencyName())
                .setMinPriceIncrement(quotationMapper.fromBigDecimal(currency.minPriceIncrement()))
                .setApiTradeAvailableFlag(currency.apiTradeAvailableFlag())
                .setUid(currency.uid())
                .setRealExchange(currency.realExchange())
                .setPositionUid(currency.positionUid())
                .setForIisFlag(currency.forIisFlag())
                .setForQualInvestorFlag(currency.forQualInvestorFlag())
                .setWeekendFlag(currency.weekendFlag())
                .setBlockedTcaFlag(currency.blockedTcaFlag())
                .setFirst1MinCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(currency.first1MinCandleDate()))
                .setFirst1DayCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(currency.first1DayCandleDate()))
                .build();
    }

    private static ru.tinkoff.piapi.contract.v1.Instrument buildTinkoffInstrument(final Currency currency) {
        final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);

        return ru.tinkoff.piapi.contract.v1.Instrument.newBuilder()
                .setFigi(currency.figi())
                .setTicker(currency.ticker())
                .setClassCode(currency.classCode())
                .setIsin(currency.isin())
                .setLot(currency.lot())
                .setCurrency(currency.currency())
                .setKlong(quotationMapper.fromBigDecimal(currency.klong()))
                .setKshort(quotationMapper.fromBigDecimal(currency.kshort()))
                .setDlong(quotationMapper.fromBigDecimal(currency.dlong()))
                .setDshort(quotationMapper.fromBigDecimal(currency.dshort()))
                .setDlongMin(quotationMapper.fromBigDecimal(currency.dlongMin()))
                .setDshortMin(quotationMapper.fromBigDecimal(currency.dshortMin()))
                .setShortEnabledFlag(currency.shortEnabledFlag())
                .setName(currency.name())
                .setExchange(currency.exchange())
                .setCountryOfRisk(currency.countryOfRisk())
                .setCountryOfRiskName(currency.countryOfRiskName())
                .setInstrumentType("currency")
                .setTradingStatus(currency.tradingStatus())
                .setOtcFlag(currency.otcFlag())
                .setBuyAvailableFlag(currency.buyAvailableFlag())
                .setSellAvailableFlag(currency.sellAvailableFlag())
                .setMinPriceIncrement(quotationMapper.fromBigDecimal(currency.minPriceIncrement()))
                .setApiTradeAvailableFlag(currency.apiTradeAvailableFlag())
                .setUid(currency.uid())
                .setRealExchange(currency.realExchange())
                .setPositionUid(currency.positionUid())
                .setForIisFlag(currency.forIisFlag())
                .setForQualInvestorFlag(currency.forQualInvestorFlag())
                .setWeekendFlag(currency.weekendFlag())
                .setBlockedTcaFlag(currency.blockedTcaFlag())
                .setInstrumentKind(InstrumentType.INSTRUMENT_TYPE_SHARE)
                .setFirst1MinCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(currency.first1MinCandleDate()))
                .setFirst1DayCandleDate(dateTimeMapper.offsetDateTimeToTimestamp(currency.first1DayCandleDate()))
                .build();
    }

    private static String buildJsonString(final Currency currency) {
        return "{" +
                "\"figi\":\"" + currency.figi() + "\"," +
                "\"ticker\":\"" + currency.ticker() + "\"," +
                "\"classCode\":\"" + currency.classCode() + "\"," +
                "\"isin\":\"" + currency.isin() + "\"," +
                "\"lot\":" + currency.lot() + "," +
                "\"currency\":\"" + currency.currency() + "\"," +
                "\"klong\":" + DecimalUtils.toPrettyStringSafe(currency.klong()) + "," +
                "\"kshort\":" + DecimalUtils.toPrettyStringSafe(currency.kshort()) + "," +
                "\"dlong\":" + DecimalUtils.toPrettyStringSafe(currency.dlong()) + "," +
                "\"dshort\":" + DecimalUtils.toPrettyStringSafe(currency.dshort()) + "," +
                "\"dlongMin\":" + DecimalUtils.toPrettyStringSafe(currency.dlongMin()) + "," +
                "\"dshortMin\":" + DecimalUtils.toPrettyStringSafe(currency.dshortMin()) + "," +
                "\"shortEnabledFlag\":" + currency.shortEnabledFlag() + "," +
                "\"name\":\"" + currency.name() + "\"," +
                "\"exchange\":\"" + currency.exchange() + "\"," +
                "\"nominal\":" + currency.nominal() + "," +
                "\"countryOfRisk\":\"" + currency.countryOfRisk() + "\"," +
                "\"countryOfRiskName\":\"" + currency.countryOfRiskName() + "\"," +
                "\"tradingStatus\":\"" + currency.tradingStatus() + "\"," +
                "\"otcFlag\":" + currency.otcFlag() + "," +
                "\"buyAvailableFlag\":" + currency.buyAvailableFlag() + "," +
                "\"sellAvailableFlag\":" + currency.sellAvailableFlag() + "," +
                "\"isoCurrencyName\":\"" + currency.isoCurrencyName() + "\"," +
                "\"minPriceIncrement\":" + DecimalUtils.toPrettyStringSafe(currency.minPriceIncrement()) + "," +
                "\"apiTradeAvailableFlag\":" + currency.apiTradeAvailableFlag() + "," +
                "\"uid\":\"" + currency.uid() + "\"," +
                "\"realExchange\":\"" + currency.realExchange() + "\"," +
                "\"positionUid\":\"" + currency.positionUid() + "\"," +
                "\"forIisFlag\":" + currency.forIisFlag() + "," +
                "\"forQualInvestorFlag\":" + currency.forQualInvestorFlag() + "," +
                "\"weekendFlag\":" + currency.weekendFlag() + "," +
                "\"blockedTcaFlag\":" + currency.blockedTcaFlag() + "," +
                "\"first1MinCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(currency.first1MinCandleDate()) + "\"," +
                "\"first1DayCandleDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(currency.first1DayCandleDate()) + "\"}";
    }

    public String getFigi() {
        return currency.figi();
    }

    public String getIsoCurrencyName() {
        return currency.isoCurrencyName();
    }

    public String getName() {
        return currency.name();
    }

    public OffsetDateTime getFirst1MinCandleDate() {
        return currency.first1MinCandleDate();
    }

    public OffsetDateTime getFirst1DayCandleDate() {
        return currency.first1DayCandleDate();
    }

    public TestCurrency withTicker(final String ticker) {
        return new TestCurrency(currency.withTicker(ticker), candles);
    }

}