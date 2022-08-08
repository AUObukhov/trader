package ru.obukhov.trader.market.impl;

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
public class MarketOrdersService {

    private final TinkoffService tinkoffService;

    public MarketOrdersService(final TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    /**
     * @return returns list of active orders with given {@code ticker} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    public List<Order> getOrders(final String accountId, final String ticker) throws IOException {
        final String figi = tinkoffService.getFigiByTicker(ticker);
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