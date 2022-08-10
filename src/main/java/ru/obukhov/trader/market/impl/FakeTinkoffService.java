package ru.obukhov.trader.market.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import org.quartz.CronExpression;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.FakeContext;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

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
    private final ExtMarketDataService extMarketDataService;
    private final ExtInstrumentsService extInstrumentsService;
    private final RealTinkoffService realTinkoffService;

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    private final FakeContext fakeContext;
    private final double commission;

    /**
     * Initializes current dateTime and one currency.
     * current dateTime is initialized by nearest work time to given {@code currentDateTime}
     *
     * @param accountId       account id
     * @param currentDateTime start dateTime for search dateTime to set as current
     * @param currency        currency which balance is initialized not by zero.
     * @param balanceConfig   balance config.
     *                        {@code currency} and {@code balanceConfig.initialBalance} must be both null or both not null.
     */
    public FakeTinkoffService(
            final MarketProperties marketProperties,
            final TinkoffServices tinkoffServices,
            final String accountId,
            final OffsetDateTime currentDateTime,
            final Currency currency,
            final double commission,
            final BalanceConfig balanceConfig
    ) {
        this.marketProperties = marketProperties;
        this.extMarketDataService = tinkoffServices.extMarketDataService();
        this.extInstrumentsService = tinkoffServices.extInstrumentsService();
        this.realTinkoffService = tinkoffServices.realTinkoffService();
        this.fakeContext = createFakeContext(accountId, currentDateTime, currency, balanceConfig);
        this.commission = commission;
    }

    private FakeContext createFakeContext(
            final String accountId,
            final OffsetDateTime currentDateTime,
            final Currency currency,
            final BalanceConfig balanceConfig
    ) {
        final OffsetDateTime ceilingWorkTime = DateUtils.getCeilingWorkTime(currentDateTime, marketProperties.getWorkSchedule());
        final BigDecimal initialBalance = getInitialBalance(currentDateTime, ceilingWorkTime, balanceConfig);

        return new FakeContext(ceilingWorkTime, accountId, currency, initialBalance);
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

    @Override
    public String getFigiByTicker(final String ticker) {
        return realTinkoffService.getFigiByTicker(ticker);
    }

    // region OperationsContext proxy

    /**
     * Searches operations which satisfy given conditions
     *
     * @param interval interval of dateTime of searchable operations, including extreme values
     * @param ticker   ticker of searchable operations, if operations with any ticker are satisfying
     * @return found operations in natural order of their dateTime
     */
    @Override
    public List<Operation> getOperations(final String accountId, final Interval interval, @Nullable final String ticker) {
        Stream<BackTestOperation> operationsStream = fakeContext.getOperations(accountId).stream()
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
    public List<Order> getOrders(final String accountId) {
        return Collections.emptyList();
    }

    /**
     * Performs market order with fake portfolio
     */
    @Override
    public PostOrderResponse postOrder(
            final String accountId,
            final String ticker,
            final long quantityLots,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) throws IOException {
        final Share share = extInstrumentsService.getShare(ticker);
        final BigDecimal currentPrice = getCurrentPrice(ticker);
        final Long quantity = quantityLots * share.getLot();
        final BigDecimal totalPrice = DecimalUtils.multiply(currentPrice, quantity);
        final BigDecimal totalCommissionAmount = DecimalUtils.getFraction(totalPrice, commission);

        if (direction == OrderDirection.ORDER_DIRECTION_BUY) {
            buyPosition(accountId, ticker, currentPrice, quantity, quantityLots, totalPrice, totalCommissionAmount);
            addOperation(accountId, ticker, currentPrice, quantity, OperationType.OPERATION_TYPE_BUY);
        } else {
            sellPosition(accountId, ticker, quantity, totalPrice, totalCommissionAmount);
            addOperation(accountId, ticker, currentPrice, quantity, OperationType.OPERATION_TYPE_SELL);
        }

        return DataStructsHelper.createPostOrderResponse(
                share.getCurrency(),
                totalPrice,
                totalCommissionAmount,
                currentPrice,
                quantityLots,
                share.getFigi(),
                direction,
                type,
                orderId
        );
    }

    private void buyPosition(
            final String accountId,
            final String ticker,
            final BigDecimal currentPrice,
            final Long quantity,
            final Long quantityLots,
            final BigDecimal totalPrice,
            final BigDecimal commissionAmount
    ) {
        final Share share = extInstrumentsService.getShare(ticker);

        updateBalance(accountId, share.getCurrency(), totalPrice.negate().subtract(commissionAmount));

        final PortfolioPosition existingPosition = fakeContext.getPosition(accountId, ticker);
        PortfolioPosition position;
        if (existingPosition == null) {
            position = new PortfolioPosition(
                    ticker,
                    InstrumentType.STOCK,
                    BigDecimal.valueOf(quantity),
                    new MoneyAmount(share.getCurrency(), currentPrice),
                    BigDecimal.ZERO,
                    new MoneyAmount(share.getCurrency(), currentPrice),
                    BigDecimal.valueOf(quantityLots)
            );
        } else {
            position = existingPosition.addQuantities(quantity, quantityLots, totalPrice);
        }

        fakeContext.addPosition(accountId, ticker, position);
    }

    private void updateBalance(final String accountId, final String currency, final BigDecimal increment) {
        final Currency enumCurrency = Currency.valueOfIgnoreCase(currency);

        final BigDecimal newBalance = fakeContext.getBalance(accountId, enumCurrency).add(increment);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        fakeContext.setCurrentBalance(accountId, enumCurrency, newBalance);
    }

    private void sellPosition(
            final String accountId,
            final String ticker,
            final Long quantity,
            final BigDecimal totalPrice,
            final BigDecimal commissionAmount
    ) {
        final PortfolioPosition existingPosition = fakeContext.getPosition(accountId, ticker);
        final BigDecimal newQuantity = DecimalUtils.subtract(existingPosition.quantity(), quantity);
        final int compareToZero = newQuantity.compareTo(BigDecimal.ZERO);
        if (compareToZero < 0) {
            final String message = "quantity " + quantity + " can't be greater than existing position's quantity " + existingPosition.quantity();
            throw new IllegalArgumentException(message);
        }

        final Share share = extInstrumentsService.getShare(ticker);

        updateBalance(accountId, share.getCurrency(), totalPrice.subtract(commissionAmount));
        if (compareToZero == 0) {
            fakeContext.removePosition(accountId, ticker);
        } else {
            final BigDecimal newQuantityLots = newQuantity.multiply(BigDecimal.valueOf(share.getLot()));
            final PortfolioPosition newPosition = existingPosition.cloneWithNewQuantity(newQuantity, newQuantityLots);
            fakeContext.addPosition(accountId, ticker, newPosition);
        }
    }

    private void addOperation(
            final String accountId,
            final String ticker,
            final BigDecimal price,
            final Long quantity,
            final OperationType operationType
    ) {
        final BackTestOperation operation = new BackTestOperation(ticker, fakeContext.getCurrentDateTime(), operationType, price, quantity);

        fakeContext.addOperation(accountId, operation);
    }

    @Override
    public void cancelOrder(final String accountId, final String orderId) {
        throw new UnsupportedOperationException("Back test does not support cancelling of orders");
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public List<PortfolioPosition> getPortfolioPositions(final String accountId) {
        return fakeContext.getPositions(accountId);
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String accountId) {
        final List<MoneyValue> money = fakeContext.getBalances(accountId).entrySet().stream()
                .map(entry -> DataStructsHelper.createMoneyValue(entry.getKey(), entry.getValue()))
                .toList();
        return DataStructsHelper.createWithdrawLimits(money);
    }

    // endregion

    // region UserContext proxy

    @Override
    public List<UserAccount> getAccounts() {
        return realTinkoffService.getAccounts();
    }

    // endregion

    // region methods for back test

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return fakeContext.getCurrentDateTime();
    }

    public BigDecimal getCurrentBalance(final String accountId, final Currency currency) {
        return fakeContext.getBalance(accountId, currency);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final Currency currency) {
        return fakeContext.getInvestments(accountId, currency);
    }

    public void addInvestment(
            final String accountId,
            final OffsetDateTime dateTime,
            final Currency currency,
            final BigDecimal increment
    ) {
        fakeContext.addInvestment(accountId, dateTime, currency, increment);
    }

    /**
     * @return last known price for instrument with given {@code ticker} not after current fake date time
     */
    public BigDecimal getCurrentPrice(final String ticker) throws IOException {
        return extMarketDataService.getLastCandle(ticker, fakeContext.getCurrentDateTime()).getClosePrice();
    }

    // endregion
}