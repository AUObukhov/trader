package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.tinkoff.piapi.contract.v1.OperationType;

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
     * @return returns list of active orders with given {@code ticker} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public List<Order> getOrders(@Nullable final String brokerAccountId, final String ticker) throws IOException {
        final String figi = tinkoffService.getFigiByTicker(ticker);
        return getOrders(brokerAccountId).stream()
                .filter(order -> figi.equals(order.figi()))
                .toList();
    }

    /**
     * @return returns list of active orders at given {@code brokerAccountId}
     * If {@code brokerAccountId} null, works with default broker account
     */
    public List<Order> getOrders(@Nullable final String brokerAccountId) throws IOException {
        return tinkoffService.getOrders(brokerAccountId);
    }

    /**
     * @return places new order with market price and given params.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public PlacedMarketOrder placeMarketOrder(
            @Nullable final String brokerAccountId,
            @NotNull final String ticker,
            final long lots,
            @NotNull final OperationType operationType
    ) throws IOException {
        final MarketOrderRequest orderRequest = new MarketOrderRequest(lots, operationType);
        return tinkoffService.placeMarketOrder(brokerAccountId, ticker, orderRequest);
    }

    /**
     * @return places new order with given {@code price} and given params.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public PlacedLimitOrder placeLimitOrder(
            @Nullable final String brokerAccountId,
            @NotNull final String ticker,
            final int lots,
            @NotNull final OperationType operationType,
            final BigDecimal price
    ) throws IOException {
        final LimitOrderRequest orderRequest = new LimitOrderRequest(lots, operationType, price);
        return tinkoffService.placeLimitOrder(brokerAccountId, ticker, orderRequest);
    }

    /**
     * cancels order with given {@code orderId} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void cancelOrder(@Nullable final String brokerAccountId, @NotNull String orderId) throws IOException {
        tinkoffService.cancelOrder(brokerAccountId, orderId);
    }

}