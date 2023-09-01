package ru.obukhov.trader.test.utils.model.instrument;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;

public class TestInstrument1 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    public static final String FIGI = "BBG000B9XRY4";
    public static final String TICKER = "AAPL";
    public static final String CLASS_CODE = "SPBXM";
    public static final String ISIN = "US0378331005";
    public static final int LOT_SIZE = 1;
    public static final String CURRENCY = Currencies.USD;
    public static final Quotation K_LONG = QuotationUtils.newQuotation(2L);
    public static final Quotation K_SHORT = QuotationUtils.newQuotation(2L);
    public static final Quotation D_LONG = QuotationUtils.newQuotation(1L);
    public static final Quotation D_SHORT = QuotationUtils.newQuotation(1L);
    public static final Quotation D_LONG_MIN = QuotationUtils.newQuotation(1L);
    public static final Quotation D_SHORT_MIN = QuotationUtils.newQuotation(1L);
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
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0L, 10_000_000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "a9eb4238-eba9-488c-b102-b6140fd08e38";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_RTS;
    public static final String POSITION_UID = "5c5e6656-c4d3-4391-a7ee-e81a76f1804e";
    public static final boolean AVAILABLE_FOR_IIS = true;
    public static final boolean FOR_QUAL_INVESTOR = true;
    public static final boolean AVAILABLE_ON_WEEKEND = false;
    public static final boolean BLOCKED_TCA = false;
    public static final InstrumentType INSTRUMENT_KIND = InstrumentType.INSTRUMENT_TYPE_SHARE;
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 1, 23, 10, 34);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(1988, 9, 12, 3);

    public static final Instrument INSTRUMENT = Instrument.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .classCode(CLASS_CODE)
            .isin(ISIN)
            .lot(LOT_SIZE)
            .currency(CURRENCY)
            .klong(K_LONG)
            .kshort(K_SHORT)
            .dlong(D_LONG)
            .dshort(D_SHORT)
            .dlongMin(D_LONG_MIN)
            .dshortMin(D_SHORT_MIN)
            .shortEnabledFlag(SHORT_ENABLED)
            .name(NAME)
            .exchange(EXCHANGE)
            .countryOfRisk(COUNTRY_OF_RISK)
            .countryOfRiskName(COUNTRY_OF_RISK_NAME)
            .instrumentType(INSTRUMENT_TYPE)
            .tradingStatus(TRADING_STATUS)
            .otcFlag(OTC_FLAG)
            .buyAvailableFlag(BUY_AVAILABLE_FLAG)
            .sellAvailableFlag(SELL_AVAILABLE_FLAG)
            .minPriceIncrement(MIN_PRICE_INCREMENT)
            .apiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .uid(UID)
            .realExchange(REAL_EXCHANGE)
            .positionUid(POSITION_UID)
            .forIisFlag(AVAILABLE_FOR_IIS)
            .forQualInvestorFlag(FOR_QUAL_INVESTOR)
            .weekendFlag(AVAILABLE_ON_WEEKEND)
            .blockedTcaFlag(BLOCKED_TCA)
            .instrumentKind(INSTRUMENT_KIND)
            .first1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();
    public static final ru.tinkoff.piapi.contract.v1.Instrument TINKOFF_INSTRUMENT = ru.tinkoff.piapi.contract.v1.Instrument.newBuilder()
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
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final String JSON_STRING = "{" +
            "\"figi\":\"BBG000B9XRY4\"," +
            "\"ticker\":\"AAPL\"," +
            "\"classCode\":\"SPBXM\"," +
            "\"isin\":\"US0378331005\"," +
            "\"lot\":1," +
            "\"currency\":\"usd\"," +
            "\"klong\":2," +
            "\"kshort\":2," +
            "\"dlong\":1," +
            "\"dshort\":1," +
            "\"dlongMin\":1," +
            "\"dshortMin\":1," +
            "\"shortEnabledFlag\":false," +
            "\"name\":\"Apple\"," +
            "\"exchange\":\"SPB\"," +
            "\"countryOfRisk\":\"US\"," +
            "\"countryOfRiskName\":\"Соединенные Штаты Америки\"," +
            "\"instrumentType\":\"share\"," +
            "\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\"," +
            "\"otcFlag\":false," +
            "\"buyAvailableFlag\":true," +
            "\"sellAvailableFlag\":true," +
            "\"minPriceIncrement\":0.01," +
            "\"apiTradeAvailableFlag\":true," +
            "\"uid\":\"a9eb4238-eba9-488c-b102-b6140fd08e38\"," +
            "\"realExchange\":\"REAL_EXCHANGE_RTS\"," +
            "\"positionUid\":\"5c5e6656-c4d3-4391-a7ee-e81a76f1804e\"," +
            "\"forIisFlag\":true," +
            "\"forQualInvestorFlag\":true," +
            "\"weekendFlag\":false," +
            "\"blockedTcaFlag\":false," +
            "\"instrumentKind\":\"INSTRUMENT_TYPE_SHARE\"," +
            "\"first1MinCandleDate\":\"2018-01-23T10:34:00+03:00\"," +
            "\"first1DayCandleDate\":\"1988-09-12T03:00:00+03:00\"" +
            "}";

}