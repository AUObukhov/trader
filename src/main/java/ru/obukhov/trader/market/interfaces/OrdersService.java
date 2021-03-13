package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.math.BigDecimal;
import java.util.List;

public interface OrdersService {

    List<Order> getOrders(String ticker);

    List<Order> getOrders();

    PlacedOrder placeOrder(@NotNull String ticker,
                           int lots,
                           @NotNull Operation operation,
                           @Nullable BigDecimal price);

    void cancelOrder(@NotNull String orderId);

}