package ru.obukhov.investor.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.CollectionUtils;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.transform.CandleMapper;
import ru.obukhov.investor.service.TinkoffContextsAware;
import ru.obukhov.investor.service.interfaces.ConnectionService;
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
import java.util.Collections;
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
public class RealTinkoffService extends TinkoffContextsAware implements TinkoffService {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    public RealTinkoffService(ConnectionService connectionService) {
        super(connectionService);
    }

    // region MarketContext proxy

    @Override
    @Cacheable("marketStocks")
    public List<Instrument> getMarketStocks() {
        return getMarketContext().getMarketStocks().join().instruments;
    }

    @Override
    @Cacheable("marketBonds")
    public List<Instrument> getMarketBonds() {
        return getMarketContext().getMarketBonds().join().instruments;
    }

    @Override
    @Cacheable("marketEtfs")
    public List<Instrument> getMarketEtfs() {
        return getMarketContext().getMarketEtfs().join().instruments;
    }

    @Override
    @Cacheable("marketCurrencies")
    public List<Instrument> getMarketCurrencies() {
        return getMarketContext().getMarketCurrencies().join().instruments;
    }

    @Override
    public Orderbook getMarketOrderbook(String ticker, int depth) {
        String figi = searchMarketInstrument(ticker).figi;
        return getMarketContext().getMarketOrderbook(figi, depth).join().orElse(null);
    }

    @Override
    @Cacheable("marketCandles")
    public List<Candle> getMarketCandles(String ticker,
                                         OffsetDateTime from,
                                         OffsetDateTime to,
                                         CandleInterval interval) {
        String figi = searchMarketInstrument(ticker).figi;
        List<Candle> candles = getMarketContext().getMarketCandles(figi, from, to, interval).join()
                .map(candleMapper::map)
                .orElse(Collections.emptyList());
        log.debug("Loaded " + candles.size() + " candles for ticker '" + ticker + "' in interval " + from + " - " + to);
        return candles;
    }

    @Override
    @Cacheable("marketInstrument")
    public Instrument searchMarketInstrument(String ticker) {
        List<Instrument> instruments = getMarketContext().searchMarketInstrumentsByTicker(ticker).join().instruments;
        return CollectionUtils.firstElement(instruments);
    }

    // endregion

    // region OperationsContext proxy

    @Override
    public List<Operation> getOperations(OffsetDateTime from, OffsetDateTime to, String ticker) {
        String figi = searchMarketInstrument(ticker).figi;
        return getOperationsContext().getOperations(from, to, figi, null).join().operations;
    }

    // endregion

    // region OrdersContext proxy

    @Override
    public List<Order> getOrders() {
        return getOrdersContext().getOrders(null).join();
    }

    @Override
    public PlacedOrder placeLimitOrder(String ticker, LimitOrder limitOrder) {
        String figi = searchMarketInstrument(ticker).figi;
        return getOrdersContext().placeLimitOrder(figi, limitOrder, null).join();
    }

    @Override
    public PlacedOrder placeMarketOrder(String ticker, MarketOrder marketOrder) {
        String figi = searchMarketInstrument(ticker).figi;
        return getOrdersContext().placeMarketOrder(figi, marketOrder, null).join();
    }

    @Override
    public void cancelOrder(String orderId) {
        getOrdersContext().cancelOrder(orderId, null);
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public List<Portfolio.PortfolioPosition> getPortfolioPositions() {
        return getPortfolioContext().getPortfolio(null).join().positions;
    }

    @Override
    public List<PortfolioCurrencies.PortfolioCurrency> getPortfolioCurrencies() {
        return getPortfolioContext().getPortfolioCurrencies(null).join().currencies;
    }

    // endregion

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now();
    }

}