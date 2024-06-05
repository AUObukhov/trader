package ru.obukhov.trader.test.utils.model.currency;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

public record TestCurrency(Currency currency, ru.tinkoff.piapi.contract.v1.Currency tinkoffCurrency, String jsonString) {

    TestCurrency(final Currency currency) {
        this(currency, buildTinkoffCurrency(currency), buildJsonString(currency));
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

    private static String buildJsonString(final Currency currency) {
        return "{" +
                "\"figi\":\"" + currency.figi() + "\"," +
                "\"ticker\":\"" + currency.ticker() + "\"," +
                "\"classCode\":\"" + currency.classCode() + "\"," +
                "\"isin\":\"" + currency.isin() + "\"," +
                "\"lot\":" + currency.lot() + "," +
                "\"currency\":\"" + currency.currency() + "\"," +
                "\"klong\":" + currency.klong() + "," +
                "\"kshort\":" + currency.kshort() + "," +
                "\"dlong\":" + currency.dlong() + "," +
                "\"dshort\":" + currency.dshort() + "," +
                "\"dlongMin\":" + currency.dlongMin() + "," +
                "\"dshortMin\":" + currency.dshortMin() + "," +
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
                "\"minPriceIncrement\":" + currency.minPriceIncrement() + "," +
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

}