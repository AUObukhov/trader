package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.transform.OrderMapper;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.OrdersService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Proxy for all Tinkoff contexts, which:<br/>
 * - hides async operations<br/>
 * - replaces Optionals by nulls<br/>
 * - extracts inner fields from unnecessary containers<br/>
 * - replaces some Tinkoff types by more useful custom types<br/>
 * - replaces some unnecessary parameters by hardcoded values
 */
@Slf4j
@RequiredArgsConstructor
public class RealTinkoffService implements TinkoffService {

    private static final OrderMapper ORDER_MAPPER = Mappers.getMapper(OrderMapper.class);

    private final OrdersService ordersService;
    private final ExtInstrumentsService extInstrumentsService;

    // region OrdersContext

    @Override
    public List<Order> getOrders(final String accountId) {
        return ordersService.getOrdersSync(accountId).stream().map(ORDER_MAPPER::map).toList();
    }

    @Override
    public PostOrderResponse postOrder(
            final String accountId,
            final String ticker,
            final long quantityLots,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) {
        final String figi = extInstrumentsService.getFigiByTicker(ticker);
        final Quotation quotationPrice = DecimalUtils.toQuotation(price);
        return ordersService.postOrderSync(figi, quantityLots, quotationPrice, direction, accountId, type, orderId);
    }

    @Override
    public void cancelOrder(final String accountId, final String orderId) {
        ordersService.cancelOrderSync(accountId, orderId);
    }

    // endregion

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now();
    }

}