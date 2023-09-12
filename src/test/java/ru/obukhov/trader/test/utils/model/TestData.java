package ru.obukhov.trader.test.utils.model;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.PortfolioPosition;
import ru.tinkoff.piapi.contract.v1.PortfolioResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.Position;

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

    private static final Random RANDOM = new Random();
    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    public static final ConservativeStrategy CONSERVATIVE_STRATEGY = new ConservativeStrategy(StrategyType.CONSERVATIVE.getValue());

    // region DecisionData creation

    public static DecisionData newDecisionData(final double averagePositionPrice, final int quantity, final double currentPrice) {
        final DecisionData decisionData = new DecisionData();
        final Position portfolioPosition = new PositionBuilder()
                .setAveragePositionPrice(averagePositionPrice)
                .setQuantity(quantity)
                .build();

        decisionData.setPosition(portfolioPosition);
        decisionData.setCurrentCandles(List.of(new CandleBuilder().setOpen(currentPrice).build()));
        decisionData.setShare(Share.builder().build());
        decisionData.setCommission(DecimalUtils.ZERO);

        return decisionData;
    }

    public static DecisionData newDecisionData(final double balance, final double currentPrice, final int lotSize, final double commission) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setBalance(DecimalUtils.setDefaultScale(balance));
        decisionData.setCurrentCandles(List.of(new CandleBuilder().setOpen(currentPrice).build()));
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setShare(Share.builder().lot(lotSize).build());
        decisionData.setCommission(DecimalUtils.setDefaultScale(commission));

        return decisionData;
    }

    // endregion

    public static PortfolioPosition newPortfolioPosition(
            final String figi,
            final InstrumentType instrumentType,
            final long quantity,
            final long averagePositionPrice,
            final long expectedYield,
            final long currentPrice,
            final String currency
    ) {
        return PortfolioPosition.newBuilder()
                .setFigi(figi)
                .setInstrumentType(instrumentType.name())
                .setQuantity(newQuotation(quantity))
                .setAveragePositionPrice(newMoneyValue(averagePositionPrice, currency))
                .setExpectedYield(newQuotation(expectedYield))
                .setCurrentNkd(newMoneyValue(currency))
                .setCurrentPrice(newMoneyValue(currentPrice, currency))
                .setAveragePositionPriceFifo(newMoneyValue(currency))
                .build();
    }

    // region Operation

    public static Operation newOperation(
            final OffsetDateTime operationDateTime,
            final OperationType operationType,
            final double operationPrice,
            final long operationQuantity,
            final String figi
    ) {
        return Operation.newBuilder()
                .setDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(operationDateTime))
                .setOperationType(operationType)
                .setPrice(newMoneyValue(operationPrice))
                .setQuantity(operationQuantity)
                .setFigi(figi)
                .build();
    }

    public static Operation newOperation(final OperationState state) {
        return Operation.newBuilder()
                .setState(state)
                .build();
    }

    public static Operation newOperation() {
        return Operation.newBuilder()
                .build();
    }

    // endregion

    // region BigDecimals list creation

    public static List<BigDecimal> newBigDecimalList(final List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> newBigDecimalList(final Double... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> newBigDecimalList(final Integer... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> newRandomBigDecimalList(final int size) {
        final List<BigDecimal> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(DecimalUtils.setDefaultScale(RANDOM.nextDouble()));
        }
        return values;
    }

    public static List<BigDecimal> newRandomBigDecimalList(final int size, final long origin, final long bound) {
        final List<BigDecimal> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(DecimalUtils.setDefaultScale(RANDOM.nextDouble(origin, bound)));
        }
        return values;
    }

    // endregion

    @SneakyThrows
    public static CronExpression newCronExpression(final String expression) {
        return new CronExpression(expression);
    }

    // region BalanceConfig creation

    public static BalanceConfig newBalanceConfig(final Double initialBalance) {
        return newBalanceConfig(initialBalance, null);
    }

    public static BalanceConfig newBalanceConfig(final Double initialBalance, final Double balanceIncrement) {
        return newBalanceConfig(initialBalance, balanceIncrement, null);
    }

    public static BalanceConfig newBalanceConfig(final Double initialBalance, final Double balanceIncrement, final String balanceIncrementCron) {
        final BalanceConfig balanceConfig = new BalanceConfig();

        if (initialBalance != null) {
            balanceConfig.setInitialBalance(DecimalUtils.setDefaultScale(initialBalance));
        }

        if (balanceIncrement != null) {
            balanceConfig.setBalanceIncrement(DecimalUtils.setDefaultScale(balanceIncrement));
        }

        if (balanceIncrementCron != null) {
            balanceConfig.setBalanceIncrementCron(newCronExpression(balanceIncrementCron));
        }

        return balanceConfig;
    }

    // endregion

    public static OrderState newOrderState(final String orderId, final String figi) {
        return OrderState.newBuilder()
                .setOrderId(orderId)
                .setFigi(figi)
                .build();
    }

    // region Quotation creation

    public static Quotation newQuotation(final long units) {
        return Quotation.newBuilder().setUnits(units).build();
    }

    public static Quotation newQuotation() {
        return Quotation.newBuilder().build();
    }

    // endregion

    // region MoneyValue creation

    public static MoneyValue newMoneyValue(final String currency) {
        return MoneyValue.newBuilder().setCurrency(currency).build();
    }

    public static MoneyValue newMoneyValue(final double value) {
        return DataStructsHelper.newMoneyValue(StringUtils.EMPTY, DecimalUtils.setDefaultScale(value));
    }

    public static MoneyValue newMoneyValue(final double value, final String currency) {
        return DataStructsHelper.newMoneyValue(currency, DecimalUtils.setDefaultScale(value));
    }

    public static MoneyValue newMoneyValue(final long units, final int nano, final String currency) {
        return MoneyValue.newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNano(nano)
                .build();
    }

    // endregion

    public static Money newMoney(final int value, final String currency) {
        return DataStructsHelper.newMoney(DecimalUtils.setDefaultScale(value), currency);
    }

    public static Portfolio newPortfolio(final PortfolioPosition... portfolioPositions) {
        final PortfolioResponse.Builder builder = PortfolioResponse.newBuilder()
                .setTotalAmountShares(newMoneyValue(Currencies.RUB))
                .setTotalAmountBonds(newMoneyValue(Currencies.RUB))
                .setTotalAmountEtf(newMoneyValue(Currencies.RUB))
                .setTotalAmountCurrencies(newMoneyValue(Currencies.RUB))
                .setTotalAmountFutures(newMoneyValue(Currencies.RUB))
                .setExpectedYield(newQuotation());
        for (final PortfolioPosition portfolioPosition : portfolioPositions) {
            builder.addPositions(portfolioPosition);
        }

        final PortfolioResponse portfolioResponse = builder.build();
        return Portfolio.fromResponse(portfolioResponse);
    }

    public static TradingDay newTradingDay(final boolean isTradingDay, OffsetDateTime startDateTime, final OffsetDateTime endDateTime) {
        return new TradingDay(
                DateUtils.toStartOfDay(startDateTime),
                isTradingDay,
                startDateTime,
                endDateTime,
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

    public static TradingDay newTradingDay(
            final boolean isTradingDay,
            final int year,
            final int month,
            final int dayOfMonth,
            final int hour,
            final int durationHours
    ) {
        final OffsetDateTime startDateTime = DateTimeTestData.createDateTime(year, month, dayOfMonth, hour);
        final OffsetDateTime endDateTime = startDateTime.plusHours(durationHours);
        return newTradingDay(isTradingDay, startDateTime, endDateTime);
    }

    public static TradingDay newTradingDay(final OffsetDateTime startDateTime, final OffsetDateTime endDateTime) {
        return newTradingDay(true, startDateTime, endDateTime);
    }

    public static List<TradingDay> newTradingSchedule(final OffsetDateTime startDateTime, final OffsetTime endTime, final int daysCount) {
        List<TradingDay> schedule = new ArrayList<>();
        for (int i = 0; i < daysCount; i++) {
            final OffsetDateTime currentStartDateTime = startDateTime.plusDays(i);
            final OffsetDateTime currentEndDateTime = DateUtils.setTime(currentStartDateTime, endTime);
            final boolean isTradingDay = DateUtils.isWorkDay(currentStartDateTime);
            schedule.add(newTradingDay(isTradingDay, currentStartDateTime, currentEndDateTime));
        }

        return schedule;
    }

    /**
     * @return WorkSchedule with start equal to given @{code hour} of day and given duration of given {@code durationHours}
     */
    public static WorkSchedule newWorkSchedule(final int hour, final int durationHours) {
        return new WorkSchedule(DateTimeTestData.createTime(hour), Duration.ofHours(durationHours));
    }

    /**
     * @return new Interval where {@code from} is start of given {@code dateTime} and {@code to} is end of given {@code dateTime}
     */
    public static Interval newIntervalOfDay(@NotNull final OffsetDateTime dateTime) {
        final OffsetDateTime from = DateUtils.toStartOfDay(dateTime);
        final OffsetDateTime to = DateUtils.toEndOfDay(from);
        return Interval.of(from, to);
    }

    public static LastPrice newLastPrice(final String figi, final double price) {
        return LastPrice.newBuilder()
                .setFigi(figi)
                .setPrice(QUOTATION_MAPPER.fromDouble(price))
                .build();
    }

    public static LastPrice newLastPrice(final String figi, final BigDecimal price) {
        return LastPrice.newBuilder()
                .setFigi(figi)
                .setPrice(QUOTATION_MAPPER.fromBigDecimal(price))
                .build();
    }

    public static List<HistoricCandle> newHistoricCandles(final List<Integer> prices, final OffsetDateTime startDateTime) {
        final List<HistoricCandle> historicCandles = new ArrayList<>(prices.size());
        for (int i = 0; i < prices.size(); i++) {
            final OffsetDateTime currentTime = startDateTime.plusMinutes(i);

            final HistoricCandle historicCandle = new HistoricCandleBuilder()
                    .setOpen(prices.get(i))
                    .setTime(currentTime)
                    .setIsComplete(true)
                    .build();
            historicCandles.add(historicCandle);
        }

        return historicCandles;
    }

    public static List<Candle> newCandles(final List<Integer> prices, final OffsetDateTime startDateTime) {
        final List<Candle> historicCandles = new ArrayList<>(prices.size());
        for (int i = 0; i < prices.size(); i++) {
            final OffsetDateTime currentTime = startDateTime.plusMinutes(i);

            final Candle historicCandle = new CandleBuilder()
                    .setOpen(prices.get(i))
                    .setTime(currentTime)
                    .build();
            historicCandles.add(historicCandle);
        }

        return historicCandles;
    }

}