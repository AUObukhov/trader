package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.transform.OrderStateMapper;
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
@Service
@AllArgsConstructor
public class RealExtOrdersService implements ExtOrdersService {

    private static final OrderStateMapper ORDER_STATE_MAPPER = Mappers.getMapper(OrderStateMapper.class);
    private final OrdersService ordersService;

    /**
     * @return returns list of active orders with given {@code figi} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     */
    @Override
    public List<OrderState> getOrders(final String accountId, final String figi) {
        return ordersService.getOrdersSync(accountId).stream()
                .filter(order -> figi.equals(order.getFigi()))
                .map(ORDER_STATE_MAPPER::map)
                .toList();
    }

    /**
     * @return returns list of active orders at given {@code accountId}
     */
    @Override
    public List<OrderState> getOrders(final String accountId) {
        return ordersService.getOrdersSync(accountId)
                .stream()
                .map(ORDER_STATE_MAPPER::map)
                .toList();
    }

    @Override
    public PostOrderResponse postOrder(
            final String accountId,
            final String figi,
            final long quantity,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) {
        final Quotation quotationPrice = DecimalUtils.toQuotation(price);
        return ordersService.postOrderSync(figi, quantity, quotationPrice, direction, accountId, type, orderId);
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