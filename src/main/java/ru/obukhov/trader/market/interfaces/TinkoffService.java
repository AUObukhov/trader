package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    List<Operation> getOperations(final String accountId, final Interval interval, final String ticker) throws IOException;

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

    List<PortfolioPosition> getPortfolioPositions(final String accountId);

    WithdrawLimits getWithdrawLimits(final String accountId);

    List<UserAccount> getAccounts();

    OffsetDateTime getCurrentDateTime();

}