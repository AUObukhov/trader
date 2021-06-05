package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.math.BigDecimal;
import java.util.List;

public interface OrdersService {

    List<Order> getOrders(final String ticker);

    List<Order> getOrders();

    PlacedMarketOrder placeMarketOrder(
            @NotNull final String ticker,
            final int lots,
            @NotNull final OperationType operationType
    );

    PlacedLimitOrder placeLimitOrder(
            @NotNull final String ticker,
            final int lots,
            @NotNull final OperationType operationType,
            final BigDecimal price
    );

    void cancelOrder(@NotNull final String orderId);

}