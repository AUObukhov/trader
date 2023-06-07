package ru.obukhov.trader.test.utils.model.currency;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestSecurityData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestCurrency3 extends TestSecurityData {

    public static final String FIGI = "BBG000VJ5YR4";
    public static final String TICKER = "GLDRUB_TOM";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY_VALUE = Currency.RUB;
    public static final String NAME = "Золото";
    public static final String EXCHANGE = "FX_MTL";
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
            .setFigi(TestCurrency3.FIGI)
            .setTicker(TestCurrency3.TICKER)
            .setLot(TestCurrency3.LOT_SIZE)
            .setCurrency(TestCurrency3.CURRENCY_VALUE.name().toLowerCase())
            .setName(TestCurrency3.NAME)
            .setExchange(TestCurrency3.EXCHANGE)
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestCurrency3.NOMINAL))
            .setCountryOfRiskName(TestCurrency3.COUNTRY)
            .setTradingStatus(TestCurrency3.TRADING_STATUS)
            .setBuyAvailableFlag(TestCurrency3.BUY_AVAILABLE)
            .setSellAvailableFlag(TestCurrency3.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestCurrency3.API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(TestCurrency3.MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestCurrency3.FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestCurrency3.FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final CurrencyInstrument CURRENCY = CurrencyInstrument.builder()
            .figi(TestCurrency3.FIGI)
            .ticker(TestCurrency3.TICKER)
            .lotSize(TestCurrency3.LOT_SIZE)
            .currency(TestCurrency3.CURRENCY_VALUE)
            .name(TestCurrency3.NAME)
            .exchange(TestCurrency3.EXCHANGE)
            .nominal(TestCurrency3.NOMINAL)
            .country(TestCurrency3.COUNTRY)
            .tradingStatus(TestCurrency3.TRADING_STATUS)
            .buyAvailable(TestCurrency3.BUY_AVAILABLE)
            .sellAvailable(TestCurrency3.SELL_AVAILABLE)
            .apiTradeAvailable(TestCurrency3.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestCurrency3.MIN_PRICE_INCREMENT)
            .first1MinCandleDate(TestCurrency3.FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(TestCurrency3.FIRST_1_DAY_CANDLE_DATE)
            .build();

}