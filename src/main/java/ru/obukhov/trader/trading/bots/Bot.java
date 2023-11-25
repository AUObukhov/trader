package ru.obukhov.trader.trading.bots;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Contains method for trading.
 * Descendants can define own invocation order and frequency.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class Bot {

    protected static final int LAST_CANDLES_COUNT = 1000;

    protected final ExtMarketDataService extMarketDataService;
    protected final ExtInstrumentsService extInstrumentsService;
    protected final ExtOperationsService extOperationsService;
    protected final ExtOrdersService ordersService;
    protected final ExtUsersService usersService;
    protected final Context context;

    protected final TradingStrategy strategy;

    protected Bot(final ServicesContainer services, final Context context, final TradingStrategy strategy) {
        this.extMarketDataService = services.extMarketDataService();
        this.extInstrumentsService = services.extInstrumentsService();
        this.extOperationsService = services.extOperationsService();
        this.ordersService = services.extOrdersService();
        this.usersService = services.extUsersService();
        this.context = context;
        this.strategy = strategy;
    }

    /**
     * Perform one trading step
     *
     * @param botConfig bot configuration
     * @param interval  interval of cached data for strategy
     */
    public void processBotConfig(final BotConfig botConfig, final Interval interval) {
        final String figi = botConfig.figi();
        final List<OrderState> orders = ordersService.getOrders(botConfig.accountId());
        if (orders.isEmpty()) {
            final Share share = extInstrumentsService.getShare(figi);
            final BigDecimal availableBalance = extOperationsService.getAvailableBalance(botConfig.accountId(), share.currency());
            final DecisionData decisionData = prepareDecisionData(botConfig, share, availableBalance);
            final long availableLots = getAvailableLots(botConfig, availableBalance);
            final StrategyCache strategyCache = strategy.initCache(botConfig, interval);
            final Decision decision = strategy.decide(decisionData, availableLots, strategyCache);
            performOperation(botConfig.accountId(), figi, decision);
        } else {
            log.info("There are not completed orders by FIGI '{}'. Do nothing", figi);
        }
    }

    private DecisionData prepareDecisionData(final BotConfig botConfig, final Share share, final BigDecimal availableBalance) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setBalance(availableBalance);
        decisionData.setPosition(extOperationsService.getSecurity(botConfig.accountId(), share.figi()));
        decisionData.setLastOperations(getLastWeekOperations(botConfig.accountId(), share.figi()));
        decisionData.setShare(share);
        decisionData.setCommission(botConfig.commission());
        return decisionData;
    }

    private long getAvailableLots(final BotConfig botConfig, final BigDecimal availableBalance) {
        final BigDecimal currentPrice = extMarketDataService.getLastPrice(botConfig.figi(), context.getCurrentDateTime());
        return DecimalUtils.divide(availableBalance, currentPrice).longValue();
    }

    private List<Operation> getLastWeekOperations(final String accountId, final String figi) {
        final OffsetDateTime now = context.getCurrentDateTime();
        final Interval interval = Interval.of(now.minusWeeks(1), now);
        return extOperationsService.getOperations(accountId, interval, figi);
    }

    private void performOperation(final String accountId, final String figi, final Decision decision) {
        if (decision.getAction() == DecisionAction.WAIT) {
            log.debug("Decision is {}. Do nothing", decision.toPrettyString());
            return;
        }

        final OrderDirection direction = decision.getAction() == DecisionAction.BUY
                ? OrderDirection.ORDER_DIRECTION_BUY
                : OrderDirection.ORDER_DIRECTION_SELL;
        final PostOrderResponse postOrderResponse = ordersService.postOrder(
                accountId,
                figi,
                decision.getQuantity(),
                null,
                direction,
                OrderType.ORDER_TYPE_MARKET,
                null
        );
        log.info("Placed order:\n{}", postOrderResponse);
    }

}