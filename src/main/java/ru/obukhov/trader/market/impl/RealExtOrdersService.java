package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.transform.OrderMapper;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.OrdersService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service to control customer orders at market
 */
@AllArgsConstructor
public class RealExtOrdersService implements ExtOrdersService {

    private static final OrderMapper ORDER_MAPPER = Mappers.getMapper(OrderMapper.class);

    private final OrdersService ordersService;

    /**
     * @return returns list of active orders with given {@code figi} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    @Override
    public List<Order> getOrders(final String accountId, final String figi) {
        return getOrders(accountId).stream()
                .filter(order -> figi.equals(order.figi()))
                .toList();
    }

    /**
     * @return returns list of active orders at given {@code accountId}
     */
    @Override
    public List<Order> getOrders(final String accountId) {
        return ordersService.getOrdersSync(accountId).stream().map(ORDER_MAPPER::map).toList();
    }

    @Override
    public PostOrderResponse postOrder(
            final String accountId,
            final String figi,
            final long quantityLots,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) {
        final Quotation quotationPrice = DecimalUtils.toQuotation(price);
        return ordersService.postOrderSync(figi, quantityLots, quotationPrice, direction, accountId, type, orderId);
    }

    /**
     * cancels order with given {@code orderId} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    @Override
    public void cancelOrder(final String accountId, @NotNull String orderId) {
        ordersService.cancelOrderSync(accountId, orderId);
    }

}