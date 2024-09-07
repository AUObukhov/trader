package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
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

@Service
@AllArgsConstructor
public class RealExtOrdersService implements ExtOrdersService {

    private static final OrderStateMapper ORDER_STATE_MAPPER = Mappers.getMapper(OrderStateMapper.class);
    private final OrdersService ordersService;

    @Override
    public List<OrderState> getOrders(final String accountId, final String figi) {
        return ordersService.getOrdersSync(accountId).stream()
                .filter(order -> figi.equals(order.getFigi()))
                .map(ORDER_STATE_MAPPER::map)
                .toList();
    }

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

    @Override
    public void cancelOrder(final String accountId, final String orderId) {
        ordersService.cancelOrderSync(accountId, orderId);
    }

}