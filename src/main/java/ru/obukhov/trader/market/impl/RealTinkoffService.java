package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.LimitOrderRequest;
import ru.obukhov.trader.market.model.MarketOrderRequest;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.Orderbook;
import ru.obukhov.trader.market.model.PlacedLimitOrder;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.transform.AccountMapper;
import ru.obukhov.trader.market.model.transform.PositionMapper;
import ru.obukhov.trader.web.client.service.interfaces.MarketClient;
import ru.obukhov.trader.web.client.service.interfaces.OrdersClient;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

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

    private static final AccountMapper ACCOUNT_MAPPER = Mappers.getMapper(AccountMapper.class);
    private static final PositionMapper POSITION_MAPPER = Mappers.getMapper(PositionMapper.class);

    private RealTinkoffService self;

    private final InstrumentsService instrumentsService;
    private final MarketDataService marketDataService;
    private final OperationsService operationsService;
    private final OrdersService ordersService;
    private final UsersService usersService;

    private final MarketClient marketClient;
    private final OrdersClient ordersClient;

    @Override
    @Cacheable(value = "figiByTicker", sync = true)
    public String getFigiByTicker(final String ticker) {
        return instrumentsService.getAssetsSync().stream()
                .flatMap(asset -> asset.getInstrumentsList().stream())
                .filter(assetInstrument -> assetInstrument.getTicker().equals(ticker))
                .findFirst()
                .map(AssetInstrument::getFigi)
                .orElse(null);
    }

    @Override
    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        return instrumentsService.getInstrumentByFigiSync(figi).getTicker();
    }

    // region InstrumentsService

    @Override
    @Cacheable(value = "allShares", sync = true)
    public List<Share> getAllShares() {
        return instrumentsService.getAllSharesSync();
    }

    // endregion

    // region MarketContext

    @Override
    public Orderbook getMarketOrderbook(final String ticker, final int depth) throws IOException {
        final String figi = self.getFigiByTicker(ticker);
        return marketClient.getMarketOrderbook(figi, depth);
    }

    @Override
    @Cacheable(value = "marketCandles", sync = true)
    public List<Candle> getMarketCandles(final String ticker, final Interval interval, final CandleInterval candleInterval) throws IOException {
        final String figi = self.getFigiByTicker(ticker);
        final List<Candle> candles = marketClient
                .getMarketCandles(figi, interval.getFrom(), interval.getTo(), candleInterval)
                .candleList();

        log.debug("Loaded {} candles for ticker '{}' in interval {}", candles.size(), ticker, interval);
        return candles;
    }

    // endregion

    // region OperationsService

    @Override
    public List<Operation> getOperations(final String brokerAccountId, final Interval interval, final String ticker) throws IOException {
        final String figi = self.getFigiByTicker(ticker);
        return operationsService.getAllOperationsSync(brokerAccountId, interval.getFrom().toInstant(), interval.getTo().toInstant(), figi);
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
        final String figi = self.getFigiByTicker(ticker);
        return ordersClient.placeLimitOrder(brokerAccountId, figi, orderRequest);
    }

    @Override
    public PlacedMarketOrder placeMarketOrder(@Nullable final String brokerAccountId, final String ticker, final MarketOrderRequest orderRequest)
            throws IOException {
        final String figi = self.getFigiByTicker(ticker);
        return ordersClient.placeMarketOrder(brokerAccountId, figi, orderRequest);
    }

    @Override
    public void cancelOrder(@Nullable final String brokerAccountId, final String orderId) throws IOException {
        ordersClient.cancelOrder(brokerAccountId, orderId);
    }

    // endregion

    // region PortfolioContext

    @Override
    public List<PortfolioPosition> getPortfolioPositions(final String brokerAccountId) {
        return operationsService.getPortfolioSync(brokerAccountId).getPositions().stream()
                .map(position -> {
                    final String ticker = instrumentsService.getInstrumentByFigiSync(position.getFigi()).getTicker();
                    return POSITION_MAPPER.map(ticker, position);
                })
                .toList();
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String brokerAccountId) {
        return operationsService.getWithdrawLimitsSync(brokerAccountId);
    }

    // endregion

    // region UserContext

    @Override
    public List<UserAccount> getAccounts() {
        return usersService.getAccountsSync().stream()
                .map(ACCOUNT_MAPPER::map)
                .toList();
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