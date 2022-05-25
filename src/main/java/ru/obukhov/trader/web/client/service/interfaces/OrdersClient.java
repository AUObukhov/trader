package ru.obukhov.trader.web.client.service.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;

import java.io.IOException;
import java.util.List;

public interface OrdersClient {

    List<Order> getOrders(@Nullable String accountId) throws IOException;

    PlacedLimitOrder placeLimitOrder(
            final String accountId,
            @NotNull final String figi,
            @NotNull final LimitOrderRequest limitOrder
    ) throws IOException;

    PlacedMarketOrder placeMarketOrder(
            final String accountId,
            @NotNull final String figi,
            @NotNull final MarketOrderRequest marketOrder
    ) throws IOException;

    void cancelOrder(final String accountId, @NotNull final String orderId) throws IOException;

}
