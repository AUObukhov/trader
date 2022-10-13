package ru.obukhov.trader.test.utils.model.currency;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestSecurityData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestCurrency4 extends TestSecurityData {

    public static final String FIGI = "BBG000VJ5YR4";
    public static final String TICKER = "GLDRUB_TOM";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY_VALUE = Currency.RUB;
    public static final String NAME = "Искусственная валюта, дублирующая золото, для тестирования";
    public static final Exchange EXCHANGE = Exchange.FX_MTL;
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1);
    public static final String COUNTRY = "Российская Федерация";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = false;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 3, 8, 0, 54);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 3, 7, 10);

    public static final ru.tinkoff.piapi.contract.v1.Currency TINKOFF_CURRENCY = ru.tinkoff.piapi.contract.v1.Currency.newBuilder()
            .setFigi(TestCurrency4.FIGI)
            .setTicker(TestCurrency4.TICKER)
            .setLot(TestCurrency4.LOT_SIZE)
            .setCurrency(TestCurrency4.CURRENCY_VALUE.name().toLowerCase())
            .setName(TestCurrency4.NAME)
            .setExchange(TestCurrency4.EXCHANGE.getValue())
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestCurrency4.NOMINAL))
            .setCountryOfRiskName(TestCurrency4.COUNTRY)
            .setTradingStatus(TestCurrency4.TRADING_STATUS)
            .setBuyAvailableFlag(TestCurrency4.BUY_AVAILABLE)
            .setSellAvailableFlag(TestCurrency4.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestCurrency4.API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(TestCurrency4.MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestCurrency4.FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestCurrency4.FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final CurrencyInstrument CURRENCY = CurrencyInstrument.builder()
            .figi(TestCurrency4.FIGI)
            .ticker(TestCurrency4.TICKER)
            .lotSize(TestCurrency4.LOT_SIZE)
            .currency(TestCurrency4.CURRENCY_VALUE)
            .name(TestCurrency4.NAME)
            .exchange(TestCurrency4.EXCHANGE)
            .nominal(TestCurrency4.NOMINAL)
            .country(TestCurrency4.COUNTRY)
            .tradingStatus(TestCurrency4.TRADING_STATUS)
            .buyAvailable(TestCurrency4.BUY_AVAILABLE)
            .sellAvailable(TestCurrency4.SELL_AVAILABLE)
            .apiTradeAvailable(TestCurrency4.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestCurrency4.MIN_PRICE_INCREMENT)
            .first1MinCandleDate(TestCurrency4.FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(TestCurrency4.FIRST_1_DAY_CANDLE_DATE)
            .build();

}