package ru.obukhov.trader.test.utils.model.currency;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.test.utils.model.TestSecurityData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;

public class TestCurrency2 extends TestSecurityData {

    public static final String FIGI = "RUB000UTSTOM";
    public static final String TICKER = "RUB000UTSTOM";
    public static final int LOT_SIZE = 1;
    public static final String CURRENCY_VALUE = Currencies.RUB;
    public static final String NAME = "Российский рубль";
    public static final String EXCHANGE = "FX";
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1);
    public static final String COUNTRY = "";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = false;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.0025);

    public static final ru.tinkoff.piapi.contract.v1.Currency TINKOFF_CURRENCY = ru.tinkoff.piapi.contract.v1.Currency.newBuilder()
            .setFigi(TestCurrency2.FIGI)
            .setTicker(TestCurrency2.TICKER)
            .setLot(TestCurrency2.LOT_SIZE)
            .setCurrency(TestCurrency2.CURRENCY_VALUE)
            .setName(TestCurrency2.NAME)
            .setExchange(TestCurrency2.EXCHANGE)
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestCurrency2.NOMINAL))
            .setCountryOfRiskName(TestCurrency2.COUNTRY)
            .setTradingStatus(TestCurrency2.TRADING_STATUS)
            .setBuyAvailableFlag(TestCurrency2.BUY_AVAILABLE)
            .setSellAvailableFlag(TestCurrency2.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestCurrency2.API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(TestCurrency2.MIN_PRICE_INCREMENT))
            .build();

    public static final CurrencyInstrument CURRENCY = CurrencyInstrument.builder()
            .figi(TestCurrency2.FIGI)
            .ticker(TestCurrency2.TICKER)
            .lotSize(TestCurrency2.LOT_SIZE)
            .currency(TestCurrency2.CURRENCY_VALUE)
            .name(TestCurrency2.NAME)
            .exchange(TestCurrency2.EXCHANGE)
            .nominal(TestCurrency2.NOMINAL)
            .country(TestCurrency2.COUNTRY)
            .tradingStatus(TestCurrency2.TRADING_STATUS)
            .buyAvailable(TestCurrency2.BUY_AVAILABLE)
            .sellAvailable(TestCurrency2.SELL_AVAILABLE)
            .apiTradeAvailable(TestCurrency2.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestCurrency2.MIN_PRICE_INCREMENT)
            .build();

}