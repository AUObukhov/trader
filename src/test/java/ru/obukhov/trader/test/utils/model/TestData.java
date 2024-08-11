package ru.obukhov.trader.test.utils.model;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.TestMoneyUtils;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.SharesFiltrationOptions;
import ru.obukhov.trader.web.model.exchange.WeightedShare;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class TestData {

    private static final Random RANDOM = new Random();
    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);
    public static final SharesFiltrationOptions BASIC_SHARE_FILTRATION_OPTIONS = new SharesFiltrationOptions(
            List.of(Currencies.RUB),
            true,
            false,
            true,
            List.of(ShareType.SHARE_TYPE_COMMON, ShareType.SHARE_TYPE_PREFERRED),
            3652,
            700,
            true
    );

    // region DecisionData creation

    public static DecisionData newDecisionData(final Share share, final long availableLots) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setShare(share);
        decisionData.setAvailableLots(availableLots);

        return decisionData;
    }

    public static DecisionData newDecisionData(final int lotSize, final long availableLots) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setShare(Share.builder().lot(lotSize).build());
        decisionData.setAvailableLots(availableLots);

        return decisionData;
    }

    public static DecisionData newDecisionData(final double averagePositionPrice, final int quantity) {
        final DecisionData decisionData = new DecisionData();
        final Position position = Position.builder()
                .averagePositionPrice(TestData.newMoney(averagePositionPrice, null))
                .quantity(BigDecimal.valueOf(quantity))
                .build();
        decisionData.setPosition(position);
        decisionData.setLastOperations(new ArrayList<>());

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

    public static PortfolioPosition newPortfolioPosition(
            final TestShare testShare,
            final long lotsQuantity,
            final long currentPrice
    ) {
        final long averagePositionPrice = 0; // to do remove
        final long quantity = testShare.getLot() * lotsQuantity;
        return PortfolioPosition.newBuilder()
                .setFigi(testShare.getFigi())
                .setInstrumentType(testShare.instrument().instrumentKind().name())
                .setQuantity(newQuotation(quantity))
                .setAveragePositionPrice(newMoneyValue(averagePositionPrice, testShare.getCurrency()))
                .setExpectedYield(newQuotation((currentPrice - averagePositionPrice) * quantity))
                .setCurrentNkd(newMoneyValue(testShare.getCurrency()))
                .setCurrentPrice(newMoneyValue(currentPrice, testShare.getCurrency()))
                .setAveragePositionPriceFifo(newMoneyValue(testShare.getCurrency()))
                .setQuantityLots(newQuotation(lotsQuantity))
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

    @SuppressWarnings("java:S6204")
    public static List<BigDecimal> newBigDecimalList(final List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    @SuppressWarnings("java:S6204")
    public static List<BigDecimal> newBigDecimalList(final Double... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    @SuppressWarnings("java:S6204")
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

    // region BalanceConfig creation

    public static BalanceConfig newBalanceConfig() {
        return new BalanceConfig(Collections.emptyMap(), Collections.emptyMap(), null);
    }

    public static BalanceConfig newBalanceConfig(final String currency, final Double initialBalance, final Double balanceIncrement) {
        return newBalanceConfig(currency, initialBalance, balanceIncrement, null);
    }

    public static BalanceConfig newBalanceConfig(
            final String currency,
            final Double initialBalance,
            final Double balanceIncrement,
            final String balanceIncrementCron
    ) {
        final Map<String, BigDecimal> initialBalances = new HashMap<>();
        initialBalances.put(currency, DecimalUtils.setDefaultScale(initialBalance));

        final Map<String, BigDecimal> balanceIncrements = new HashMap<>();
        if (balanceIncrement != null) {
            balanceIncrements.put(currency, DecimalUtils.setDefaultScale(balanceIncrement));
        }

        return new BalanceConfig(initialBalances, balanceIncrements, balanceIncrementCron);
    }

    public static BalanceConfig newBalanceConfig(final Map<String, BigDecimal> initialBalances, final Map<String, BigDecimal> balanceIncrements) {
        return new BalanceConfig(initialBalances, balanceIncrements, null);
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

    public static Money newMoney(final double value, final String currency) {
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

    public static TradingDay newTradingDay(
            final boolean isTradingDay,
            final OffsetDateTime startDateTime,
            final OffsetDateTime endDateTime,
            final OffsetDateTime openingAuctionStartTime,
            final OffsetDateTime closingAuctionEndTime,
            final OffsetDateTime eveningOpeningAuctionStartTime,
            final OffsetDateTime eveningStartTime,
            final OffsetDateTime eveningEndTime,
            final OffsetDateTime clearingStartTime,
            final OffsetDateTime clearingEndTime,
            final OffsetDateTime premarketStartTime,
            final OffsetDateTime premarketEndTime
    ) {
        return new TradingDay(
                DateUtils.toStartOfDay(startDateTime),
                isTradingDay,
                startDateTime,
                endDateTime,
                openingAuctionStartTime,
                closingAuctionEndTime,
                eveningOpeningAuctionStartTime,
                eveningStartTime,
                eveningEndTime,
                clearingStartTime,
                clearingEndTime,
                premarketStartTime,
                premarketEndTime
        );
    }

    public static TradingDay newTradingDay(final boolean isTradingDay, final OffsetDateTime startDateTime, final OffsetDateTime endDateTime) {
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
        final OffsetDateTime startDateTime = DateTimeTestData.newDateTime(year, month, dayOfMonth, hour);
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

    public static ru.tinkoff.piapi.contract.v1.TradingDay newTinkoffTradingDay(
            final boolean isTradingDay,
            final OffsetDateTime startDateTime,
            final OffsetDateTime endDateTime
    ) {
        return ru.tinkoff.piapi.contract.v1.TradingDay.newBuilder()
                .setDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(DateUtils.toStartOfDay(startDateTime)))
                .setIsTradingDay(isTradingDay)
                .setStartTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(startDateTime))
                .setEndTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(endDateTime))
                .build();
    }

    /**
     * @return WorkSchedule with start equal to given @{code hour} of day and given duration of given {@code durationHours}
     */
    public static WorkSchedule newWorkSchedule(final int hour, final int durationHours) {
        return new WorkSchedule(DateTimeTestData.newTime(hour), Duration.ofHours(durationHours));
    }

    public static LastPrice newLastPrice(final String figi, final double price) {
        return LastPrice.newBuilder()
                .setFigi(figi)
                .setPrice(QUOTATION_MAPPER.fromDouble(price))
                .build();
    }

    public static LastPrice newLastPrice(final String figi, final double price, final OffsetDateTime time) {
        return LastPrice.newBuilder()
                .setFigi(figi)
                .setPrice(QUOTATION_MAPPER.fromDouble(price))
                .setTime(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(time))
                .build();
    }

    public static LastPrice newLastPrice(final String figi, final BigDecimal price) {
        return LastPrice.newBuilder()
                .setFigi(figi)
                .setPrice(QUOTATION_MAPPER.fromBigDecimal(price))
                .build();
    }

    public static LastPrice newLastPrice(final String figi, final Quotation price, final Timestamp time) {
        return LastPrice.newBuilder()
                .setFigi(figi)
                .setPrice(price)
                .setTime(time)
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

    public static BigDecimal newBigDecimal(final Object value) {
        return switch (value) {
            case Integer i -> DecimalUtils.setDefaultScale(i);
            case Long l -> DecimalUtils.setDefaultScale(l);
            case Double v -> DecimalUtils.setDefaultScale(v);
            case BigDecimal bigDecimal -> DecimalUtils.setDefaultScale(bigDecimal);
            default -> throw new IllegalArgumentException("Unexpected class: " + value.getClass());
        };
    }

    public static Map<String, BigDecimal> newDecimalMap(final Object... keysAndValues) {
        final Map<String, BigDecimal> decimalMap = new HashMap<>(keysAndValues.length / 2, 1);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            final BigDecimal decimalValue = newBigDecimal(keysAndValues[i + 1]);
            decimalMap.put((String) keysAndValues[i], decimalValue);
        }
        return decimalMap;
    }

    public static WeightedShare newWeightedShare(
            final TestShare testShare,
            final Map<String, Portfolio> accountsToPortfolios,
            final double currencyPrice,
            final double capitalizationWeight,
            final double portfolioWeight,
            final double needToBuy
    ) {
        final Position portfolioPosition = accountsToPortfolios.values().stream()
                .flatMap(portfolio -> portfolio.getPositions().stream())
                .filter(position -> position.getFigi().equals(testShare.getFigi()))
                .findFirst()
                .orElseThrow();
        final Money currentPriceRub = TestMoneyUtils.multiply(portfolioPosition.getCurrentPrice(), currencyPrice);
        final int portfolioSharesQuantity = portfolioPosition.getQuantityLots().intValue() * testShare.getLot();
        return new WeightedShare(
                testShare.getFigi(),
                testShare.getTicker(),
                testShare.getName(),
                currentPriceRub.getValue(),
                DecimalUtils.setDefaultScale(capitalizationWeight),
                testShare.getLot(),
                DecimalUtils.multiply(currentPriceRub.getValue(), testShare.getLot()),
                portfolioSharesQuantity,
                DecimalUtils.multiply(currentPriceRub.getValue(), portfolioSharesQuantity),
                DecimalUtils.setDefaultScale(portfolioWeight),
                DecimalUtils.setDefaultScale(needToBuy)
        );
    }

}