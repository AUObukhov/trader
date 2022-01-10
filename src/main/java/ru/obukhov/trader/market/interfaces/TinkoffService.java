package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.LimitOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.MarketOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.Orderbook;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;
import ru.tinkoff.invest.openapi.model.rest.UserAccount;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    List<MarketInstrument> getMarketStocks();

    List<MarketInstrument> getMarketBonds();

    List<MarketInstrument> getMarketEtfs();

    List<MarketInstrument> getMarketCurrencies();

    Orderbook getMarketOrderbook(final String ticker, final int depth);

    List<Candle> getMarketCandles(final String ticker, final Interval interval, final CandleResolution candleResolution);

    MarketInstrument searchMarketInstrument(final String ticker);

    List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, final String ticker);

    List<Order> getOrders(@Nullable final String brokerAccountId);

    PlacedLimitOrder placeLimitOrder(@Nullable final String brokerAccountId, final String ticker, final LimitOrderRequest orderRequest);

    PlacedMarketOrder placeMarketOrder(@Nullable final String brokerAccountId, final String ticker, final MarketOrderRequest orderRequest);

    void cancelOrder(@Nullable final String brokerAccountId, final String orderId);

    List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId);

    List<CurrencyPosition> getPortfolioCurrencies(@Nullable final String brokerAccountId);

    List<UserAccount> getAccounts();

    OffsetDateTime getCurrentDateTime();

}