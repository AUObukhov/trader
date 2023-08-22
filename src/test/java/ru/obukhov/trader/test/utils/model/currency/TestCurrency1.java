package ru.obukhov.trader.test.utils.model.currency;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.tinkoff.piapi.contract.v1.Currency;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestCurrency1 {

    public static final String FIGI = "BBG0013HGFT4";
    public static final String TICKER = "USD000UTSTOM";
    public static final String CLASS_CODE = "CETS";
    public static final String ISIN = "";
    public static final int LOT = 1000;
    public static final String CURRENCY_VALUE = Currencies.RUB;
    public static final Quotation KLONG = QuotationUtils.newQuotation(2L);
    public static final Quotation KSHORT = QuotationUtils.newQuotation(2L);
    public static final Quotation DLONG = QuotationUtils.newQuotation(0L, 500000000);
    public static final Quotation DSHORT = QuotationUtils.newQuotation(0L, 500000000);
    public static final Quotation DLONG_MIN = QuotationUtils.newQuotation(0L, 292900000);
    public static final Quotation DSHORT_MIN = QuotationUtils.newQuotation(0L, 224700000);
    public static final boolean SHORT_ENABLED_FLAG = true;
    public static final String NAME = "Доллар США";
    public static final String EXCHANGE = "FX";
    public static final MoneyValue NOMINAL = MoneyValue.newBuilder().setCurrency(Currencies.USD).setUnits(1L).build();
    public static final String COUNTRY_OF_RISK = "";
    public static final String COUNTRY_OF_RISK_NAME = "";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final String ISO_CURRENCY_NAME = "usd";
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0L, 2500000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "a22a1263-8e1b-4546-a1aa-416463f104d3";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "6e97aa9b-50b6-4738-bce7-17313f2b2cc2";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = TimestampUtils.newTimestamp(2018, 3, 7, 19, 16);
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = TimestampUtils.newTimestamp(2000, 5, 16, 3);

    public static final Currency CURRENCY = Currency.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setClassCode(CLASS_CODE)
            .setIsin(ISIN)
            .setLot(LOT)
            .setCurrency(CURRENCY_VALUE)
            .setKlong(KLONG)
            .setKshort(KSHORT)
            .setDlong(DLONG)
            .setDshort(DSHORT)
            .setDlongMin(DLONG_MIN)
            .setDshortMin(DSHORT_MIN)
            .setShortEnabledFlag(SHORT_ENABLED_FLAG)
            .setName(NAME)
            .setExchange(EXCHANGE)
            .setNominal(NOMINAL)
            .setCountryOfRiskName(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setTradingStatus(TRADING_STATUS)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
            .setIsoCurrencyName(ISO_CURRENCY_NAME)
            .setMinPriceIncrement(MIN_PRICE_INCREMENT)
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .setUid(UID)
            .setRealExchange(REAL_EXCHANGE)
            .setPositionUid(POSITION_UID)
            .setForIisFlag(FOR_IIS_FLAG)
            .setForQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .setWeekendFlag(WEEKEND_FLAG)
            .setBlockedTcaFlag(BLOCKED_TCA_FLAG)
            .setFirst1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .setFirst1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();

    public static final String JSON_STRING = "{\"figi\":\"BBG0013HGFT4\",\"ticker\":\"USD000UTSTOM\",\"classCode\":\"CETS\",\"isin\":\"\",\"lot\":1000,\"currency\":\"rub\",\"klong\":2,\"kshort\":2,\"dlong\":0.5,\"dshort\":0.5,\"dlongMin\":0.2929,\"dshortMin\":0.2247,\"shortEnabledFlag\":true,\"name\":\"Доллар США\",\"exchange\":\"FX\",\"nominal\":{\"currency\":\"usd\",\"units\":1,\"nano\":0},\"countryOfRisk\":\"\",\"countryOfRiskName\":\"\",\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\",\"otcFlag\":false,\"buyAvailableFlag\":true,\"sellAvailableFlag\":true,\"isoCurrencyName\":\"usd\",\"minPriceIncrement\":0.0025,\"apiTradeAvailableFlag\":true,\"uid\":\"a22a1263-8e1b-4546-a1aa-416463f104d3\",\"realExchange\":\"REAL_EXCHANGE_MOEX\",\"positionUid\":\"6e97aa9b-50b6-4738-bce7-17313f2b2cc2\",\"forIisFlag\":true,\"forQualInvestorFlag\":false,\"weekendFlag\":false,\"blockedTcaFlag\":false,\"first1MinCandleDate\":{\"seconds\":1520439360,\"nanos\":0},\"first1DayCandleDate\":{\"seconds\":958435200,\"nanos\":0}}";

}