package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.Money;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.util.PostOrderResponseBuilder;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Service to control customer orders at market
 */
@AllArgsConstructor
public class FakeExtOrdersService implements ExtOrdersService {

    private final FakeContext fakeContext;
    private final ExtInstrumentsService extInstrumentsService;
    private final ExtMarketDataService extMarketDataService;
    private final double commission;

    /**
     * @return returns list of active orders with given {@code ticker} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    @Override
    public List<Order> getOrders(final String accountId, final String ticker) {
        final String figi = extInstrumentsService.getSingleFigiByTicker(ticker);
        return getOrders(accountId).stream()
                .filter(order -> figi.equals(order.figi()))
                .toList();
    }

    /**
     * @return returns list of active orders at given {@code accountId}
     * If {@code accountId} null, works with default broker account
     */
    @Override
    public List<Order> getOrders(final String accountId) {
        return Collections.emptyList();
    }

    @Override
    public PostOrderResponse postOrder(
            final String accountId,
            final String ticker,
            final long quantityLots,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) {
        final Share share = extInstrumentsService.getSingleShare(ticker);
        final BigDecimal currentPrice = getCurrentPrice(ticker);
        final Long quantity = quantityLots * share.lotSize();
        final BigDecimal totalPrice = DecimalUtils.multiply(currentPrice, quantity);
        final BigDecimal totalCommissionAmount = DecimalUtils.getFraction(totalPrice, commission);

        if (direction == OrderDirection.ORDER_DIRECTION_BUY) {
            buyPosition(accountId, ticker, currentPrice, quantity, quantityLots, totalPrice, totalCommissionAmount);
            addOperation(accountId, ticker, currentPrice, quantity, OperationType.OPERATION_TYPE_BUY);
        } else {
            sellPosition(accountId, ticker, currentPrice, quantity, totalPrice, totalCommissionAmount);
            addOperation(accountId, ticker, currentPrice, quantity, OperationType.OPERATION_TYPE_SELL);
        }

        return new PostOrderResponseBuilder()
                .setCurrency(share.currency())
                .setTotalOrderAmount(totalPrice)
                .setTotalCommissionAmount(totalCommissionAmount)
                .setInitialSecurityPrice(currentPrice)
                .setQuantityLots(quantityLots)
                .setFigi(share.figi())
                .setDirection(direction)
                .setType(type)
                .setOrderId(orderId)
                .build();
    }

    @Override
    public void cancelOrder(final String accountId, @NotNull String orderId) {
        throw new UnsupportedOperationException("Back test does not support cancelling of orders");
    }

    /**
     * @return last known price for instrument with given {@code ticker} not after current fake date time
     */
    private BigDecimal getCurrentPrice(final String ticker) {
        final OffsetDateTime currentDateTime = fakeContext.getCurrentDateTime();
        return extMarketDataService.getLastPrice(ticker, currentDateTime);
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
        final Share share = extInstrumentsService.getSingleShare(ticker);

        updateBalance(accountId, share.currency(), totalPrice.negate().subtract(commissionAmount));

        final PortfolioPosition existingPosition = fakeContext.getPosition(accountId, ticker);
        PortfolioPosition position;
        if (existingPosition == null) {
            final Money price = Money.of(share.currency(), currentPrice);
            position = new PortfolioPosition(
                    ticker,
                    InstrumentType.SHARE,
                    BigDecimal.valueOf(quantity),
                    price,
                    DecimalUtils.setDefaultScale(0),
                    price,
                    BigDecimal.valueOf(quantityLots)
            );
        } else {
            position = existingPosition.addQuantities(quantity, quantityLots, totalPrice, currentPrice);
        }

        fakeContext.addPosition(accountId, ticker, position);
    }

    private void updateBalance(final String accountId, final Currency currency, final BigDecimal increment) {
        final BigDecimal newBalance = fakeContext.getBalance(accountId, currency).add(increment);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        fakeContext.setBalance(accountId, currency, newBalance);
    }

    private void sellPosition(
            final String accountId,
            final String ticker,
            final BigDecimal currentPrice,
            final Long quantity,
            final BigDecimal totalPrice,
            final BigDecimal commissionAmount
    ) {
        final PortfolioPosition existingPosition = fakeContext.getPosition(accountId, ticker);
        final BigDecimal newQuantity = DecimalUtils.subtract(existingPosition.quantity(), quantity).setScale(0, RoundingMode.UNNECESSARY);
        final int compareToZero = newQuantity.compareTo(BigDecimal.ZERO);
        if (compareToZero < 0) {
            final String message = "quantity " + quantity + " can't be greater than existing position's quantity " + existingPosition.quantity();
            throw new IllegalArgumentException(message);
        }

        final Share share = extInstrumentsService.getSingleShare(ticker);

        updateBalance(accountId, share.currency(), totalPrice.subtract(commissionAmount));
        if (compareToZero == 0) {
            fakeContext.removePosition(accountId, ticker);
        } else {
            final BigDecimal newQuantityLots = newQuantity.divide(BigDecimal.valueOf(share.lotSize()), 0, RoundingMode.UNNECESSARY);
            final BigDecimal newExpectedYield = currentPrice.subtract(existingPosition.averagePositionPrice().value())
                    .multiply(newQuantity);
            final PortfolioPosition newPosition = existingPosition.cloneWithNewValues(newQuantity, newExpectedYield, currentPrice, newQuantityLots);
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
        final OffsetDateTime currentDateTime = fakeContext.getCurrentDateTime();
        final BackTestOperation operation = new BackTestOperation(ticker, currentDateTime, operationType, price, quantity);

        fakeContext.addOperation(accountId, operation);
    }
}