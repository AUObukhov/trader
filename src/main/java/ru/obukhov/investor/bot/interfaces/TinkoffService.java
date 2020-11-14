package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.model.Candle;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.Orderbook;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    List<Instrument> getMarketStocks();

    List<Instrument> getMarketBonds();

    List<Instrument> getMarketEtfs();

    List<Instrument> getMarketCurrencies();

    Orderbook getMarketOrderbook(String figi, int depth);

    List<Candle> getMarketCandles(String figi, OffsetDateTime from, OffsetDateTime to, CandleInterval interval);

    Instrument searchMarketInstrumentByTicker(String ticker);

    Instrument searchMarketInstrumentByFigi(String figi);

    List<Operation> getOperations(OffsetDateTime from, OffsetDateTime to, String figi);

    List<Order> getOrders();

    PlacedOrder placeLimitOrder(String figi, LimitOrder limitOrder);

    PlacedOrder placeMarketOrder(String figi, MarketOrder marketOrder);

    void cancelOrder(String orderId);

    List<Portfolio.PortfolioPosition> getPortfolioPositions();

    List<PortfolioCurrencies.PortfolioCurrency> getPortfolioCurrencies();

}