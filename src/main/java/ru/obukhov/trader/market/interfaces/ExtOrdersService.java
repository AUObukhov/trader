package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.model.Order;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ExtOrdersService {
    List<Order> getOrders(String accountId, String figi);

    List<Order> getOrders(String accountId);

    PostOrderResponse postOrder(
            String accountId,
            String figi,
            long quantity,
            BigDecimal price,
            OrderDirection direction,
            OrderType type,
            String orderId
    );

    void cancelOrder(String accountId, @NotNull String orderId);
}