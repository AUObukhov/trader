package ru.obukhov.trader.test.utils.model.instrument;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestInstrument1 {

    public static final String FIGI = "BBG000B9XRY4";
    public static final String TICKER = "AAPL";
    public static final String CLASS_CODE = "SPBXM";
    public static final String ISIN = "US0378331005";
    public static final int LOT_SIZE = 1;
    public static final String CURRENCY = Currencies.USD;
    public static final Quotation K_LONG = QuotationUtils.newNormalizedQuotation(2, 0);
    public static final Quotation K_SHORT = QuotationUtils.newNormalizedQuotation(2, 0);
    public static final Quotation D_LONG = QuotationUtils.newNormalizedQuotation(1, 0);
    public static final Quotation D_SHORT = QuotationUtils.newNormalizedQuotation(1, 0);
    public static final Quotation D_LONG_MIN = QuotationUtils.newNormalizedQuotation(1, 0);
    public static final Quotation D_SHORT_MIN = QuotationUtils.newNormalizedQuotation(1, 0);
    public static final boolean SHORT_ENABLED = false;
    public static final String NAME = "Apple";
    public static final String EXCHANGE = "SPB";
    public static final String COUNTRY_OF_RISK = "US";
    public static final String COUNTRY_OF_RISK_NAME = "Соединенные Штаты Америки";
    public static final String INSTRUMENT_TYPE = "share";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newNormalizedQuotation(0, 10_000_000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "a9eb4238-eba9-488c-b102-b6140fd08e38";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_RTS;
    public static final String POSITION_UID = "5c5e6656-c4d3-4391-a7ee-e81a76f1804e";
    public static final boolean AVAILABLE_FOR_IIS = true;
    public static final boolean FOR_QUAL_INVESTOR = true;
    public static final boolean AVAILABLE_ON_WEEKEND = false;
    public static final boolean BLOCKED_TCA = false;
    public static final InstrumentType INSTRUMENT_KIND = InstrumentType.INSTRUMENT_TYPE_SHARE;
    public static final Timestamp FIRST_1_MIN_CANDLE_DATE = TimestampUtils.newTimestamp(2018, 1, 23, 10, 34);
    public static final Timestamp FIRST_1_DAY_CANDLE_DATE = TimestampUtils.newTimestamp(1988, 9, 12, 3);

    public static final Instrument INSTRUMENT = Instrument.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setClassCode(CLASS_CODE)
            .setIsin(ISIN)
            .setLot(LOT_SIZE)
            .setCurrency(CURRENCY)
            .setKlong(K_LONG)
            .setKshort(K_SHORT)
            .setDlong(D_LONG)
            .setDshort(D_SHORT)
            .setDlongMin(D_LONG_MIN)
            .setDshortMin(D_SHORT_MIN)
            .setShortEnabledFlag(SHORT_ENABLED)
            .setName(NAME)
            .setExchange(EXCHANGE)
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setInstrumentType(INSTRUMENT_TYPE)
            .setTradingStatus(TRADING_STATUS)
            .setOtcFlag(OTC_FLAG)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
            .setMinPriceIncrement(MIN_PRICE_INCREMENT)
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .setUid(UID)
            .setRealExchange(REAL_EXCHANGE)
            .setPositionUid(POSITION_UID)
            .setForIisFlag(AVAILABLE_FOR_IIS)
            .setForQualInvestorFlag(FOR_QUAL_INVESTOR)
            .setWeekendFlag(AVAILABLE_ON_WEEKEND)
            .setBlockedTcaFlag(BLOCKED_TCA)
            .setInstrumentKind(INSTRUMENT_KIND)
            .setFirst1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .setFirst1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();

    public static final String JSON_STRING = "{\"figi\":\"BBG000B9XRY4\",\"ticker\":\"AAPL\",\"classCode\":\"SPBXM\",\"isin\":\"US0378331005\",\"lot\":1,\"currency\":\"usd\",\"klong\":{\"units\":2,\"nano\":0},\"kshort\":{\"units\":2,\"nano\":0},\"dlong\":{\"units\":1,\"nano\":0},\"dshort\":{\"units\":1,\"nano\":0},\"dlongMin\":{\"units\":1,\"nano\":0},\"dshortMin\":{\"units\":1,\"nano\":0},\"shortEnabledFlag\":false,\"name\":\"Apple\",\"exchange\":\"SPB\",\"countryOfRisk\":\"US\",\"countryOfRiskName\":\"Соединенные Штаты Америки\",\"instrumentType\":\"share\",\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\",\"otcFlag\":false,\"buyAvailableFlag\":true,\"sellAvailableFlag\":true,\"minPriceIncrement\":{\"units\":0,\"nano\":10000000},\"apiTradeAvailableFlag\":true,\"uid\":\"a9eb4238-eba9-488c-b102-b6140fd08e38\",\"realExchange\":\"REAL_EXCHANGE_RTS\",\"positionUid\":\"5c5e6656-c4d3-4391-a7ee-e81a76f1804e\",\"forIisFlag\":true,\"forQualInvestorFlag\":true,\"weekendFlag\":false,\"blockedTcaFlag\":false,\"instrumentKind\":\"INSTRUMENT_TYPE_SHARE\",\"first1MinCandleDate\":{\"seconds\":1516692840,\"nanos\":0},\"first1DayCandleDate\":{\"seconds\":590025600,\"nanos\":0}}";

}