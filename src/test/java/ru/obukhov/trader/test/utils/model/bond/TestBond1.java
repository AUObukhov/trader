package ru.obukhov.trader.test.utils.model.bond;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Sector;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestSecurityData;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestBond1 extends TestSecurityData {

    public static final String FIGI = "BBG00J7HHGH1";
    public static final String TICKER = "RU000A0ZYG52";
    public static final int LOT_SIZE = 1;
    public static final Currency CURRENCY = Currency.RUB;
    public static final String NAME = "Ростелеком выпуск 3";
    public static final Exchange EXCHANGE = Exchange.MOEX;
    public static final OffsetDateTime MATURITY_DATE = DateTimeTestData.createDateTime(2027, 11, 9, 3);
    public static final BigDecimal NOMINAL = DecimalUtils.setDefaultScale(1000);
    public static final OffsetDateTime STATE_REG_DATE = DateTimeTestData.createDateTime(2017, 11, 16, 3);
    public static final OffsetDateTime PLACEMENT_DATE = DateTimeTestData.createDateTime(2017, 11, 21, 3);
    public static final BigDecimal PLACEMENT_PRICE = DecimalUtils.setDefaultScale(1000);
    public static final BigDecimal ACI_VALUE = DecimalUtils.setDefaultScale(30.800000000);
    public static final String COUNTRY = "Российская Федерация";
    public static final Sector SECTOR = Sector.TELECOM;
    public static final long ISSUE_SIZE = 10000000;
    public static final long ISSUE_SIZE_PLAN = 10000000;
    public static final SecurityTradingStatus TRADING_STATUS = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
    public static final boolean BUY_AVAILABLE = true;
    public static final boolean SELL_AVAILABLE = true;
    public static final boolean API_TRADE_AVAILABLE = true;
    public static final BigDecimal MIN_PRICE_INCREMENT = DecimalUtils.setDefaultScale(0.01);
    public static final OffsetDateTime FIRST_1_MIN_CANDLE_DATE = DateTimeTestData.createDateTime(2018, 3, 14, 9);
    public static final OffsetDateTime FIRST_1_DAY_CANDLE_DATE = DateTimeTestData.createDateTime(2017, 11, 21, 3);

    public static final ru.tinkoff.piapi.contract.v1.Bond TINKOFF_BOND = ru.tinkoff.piapi.contract.v1.Bond.newBuilder()
            .setFigi(TestBond1.FIGI)
            .setTicker(TestBond1.TICKER)
            .setLot(TestBond1.LOT_SIZE)
            .setCurrency(TestBond1.CURRENCY.name().toLowerCase())
            .setName(TestBond1.NAME)
            .setExchange(TestBond1.EXCHANGE.getValue())
            .setMaturityDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond1.MATURITY_DATE))
            .setNominal(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond1.NOMINAL))
            .setStateRegDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond1.STATE_REG_DATE))
            .setPlacementDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond1.PLACEMENT_DATE))
            .setPlacementPrice(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond1.PLACEMENT_PRICE))
            .setAciValue(MONEY_VALUE_MAPPER.bigDecimalToMoneyValue(TestBond1.ACI_VALUE))
            .setCountryOfRiskName(TestBond1.COUNTRY)
            .setSector(TestBond1.SECTOR.name().toLowerCase())
            .setIssueSize(TestBond1.ISSUE_SIZE)
            .setIssueSizePlan(TestBond1.ISSUE_SIZE_PLAN)
            .setTradingStatus(TestBond1.TRADING_STATUS)
            .setBuyAvailableFlag(TestBond1.BUY_AVAILABLE)
            .setSellAvailableFlag(TestBond1.SELL_AVAILABLE)
            .setApiTradeAvailableFlag(TestBond1.API_TRADE_AVAILABLE)
            .setMinPriceIncrement(QUOTATION_MAPPER.fromBigDecimal(TestBond1.MIN_PRICE_INCREMENT))
            .setFirst1MinCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond1.FIRST_1_MIN_CANDLE_DATE))
            .setFirst1DayCandleDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(TestBond1.FIRST_1_DAY_CANDLE_DATE))
            .build();

    public static final Bond BOND = Bond.builder()
            .figi(TestBond1.FIGI)
            .ticker(TestBond1.TICKER)
            .lotSize(TestBond1.LOT_SIZE)
            .currency(TestBond1.CURRENCY)
            .name(TestBond1.NAME)
            .exchange(TestBond1.EXCHANGE)
            .maturityDate(TestBond1.MATURITY_DATE)
            .nominal(TestBond1.NOMINAL)
            .stateRegDate(TestBond1.STATE_REG_DATE)
            .placementDate(TestBond1.PLACEMENT_DATE)
            .placementPrice(TestBond1.PLACEMENT_PRICE)
            .aciValue(TestBond1.ACI_VALUE)
            .country(TestBond1.COUNTRY)
            .sector(TestBond1.SECTOR)
            .issueSize(TestBond1.ISSUE_SIZE)
            .issueSizePlan(TestBond1.ISSUE_SIZE_PLAN)
            .tradingStatus(TestBond1.TRADING_STATUS)
            .buyAvailable(TestBond1.BUY_AVAILABLE)
            .sellAvailable(TestBond1.SELL_AVAILABLE)
            .apiTradeAvailable(TestBond1.API_TRADE_AVAILABLE)
            .minPriceIncrement(TestBond1.MIN_PRICE_INCREMENT)
            .first1MinCandleDate(TestBond1.FIRST_1_MIN_CANDLE_DATE)
            .first1DayCandleDate(TestBond1.FIRST_1_DAY_CANDLE_DATE)
            .build();

}