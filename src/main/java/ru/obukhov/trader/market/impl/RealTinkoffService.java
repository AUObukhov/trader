package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.web.client.service.interfaces.MarketClient;
import ru.obukhov.trader.web.client.service.interfaces.OperationsClient;
import ru.obukhov.trader.web.client.service.interfaces.OrdersClient;
import ru.obukhov.trader.web.client.service.interfaces.PortfolioClient;
import ru.obukhov.trader.web.client.service.interfaces.UserClient;

import java.io.IOException;
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
public class RealTinkoffService implements TinkoffService, ApplicationContextAware {

    private RealTinkoffService self;

    private final MarketClient marketClient;
    private final OperationsClient operationsClient;
    private final OrdersClient ordersClient;
    private final PortfolioClient portfolioClient;
    private final UserClient userClient;

    // region MarketContext

    @Override
    @Cacheable(value = "marketStocks", sync = true)
    public List<MarketInstrument> getMarketStocks() throws IOException {
        return marketClient.getMarketStocks();
    }

    @Override
    @Cacheable(value = "marketBonds", sync = true)
    public List<MarketInstrument> getMarketBonds() throws IOException {
        return marketClient.getMarketBonds();
    }

    @Override
    @Cacheable(value = "marketEtfs", sync = true)
    public List<MarketInstrument> getMarketEtfs() throws IOException {
        return marketClient.getMarketEtfs();
    }

    @Override
    @Cacheable(value = "marketCurrencies", sync = true)
    public List<MarketInstrument> getMarketCurrencies() throws IOException {
        return marketClient.getMarketCurrencies();
    }

    @Override
    public Orderbook getMarketOrderbook(final String ticker, final int depth) throws IOException {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return marketClient.getMarketOrderbook(figi, depth);
    }

    @Override
    @Cacheable(value = "marketCandles", sync = true)
    public List<Candle> getMarketCandles(final String ticker, final Interval interval, final CandleInterval candleInterval) throws IOException {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        final List<Candle> candles = marketClient
                .getMarketCandles(figi, interval.getFrom(), interval.getTo(), candleInterval)
                .candleList();

        log.debug("Loaded {} candles for ticker '{}' in interval {}", candles.size(), ticker, interval);
        return candles;
    }

    @Override
    @Cacheable(value = "marketInstrument", sync = true)
    public MarketInstrument searchMarketInstrument(final String ticker) throws IOException {
        final List<MarketInstrument> instruments = marketClient.searchMarketInstrumentsByTicker(ticker);
        return CollectionUtils.firstElement(instruments);
    }

    // endregion

    // region OperationsContext

    @Override
    public List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, final String ticker) throws IOException {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return operationsClient.getOperations(brokerAccountId, interval.getFrom(), interval.getTo(), figi);
    }

    // endregion

    // region OrdersContext

    @Override
    public List<Order> getOrders(@Nullable final String brokerAccountId) throws IOException {
        return ordersClient.getOrders(brokerAccountId);
    }

    @Override
    public PlacedLimitOrder placeLimitOrder(@Nullable final String brokerAccountId, final String ticker, final LimitOrderRequest orderRequest)
            throws IOException {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return ordersClient.placeLimitOrder(brokerAccountId, figi, orderRequest);
    }

    @Override
    public PlacedMarketOrder placeMarketOrder(@Nullable final String brokerAccountId, final String ticker, final MarketOrderRequest orderRequest)
            throws IOException {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return ordersClient.placeMarketOrder(brokerAccountId, figi, orderRequest);
    }

    @Override
    public void cancelOrder(@Nullable final String brokerAccountId, final String orderId) throws IOException {
        ordersClient.cancelOrder(brokerAccountId, orderId);
    }

    // endregion

    // region PortfolioContext

    @Override
    public List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId) throws IOException {
        return portfolioClient.getPortfolio(brokerAccountId);
    }

    @Override
    public List<CurrencyPosition> getPortfolioCurrencies(@Nullable final String brokerAccountId) throws IOException {
        return portfolioClient.getPortfolioCurrencies(brokerAccountId);
    }

    // endregion

    // region UserContext

    @Override
    public List<UserAccount> getAccounts() throws IOException {
        return userClient.getAccounts();
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