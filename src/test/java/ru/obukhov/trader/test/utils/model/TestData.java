package ru.obukhov.trader.test.utils.model;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.mapstruct.factory.Mappers;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.Money;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
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

    public static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    public static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
    public static final ConservativeStrategy CONSERVATIVE_STRATEGY = new ConservativeStrategy(StrategyType.CONSERVATIVE.getValue());

    // region HistoricCandle creation

    public static HistoricCandle createHistoricCandle(
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice,
            final OffsetDateTime time,
            final boolean isComplete
    ) {
        return HistoricCandle.newBuilder()
                .setOpen(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(openPrice)))
                .setClose(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(closePrice)))
                .setHigh(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(highestPrice)))
                .setLow(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(lowestPrice)))
                .setTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(time))
                .setIsComplete(isComplete)
                .build();
    }

    public static HistoricCandle createHistoricCandleOpen(final double openPrice, final OffsetDateTime time) {
        return HistoricCandle.newBuilder()
                .setOpen(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(openPrice)))
                .setTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(time))
                .setIsComplete(true)
                .build();
    }

    public static HistoricCandle createHistoricCandleClosed(final double closePrice, final OffsetDateTime time) {
        return HistoricCandle.newBuilder()
                .setClose(DecimalUtils.toQuotation(DecimalUtils.setDefaultScale(closePrice)))
                .setTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(time))
                .setIsComplete(true)
                .build();
    }

    // endregion

    // region Candle creation

    public static Candle createCandle(
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice,
            final OffsetDateTime time
    ) {
        return new Candle(
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(highestPrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(lowestPrice)),
                time
        );
    }

    public static Candle createCandleWithOpenPrice(final double openPrice) {
        return new Candle().setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
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

    public static DecisionData createDecisionData(
            final double averagePositionPrice,
            final int positionLotsCount,
            final int lotSize,
            final double currentPrice
    ) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setPosition(createPortfolioPosition(positionLotsCount, averagePositionPrice));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));
        decisionData.setShare(Share.builder().lotSize(lotSize).build());

        return decisionData;
    }

    public static DecisionData createDecisionData(final double balance, final double currentPrice, final int lotSize, final double commission) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setBalance(DecimalUtils.setDefaultScale(balance));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setShare(Share.builder().lotSize(lotSize).build());
        decisionData.setCommission(commission);

        return decisionData;
    }

    // endregion

    // region PortfolioPosition creation

    public static PortfolioPosition createPortfolioPosition() {
        return new PortfolioPosition(
                null,
                null,
                BigDecimal.ZERO,
                createMoney(Currency.RUB, 0),
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO
        );
    }

    public static PortfolioPosition createPortfolioPosition(final String ticker) {
        return new PortfolioPosition(
                ticker,
                null,
                BigDecimal.ZERO,
                createMoney(Currency.RUB, 0),
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO
        );
    }

    public static PortfolioPosition createPortfolioPosition(final int quantityLots) {
        return new PortfolioPosition(
                null,
                null,
                BigDecimal.ZERO,
                createMoney(Currency.RUB, 0),
                BigDecimal.ZERO,
                null,
                createIntegerDecimal(quantityLots)
        );
    }

    public static PortfolioPosition createPortfolioPosition(final String ticker, final int quantityLots) {
        return new PortfolioPosition(
                ticker,
                null,
                BigDecimal.ZERO,
                createMoney(Currency.RUB, 0),
                BigDecimal.ZERO,
                null,
                createIntegerDecimal(quantityLots)
        );
    }

    public static PortfolioPosition createPortfolioPosition(final String ticker, final int quantity, final int quantityLots) {
        return new PortfolioPosition(
                ticker,
                null,
                createIntegerDecimal(quantity),
                createMoney(Currency.RUB, 0),
                BigDecimal.ZERO,
                null,
                createIntegerDecimal(quantityLots)
        );
    }

    public static PortfolioPosition createPortfolioPosition(final int quantityLots, final double averagePositionPrice) {
        return new PortfolioPosition(
                null,
                null,
                BigDecimal.ZERO,
                createMoney(Currency.RUB, averagePositionPrice),
                BigDecimal.ZERO,
                null,
                createIntegerDecimal(quantityLots)
        );
    }

    public static PortfolioPosition createPortfolioPosition(
            final String ticker,
            final InstrumentType instrumentType,
            final double quantity,
            final double averagePositionPrice,
            final double expectedYield,
            final double currentPrice,
            final long quantityLots,
            final Currency currency
    ) {
        return new PortfolioPosition(
                ticker,
                instrumentType,
                createIntegerDecimal(quantity),
                createMoney(currency, averagePositionPrice),
                DecimalUtils.setDefaultScale(expectedYield),
                createMoney(currency, currentPrice),
                createIntegerDecimal(quantityLots)
        );
    }

    public static PortfolioPosition createPortfolioPosition(
            final String ticker,
            final InstrumentType instrumentType,
            final long quantityLots,
            final long lotSize,
            final Currency currency,
            final double averagePositionPrice,
            final double expectedYield,
            final double currentPrice
    ) {
        return new PortfolioPosition(
                ticker,
                instrumentType,
                createIntegerDecimal(quantityLots * lotSize),
                createMoney(currency, averagePositionPrice),
                DecimalUtils.setDefaultScale(expectedYield),
                createMoney(currency, currentPrice),
                createIntegerDecimal(quantityLots)
        );
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
            final Currency currency
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
            final long operationQuantity
    ) {
        return Operation.newBuilder()
                .setDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(operationDateTime))
                .setOperationType(operationType)
                .setPrice(MONEY_VALUE_MAPPER.doubleToMoneyValue(operationPrice))
                .setQuantity(operationQuantity)
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

    // region OrderState creation

    public static OrderState createOrderState(final String orderId, final String figi) {
        return OrderState.newBuilder()
                .setOrderId(orderId)
                .setFigi(figi)
                .build();
    }

    public static OrderState createOrderState(
            final Currency currency,
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
                .setCurrency(currency.name())
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
            final Currency currency,
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
                BigDecimal.valueOf(initialOrderPrice),
                BigDecimal.valueOf(totalOrderAmount),
                BigDecimal.valueOf(averagePositionPrice),
                BigDecimal.valueOf(executedCommission),
                figi,
                orderDirection,
                BigDecimal.valueOf(initialSecurityPrice),
                BigDecimal.valueOf(serviceCommission),
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

    public static ru.tinkoff.piapi.contract.v1.MoneyValue createTinkoffMoneyValue(final double value, final Currency currency) {
        return DataStructsHelper.createMoneyValue(currency, BigDecimal.valueOf(value));
    }

    public static ru.tinkoff.piapi.contract.v1.MoneyValue createTinkoffMoneyValue(final Currency currency) {
        return ru.tinkoff.piapi.contract.v1.MoneyValue.newBuilder().setCurrency(currency.name()).build();
    }

    // endregion

    public static Money createMoney(final Currency currency, final double value) {
        return new Money(currency, DecimalUtils.setDefaultScale(value));
    }

    public static Money createMoney(final Currency currency, final long value) {
        return new Money(currency, DecimalUtils.setDefaultScale(value));
    }

    public static ru.tinkoff.piapi.core.models.Money createMoney(final Currency currency, final BigDecimal value) {
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

    public static Share createShare(final String ticker, final Currency currency, final int lotSize) {
        return Share.builder()
                .ticker(ticker)
                .currency(currency)
                .lotSize(lotSize)
                .build();
    }

    public static Share createShare(final String figi, final String ticker, final Currency currency, final int lotSize) {
        return Share.builder()
                .figi(figi)
                .ticker(ticker)
                .currency(currency)
                .lotSize(lotSize)
                .build();
    }

    public static ru.tinkoff.piapi.contract.v1.Share createTinkoffShare(
            final String figi,
            final String ticker,
            final Currency currency,
            final int lotSize
    ) {
        return ru.tinkoff.piapi.contract.v1.Share.newBuilder()
                .setFigi(figi)
                .setTicker(ticker)
                .setCurrency(currency.name().toLowerCase())
                .setLot(lotSize)
                .build();
    }

    public static OrderStage createOrderStage(final Currency currency, final double price, final long quantity, final String tradeId) {
        return OrderStage.newBuilder()
                .setPrice(createTinkoffMoneyValue(price, currency))
                .setQuantity(quantity)
                .setTradeId(tradeId)
                .build();
    }

    public static AssetInstrument createAssetInstrument(final String figi, final String ticker) {
        return AssetInstrument.newBuilder()
                .setFigi(figi)
                .setTicker(ticker)
                .build();
    }

    /**
     * @return BigDecimal equal to given {@code value} with zero scale
     * @throws ArithmeticException if given {@code value} is not integer
     */
    public static BigDecimal createIntegerDecimal(final double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.UNNECESSARY);
    }

}