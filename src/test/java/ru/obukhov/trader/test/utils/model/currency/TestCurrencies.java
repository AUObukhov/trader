package ru.obukhov.trader.test.utils.model.currency;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestCurrencies {

    private static final Currency USD_CURRENCY = Currency.builder()
            .figi("BBG0013HGFT4")
            .ticker("USD000UTSTOM")
            .classCode("CETS")
            .isin("")
            .lot(1000)
            .currency(Currencies.RUB)
            .klong(DecimalUtils.setDefaultScale(2L))
            .kshort(DecimalUtils.setDefaultScale(2L))
            .dlong(DecimalUtils.setDefaultScale(0.5))
            .dshort(DecimalUtils.setDefaultScale(0.5))
            .dlongMin(DecimalUtils.setDefaultScale(0.2929))
            .dshortMin(DecimalUtils.setDefaultScale(0.2247))
            .shortEnabledFlag(true)
            .name("Доллар США")
            .exchange("FX")
            .nominal(DecimalUtils.ONE)
            .countryOfRisk("")
            .countryOfRiskName("")
            .tradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .otcFlag(false)
            .buyAvailableFlag(true)
            .sellAvailableFlag(true)
            .isoCurrencyName("usd")
            .minPriceIncrement(DecimalUtils.setDefaultScale(0.0025))
            .apiTradeAvailableFlag(true)
            .uid("a22a1263-8e1b-4546-a1aa-416463f104d3")
            .realExchange(RealExchange.REAL_EXCHANGE_MOEX)
            .positionUid("6e97aa9b-50b6-4738-bce7-17313f2b2cc2")
            .forIisFlag(true)
            .forQualInvestorFlag(false)
            .weekendFlag(false)
            .blockedTcaFlag(false)
            .first1MinCandleDate(DateTimeTestData.newDateTime(2018, 3, 7, 19, 16))
            .first1DayCandleDate(DateTimeTestData.newDateTime(2000, 5, 16, 3))
            .build();

    private static final Currency RUB_CURRENCY = Currency.builder()
            .figi("RUB000UTSTOM")
            .ticker("RUB000UTSTOM")
            .classCode("CETS")
            .isin("")
            .lot(1000)
            .currency(Currencies.RUB)
            .klong(DecimalUtils.ZERO)
            .kshort(DecimalUtils.ZERO)
            .dlong(DecimalUtils.ZERO)
            .dshort(DecimalUtils.ZERO)
            .dlongMin(DecimalUtils.ZERO)
            .dshortMin(DecimalUtils.ZERO)
            .shortEnabledFlag(false)
            .name("Российский рубль")
            .exchange("FX")
            .nominal(DecimalUtils.ONE)
            .countryOfRisk("")
            .countryOfRiskName("")
            .tradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NOT_AVAILABLE_FOR_TRADING)
            .otcFlag(false)
            .buyAvailableFlag(false)
            .sellAvailableFlag(false)
            .isoCurrencyName("rub")
            .minPriceIncrement(DecimalUtils.setDefaultScale(0.0025))
            .apiTradeAvailableFlag(false)
            .uid("a92e2e25-a698-45cc-a781-167cf465257c")
            .realExchange(RealExchange.REAL_EXCHANGE_MOEX)
            .positionUid("33e24a92-aab0-409c-88b8-f2d57415b920")
            .forIisFlag(true)
            .forQualInvestorFlag(false)
            .weekendFlag(false)
            .blockedTcaFlag(false)
            .first1MinCandleDate(DateTimeTestData.newDateTime(1970, 1, 1))
            .first1DayCandleDate(DateTimeTestData.newDateTime(1970, 1, 1))
            .build();
    private static final Currency CNY_CURRENCY = Currency.builder()
            .figi("BBG0013HRTL0")
            .ticker("CNYRUB_TOM")
            .classCode("CETS")
            .isin("")
            .lot(1000)
            .currency(Currencies.RUB)
            .klong(DecimalUtils.setDefaultScale(2))
            .kshort(DecimalUtils.setDefaultScale(2))
            .dlong(DecimalUtils.setDefaultScale(0.2))
            .dshort(DecimalUtils.setDefaultScale(0.2))
            .dlongMin(DecimalUtils.setDefaultScale(0.1056))
            .dshortMin(DecimalUtils.setDefaultScale(0.0954))
            .shortEnabledFlag(true)
            .name("Китайский юань")
            .exchange("FX")
            .nominal(DecimalUtils.ONE)
            .countryOfRisk("")
            .countryOfRiskName("")
            .tradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .otcFlag(false)
            .buyAvailableFlag(true)
            .sellAvailableFlag(true)
            .isoCurrencyName("cny")
            .minPriceIncrement(DecimalUtils.setDefaultScale(0.001))
            .apiTradeAvailableFlag(true)
            .uid("4587ab1d-a9c9-4910-a0d6-86c7b9c42510")
            .realExchange(RealExchange.REAL_EXCHANGE_MOEX)
            .positionUid("176c3dbf-b346-48a6-b20c-daa9d028f031")
            .forIisFlag(true)
            .forQualInvestorFlag(false)
            .weekendFlag(false)
            .blockedTcaFlag(false)
            .first1MinCandleDate(DateTimeTestData.newDateTime(2018, 3, 7, 22, 28))
            .first1DayCandleDate(DateTimeTestData.newDateTime(1993, 7, 9, 3))
            .build();

    public static final TestCurrency USD = new TestCurrency(USD_CURRENCY);
    public static final TestCurrency RUB = new TestCurrency(RUB_CURRENCY);
    public static final TestCurrency CNY = new TestCurrency(CNY_CURRENCY);

}