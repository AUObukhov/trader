package ru.obukhov.trader.test.utils.model.etf;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestEtf2 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public static final String FIGI = "BBG005HLSZ23";
    public static final String TICKER = "FXUS";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY = Currency.RUB;
    public static final String NAME = "FinEx Акции американских компаний";
    public static final Exchange EXCHANGE = Exchange.MOEX;
    public static final OffsetDateTime RELEASED_DATE = DateTimeTestData.createDateTime(2013, 10, 31, 3, 0, 0);
    public static final BigDecimal NUM_SHARES = DecimalUtils.setDefaultScale(330000.000000000);
    public static final String COUNTRY = "Соединенные Штаты Америки";
    public static final Sector SECTOR = Sector.OTHER;
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = true;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.010000000);
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 3, 7, 22, 17);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2013, 10, 31, 3);

    public static final ru.tinkoff.piapi.contract.v1.Etf TINKOFF_ETF = ru.tinkoff.piapi.contract.v1.Etf.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setLot(LOT_SIZE)
            .setCurrency(CURRENCY.name().toLowerCase())
            .setName(NAME)
            .setExchange(EXCHANGE.getValue())
            .setReleasedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(RELEASED_DATE))
            .setNumShares(QUOTATION_MAPPER.fromBigDecimal(NUM_SHARES))
            .setCountryOfRiskName(COUNTRY)
            .setSector(SECTOR.name().toLowerCase())
            .setTradingStatus(TRADING_STATUS)
            .setBuyAvailableFlag(BUY_AVAILABLE)
            .setSellAvailableFlag(SELL_AVAILABLE)
            .setApiTradeAvailableFlag(API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final Etf ETF = Etf.builder()
            .figi(FIGI)
            .ticker(TICKER)
            .lotSize(LOT_SIZE)
            .currency(CURRENCY)
            .name(NAME)
            .exchange(EXCHANGE)
            .releasedDate(RELEASED_DATE)
            .numShares(NUM_SHARES)
            .country(COUNTRY)
            .sector(SECTOR)
            .tradingStatus(TRADING_STATUS)
            .buyAvailable(BUY_AVAILABLE)
            .sellAvailable(SELL_AVAILABLE)
            .apiTradeAvailable(API_TRADE_AVAILABLE)
            .minPriceIncrement(MIN_PRICE_INCREMENT)
            .first1MinCandleDate(FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(FIRST_1_DAY_CANDLE_DATE)
            .build();

}