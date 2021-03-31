package ru.obukhov.trader.market.interfaces;

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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    List<MarketInstrument> getMarketStocks();

    List<MarketInstrument> getMarketBonds();

    List<MarketInstrument> getMarketEtfs();

    List<MarketInstrument> getMarketCurrencies();

    Orderbook getMarketOrderbook(String ticker, int depth);

    List<Candle> getMarketCandles(String ticker, Interval interval, CandleResolution candleInterval);

    MarketInstrument searchMarketInstrument(String ticker);

    List<Operation> getOperations(Interval interval, String ticker);

    List<Order> getOrders();

    PlacedLimitOrder placeLimitOrder(String ticker, LimitOrderRequest orderRequest);

    PlacedMarketOrder placeMarketOrder(String ticker, MarketOrderRequest orderRequest);

    void cancelOrder(String orderId);

    Collection<PortfolioPosition> getPortfolioPositions();

    List<CurrencyPosition> getPortfolioCurrencies();

    OffsetDateTime getCurrentDateTime();

}