package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service to control customer orders at market
 */
public class OrdersService {

    private final TinkoffService tinkoffService;
    private final MarketService marketService;

    public OrdersService(final TinkoffService tinkoffService, final MarketService marketService) {
        this.tinkoffService = tinkoffService;
        this.marketService = marketService;
    }

    /**
     * @return returns list of active orders with given {@code ticker} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public List<Order> getOrders(@Nullable final String brokerAccountId, final String ticker) {
        final String figi = marketService.getFigi(ticker);
        return getOrders(brokerAccountId).stream()
                .filter(order -> figi.equals(order.getFigi()))
                .toList();
    }

    /**
     * @return returns list of active orders at given {@code brokerAccountId}
     * If {@code brokerAccountId} null, works with default broker account
     */
    public List<Order> getOrders(@Nullable final String brokerAccountId) {
        return tinkoffService.getOrders(brokerAccountId);
    }

    /**
     * @return places new order with market price and given params.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public PlacedMarketOrder placeMarketOrder(
            @Nullable final String brokerAccountId,
            @NotNull final String ticker,
            final int lots,
            @NotNull final OperationType operationType
    ) {
        final MarketOrderRequest orderRequest = new MarketOrderRequest()
                .lots(lots)
                .operation(operationType);
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
    ) {
        final LimitOrderRequest orderRequest = new LimitOrderRequest()
                .lots(lots)
                .operation(operationType)
                .price(price);
        return tinkoffService.placeLimitOrder(brokerAccountId, ticker, orderRequest);
    }

    /**
     * cancels order with given {@code orderId} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void cancelOrder(@Nullable final String brokerAccountId, @NotNull String orderId) {
        tinkoffService.cancelOrder(brokerAccountId, orderId);
    }

}