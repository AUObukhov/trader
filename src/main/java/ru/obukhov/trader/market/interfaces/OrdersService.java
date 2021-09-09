package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.math.BigDecimal;
import java.util.List;

public interface OrdersService {

    List<Order> getOrders(@Nullable final String brokerAccountId, final String ticker);

    List<Order> getOrders(@Nullable final String brokerAccountId);

    PlacedMarketOrder placeMarketOrder(
            @Nullable final String brokerAccountId, @NotNull final String ticker, final int lots, @NotNull final OperationType operationType);

    PlacedLimitOrder placeLimitOrder(
            @Nullable final String brokerAccountId,
            @NotNull final String ticker,
            final int lots,
            @NotNull final OperationType operationType,
            final BigDecimal price
    );

    void cancelOrder(@Nullable final String brokerAccountId, @NotNull final String orderId);

}