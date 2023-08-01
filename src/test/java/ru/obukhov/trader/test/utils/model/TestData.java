package ru.obukhov.trader.test.utils.model;

import com.google.protobuf.Timestamp;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.mapstruct.factory.Mappers;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Money;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderStage;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.PortfolioResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class TestData {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
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
        final PortfolioPosition portfolioPosition = new PortfolioPositionBuilder()
                .setQuantityLots(positionLotsCount)
                .setAveragePositionPrice(averagePositionPrice)
                .build();
        decisionData.setPosition(portfolioPosition);
        decisionData.setCurrentCandles(List.of(new CandleBuilder().setOpenPrice(currentPrice).build()));
        decisionData.setShare(Share.builder().lotSize(lotSize).build());

        return decisionData;
    }

    public static DecisionData createDecisionData(final double balance, final double currentPrice, final int lotSize, final double commission) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setBalance(DecimalUtils.setDefaultScale(balance));
        decisionData.setCurrentCandles(List.of(new CandleBuilder().setOpenPrice(currentPrice).build()));
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setShare(Share.builder().lotSize(lotSize).build());
        decisionData.setCommission(commission);

        return decisionData;
    }

    // endregion

    public static ru.tinkoff.piapi.contract.v1.PortfolioPosition createTinkoffPortfolioPosition(
            final String figi,
            final InstrumentType instrumentType,
            final long quantity,
            final long averagePositionPrice,
            final long expectedYield,
            final long currentPrice,
            final long quantityLots,
            final String currency
    ) {
        return ru.tinkoff.piapi.contract.v1.PortfolioPosition.newBuilder()
                .setFigi(figi)
                .setInstrumentType(instrumentType.name())
                .setQuantity(createQuotation(quantity))
                .setAveragePositionPrice(createTinkoffMoneyValue(averagePositionPrice, currency))
                .setExpectedYield(createQuotation(expectedYield))
                .setCurrentNkd(createTinkoffMoneyValue(currency))
                .setAveragePositionPricePt(createQuotation())
                .setCurrentPrice(createTinkoffMoneyValue(currentPrice, currency))
                .setAveragePositionPriceFifo(createTinkoffMoneyValue(currency))
                .setQuantityLots(createQuotation(quantityLots))
                .build();
    }

    // region Operation

    public static Operation createOperation(
            final OffsetDateTime operationDateTime,
            final OperationType operationType,
            final double operationPrice,
            final long operationQuantity,
            final String figi
    ) {
        return Operation.newBuilder()
                .setDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(operationDateTime))
                .setOperationType(operationType)
                .setPrice(MONEY_VALUE_MAPPER.doubleToMoneyValue(operationPrice))
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

    // region OrderState creation

    public static OrderState createOrderState(final String orderId, final String figi) {
        return OrderState.newBuilder()
                .setOrderId(orderId)
                .setFigi(figi)
                .build();
    }

    public static OrderState createOrderState(
            final String currency,
            final String orderId,
            final OrderExecutionReportStatus executionReportStatus,
            final int lotsRequested,
            final int lotsExecuted,
            final double initialOrderPrice,
            final double totalOrderAmount,
            final double averagePositionPrice,
            final double initialCommission,
            final double executedCommission,
            final String figi,
            final OrderDirection orderDirection,
            final double initialSecurityPrice,
            final List<OrderStage> stages,
            final double serviceCommission,
            final ru.tinkoff.piapi.contract.v1.OrderType orderType,
            final OffsetDateTime orderDate
    ) {
        return OrderState.newBuilder()
                .setOrderId(orderId)
                .setExecutionReportStatus(executionReportStatus)
                .setLotsRequested(lotsRequested)
                .setLotsExecuted(lotsExecuted)
                .setInitialOrderPrice(TestData.createTinkoffMoneyValue(initialOrderPrice, currency))
                .setTotalOrderAmount(TestData.createTinkoffMoneyValue(totalOrderAmount, currency))
                .setAveragePositionPrice(TestData.createTinkoffMoneyValue(averagePositionPrice, currency))
                .setInitialCommission(TestData.createTinkoffMoneyValue(initialCommission, currency))
                .setExecutedCommission(TestData.createTinkoffMoneyValue(executedCommission, currency))
                .setFigi(figi)
                .setDirection(orderDirection)
                .setInitialSecurityPrice(TestData.createTinkoffMoneyValue(initialSecurityPrice, currency))
                .addAllStages(stages)
                .setServiceCommission(TestData.createTinkoffMoneyValue(serviceCommission, currency))
                .setCurrency(currency)
                .setOrderType(orderType)
                .setOrderDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(orderDate))
                .build();
    }

    // endregion

    // region Order creation

    public static Order createOrder() {
        return new Order(
                null,
                null,
                0,
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

    public static Order createOrder(
            final String currency,
            final String orderId,
            final OrderExecutionReportStatus executionReportStatus,
            final int lotsExecuted,
            final double initialOrderPrice,
            final double totalOrderAmount,
            final double averagePositionPrice,
            final double executedCommission,
            final String figi,
            final OrderDirection orderDirection,
            final double initialSecurityPrice,
            final double serviceCommission,
            final ru.tinkoff.piapi.contract.v1.OrderType orderType,
            final OffsetDateTime orderDate
    ) {
        return new Order(
                orderId,
                executionReportStatus,
                lotsExecuted,
                DecimalUtils.setDefaultScale(initialOrderPrice),
                BigDecimal.valueOf(totalOrderAmount),
                DecimalUtils.setDefaultScale(averagePositionPrice),
                DecimalUtils.setDefaultScale(executedCommission),
                figi,
                orderDirection,
                DecimalUtils.setDefaultScale(initialSecurityPrice),
                DecimalUtils.setDefaultScale(serviceCommission),
                currency,
                orderType,
                orderDate
        );
    }

    // endregion

    // region Quotation creation

    public static Quotation createQuotation(final long units) {
        return Quotation.newBuilder().setUnits(units).build();
    }

    public static Quotation createQuotation() {
        return Quotation.newBuilder().build();
    }

    // endregion

    // region MoneyValue creation

    public static ru.tinkoff.piapi.contract.v1.MoneyValue createTinkoffMoneyValue(final double value, final String currency) {
        return DataStructsHelper.createMoneyValue(currency, DecimalUtils.setDefaultScale(value));
    }

    public static ru.tinkoff.piapi.contract.v1.MoneyValue createTinkoffMoneyValue(final String currency) {
        return ru.tinkoff.piapi.contract.v1.MoneyValue.newBuilder().setCurrency(currency).build();
    }

    // endregion

    public static Money createMoney(final String currency, final double value) {
        return new Money(currency, DecimalUtils.setDefaultScale(value));
    }

    public static ru.tinkoff.piapi.core.models.Money createMoney(final String currency, final BigDecimal value) {
        return DataStructsHelper.createMoney(currency, value);
    }

    public static Portfolio createPortfolio(final ru.tinkoff.piapi.contract.v1.PortfolioPosition... portfolioPositions) {
        final PortfolioResponse.Builder builder = PortfolioResponse.newBuilder()
                .setTotalAmountShares(createTinkoffMoneyValue(Currency.RUB))
                .setTotalAmountBonds(createTinkoffMoneyValue(Currency.RUB))
                .setTotalAmountEtf(createTinkoffMoneyValue(Currency.RUB))
                .setTotalAmountCurrencies(createTinkoffMoneyValue(Currency.RUB))
                .setTotalAmountFutures(createTinkoffMoneyValue(Currency.RUB))
                .setExpectedYield(createQuotation());
        for (final ru.tinkoff.piapi.contract.v1.PortfolioPosition portfolioPosition : portfolioPositions) {
            builder.addPositions(portfolioPosition);
        }

        final PortfolioResponse portfolioResponse = builder.build();
        return Portfolio.fromResponse(portfolioResponse);
    }

    public static OrderStage createOrderStage(final String currency, final double price, final long quantity, final String tradeId) {
        return OrderStage.newBuilder()
                .setPrice(createTinkoffMoneyValue(price, currency))
                .setQuantity(quantity)
                .setTradeId(tradeId)
                .build();
    }

    /**
     * @return BigDecimal equal to given {@code value} with zero scale
     * @throws ArithmeticException if given {@code value} is not integer
     */
    public static BigDecimal createIntegerDecimal(final double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.UNNECESSARY);
    }

    public static ru.tinkoff.piapi.contract.v1.TradingDay createTinkoffTradingDay(
            final boolean isTradingDay,
            final OffsetDateTime startTime,
            final OffsetDateTime endTime
    ) {
        final Timestamp startTimestamp = DATE_TIME_MAPPER.offsetDateTimeToTimestamp(startTime);
        return ru.tinkoff.piapi.contract.v1.TradingDay.newBuilder()
                .setDate(startTimestamp)
                .setIsTradingDay(isTradingDay)
                .setStartTime(startTimestamp)
                .setEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(endTime))
                .build();
    }

    public static TradingDay createTradingDay(final boolean isTradingDay, OffsetDateTime startTime, final OffsetDateTime endTime) {
        return new TradingDay(startTime, isTradingDay, startTime, endTime, null, null, null, null, null, null, null, null);
    }

    public static TradingDay createTradingDay(final OffsetDateTime startTime, final OffsetDateTime endTime) {
        return new TradingDay(startTime, true, startTime, endTime, null, null, null, null, null, null, null, null);
    }

    public static List<TradingDay> createTradingSchedule(final OffsetDateTime startDateTime, final OffsetTime endTime, final int daysCount) {
        List<TradingDay> schedule = new ArrayList<>();
        for (int i = 0; i < daysCount; i++) {
            final OffsetDateTime currentStartDateTime = startDateTime.plusDays(i);
            final OffsetDateTime currentEndDateTime = DateUtils.setTime(currentStartDateTime, endTime);
            final boolean isTradingDay = DateUtils.isWorkDay(currentStartDateTime);
            schedule.add(createTradingDay(isTradingDay, currentStartDateTime, currentEndDateTime));
        }

        return schedule;
    }
}