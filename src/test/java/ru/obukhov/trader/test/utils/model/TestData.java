package ru.obukhov.trader.test.utils.model;

import com.google.protobuf.Timestamp;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.PortfolioPosition;
import ru.tinkoff.piapi.contract.v1.PortfolioResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class TestData {

    private static final Random RANDOM = new Random();

    public static final ConservativeStrategy CONSERVATIVE_STRATEGY = new ConservativeStrategy(StrategyType.CONSERVATIVE.getValue());

    public static final String ACCOUNT_ID1 = "2000124699";
    public static final String ACCOUNT_ID2 = "2000124698";

    // region DecisionData creation

    public static DecisionData createDecisionData(
            final double averagePositionPrice,
            final int positionLotsCount,
            final int lotSize,
            final double currentPrice
    ) {
        final DecisionData decisionData = new DecisionData();
        final Position portfolioPosition = new PositionBuilder()
                .setAveragePositionPrice(averagePositionPrice)
                .setQuantityLots(positionLotsCount)
                .build();

        decisionData.setPosition(portfolioPosition);
        decisionData.setCurrentCandles(List.of(new CandleBuilder().setOpen(currentPrice).build()));
        decisionData.setShare(Share.newBuilder().setLot(lotSize).build());
        decisionData.setCommission(QuotationUtils.ZERO);

        return decisionData;
    }

    public static DecisionData createDecisionData(final double balance, final double currentPrice, final int lotSize, final double commission) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setBalance(QuotationUtils.newQuotation(balance));
        decisionData.setCurrentCandles(List.of(new CandleBuilder().setOpen(currentPrice).build()));
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setShare(Share.newBuilder().setLot(lotSize).build());
        decisionData.setCommission(QuotationUtils.newQuotation(commission));

        return decisionData;
    }

    // endregion

    public static PortfolioPosition createPortfolioPosition(
            final String figi,
            final InstrumentType instrumentType,
            final long quantity,
            final long averagePositionPrice,
            final long expectedYield,
            final long currentPrice,
            final long quantityLots,
            final String currency
    ) {
        return PortfolioPosition.newBuilder()
                .setFigi(figi)
                .setInstrumentType(instrumentType.name())
                .setQuantity(createQuotation(quantity))
                .setAveragePositionPrice(createMoneyValue(averagePositionPrice, currency))
                .setExpectedYield(createQuotation(expectedYield))
                .setCurrentNkd(createMoneyValue(currency))
                .setAveragePositionPricePt(createQuotation())
                .setCurrentPrice(createMoneyValue(currentPrice, currency))
                .setAveragePositionPriceFifo(createMoneyValue(currency))
                .setQuantityLots(createQuotation(quantityLots))
                .build();
    }

    // region Operation

    public static Operation createOperation(
            final Timestamp operationTimestamp,
            final OperationType operationType,
            final double operationPrice,
            final long operationQuantity,
            final String figi
    ) {
        return Operation.newBuilder()
                .setDate(operationTimestamp)
                .setOperationType(operationType)
                .setPrice(createMoneyValue(operationPrice))
                .setQuantity(operationQuantity)
                .setFigi(figi)
                .build();
    }

    public static Operation createOperation(final OperationState state) {
        return Operation.newBuilder()
                .setState(state)
                .build();
    }

    public static Operation createOperation() {
        return Operation.newBuilder()
                .build();
    }

    // endregion

    // region BigDecimals list creation

    public static List<BigDecimal> createBigDecimals(final List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createBigDecimals(final Double... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createBigDecimals(final Integer... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createRandomBigDecimals(final int size) {
        final List<BigDecimal> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(DecimalUtils.setDefaultScale(RANDOM.nextDouble()));
        }
        return values;
    }

    // endregion

    public static List<Long> createRandomLongs(final int size, final long origin, final long bound) {
        final List<Long> doubles = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            doubles.add(RANDOM.nextLong(origin, bound));
        }

        return doubles;
    }

    @SneakyThrows
    public static CronExpression createCronExpression(final String expression) {
        return new CronExpression(expression);
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
            balanceConfig.setInitialBalance(QuotationUtils.newQuotation(initialBalance));
        }

        if (balanceIncrement != null) {
            balanceConfig.setBalanceIncrement(QuotationUtils.newQuotation(balanceIncrement));
        }

        if (balanceIncrementCron != null) {
            balanceConfig.setBalanceIncrementCron(createCronExpression(balanceIncrementCron));
        }

        return balanceConfig;
    }

    // endregion

    public static OrderState createOrderState(final String orderId, final String figi) {
        return OrderState.newBuilder()
                .setOrderId(orderId)
                .setFigi(figi)
                .build();
    }

    // region Quotation creation

    public static Quotation createQuotation(final long units) {
        return Quotation.newBuilder().setUnits(units).build();
    }

    public static Quotation createQuotation() {
        return Quotation.newBuilder().build();
    }

    // endregion

    // region Quotations collection creation

    public static List<Quotation> createQuotations(final List<Double> values) {
        return values.stream().map(QuotationUtils::newQuotation).collect(Collectors.toList());
    }

    public static List<Quotation> createQuotations(final Double... values) {
        return Arrays.stream(values).map(QuotationUtils::newQuotation).collect(Collectors.toList());
    }

    public static List<Quotation> createQuotations(final Integer... values) {
        return Arrays.stream(values).map(QuotationUtils::newQuotation).collect(Collectors.toList());
    }

    public static List<Quotation> createRandomQuotations(final int size, final long origin, final long bound) {
        final List<Quotation> quotations = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            quotations.add(createRandomQuotation(origin, bound));
        }

        return quotations;
    }

    public static Quotation createRandomQuotation(final long origin, final long bound) {
        long unit = RANDOM.nextLong(origin, bound);
        int nano = RANDOM.nextInt(1, 1000000000);

        if (RANDOM.nextInt(1000) == 0) {
            if (RANDOM.nextBoolean()) {
                unit = 0;
            } else {
                nano = 0;
            }
        }

        return QuotationUtils.newNormalizedQuotation(unit, nano);
    }

    // endregion

    // region MoneyValue creation

    public static MoneyValue createMoneyValue(final String currency) {
        return MoneyValue.newBuilder().setCurrency(currency).build();
    }

    public static MoneyValue createMoneyValue(final double value) {
        return DataStructsHelper.createMoneyValue(StringUtils.EMPTY, DecimalUtils.setDefaultScale(value));
    }

    public static MoneyValue createMoneyValue(final double value, final String currency) {
        return DataStructsHelper.createMoneyValue(currency, DecimalUtils.setDefaultScale(value));
    }

    public static MoneyValue createMoneyValue(final long units, final int nano, final String currency) {
        return MoneyValue.newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNano(nano)
                .build();
    }

    // endregion

    public static Money createMoney(final int value, final String currency) {
        return DataStructsHelper.createMoney(DecimalUtils.setDefaultScale(value), currency);
    }

    public static Portfolio createPortfolio(final PortfolioPosition... portfolioPositions) {
        final PortfolioResponse.Builder builder = PortfolioResponse.newBuilder()
                .setTotalAmountShares(createMoneyValue(Currencies.RUB))
                .setTotalAmountBonds(createMoneyValue(Currencies.RUB))
                .setTotalAmountEtf(createMoneyValue(Currencies.RUB))
                .setTotalAmountCurrencies(createMoneyValue(Currencies.RUB))
                .setTotalAmountFutures(createMoneyValue(Currencies.RUB))
                .setExpectedYield(createQuotation());
        for (final PortfolioPosition portfolioPosition : portfolioPositions) {
            builder.addPositions(portfolioPosition);
        }

        final PortfolioResponse portfolioResponse = builder.build();
        return Portfolio.fromResponse(portfolioResponse);
    }

    public static TradingDay createTradingDay(final boolean isTradingDay, Timestamp startTimestamp, final Timestamp endTimestamp) {
        return TradingDay.newBuilder()
                .setDate(TimestampUtils.toStartOfDay(startTimestamp))
                .setIsTradingDay(isTradingDay)
                .setStartTime(startTimestamp)
                .setEndTime(endTimestamp)
                .build();
    }

    public static TradingDay createTradingDay(final Timestamp startTimestamp, final Timestamp endTimestamp) {
        return TradingDay.newBuilder()
                .setDate(startTimestamp)
                .setIsTradingDay(true)
                .setStartTime(startTimestamp)
                .setEndTime(endTimestamp)
                .build();
    }

    public static List<TradingDay> createTradingSchedule(final Timestamp startTimestamp, final OffsetTime endTime, final int daysCount) {
        List<TradingDay> schedule = new ArrayList<>();
        for (int i = 0; i < daysCount; i++) {
            final Timestamp currentStartDateTime = TimestampUtils.plusDays(startTimestamp, i);
            final Timestamp currentEndDateTime = TimestampUtils.setTime(currentStartDateTime, endTime);
            final boolean isTradingDay = TimestampUtils.isWorkDay(currentStartDateTime);
            schedule.add(createTradingDay(isTradingDay, currentStartDateTime, currentEndDateTime));
        }

        return schedule;
    }

    /**
     * @return new Interval with {@code from} is start of given date and {@code to} is end of given date
     */
    public static Interval createIntervalOfDay(@NotNull final Timestamp timestamp) {
        final Timestamp from = TimestampUtils.toStartOfDay(timestamp);
        final Timestamp to = TimestampUtils.toEndOfDay(from);
        return Interval.of(from, to);
    }

}