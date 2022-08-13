package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Order;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service to control customer orders at market
 */
@AllArgsConstructor
public class ExtOrdersService {

    private final TinkoffService tinkoffService;
    private final ExtInstrumentsService extInstrumentsService;

    /**
     * @return returns list of active orders with given {@code ticker} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    public List<Order> getOrders(final String accountId, final String ticker) {
        final String figi = extInstrumentsService.getFigiByTicker(ticker);
        return getOrders(accountId).stream()
                .filter(order -> figi.equals(order.figi()))
                .toList();
    }

    /**
     * @return returns list of active orders at given {@code accountId}
     * If {@code accountId} null, works with default broker account
     */
    public List<Order> getOrders(final String accountId) {
        return tinkoffService.getOrders(accountId);
    }

    public PostOrderResponse postOrder(
            final String accountId,
            final String ticker,
            final long quantity,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) throws IOException {
        return tinkoffService.postOrder(accountId, ticker, quantity, price, direction, type, orderId);
    }

    /**
     * cancels order with given {@code orderId} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    public void cancelOrder(final String accountId, @NotNull String orderId) {
        tinkoffService.cancelOrder(accountId, orderId);
    }

}