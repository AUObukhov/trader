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

public class TestBond2 extends TestSecurityData {

    public static final String FIGI = "TCS00A1050H0";
    public static final String TICKER = "RU000A1050H0";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY = Currency.RUB;
    public static final String NAME = "ЕАБР";
    public static final String EXCHANGE = "MOEX";
    public static final OffsetDateTime MATURITY_DATE = DateTimeTestData.createDateTime(2025, 7, 29, 3);
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1000);
    public static final OffsetDateTime STATE_REG_DATE = DateTimeTestData.createDateTime(2022, 7, 28, 3);
    public static final OffsetDateTime PLACEMENT_DATE = DateTimeTestData.createDateTime(2022, 8, 2, 3);
    public static final BigDecimal PLACEMENT_PRICE = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal ACI_VALUE = DecimalUtils.setDefaultScale(17.680000000);
    public static final String COUNTRY = "Республика Казахстан";
    public static final Sector SECTOR = Sector.FINANCIAL;
    public static final long ISSUE_SIZE = 10000000;
    public static final long ISSUE_SIZE_PLAN = 10000000;
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NOT_AVAILABLE_FOR_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = true;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2022, 8, 2, 14, 45);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2022, 8, 2, 10);

    public static final ru.tinkoff.piapi.contract.v1.Bond TINKOFF_BOND = ru.tinkoff.piapi.contract.v1.Bond.newBuilder()
            .setFigi(TestBond2.FIGI)
            .setTicker(TestBond2.TICKER)
            .setLot(TestBond2.LOT_SIZE)
            .setCurrency(TestBond2.CURRENCY.name().toLowerCase())
            .setName(TestBond2.NAME)
            .setExchange(TestBond2.EXCHANGE)
            .setMaturityDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond2.MATURITY_DATE))
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond2.NOMINAL))
            .setStateRegDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond2.STATE_REG_DATE))
            .setPlacementDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond2.PLACEMENT_DATE))
            .setPlacementPrice(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond2.PLACEMENT_PRICE))
            .setAciValue(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond2.ACI_VALUE))
            .setCountryOfRiskName(TestBond2.COUNTRY)
            .setSector(TestBond2.SECTOR.name().toLowerCase())
            .setIssueSize(TestBond2.ISSUE_SIZE)
            .setIssueSizePlan(TestBond2.ISSUE_SIZE_PLAN)
            .setTradingStatus(TestBond2.TRADING_STATUS)
            .setBuyAvailableFlag(TestBond2.BUY_AVAILABLE)
            .setSellAvailableFlag(TestBond2.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestBond2.API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(TestBond2.MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond2.FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond2.FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final Bond BOND = Bond.builder()
            .figi(TestBond2.FIGI)
            .ticker(TestBond2.TICKER)
            .lotSize(TestBond2.LOT_SIZE)
            .currency(TestBond2.CURRENCY)
            .name(TestBond2.NAME)
            .exchange(TestBond2.EXCHANGE)
            .maturityDate(TestBond2.MATURITY_DATE)
            .nominal(TestBond2.NOMINAL)
            .stateRegDate(TestBond2.STATE_REG_DATE)
            .placementDate(TestBond2.PLACEMENT_DATE)
            .placementPrice(TestBond2.PLACEMENT_PRICE)
            .aciValue(TestBond2.ACI_VALUE)
            .country(TestBond2.COUNTRY)
            .sector(TestBond2.SECTOR)
            .issueSize(TestBond2.ISSUE_SIZE)
            .issueSizePlan(TestBond2.ISSUE_SIZE_PLAN)
            .tradingStatus(TestBond2.TRADING_STATUS)
            .buyAvailable(TestBond2.BUY_AVAILABLE)
            .sellAvailable(TestBond2.SELL_AVAILABLE)
            .apiTradeAvailable(TestBond2.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestBond2.MIN_PRICE_INCREMENT)
            .first1MinCandleDate(TestBond2.FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(TestBond2.FIRST_1_DAY_CANDLE_DATE)
            .build();

}