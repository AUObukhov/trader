package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.Orderbook;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    List<Instrument> getMarketStocks();

    List<Instrument> getMarketBonds();

    List<Instrument> getMarketEtfs();

    List<Instrument> getMarketCurrencies();

    Orderbook getMarketOrderbook(String ticker, int depth);

    List<Candle> getMarketCandles(String ticker, Interval interval, CandleInterval candleInterval);

    Instrument searchMarketInstrument(String ticker);

    List<Operation> getOperations(Interval interval, String ticker);

    List<Order> getOrders();

    PlacedOrder placeLimitOrder(String ticker, LimitOrder limitOrder);

    PlacedOrder placeMarketOrder(String ticker, MarketOrder marketOrder);

    void cancelOrder(String orderId);

    Collection<PortfolioPosition> getPortfolioPositions();

    List<PortfolioCurrencies.PortfolioCurrency> getPortfolioCurrencies();

    OffsetDateTime getCurrentDateTime();

}