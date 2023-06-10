package ru.obukhov.trader.test.utils.model.instrument;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestInstrument1 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public static final String FIGI = "BBG000B9XRY4";
    public static final String TICKER = "AAPL";
    public static final String CLASS_CODE = "SPBXM";
    public static final String ISIN = "US0378331005";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY = Currency.USD;
    public static final BigDecimal K_LONG = DecimalUtils.setDefaultScale(2);
    public static final BigDecimal K_SHORT = DecimalUtils.setDefaultScale(2);
    public static final BigDecimal D_LONG = DecimalUtils.setDefaultScale(1);
    public static final BigDecimal D_SHORT = DecimalUtils.setDefaultScale(1);
    public static final BigDecimal D_LONG_MIN = DecimalUtils.setDefaultScale(1);
    public static final BigDecimal D_SHORT_MIN = DecimalUtils.setDefaultScale(1);
    public static final boolean SHORT_ENABLED = false;
    public static final String EXCHANGE = "SPB";
    public static final String COUNTRY_OF_RISK = "US";
    public static final String COUNTRY_OF_RISK_NAME = "Соединенные Штаты Америки";
    public static final String INSTRUMENT_TYPE = "share";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final boolean API_TRADE_AVAILABLE = true;
    public static final String UID = "a9eb4238-eba9-488c-b102-b6140fd08e38";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_RTS;
    public static final String POSITION_UID = "5c5e6656-c4d3-4391-a7ee-e81a76f1804e";
    public static final boolean AVAILABLE_FOR_IIS = false;
    public static final boolean FOR_QUAL_INVESTOR = false;
    public static final boolean AVAILABLE_ON_WEEKEND = false;
    public static final boolean BLOCKED_TCA = false;
    public static final ru.tinkoff.piapi.contract.v1.InstrumentType INSTRUMENT_KIND = InstrumentType.INSTRUMENT_TYPE_SHARE;
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 1, 23, 10, 34);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2013, 9, 12, 3);
    public static final ru.tinkoff.piapi.contract.v1.Instrument TINKOFF_INSTRUMENT = ru.tinkoff.piapi.contract.v1.Instrument.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setClassCode(CLASS_CODE)
            .setIsin(ISIN)
            .setLot(LOT_SIZE)
            .setCurrency(CURRENCY.name())
            .setKlong(QUOTATION_MAPPER.fromBigDecimal(K_LONG))
            .setKshort(QUOTATION_MAPPER.fromBigDecimal(K_SHORT))
            .setDlong(QUOTATION_MAPPER.fromBigDecimal(D_LONG))
            .setDshort(QUOTATION_MAPPER.fromBigDecimal(D_SHORT))
            .setDlongMin(QUOTATION_MAPPER.fromBigDecimal(D_LONG_MIN))
            .setDshortMin(QUOTATION_MAPPER.fromBigDecimal(D_SHORT_MIN))
            .setShortEnabledFlag(SHORT_ENABLED)
            .setExchange(EXCHANGE)
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setInstrumentType(INSTRUMENT_TYPE)
            .setTradingStatus(TRADING_STATUS)
            .setOtcFlag(OTC_FLAG)
            .setBuyAvailableFlag(BUY_AVAILABLE)
            .setSellAvailableFlag(SELL_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(MIN_PRICE_INCREMENT))
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE)
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

    public static final Instrument INSTRUMENT = Instrument.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .classCode(CLASS_CODE)
            .isin(ISIN)
            .lotSize(LOT_SIZE)
            .currency(CURRENCY)
            .kLong(K_LONG)
            .kShort(K_SHORT)
            .dLong(D_LONG)
            .dShort(D_SHORT)
            .dLongMin(D_LONG_MIN)
            .dShortMin(D_SHORT_MIN)
            .shortEnabled(SHORT_ENABLED)
            .exchange(EXCHANGE)
            .countryOfRisk(COUNTRY_OF_RISK)
            .countryOfRiskName(COUNTRY_OF_RISK_NAME)
            .instrumentType(INSTRUMENT_TYPE)
            .tradingStatus(TRADING_STATUS)
            .otcFlag(OTC_FLAG)
            .buyAvailable(BUY_AVAILABLE)
            .sellAvailable(SELL_AVAILABLE)
            .minPriceIncrement(MIN_PRICE_INCREMENT)
            .apiTradeAvailable(API_TRADE_AVAILABLE)
            .uid(UID)
            .realExchange(REAL_EXCHANGE)
            .positionUid(POSITION_UID)
            .availableForIis(AVAILABLE_FOR_IIS)
            .forQualInvestor(FOR_QUAL_INVESTOR)
            .availableOnWeekend(AVAILABLE_ON_WEEKEND)
            .blockedTca(BLOCKED_TCA)
            .instrumentKind(INSTRUMENT_KIND)
            .first1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();
}