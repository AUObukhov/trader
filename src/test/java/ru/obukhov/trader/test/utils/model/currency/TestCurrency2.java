package ru.obukhov.trader.test.utils.model.currency;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;

public class TestCurrency2 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    public static final String FIGI = "RUB000UTSTOM";
    public static final String TICKER = "RUB000UTSTOM";
    public static final String CLASS_CODE = "CETS";
    public static final String ISIN = "";
    public static final int LOT = 1000;
    public static final String CURRENCY_VALUE = Currencies.RUB;
    public static final Quotation KLONG = QuotationUtils.ZERO;
    public static final Quotation KSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG = QuotationUtils.ZERO;
    public static final Quotation DSHORT = QuotationUtils.ZERO;
    public static final Quotation DLONG_MIN = QuotationUtils.ZERO;
    public static final Quotation DSHORT_MIN = QuotationUtils.ZERO;
    public static final boolean SHORT_ENABLED_FLAG = false;
    public static final String NAME = "Российский рубль";
    public static final String EXCHANGE = "FX";
    public static final MoneyValue NOMINAL = TestData.createMoneyValue(1, Currencies.USD);
    public static final String COUNTRY_OF_RISK = "";
    public static final String COUNTRY_OF_RISK_NAME = "";
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NOT_AVAILABLE_FOR_TRADING;
    public static final boolean BUY_AVAILABLE_FLAG = false;
    public static final boolean SELL_AVAILABLE_FLAG = false;
    public static final String ISO_CURRENCY_NAME = "rub";
    public static final Quotation MIN_PRICE_INCREMENT = QuotationUtils.newQuotation(0L, 2500000);
    public static final boolean API_TRADE_AVAILABLE_FLAG = false;
    public static final String UID = "a92e2e25-a698-45cc-a781-167cf465257c";
    public static final RealExchange REAL_EXCHANGE = RealExchange.REAL_EXCHANGE_MOEX;
    public static final String POSITION_UID = "33e24a92-aab0-409c-88b8-f2d57415b920";
    public static final boolean FOR_IIS_FLAG = true;
    public static final boolean FOR_QUAL_INVESTOR_FLAG = false;
    public static final boolean WEEKEND_FLAG = false;
    public static final boolean BLOCKED_TCA_FLAG = false;
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(1970, 1, 1);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(1970, 1, 1);

    public static final ru.obukhov.trader.market.model.Currency CURRENCY = ru.obukhov.trader.market.model.Currency.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .classCode(CLASS_CODE)
            .isin(ISIN)
            .lot(LOT)
            .currency(CURRENCY_VALUE)
            .klong(KLONG)
            .kshort(KSHORT)
            .dlong(DLONG)
            .dshort(DSHORT)
            .dlongMin(DLONG_MIN)
            .dshortMin(DSHORT_MIN)
            .shortEnabledFlag(SHORT_ENABLED_FLAG)
            .name(NAME)
            .exchange(EXCHANGE)
            .nominal(NOMINAL)
            .countryOfRisk(COUNTRY_OF_RISK)
            .countryOfRiskName(COUNTRY_OF_RISK_NAME)
            .tradingStatus(TRADING_STATUS)
            .buyAvailableFlag(BUY_AVAILABLE_FLAG)
            .sellAvailableFlag(SELL_AVAILABLE_FLAG)
            .isoCurrencyName(ISO_CURRENCY_NAME)
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

    public static final ru.tinkoff.piapi.contract.v1.Currency TINKOFF_CURRENCY = ru.tinkoff.piapi.contract.v1.Currency.newBuilder()
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
            .setCountryOfRisk(COUNTRY_OF_RISK)
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
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_DAY_CANDLE_DATE))
            .build();

}