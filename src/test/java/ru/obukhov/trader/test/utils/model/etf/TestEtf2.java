package ru.obukhov.trader.test.utils.model.etf;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;

public class TestEtf2 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    public static final String FIGI = "BBG005HLSZ23";
    public static final String TICKER = "FXUS";
    public static final String CLASS_CODE = "TQTF";
    public static final String ISIN = "IE00BD3QHZ91";
    public static final int LOT = 1;
    public static final String CURRENCY = "rub";
    public static final Quotation KLONG = QuotationUtils.ZERO;
    public static final Quotation KSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG = QuotationUtils.ZERO;
    public static final Quotation DSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG_MIN = QuotationUtils.ZERO;
    public static final Quotation DSHORT_MIN = QuotationUtils.ZERO;
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "FinEx Акции американских компаний";
    public static final String EXCHANGE = "moex_close";
    public static final Quotation FIXED_COMMISSION = QuotationUtils.newQuotation(0L, 900000000);
    public static final String FOCUS_TYPE = "equity";
    public static final OffsetDateTime RELEASED_DATE = DateTimeTestData.createDateTime(2013, 10, 31, 3);
    public static final Quotation NUM_SHARES = QuotationUtils.newQuotation(330000L, 0);
    public static final String COUNTRY_OF_RISK = "US";
    public static final String COUNTRY_OF_RISK_NAME = "Соединенные Штаты Америки";
    public static final String SECTOR = "other";
    public static final String REBALANCING_FREQUENCY = "quarterly";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean OTC_FLAG = false;
    public static final boolean BUY_AVAILABLE_FLAG = true;
    public static final boolean SELL_AVAILABLE_FLAG = true;
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0L, 10000000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = true;
    public static final String UID = "46a525d0-d33b-4100-a9d3-152045e5fef7";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "37f20627-aef5-45f4-b184-e597ba3a28e5";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = true;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 3, 7, 22, 17);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2013, 10, 31, 3);

    public static final Etf ETF = Etf.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .classCode(CLASS_CODE)
            .isin(ISIN)
            .lot(LOT)
            .currency(CURRENCY)
            .klong(KLONG)
            .kshort(KSHORT)
            .dlong(DLONG)
            .dshort(DSHORT)
            .dlongMin(DLONG_MIN)
            .dshortMin(DSHORT_MIN)
            .shortEnabledFlag(SHORT_ENABLED_FLAG)
            .name(NAME)
            .exchange(EXCHANGE)
            .fixedCommission(FIXED_COMMISSION)
            .focusType(FOCUS_TYPE)
            .releasedDate(RELEASED_DATE)
            .numShares(NUM_SHARES)
            .countryOfRisk(COUNTRY_OF_RISK)
            .countryOfRiskName(COUNTRY_OF_RISK_NAME)
            .sector(SECTOR)
            .rebalancingFreq(REBALANCING_FREQUENCY)
            .tradingStatus(TRADING_STATUS)
            .otcFlag(OTC_FLAG)
            .buyAvailableFlag(BUY_AVAILABLE_FLAG)
            .sellAvailableFlag(SELL_AVAILABLE_FLAG)
            .minPriceIncrement(MIN_PRICE_INCREMENT)
            .apiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .uid(UID)
            .realExchange(REAL_EXCHANGE)
            .positionUid(POSITION_UID)
            .forIisFlag(FOR_IIS_FLAG)
            .forQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .weekendFlag(WEEKEND_FLAG)
            .blockedTcaFlag(BLOCKED_TCA_FLAG)
            .first1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();

    public static final ru.tinkoff.piapi.contract.v1.Etf TINKOFF_ETF = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setClassCode(CLASS_CODE)
            .setIsin(ISIN)
            .setLot(LOT)
            .setCurrency(CURRENCY)
            .setKlong(KLONG)
            .setKshort(KSHORT)
            .setDlong(DLONG)
            .setDshort(DSHORT)
            .setDlongMin(DLONG_MIN)
            .setDshortMin(DSHORT_MIN)
            .setShortEnabledFlag(SHORT_ENABLED_FLAG)
            .setName(NAME)
            .setExchange(EXCHANGE)
            .setFixedCommission(FIXED_COMMISSION)
            .setFocusType(FOCUS_TYPE)
            .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(RELEASED_DATE))
            .setNumShares(NUM_SHARES)
            .setCountryOfRisk(COUNTRY_OF_RISK)
            .setCountryOfRiskName(COUNTRY_OF_RISK_NAME)
            .setSector(SECTOR)
            .setRebalancingFreq(REBALANCING_FREQUENCY)
            .setTradingStatus(TRADING_STATUS)
            .setOtcFlag(OTC_FLAG)
            .setBuyAvailableFlag(BUY_AVAILABLE_FLAG)
            .setSellAvailableFlag(SELL_AVAILABLE_FLAG)
            .setMinPriceIncrement(MIN_PRICE_INCREMENT)
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE_FLAG)
            .setUid(UID)
            .setRealExchange(REAL_EXCHANGE)
            .setPositionUid(POSITION_UID)
            .setForIisFlag(FOR_IIS_FLAG)
            .setForQualInvestorFlag(FOR_QUAL_INVESTOR_FLAG)
            .setWeekendFlag(WEEKEND_FLAG)
            .setBlockedTcaFlag(BLOCKED_TCA_FLAG)
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final String JSON_STRING = "{\"figi\":\"BBG005HLSZ23\"," +
            "\"ticker\":\"FXUS\"," +
            "\"classCode\":\"TQTF\"," +
            "\"isin\":\"IE00BD3QHZ91\"," +
            "\"lot\":1," +
            "\"currency\":\"rub\"," +
            "\"klong\":0," +
            "\"kshort\":0," +
            "\"dlong\":0," +
            "\"dshort\":0," +
            "\"dlongMin\":0," +
            "\"dshortMin\":0," +
            "\"shortEnabledFlag\":false," +
            "\"name\":\"FinEx Акции американских компаний\"," +
            "\"exchange\":\"moex_close\"," +
            "\"fixedCommission\":0.9," +
            "\"focusType\":\"equity\"," +
            "\"releasedDate\":\"2013-10-31T03:00:00+03:00\"," +
            "\"numShares\":330000," +
            "\"countryOfRisk\":\"US\"," +
            "\"countryOfRiskName\":\"Соединенные Штаты Америки\"," +
            "\"sector\":\"other\"," +
            "\"rebalancingFreq\":\"quarterly\"," +
            "\"tradingStatus\":\"SECURITY_TRADING_STATUS_NORMAL_TRADING\"," +
            "\"otcFlag\":false," +
            "\"buyAvailableFlag\":true," +
            "\"sellAvailableFlag\":true," +
            "\"minPriceIncrement\":0.01," +
            "\"apiTradeAvailableFlag\":true," +
            "\"uid\":\"46a525d0-d33b-4100-a9d3-152045e5fef7\"," +
            "\"realExchange\":\"REAL_EXCHANGE_MOEX\"," +
            "\"positionUid\":\"37f20627-aef5-45f4-b184-e597ba3a28e5\"," +
            "\"forIisFlag\":true," +
            "\"forQualInvestorFlag\":true," +
            "\"weekendFlag\":false," +
            "\"blockedTcaFlag\":false," +
            "\"first1MinCandleDate\":\"2018-03-07T22:17:00+03:00\"," +
            "\"first1DayCandleDate\":\"2013-10-31T03:00:00+03:00\"}";

}