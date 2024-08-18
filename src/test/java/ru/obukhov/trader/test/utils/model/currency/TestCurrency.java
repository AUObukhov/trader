package ru.obukhov.trader.test.utils.model.currency;

import org.mapstruct.factory.Mappers;
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
        ru.tinkoff.piapi.contract.v1.Currency tCurrency,
        ru.tinkoff.piapi.contract.v1.Instrument tInstrument,
        Map<CandleInterval, List<HistoricCandle>> candles
) {

    TestCurrency(final Currency currency, final Map<CandleInterval, List<HistoricCandle>> candles) {
        this(currency, buildTCurrency(currency), buildTInstrument(currency), candles);
    }

    private static ru.tinkoff.piapi.contract.v1.Currency buildTCurrency(final Currency currency) {
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

    private static ru.tinkoff.piapi.contract.v1.Instrument buildTInstrument(final Currency currency) {
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