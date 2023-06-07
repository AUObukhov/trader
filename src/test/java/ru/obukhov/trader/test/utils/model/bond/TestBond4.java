package ru.obukhov.trader.test.utils.model.bond;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestSecurityData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestBond4 extends TestSecurityData {
    public static final String FIGI = "ISSUANCEBNF1";
    public static final String TICKER = "RU000A1043Z7";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY = Currency.RUB;
    public static final String NAME = "Биннофарм Групп выпуск 1";
    public static final String EXCHANGE = "Issuance";
    public static final OffsetDateTime MATURITY_DATE = DateTimeTestData.createDateTime(2036, 11, 5, 3);
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(0);
    public static final OffsetDateTime STATE_REG_DATE = DateTimeTestData.createDateTime(2021, 11, 18, 3);
    public static final OffsetDateTime PLACEMENT_DATE = DateTimeTestData.createDateTime(2021, 11, 24, 3);
    public static final BigDecimal PLACEMENT_PRICE = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal ACI_VALUE = DecimalUtils.setDefaultScale(0);
    public static final String COUNTRY = "Российская Федерация";
    public static final Sector SECTOR = Sector.HEALTH_CARE;
    public static final long ISSUE_SIZE = 0;
    public static final long ISSUE_SIZE_PLAN = 3000000;
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = false;
    public static final boolean SELL_AVAILABLE = false;
    public static final boolean API_TRADE_AVAILABLE = false;
    public static final BigDecimal MIN_PRICE_INCREMENT = null;
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2021, 11, 24, 14, 35);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2021, 11, 24, 10);

    public static final ru.tinkoff.piapi.contract.v1.Bond TINKOFF_BOND = ru.tinkoff.piapi.contract.v1.Bond.newBuilder()
            .setFigi(TestBond4.FIGI)
            .setTicker(TestBond4.TICKER)
            .setLot(TestBond4.LOT_SIZE)
            .setCurrency(TestBond4.CURRENCY.name().toLowerCase())
            .setName(TestBond4.NAME)
            .setExchange(TestBond4.EXCHANGE)
            .setMaturityDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond4.MATURITY_DATE))
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond4.NOMINAL))
            .setStateRegDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond4.STATE_REG_DATE))
            .setPlacementDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond4.PLACEMENT_DATE))
            .setPlacementPrice(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond4.PLACEMENT_PRICE))
            .setAciValue(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond4.ACI_VALUE))
            .setCountryOfRiskName(TestBond4.COUNTRY)
            .setSector(TestBond4.SECTOR.name().toLowerCase())
            .setIssueSize(TestBond4.ISSUE_SIZE)
            .setIssueSizePlan(TestBond4.ISSUE_SIZE_PLAN)
            .setTradingStatus(TestBond4.TRADING_STATUS)
            .setBuyAvailableFlag(TestBond4.BUY_AVAILABLE)
            .setSellAvailableFlag(TestBond4.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestBond4.API_TRADE_AVAILABLE)
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond4.FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond4.FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final Bond BOND = Bond.builder()
            .figi(TestBond4.FIGI)
            .ticker(TestBond4.TICKER)
            .lotSize(TestBond4.LOT_SIZE)
            .currency(TestBond4.CURRENCY)
            .name(TestBond4.NAME)
            .exchange(TestBond4.EXCHANGE)
            .maturityDate(TestBond4.MATURITY_DATE)
            .nominal(TestBond4.NOMINAL)
            .stateRegDate(TestBond4.STATE_REG_DATE)
            .placementDate(TestBond4.PLACEMENT_DATE)
            .placementPrice(TestBond4.PLACEMENT_PRICE)
            .aciValue(TestBond4.ACI_VALUE)
            .country(TestBond4.COUNTRY)
            .sector(TestBond4.SECTOR)
            .issueSize(TestBond4.ISSUE_SIZE)
            .issueSizePlan(TestBond4.ISSUE_SIZE_PLAN)
            .tradingStatus(TestBond4.TRADING_STATUS)
            .buyAvailable(TestBond4.BUY_AVAILABLE)
            .sellAvailable(TestBond4.SELL_AVAILABLE)
            .apiTradeAvailable(TestBond4.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestBond4.MIN_PRICE_INCREMENT)
            .first1MinCandleDate(TestBond4.FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(TestBond4.FIRST_1_DAY_CANDLE_DATE)
            .build();

}