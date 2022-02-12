package ru.obukhov.trader.market.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.TinkoffContextsAware;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.market.model.Candles;
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
import ru.tinkoff.invest.openapi.okhttp.OpenApi;

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
public class RealTinkoffService extends TinkoffContextsAware implements TinkoffService, ApplicationContextAware {

    private RealTinkoffService self;

    public RealTinkoffService(final OpenApi opeApi) {
        super(opeApi);
    }

    // region MarketContext

    @Override
    @Cacheable(value = "marketStocks", sync = true)
    public List<MarketInstrument> getMarketStocks() {
        return getMarketContext().getMarketStocks().join().getInstruments();
    }

    @Override
    @Cacheable(value = "marketBonds", sync = true)
    public List<MarketInstrument> getMarketBonds() {
        return getMarketContext().getMarketBonds().join().getInstruments();
    }

    @Override
    @Cacheable(value = "marketEtfs", sync = true)
    public List<MarketInstrument> getMarketEtfs() {
        return getMarketContext().getMarketEtfs().join().getInstruments();
    }

    @Override
    @Cacheable(value = "marketCurrencies", sync = true)
    public List<MarketInstrument> getMarketCurrencies() {
        return getMarketContext().getMarketCurrencies().join().getInstruments();
    }

    @Override
    public Orderbook getMarketOrderbook(final String ticker, final int depth) {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return getMarketContext()
                .getMarketOrderbook(figi, depth)
                .join()
                .orElse(null);
    }

    @Override
    @Cacheable(value = "marketCandles", sync = true)
    public List<Candle> getMarketCandles(final String ticker, final Interval interval, final CandleResolution candleResolution) {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        final List<Candle> candles = getMarketContext()
                .getMarketCandles(figi, interval.getFrom(), interval.getTo(), candleResolution)
                .join()
                .map(Candles::getCandles)
                .orElse(Collections.emptyList());
        log.debug("Loaded {} candles for ticker '{}' in interval {}", candles.size(), ticker, interval);
        return candles;
    }

    @Override
    @Cacheable(value = "marketInstrument", sync = true)
    public MarketInstrument searchMarketInstrument(final String ticker) {
        final List<MarketInstrument> instruments = getMarketContext()
                .searchMarketInstrumentsByTicker(ticker)
                .join()
                .getInstruments();
        return CollectionUtils.firstElement(instruments);
    }

    // endregion

    // region OperationsContext

    @Override
    public List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, final String ticker) {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return getOperationsContext()
                .getOperations(interval.getFrom(), interval.getTo(), figi, brokerAccountId)
                .join()
                .getOperations();
    }

    // endregion

    // region OrdersContext

    @Override
    public List<Order> getOrders(@Nullable final String brokerAccountId) {
        return getOrdersContext().getOrders(brokerAccountId).join();
    }

    @Override
    public PlacedLimitOrder placeLimitOrder(@Nullable final String brokerAccountId, final String ticker, final LimitOrderRequest orderRequest) {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return getOrdersContext().placeLimitOrder(figi, orderRequest, brokerAccountId).join();
    }

    @Override
    public PlacedMarketOrder placeMarketOrder(@Nullable final String brokerAccountId, final String ticker, final MarketOrderRequest orderRequest) {
        final String figi = self.searchMarketInstrument(ticker).getFigi();
        return getOrdersContext().placeMarketOrder(figi, orderRequest, brokerAccountId).join();
    }

    @Override
    public void cancelOrder(@Nullable final String brokerAccountId, final String orderId) {
        getOrdersContext().cancelOrder(orderId, brokerAccountId).join();
    }

    // endregion

    // region PortfolioContext

    @Override
    public List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId) {
        return getPortfolioContext()
                .getPortfolio(brokerAccountId)
                .join()
                .getPositions();
    }

    @Override
    public List<CurrencyPosition> getPortfolioCurrencies(@Nullable final String brokerAccountId) {
        return getPortfolioContext().getPortfolioCurrencies(brokerAccountId).join().getCurrencies();
    }

    // endregion

    // region UserContext

    @Override
    public List<UserAccount> getAccounts() {
        return getUserContext().getAccounts().join().getAccounts();
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