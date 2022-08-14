package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.market.model.Order;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    List<Order> getOrders(final String accountId);

    PostOrderResponse postOrder(
            final String accountId,
            final String ticker,
            final long quantity,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) throws IOException;

    void cancelOrder(final String accountId, final String orderId);

    OffsetDateTime getCurrentDateTime();

}