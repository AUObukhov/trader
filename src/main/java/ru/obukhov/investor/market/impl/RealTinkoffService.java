package ru.obukhov.investor.market.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.market.TinkoffContextsAware;
import ru.obukhov.investor.market.interfaces.ConnectionService;
import ru.obukhov.investor.market.interfaces.TinkoffService;
import ru.obukhov.investor.market.model.Candle;
import ru.obukhov.investor.market.model.PortfolioPosition;
import ru.obukhov.investor.market.model.transform.CandleMapper;
import ru.obukhov.investor.market.model.transform.PortfolioPositionMapper;
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
import java.util.Collection;
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
public class RealTinkoffService extends TinkoffContextsAware implements TinkoffService, ApplicationContextAware {

    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);
    private final PortfolioPositionMapper portfolioPositionMapper = Mappers.getMapper(PortfolioPositionMapper.class);
    private RealTinkoffService self;

    public RealTinkoffService(ConnectionService connectionService) {
        super(connectionService);
    }

    // region MarketContext

    @Override
    @Cacheable(value = "marketStocks", sync = true)
    public List<Instrument> getMarketStocks() {
        return getMarketContext().getMarketStocks().join().instruments;
    }

    @Override
    @Cacheable(value = "marketBonds", sync = true)
    public List<Instrument> getMarketBonds() {
        return getMarketContext().getMarketBonds().join().instruments;
    }

    @Override
    @Cacheable(value = "marketEtfs", sync = true)
    public List<Instrument> getMarketEtfs() {
        return getMarketContext().getMarketEtfs().join().instruments;
    }

    @Override
    @Cacheable(value = "marketCurrencies", sync = true)
    public List<Instrument> getMarketCurrencies() {
        return getMarketContext().getMarketCurrencies().join().instruments;
    }

    @Override
    public Orderbook getMarketOrderbook(String ticker, int depth) {
        String figi = self.searchMarketInstrument(ticker).figi;
        return getMarketContext()
                .getMarketOrderbook(figi, depth)
                .join()
                .orElse(null);
    }

    @Override
    @Cacheable(value = "marketCandles", sync = true)
    public List<Candle> getMarketCandles(String ticker, Interval interval, CandleInterval candleInterval) {
        String figi = self.searchMarketInstrument(ticker).figi;
        List<Candle> candles = getMarketContext()
                .getMarketCandles(figi, interval.getFrom(), interval.getTo(), candleInterval)
                .join()
                .map(candleMapper::map)
                .orElse(Collections.emptyList());
        log.debug("Loaded {} candles for ticker '{}' in interval {}", candles.size(), ticker, interval);
        return candles;
    }

    @Override
    @Cacheable(value = "marketInstrument", sync = true)
    public Instrument searchMarketInstrument(String ticker) {
        List<Instrument> instruments = getMarketContext()
                .searchMarketInstrumentsByTicker(ticker)
                .join()
                .instruments;
        return CollectionUtils.firstElement(instruments);
    }

    // endregion

    // region OperationsContext

    @Override
    public List<Operation> getOperations(Interval interval, String ticker) {
        String figi = self.searchMarketInstrument(ticker).figi;
        return getOperationsContext()
                .getOperations(interval.getFrom(), interval.getTo(), figi, null)
                .join()
                .operations;
    }

    // endregion

    // region OrdersContext

    @Override
    public List<Order> getOrders() {
        return getOrdersContext().getOrders(null).join();
    }

    @Override
    public PlacedOrder placeLimitOrder(String ticker, LimitOrder limitOrder) {
        String figi = self.searchMarketInstrument(ticker).figi;
        return getOrdersContext().placeLimitOrder(figi, limitOrder, null).join();
    }

    @Override
    public PlacedOrder placeMarketOrder(String ticker, MarketOrder marketOrder) {
        String figi = self.searchMarketInstrument(ticker).figi;
        return getOrdersContext().placeMarketOrder(figi, marketOrder, null).join();
    }

    @Override
    public void cancelOrder(String orderId) {
        getOrdersContext().cancelOrder(orderId, null);
    }

    // endregion

    // region PortfolioContext

    @Override
    public Collection<PortfolioPosition> getPortfolioPositions() {
        List<Portfolio.PortfolioPosition> positions = getPortfolioContext()
                .getPortfolio(null)
                .join()
                .positions;
        return portfolioPositionMapper.map(positions);
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

    // region ApplicationContextAware implementation

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.self = applicationContext.getBean(RealTinkoffService.class);
    }

    // endregion

}