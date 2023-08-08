package ru.obukhov.trader.test.utils.model.currency;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.test.utils.model.TestSecurityData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;

public class TestCurrency1 extends TestSecurityData {

    public static final String FIGI = "BBG0013HGFT4";
    public static final String TICKER = "USD000UTSTOM";
    public static final int LOT_SIZE = 1000;
    public static final String CURRENCY_VALUE = Currencies.RUB;
    public static final String NAME = "Доллар США";
    public static final String EXCHANGE = "FX";
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1);
    public static final String COUNTRY = "";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = true;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.0025);
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = TimestampUtils.newTimestamp(2018, 3, 7, 19, 16);
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = TimestampUtils.newTimestamp(2000, 5, 16, 3);

    public static final ru.tinkoff.piapi.contract.v1.Currency TINKOFF_CURRENCY = ru.tinkoff.piapi.contract.v1.Currency.newBuilder()
            .setFigi(TestCurrency1.FIGI)
            .setTicker(TestCurrency1.TICKER)
            .setLot(TestCurrency1.LOT_SIZE)
            .setCurrency(TestCurrency1.CURRENCY_VALUE)
            .setName(TestCurrency1.NAME)
            .setExchange(TestCurrency1.EXCHANGE)
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestCurrency1.NOMINAL))
            .setCountryOfRiskName(TestCurrency1.COUNTRY)
            .setTradingStatus(TestCurrency1.TRADING_STATUS)
            .setBuyAvailableFlag(TestCurrency1.BUY_AVAILABLE)
            .setSellAvailableFlag(TestCurrency1.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestCurrency1.API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(TestCurrency1.MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(TestCurrency1.FIRST_1_MIN_CANDLE_DATE)
            .setFirst1DayCandleDate(TestCurrency1.FIRST_1_DAY_CANDLE_DATE)
            .build();

    public static final CurrencyInstrument CURRENCY = CurrencyInstrument.builder()
            .figi(TestCurrency1.FIGI)
            .ticker(TestCurrency1.TICKER)
            .lotSize(TestCurrency1.LOT_SIZE)
            .currency(TestCurrency1.CURRENCY_VALUE)
            .name(TestCurrency1.NAME)
            .exchange(TestCurrency1.EXCHANGE)
            .nominal(TestCurrency1.NOMINAL)
            .country(TestCurrency1.COUNTRY)
            .tradingStatus(TestCurrency1.TRADING_STATUS)
            .buyAvailable(TestCurrency1.BUY_AVAILABLE)
            .sellAvailable(TestCurrency1.SELL_AVAILABLE)
            .apiTradeAvailable(TestCurrency1.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestCurrency1.MIN_PRICE_INCREMENT)
            .first1MinCandleDate(TimestampUtils.toOffsetDateTime(TestCurrency1.FIRST_1_MIN_CANDLE_DATE))
            .first1DayCandleDate(TimestampUtils.toOffsetDateTime(TestCurrency1.FIRST_1_DAY_CANDLE_DATE))
            .build();

}