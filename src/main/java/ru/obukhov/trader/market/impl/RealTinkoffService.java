package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.transform.AccountMapper;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.market.model.transform.OrderMapper;
import ru.obukhov.trader.market.model.transform.PositionMapper;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
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
    private static final OrderMapper ORDER_MAPPER = Mappers.getMapper(OrderMapper.class);
    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);

    private RealTinkoffService self;

    private final InstrumentsService instrumentsService;
    private final MarketDataService marketDataService;
    private final OperationsService operationsService;
    private final OrdersService ordersService;
    private final UsersService usersService;

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

    // region MarketContext

    @Override
    @Cacheable(value = "marketCandles", sync = true)
    public List<Candle> getMarketCandles(final String ticker, final Interval interval, final CandleInterval candleInterval) {
        final String figi = self.getFigiByTicker(ticker);
        final Instant fromInstant = interval.getFrom().toInstant();
        final Instant toInstant = interval.getTo().toInstant();
        final List<Candle> candles = marketDataService.getCandlesSync(figi, fromInstant, toInstant, candleInterval)
                .stream()
                .filter(HistoricCandle::getIsComplete)
                .map(CANDLE_MAPPER::map)
                .toList();

        log.debug("Loaded {} candles for ticker '{}' in interval {}", candles.size(), ticker, interval);
        return candles;
    }

    // endregion

    // region OperationsService

    @Override
    public List<Operation> getOperations(final String accountId, final Interval interval, final String ticker) throws IOException {
        final String figi = self.getFigiByTicker(ticker);
        return operationsService.getAllOperationsSync(accountId, interval.getFrom().toInstant(), interval.getTo().toInstant(), figi);
    }

    // endregion

    // region OrdersContext

    @Override
    public List<Order> getOrders(final String accountId) {
        return ordersService.getOrdersSync(accountId).stream().map(ORDER_MAPPER::map).toList();
    }

    @Override
    public PostOrderResponse postOrder(
            final String accountId,
            final String ticker,
            final long quantityLots,
            final BigDecimal price,
            final OrderDirection direction,
            final OrderType type,
            final String orderId
    ) {
        final String figi = self.getFigiByTicker(ticker);
        final Quotation quotationPrice = DecimalUtils.toQuotation(price);
        return ordersService.postOrderSync(figi, quantityLots, quotationPrice, direction, accountId, type, orderId);
    }

    @Override
    public void cancelOrder(final String accountId, final String orderId) {
        ordersService.cancelOrderSync(accountId, orderId);
    }

    // endregion

    // region PortfolioContext

    @Override
    public List<PortfolioPosition> getPortfolioPositions(final String accountId) {
        return operationsService.getPortfolioSync(accountId).getPositions().stream()
                .map(position -> {
                    final String ticker = instrumentsService.getInstrumentByFigiSync(position.getFigi()).getTicker();
                    return POSITION_MAPPER.map(ticker, position);
                })
                .toList();
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String accountId) {
        return operationsService.getWithdrawLimitsSync(accountId);
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