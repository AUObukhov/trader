package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.math.BigDecimal;
import java.util.List;

public interface OrdersService {

    List<Order> getOrders(String ticker);

    List<Order> getOrders();

    PlacedMarketOrder placeMarketOrder(@NotNull String ticker,
                                       int lots,
                                       @NotNull OperationType operationType);

    PlacedLimitOrder placeLimitOrder(@NotNull String ticker,
                                     int lots,
                                     @NotNull OperationType operationType,
                                     BigDecimal price);

    void cancelOrder(@NotNull String orderId);

}