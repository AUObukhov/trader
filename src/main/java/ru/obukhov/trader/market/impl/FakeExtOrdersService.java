package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.model.PositionUtils;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.market.util.PostOrderResponseBuilder;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final Quotation commission;

    /**
     * @return returns list of active orders with given {@code figi} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    @Override
    public List<OrderState> getOrders(final String accountId, final String figi) {
        return getOrders(accountId).stream()
                .filter(order -> figi.equals(order.getFigi()))
                .toList();
    }

    /**
     * @return returns list of active orders at given {@code accountId}
     * If {@code accountId} null, works with default broker account
     */
    @Override
    public List<OrderState> getOrders(final String accountId) {
        return Collections.emptyList();
    }

    @Override
    public PostOrderResponse postOrder(
            final String accountId,
            final String figi,
            final long quantityLots,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) {
        final Share share = extInstrumentsService.getShare(figi);
        final Quotation currentPrice = getCurrentPrice(figi);
        final long quantity = quantityLots * share.getLot();
        final Quotation totalPrice = QuotationUtils.multiply(currentPrice, quantity);
        final Quotation totalCommissionAmount = QuotationUtils.multiply(totalPrice, commission);

        if (direction == OrderDirection.ORDER_DIRECTION_BUY) {
            buyPosition(accountId, figi, currentPrice, quantity, quantityLots, totalPrice, totalCommissionAmount);
            addOperation(accountId, figi, share.getCurrency(), currentPrice, quantity, OperationType.OPERATION_TYPE_BUY);
        } else {
            sellPosition(accountId, figi, currentPrice, quantity, totalPrice, totalCommissionAmount);
            addOperation(accountId, figi, share.getCurrency(), currentPrice, quantity, OperationType.OPERATION_TYPE_SELL);
        }

        return new PostOrderResponseBuilder()
                .setCurrency(share.getCurrency())
                .setTotalOrderAmount(totalPrice)
                .setTotalCommissionAmount(totalCommissionAmount)
                .setInitialSecurityPrice(currentPrice)
                .setQuantityLots(quantityLots)
                .setFigi(share.getFigi())
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
     * @return last known price for instrument with given {@code figi} not after current fake date time
     */
    private Quotation getCurrentPrice(final String figi) {
        final Timestamp currentTimestamp = fakeContext.getCurrentTimestamp();
        return extMarketDataService.getLastPrice(figi, currentTimestamp);
    }

    private void buyPosition(
            final String accountId,
            final String figi,
            final Quotation currentPrice,
            final Long quantity,
            final Long quantityLots,
            final Quotation totalPrice,
            final Quotation commissionAmount
    ) {
        final Share share = extInstrumentsService.getShare(figi);

        updateBalance(accountId, share.getCurrency(), QuotationUtils.subtract(QuotationUtils.negate(totalPrice), commissionAmount));

        final Position existingPosition = fakeContext.getPosition(accountId, figi);
        Position position;
        if (existingPosition == null) {
            position = new PositionBuilder()
                    .setCurrency(share.getCurrency())
                    .setFigi(figi)
                    .setInstrumentType(InstrumentType.INSTRUMENT_TYPE_SHARE)
                    .setAveragePositionPrice(currentPrice)
                    .setExpectedYield(0)
                    .setCurrentPrice(currentPrice)
                    .setQuantity(quantity)
                    .setQuantityLots(quantityLots)
                    .build();
        } else {
            position = PositionUtils.addQuantities(existingPosition, quantity, quantityLots, totalPrice, currentPrice);
        }

        fakeContext.addPosition(accountId, figi, position);
    }

    private void updateBalance(final String accountId, final String currency, final Quotation increment) {
        final Quotation newBalance = QuotationUtils.add(fakeContext.getBalance(accountId, currency), increment);
        Assert.isTrue(QuotationUtils.getSign(newBalance) >= 0, "balance can't be negative");

        fakeContext.setBalance(accountId, currency, newBalance);
    }

    private void sellPosition(
            final String accountId,
            final String figi,
            final Quotation currentPrice,
            final Long quantity,
            final Quotation totalPrice,
            final Quotation commissionAmount
    ) {
        final Position existingPosition = fakeContext.getPosition(accountId, figi);
        final BigDecimal newQuantity = DecimalUtils.subtract(existingPosition.getQuantity(), quantity).setScale(0, RoundingMode.UNNECESSARY);
        final int compareToZero = newQuantity.compareTo(BigDecimal.ZERO);
        if (compareToZero < 0) {
            final String message = "quantity " + quantity + " can't be greater than existing position's quantity " + existingPosition.getQuantity();
            throw new IllegalArgumentException(message);
        }

        final Share share = extInstrumentsService.getShare(figi);

        updateBalance(accountId, share.getCurrency(), QuotationUtils.subtract(totalPrice, commissionAmount));
        if (compareToZero == 0) {
            fakeContext.removePosition(accountId, figi);
        } else {
            final BigDecimal newQuantityLots = newQuantity.divide(BigDecimal.valueOf(share.getLot()), 0, RoundingMode.UNNECESSARY);
            final Quotation newExpectedYield = QuotationUtils.multiply(
                    QuotationUtils.subtract(currentPrice, existingPosition.getAveragePositionPrice().getValue()),
                    newQuantity
            );
            final Position newPosition = PositionUtils.cloneWithNewValues(existingPosition, newQuantity, newExpectedYield, currentPrice, newQuantityLots);
            fakeContext.addPosition(accountId, figi, newPosition);
        }
    }

    private void addOperation(
            final String accountId,
            final String figi,
            final String currency,
            final Quotation price,
            final long quantity,
            final OperationType operationType
    ) {
        final Timestamp currentTimestamp = fakeContext.getCurrentTimestamp();
        final Operation operation = Operation.newBuilder()
                .setFigi(figi)
                .setDate(currentTimestamp)
                .setOperationType(operationType)
                .setPrice(DataStructsHelper.createMoneyValue(currency, price))
                .setQuantity(quantity)
                .build();

        fakeContext.addOperation(accountId, operation);
    }
}