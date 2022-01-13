package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.model.rest.LimitOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.MarketOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.math.BigDecimal;
import java.util.List;

public class OrdersService {

    private final TinkoffService tinkoffService;
    private final MarketService marketService;

    public OrdersService(final TinkoffService tinkoffService, final MarketService marketService) {
        this.tinkoffService = tinkoffService;
        this.marketService = marketService;
    }

    public List<Order> getOrders(@Nullable final String brokerAccountId, final String ticker) {
        final String figi = marketService.getFigi(ticker);
        return getOrders(brokerAccountId).stream()
                .filter(order -> figi.equals(order.getFigi()))
                .toList();
    }

    public List<Order> getOrders(@Nullable final String brokerAccountId) {
        return tinkoffService.getOrders(brokerAccountId);
    }

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

    public void cancelOrder(@Nullable final String brokerAccountId, @NotNull String orderId) {
        tinkoffService.cancelOrder(brokerAccountId, orderId);
    }

}