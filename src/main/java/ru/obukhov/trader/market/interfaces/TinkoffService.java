package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    String getFigiByTicker(String ticker);

    String getTickerByFigi(String figi);

    List<Share> getAllShares();

    Orderbook getMarketOrderbook(final String ticker, final int depth) throws IOException;

    List<Candle> getMarketCandles(final String ticker, final Interval interval, final CandleInterval candleInterval) throws IOException;

    List<Operation> getOperations(final String accountId, final Interval interval, final String ticker) throws IOException;

    List<Order> getOrders(final String accountId) throws IOException;

    PlacedLimitOrder placeLimitOrder(final String accountId, final String ticker, final LimitOrderRequest orderRequest) throws IOException;

    PlacedMarketOrder placeMarketOrder(final String accountId, final String ticker, final MarketOrderRequest orderRequest) throws IOException;

    void cancelOrder(final String accountId, final String orderId) throws IOException;

    List<PortfolioPosition> getPortfolioPositions(final String accountId);

    WithdrawLimits getWithdrawLimits(final String accountId);

    List<UserAccount> getAccounts();

    OffsetDateTime getCurrentDateTime();

}