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

public class TestBond3 extends TestSecurityData {
    public static final String FIGI = "TCS00A1043Z7";
    public static final String TICKER = "RU000A1043Z7";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY = Currency.RUB;
    public static final String NAME = "Биннофарм Групп выпуск 1";
    public static final String EXCHANGE = "MOEX";
    public static final OffsetDateTime MATURITY_DATE = DateTimeTestData.createDateTime(2036, 11, 5, 3);
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1000);
    public static final OffsetDateTime STATE_REG_DATE = DateTimeTestData.createDateTime(2021, 11, 18, 3);
    public static final OffsetDateTime PLACEMENT_DATE = DateTimeTestData.createDateTime(2021, 11, 24, 3);
    public static final BigDecimal PLACEMENT_PRICE = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal ACI_VALUE = DecimalUtils.setDefaultScale(12.750000000);
    public static final String COUNTRY = "Российская Федерация";
    public static final Sector SECTOR = Sector.HEALTH_CARE;
    public static final long ISSUE_SIZE = 3000000;
    public static final long ISSUE_SIZE_PLAN = 3000000;
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = true;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2021, 11, 24, 14, 49);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2021, 11, 24, 10);

    public static final ru.tinkoff.piapi.contract.v1.Bond TINKOFF_BOND = ru.tinkoff.piapi.contract.v1.Bond.newBuilder()
            .setFigi(TestBond3.FIGI)
            .setTicker(TestBond3.TICKER)
            .setLot(TestBond3.LOT_SIZE)
            .setCurrency(TestBond3.CURRENCY.name().toLowerCase())
            .setName(TestBond3.NAME)
            .setExchange(TestBond3.EXCHANGE)
            .setMaturityDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond3.MATURITY_DATE))
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond3.NOMINAL))
            .setStateRegDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond3.STATE_REG_DATE))
            .setPlacementDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond3.PLACEMENT_DATE))
            .setPlacementPrice(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond3.PLACEMENT_PRICE))
            .setAciValue(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond3.ACI_VALUE))
            .setCountryOfRiskName(TestBond3.COUNTRY)
            .setSector(TestBond3.SECTOR.name().toLowerCase())
            .setIssueSize(TestBond3.ISSUE_SIZE)
            .setIssueSizePlan(TestBond3.ISSUE_SIZE_PLAN)
            .setTradingStatus(TestBond3.TRADING_STATUS)
            .setBuyAvailableFlag(TestBond3.BUY_AVAILABLE)
            .setSellAvailableFlag(TestBond3.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestBond3.API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(TestBond3.MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond3.FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond3.FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final Bond BOND = Bond.builder()
            .figi(TestBond3.FIGI)
            .ticker(TestBond3.TICKER)
            .lotSize(TestBond3.LOT_SIZE)
            .currency(TestBond3.CURRENCY)
            .name(TestBond3.NAME)
            .exchange(TestBond3.EXCHANGE)
            .maturityDate(TestBond3.MATURITY_DATE)
            .nominal(TestBond3.NOMINAL)
            .stateRegDate(TestBond3.STATE_REG_DATE)
            .placementDate(TestBond3.PLACEMENT_DATE)
            .placementPrice(TestBond3.PLACEMENT_PRICE)
            .aciValue(TestBond3.ACI_VALUE)
            .country(TestBond3.COUNTRY)
            .sector(TestBond3.SECTOR)
            .issueSize(TestBond3.ISSUE_SIZE)
            .issueSizePlan(TestBond3.ISSUE_SIZE_PLAN)
            .tradingStatus(TestBond3.TRADING_STATUS)
            .buyAvailable(TestBond3.BUY_AVAILABLE)
            .sellAvailable(TestBond3.SELL_AVAILABLE)
            .apiTradeAvailable(TestBond3.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestBond3.MIN_PRICE_INCREMENT)
            .first1MinCandleDate(TestBond3.FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(TestBond3.FIRST_1_DAY_CANDLE_DATE)
            .build();

}