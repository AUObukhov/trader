package ru.obukhov.trader.market.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import org.quartz.CronExpression;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.FakeContext;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.OrderStatus;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.web.model.BalanceConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Stream;

/**
 * Prices are loaded from real market, but any operations do not affect the real portfolio - all data is stored in
 * memory.
 */
@Slf4j
public class FakeTinkoffService implements TinkoffService {

    private final MarketProperties marketProperties;
    private final MarketService marketService;
    private final RealTinkoffService realTinkoffService;

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    private final FakeContext fakeContext;
    private final double commission;

    /**
     * Initializes current dateTime and one currency.
     * current dateTime is initialized by nearest work time to given {@code currentDateTime}
     *
     * @param brokerAccountId account id
     * @param currentDateTime start dateTime for search dateTime to set as current
     * @param currency        currency which balance is initialized not by zero.
     * @param balanceConfig   balance config.
     *                        {@code currency} and {@code balanceConfig.initialBalance} must be both null or both not null.
     */
    public FakeTinkoffService(
            final MarketProperties marketProperties,
            final TinkoffServices tinkoffServices,
            final String brokerAccountId,
            final OffsetDateTime currentDateTime,
            final Currency currency,
            final double commission,
            final BalanceConfig balanceConfig
    ) {
        this.marketProperties = marketProperties;
        this.marketService = tinkoffServices.marketService();
        this.realTinkoffService = tinkoffServices.realTinkoffService();
        this.fakeContext = createFakeContext(brokerAccountId, currentDateTime, currency, balanceConfig);
        this.commission = commission;
    }

    private FakeContext createFakeContext(
            final String brokerAccountId,
            final OffsetDateTime currentDateTime,
            final Currency currency,
            final BalanceConfig balanceConfig
    ) {
        final OffsetDateTime ceilingWorkTime = DateUtils.getCeilingWorkTime(currentDateTime, marketProperties.getWorkSchedule());
        final BigDecimal initialBalance = getInitialBalance(currentDateTime, ceilingWorkTime, balanceConfig);

        return new FakeContext(ceilingWorkTime, brokerAccountId, currency, initialBalance);
    }

    private BigDecimal getInitialBalance(OffsetDateTime currentDateTime, final OffsetDateTime ceilingWorkTime, BalanceConfig balanceConfig) {
        BigDecimal initialBalance = balanceConfig.getInitialBalance() == null ? BigDecimal.ZERO : balanceConfig.getInitialBalance();

        // adding balance increments which were skipped by moving to ceiling work time above
        final CronExpression balanceIncrementCron = balanceConfig.getBalanceIncrementCron();
        if (balanceIncrementCron != null) {
            final int incrementsCount = DateUtils.getCronHitsBetweenDates(balanceIncrementCron, currentDateTime, ceilingWorkTime)
                    .size();
            if (incrementsCount > 0) {
                final BigDecimal totalBalanceIncrement = DecimalUtils.multiply(balanceConfig.getBalanceIncrement(), incrementsCount);
                initialBalance = initialBalance.add(totalBalanceIncrement);
            }
        }
        return initialBalance;
    }

    /**
     * Changes currentDateTime to the nearest work time after it
     *
     * @return new value of currentDateTime
     */
    public OffsetDateTime nextMinute() {
        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(
                fakeContext.getCurrentDateTime(),
                marketProperties.getWorkSchedule()
        );
        fakeContext.setCurrentDateTime(nextWorkMinute);

        return nextWorkMinute;
    }

    // region MarketContext proxy

    @Override
    public List<MarketInstrument> getMarketStocks() throws IOException {
        return realTinkoffService.getMarketStocks();
    }

    @Override
    public List<MarketInstrument> getMarketBonds() throws IOException {
        return realTinkoffService.getMarketBonds();
    }

    @Override
    public List<MarketInstrument> getMarketEtfs() throws IOException {
        return realTinkoffService.getMarketEtfs();
    }

    @Override
    public List<MarketInstrument> getMarketCurrencies() throws IOException {
        return realTinkoffService.getMarketCurrencies();
    }

    @Override
    public Orderbook getMarketOrderbook(final String ticker, final int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Candle> getMarketCandles(final String ticker, final Interval interval, final CandleInterval candleInterval) throws IOException {
        return realTinkoffService.getMarketCandles(ticker, interval, candleInterval);
    }

    @Override
    public MarketInstrument searchMarketInstrument(final String ticker) throws IOException {
        return realTinkoffService.searchMarketInstrument(ticker);
    }

    // endregion

    // region OperationsContext proxy

    /**
     * Searches operations which satisfy given conditions
     *
     * @param interval interval of dateTime of searchable operations, including extreme values
     * @param ticker   ticker of searchable operations, if operations with any ticker are satisfying
     * @return found operations in natural order of their dateTime
     */
    @Override
    public List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, @Nullable final String ticker) {
        Stream<BackTestOperation> operationsStream = fakeContext.getOperations(brokerAccountId).stream()
                .filter(operation -> interval.contains(operation.dateTime()));
        if (ticker != null) {
            operationsStream = operationsStream.filter(operation -> ticker.equals(operation.ticker()));
        }

        return operationsStream
                .sorted(Comparator.comparing(operation -> operation.dateTime().toInstant()))
                .map(operationMapper::map)
                .toList();
    }

    // endregion

    // region OrdersContext proxy

    @Override
    public List<Order> getOrders(@Nullable final String brokerAccountId) {
        return Collections.emptyList();
    }

    @Override
    public PlacedLimitOrder placeLimitOrder(@Nullable final String brokerAccountId, final String ticker, final LimitOrderRequest orderRequest) {
        throw new UnsupportedOperationException("Only market orders supported in back test");
    }

    /**
     * Performs market order with fake portfolio
     *
     * @param ticker       ticker of executed order
     * @param orderRequest model of executed order
     * @return result of order execution
     */
    @Override
    public PlacedMarketOrder placeMarketOrder(@Nullable final String brokerAccountId, final String ticker, final MarketOrderRequest orderRequest)
            throws IOException {
        final MarketInstrument instrument = searchMarketInstrument(ticker);
        final BigDecimal currentPrice = getCurrentPrice(ticker);
        final int count = instrument.lot() * orderRequest.lotsCount();
        final BigDecimal totalPrice = DecimalUtils.multiply(currentPrice, count);
        final BigDecimal totalCommissionAmount = DecimalUtils.getFraction(totalPrice, commission);
        final MoneyAmount totalCommission = new MoneyAmount(Currency.RUB, totalCommissionAmount);

        if (orderRequest.operation() == OperationType.BUY) {
            buyPosition(brokerAccountId, ticker, currentPrice, count, totalPrice, totalCommissionAmount);
        } else {
            sellPosition(brokerAccountId, ticker, count, totalPrice, totalCommissionAmount);
        }

        addOperation(brokerAccountId, ticker, currentPrice, count, totalCommissionAmount, orderRequest.operation());
        return new PlacedMarketOrder(
                null,
                orderRequest.operation(),
                OrderStatus.NEW,
                null,
                null,
                orderRequest.lotsCount(),
                orderRequest.lotsCount(),
                totalCommission
        );
    }

    private void buyPosition(
            @Nullable final String brokerAccountId,
            final String ticker,
            final BigDecimal currentPrice,
            final int count,
            final BigDecimal totalPrice,
            final BigDecimal commissionAmount
    ) throws IOException {
        final MarketInstrument instrument = searchMarketInstrument(ticker);

        updateBalance(brokerAccountId, instrument.currency(), totalPrice.negate().subtract(commissionAmount));

        final PortfolioPosition existingPosition = fakeContext.getPosition(null, ticker);
        PortfolioPosition position;
        if (existingPosition == null) {
            position = new PortfolioPosition(
                    ticker,
                    totalPrice,
                    BigDecimal.ZERO,
                    new MoneyAmount(instrument.currency(), BigDecimal.ZERO),
                    count,
                    new MoneyAmount(instrument.currency(), currentPrice),
                    null,
                    StringUtils.EMPTY
            );
        } else {
            position = addValuesToPosition(existingPosition, count, totalPrice);
        }

        fakeContext.addPosition(brokerAccountId, ticker, position);
    }

    private PortfolioPosition addValuesToPosition(final PortfolioPosition existingPosition, final int count, final BigDecimal totalPrice) {
        final BigDecimal newBalance = existingPosition.balance().add(totalPrice);
        final int newLotsCount = existingPosition.count() + count;
        final BigDecimal newAveragePrice = DecimalUtils.divide(newBalance, newLotsCount);
        return clonePositionWithNewValues(existingPosition, newBalance, newLotsCount, newAveragePrice);
    }

    private PortfolioPosition clonePositionWithNewValues(
            final PortfolioPosition position,
            final BigDecimal balance,
            final int lotsCount,
            final BigDecimal averagePositionPrice
    ) {
        final MoneyAmount expectedYield = position.expectedYield();
        return new PortfolioPosition(
                position.ticker(),
                balance,
                position.blocked(),
                expectedYield,
                lotsCount,
                new MoneyAmount(expectedYield.currency(), averagePositionPrice),
                position.averagePositionPriceNoNkd(),
                position.name()
        );
    }

    private void updateBalance(@Nullable final String brokerAccountId, final Currency currency, final BigDecimal increment) {
        final BigDecimal newBalance = fakeContext.getBalance(null, currency).add(increment);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        fakeContext.setCurrentBalance(brokerAccountId, currency, newBalance);
    }

    private void sellPosition(
            @Nullable final String brokerAccountId,
            final String ticker,
            final int count,
            final BigDecimal totalPrice,
            final BigDecimal commissionAmount
    ) throws IOException {
        final PortfolioPosition existingPosition = fakeContext.getPosition(brokerAccountId, ticker);
        final int newLotsCount = existingPosition.count() - count;
        if (newLotsCount < 0) {
            final String message = "count " + count + " can't be greater than existing position lotsCount count " + existingPosition.count();
            throw new IllegalArgumentException(message);
        }

        final MarketInstrument instrument = searchMarketInstrument(ticker);

        updateBalance(brokerAccountId, instrument.currency(), totalPrice.subtract(commissionAmount));
        if (newLotsCount == 0) {
            fakeContext.removePosition(brokerAccountId, ticker);
        } else {
            PortfolioPosition newPosition = clonePositionWithNewLotsCount(existingPosition, newLotsCount);
            fakeContext.addPosition(brokerAccountId, ticker, newPosition);
        }
    }

    private PortfolioPosition clonePositionWithNewLotsCount(final PortfolioPosition position, final int count) {
        return new PortfolioPosition(
                position.ticker(),
                position.balance(),
                position.blocked(),
                position.expectedYield(),
                count,
                position.averagePositionPrice(),
                position.averagePositionPriceNoNkd(),
                position.name()
        );
    }

    private void addOperation(
            @Nullable final String brokerAccountId,
            final String ticker,
            final BigDecimal price,
            final int quantity,
            final BigDecimal commissionAmount,
            final OperationType operationType
    ) {
        final BackTestOperation operation = new BackTestOperation(
                ticker,
                fakeContext.getCurrentDateTime(),
                operationType,
                price,
                quantity,
                commissionAmount
        );

        fakeContext.addOperation(brokerAccountId, operation);
    }

    @Override
    public void cancelOrder(@Nullable final String brokerAccountId, final String orderId) {
        throw new UnsupportedOperationException();
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId) {
        return fakeContext.getPositions(brokerAccountId);
    }

    @Override
    public List<CurrencyPosition> getPortfolioCurrencies(@Nullable final String brokerAccountId) {
        return fakeContext.getBalances(null).entrySet().stream()
                .map(entry -> new CurrencyPosition(entry.getKey(), entry.getValue(), null))
                .toList();
    }

    // endregion

    // region UserContext proxy

    @Override
    public List<UserAccount> getAccounts() throws IOException {
        return realTinkoffService.getAccounts();
    }

    // endregion

    // region methods for back test

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return fakeContext.getCurrentDateTime();
    }

    public BigDecimal getCurrentBalance(@Nullable final String brokerAccountId, final Currency currency) {
        return fakeContext.getBalance(brokerAccountId, currency);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(@Nullable final String brokerAccountId, final Currency currency) {
        return fakeContext.getInvestments(brokerAccountId, currency);
    }

    public void addInvestment(
            final String brokerAccountId,
            final OffsetDateTime dateTime,
            final Currency currency,
            final BigDecimal increment
    ) {
        fakeContext.addInvestment(brokerAccountId, dateTime, currency, increment);
    }

    /**
     * @return last known price for instrument with given {@code ticker} not after current fake date time
     */
    public BigDecimal getCurrentPrice(final String ticker) throws IOException {
        return marketService.getLastCandle(ticker, fakeContext.getCurrentDateTime()).getClosePrice();
    }

    // endregion
}