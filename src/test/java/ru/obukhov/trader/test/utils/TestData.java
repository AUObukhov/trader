package ru.obukhov.trader.test.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.MarketInstrumentList;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.OperationStatus;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.OperationTypeWithCommission;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.OrderStatus;
import ru.obukhov.trader.market.model.OrderType;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.web.client.exchange.MarketInstrumentListResponse;
import ru.obukhov.trader.web.model.BalanceConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class TestData {

    public static final ConservativeStrategy CONSERVATIVE_STRATEGY = new ConservativeStrategy(StrategyType.CONSERVATIVE.getValue());

    // region Tinkoff Candle creation

    public static Candle createTinkoffCandle(
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice
    ) {
        return createTinkoffCandle(CandleInterval.DAY, openPrice, closePrice, highestPrice, lowestPrice);
    }

    public static Candle createTinkoffCandle(
            final CandleInterval interval,
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice
    ) {
        return createTinkoffCandle(interval, openPrice, closePrice, highestPrice, lowestPrice, OffsetDateTime.now());
    }

    public static Candle createTinkoffCandle(
            final CandleInterval interval,
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice,
            final OffsetDateTime time
    ) {
        return new Candle()
                .setInterval(interval)
                .setOpenPrice(DecimalUtils.setDefaultScale(openPrice))
                .setClosePrice(DecimalUtils.setDefaultScale(closePrice))
                .setHighestPrice(DecimalUtils.setDefaultScale(highestPrice))
                .setLowestPrice(DecimalUtils.setDefaultScale(lowestPrice))
                .setTime(time);
    }

    // endregion

    // region Candle creation

    public static Candle createCandle(
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice,
            final OffsetDateTime time,
            final CandleInterval interval
    ) {
        return new Candle(
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(highestPrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(lowestPrice)),
                time,
                interval
        );
    }

    public static Candle createCandleWithOpenPrice(final double openPrice) {
        return new Candle().setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
    }

    public static Candle createCandleWithOpenPriceAndTime(final double openPrice, final OffsetDateTime time) {
        return createCandleWithOpenPrice(openPrice).setTime(time);
    }

    public static Candle createCandleWithClosePrice(final double closePrice) {
        return new Candle()
                .setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
    }

    public static Candle createCandleWithClosePriceAndTime(final double closePrice, final OffsetDateTime time) {
        return createCandleWithClosePrice(closePrice)
                .setTime(time);
    }

    public static Candle createCandleWithOpenPriceAndClosePrice(final double openPrice, final double closePrice) {
        return new Candle()
                .setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)))
                .setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
    }

    // endregion

    // region DecisionData creation

    public static DecisionData createDecisionData(final Candle... candles) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(List.of(candles));
        return decisionData;
    }

    public static DecisionData createDecisionData(
            final double averagePositionPrice,
            final int positionLotsCount,
            final int lotSize,
            final double currentPrice
    ) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setPosition(createPortfolioPosition(averagePositionPrice, positionLotsCount));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));
        decisionData.setInstrument(createMarketInstrument(lotSize));

        return decisionData;
    }

    public static DecisionData createDecisionData(final double balance, final double currentPrice, final int lotSize, final double commission) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setBalance(DecimalUtils.setDefaultScale(balance));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setInstrument(createMarketInstrument(lotSize));
        decisionData.setCommission(commission);

        return decisionData;
    }

    // endregion

    // region PortfolioPosition creation

    public static PortfolioPosition createPortfolioPosition(final String ticker) {
        return createPortfolioPosition(ticker, 1);
    }

    public static PortfolioPosition createPortfolioPosition(final double averagePositionPrice, final int lotsCount) {
        return new PortfolioPosition()
                .setBalance(BigDecimal.ZERO)
                .setAveragePositionPrice(createMoneyAmount(Currency.USD, averagePositionPrice))
                .setCount(lotsCount)
                .setName(StringUtils.EMPTY);
    }

    public static PortfolioPosition createPortfolioPosition(final String ticker, final int lotsCount) {
        return new PortfolioPosition()
                .setTicker(ticker)
                .setBalance(BigDecimal.ZERO)
                .setCount(lotsCount)
                .setName(StringUtils.EMPTY);
    }

    public static PortfolioPosition createPortfolioPosition(final int lotsCount) {
        return new PortfolioPosition()
                .setBalance(BigDecimal.ZERO)
                .setCount(lotsCount)
                .setName(StringUtils.EMPTY);
    }

    // endregion

    // region CurrencyPosition creation

    public static CurrencyPosition createCurrencyPosition(final Currency currency, final long balance) {
        return new CurrencyPosition(currency, DecimalUtils.setDefaultScale(balance), null);
    }

    public static CurrencyPosition createCurrencyPosition(final Currency currency, final long balance, final long blocked) {
        return new CurrencyPosition(currency, DecimalUtils.setDefaultScale(balance), DecimalUtils.setDefaultScale(blocked));
    }

    // endregion

    // region Operation

    public static Operation createOperation(
            final OffsetDateTime operationDateTime,
            final OperationTypeWithCommission operationType,
            final double operationPrice,
            final int operationQuantity,
            final double commissionValue
    ) {
        return new Operation(
                null,
                null,
                null,
                createMoneyAmount(Currency.RUB, commissionValue),
                null,
                null,
                DecimalUtils.setDefaultScale(operationPrice),
                operationQuantity,
                operationQuantity,
                null,
                null,
                null,
                operationDateTime,
                operationType
        );
    }

    public static Operation createOperation(final OperationStatus operationStatus) {
        return new Operation(
                null,
                operationStatus,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Operation createOperation() {
        return new Operation(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    // endregion

    public static MoneyAmount createMoneyAmount(final Currency currency, final double value) {
        return new MoneyAmount(currency, DecimalUtils.setDefaultScale(value));
    }

    public static MoneyAmount createMoneyAmount(final Currency currency, final long value) {
        return new MoneyAmount(currency, DecimalUtils.setDefaultScale(value));
    }

    // region MarketInstrument creation

    public static MarketInstrument createMarketInstrument() {
        return new MarketInstrument(
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                null,
                1,
                null,
                Currency.RUB,
                StringUtils.EMPTY,
                InstrumentType.STOCK
        );
    }

    public static MarketInstrument createMarketInstrument(final String ticker) {
        return new MarketInstrument(
                StringUtils.EMPTY,
                ticker,
                StringUtils.EMPTY,
                null,
                1,
                null,
                Currency.RUB,
                StringUtils.EMPTY,
                InstrumentType.STOCK
        );
    }

    public static MarketInstrument createMarketInstrument(final int lotSize) {
        return new MarketInstrument(
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                null,
                lotSize,
                null,
                Currency.RUB,
                StringUtils.EMPTY,
                InstrumentType.STOCK
        );
    }

    public static MarketInstrument createMarketInstrument(final String ticker, final int lotSize) {
        return new MarketInstrument(
                StringUtils.EMPTY,
                ticker,
                StringUtils.EMPTY,
                null,
                lotSize,
                null,
                Currency.RUB,
                StringUtils.EMPTY,
                InstrumentType.STOCK
        );
    }

    public static MarketInstrument createMarketInstrument(final String ticker, final String figi) {
        return new MarketInstrument(
                figi,
                ticker,
                StringUtils.EMPTY,
                null,
                1,
                null,
                Currency.RUB,
                StringUtils.EMPTY,
                InstrumentType.STOCK
        );
    }

    // endregion

    // region BigDecimals list creation

    public static List<BigDecimal> createBigDecimalsList(final List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createBigDecimalsList(final Double... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createBigDecimalsList(final Integer... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createRandomBigDecimalsList(final int size) {
        final Random random = new Random();
        final List<BigDecimal> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(DecimalUtils.setDefaultScale(random.nextDouble()));
        }
        return values;
    }

    // endregion

    @SneakyThrows
    public static CronExpression createCronExpression(final String expression) {
        return new CronExpression(expression);
    }

    public static MarketProperties createMarketProperties() {
        final OffsetTime workStartTime = DateTimeTestData.createTime(10, 0, 0);
        final WorkSchedule workSchedule = new WorkSchedule(workStartTime, Duration.ofHours(9));
        final OffsetDateTime startDate = DateTimeTestData.createDateTime(2000, 1, 1);
        return new MarketProperties(workSchedule, 7, startDate);
    }

    // region BalanceConfig creation

    public static BalanceConfig createBalanceConfig(final Double initialBalance) {
        return createBalanceConfig(initialBalance, null);
    }

    public static BalanceConfig createBalanceConfig(final Double initialBalance, final Double balanceIncrement) {
        return createBalanceConfig(initialBalance, balanceIncrement, null);
    }

    public static BalanceConfig createBalanceConfig(final Double initialBalance, final Double balanceIncrement, final String balanceIncrementCron) {
        final BalanceConfig balanceConfig = new BalanceConfig();

        if (initialBalance != null) {
            balanceConfig.setInitialBalance(DecimalUtils.setDefaultScale(initialBalance));
        }

        if (balanceIncrement != null) {
            balanceConfig.setBalanceIncrement(DecimalUtils.setDefaultScale(balanceIncrement));
        }

        if (balanceIncrementCron != null) {
            balanceConfig.setBalanceIncrementCron(createCronExpression(balanceIncrementCron));
        }

        return balanceConfig;
    }

    // endregion

    public static MarketInstrumentListResponse createMarketInstrumentListResponse(List<MarketInstrument> instruments) {
        final MarketInstrumentList marketInstrumentList = new MarketInstrumentList(BigDecimal.valueOf(instruments.size()), instruments);

        final MarketInstrumentListResponse response = new MarketInstrumentListResponse();
        response.setPayload(marketInstrumentList);
        return response;
    }

    // region Order creation

    public Order createOrder(String id, String figi) {
        return new Order(id, figi, OperationType.BUY, OrderStatus.FILL, 1, 1, OrderType.MARKET, BigDecimal.TEN);
    }

    public static Order createOrder() {
        return new Order(null, null, null, null, null, null, null, null);
    }

    // endregion

}