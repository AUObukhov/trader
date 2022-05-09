package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CurrencyPosition;
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

    List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, final String ticker) throws IOException;

    List<Order> getOrders(@Nullable final String brokerAccountId) throws IOException;

    PlacedLimitOrder placeLimitOrder(@Nullable final String brokerAccountId, final String ticker, final LimitOrderRequest orderRequest) throws IOException;

    PlacedMarketOrder placeMarketOrder(@Nullable final String brokerAccountId, final String ticker, final MarketOrderRequest orderRequest) throws IOException;

    void cancelOrder(@Nullable final String brokerAccountId, final String orderId) throws IOException;

    List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId) throws IOException;

    List<CurrencyPosition> getPortfolioCurrencies(@Nullable final String brokerAccountId) throws IOException;

    List<UserAccount> getAccounts();

    OffsetDateTime getCurrentDateTime();

}