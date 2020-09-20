package ru.obukhov.investor.service.context;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.obukhov.investor.service.aop.Throttled;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ThrottledOrdersContext implements OrdersContext {

    @Setter
    private OrdersContext innerContext;

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<List<Order>> getOrders(@Nullable String brokerAccountId) {
        return innerContext.getOrders(brokerAccountId);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<PlacedOrder> placeLimitOrder(@NotNull String figi,
                                                          @NotNull LimitOrder limitOrder,
                                                          @Nullable String brokerAccountId) {
        return innerContext.placeLimitOrder(figi, limitOrder, brokerAccountId);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<PlacedOrder> placeMarketOrder(@NotNull String figi,
                                                           @NotNull MarketOrder marketOrder,
                                                           @Nullable String brokerAccountId) {
        return innerContext.placeMarketOrder(figi, marketOrder, brokerAccountId);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Void> cancelOrder(@NotNull String orderId, @Nullable String brokerAccountId) {
        return innerContext.cancelOrder(orderId, brokerAccountId);
    }

    @NotNull
    @Override
    public String getPath() {
        return innerContext.getPath();
    }

}