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
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains method for trading.
 * Descendants can define own invocation order and frequency.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class Bot {

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
        final List<String> figies = botConfig.figies();
        final List<OrderState> orders = ordersService.getOrders(botConfig.accountId());
        if (orders.isEmpty()) {
            final DecisionsData decisionsData = prepareDecisionsData(botConfig, figies);
            final StrategyCache strategyCache = strategy.initCache(botConfig, interval);
            final Map<String, Decision> figiesToDecisions = strategy.decide(decisionsData, strategyCache);
            performOperations(botConfig.accountId(), figiesToDecisions);
        } else {
            log.info("There are not completed orders by FIGIes '{}'. Do nothing", figies);
        }
    }

    private DecisionsData prepareDecisionsData(final BotConfig botConfig, final List<String> figies) {
        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setCommission(botConfig.commission());
        final List<DecisionData> decisionDataList = new ArrayList<>();
        final List<Share> shares = extInstrumentsService.getShares(figies);
        final Map<String, BigDecimal> availableBalances = extOperationsService.getAvailableBalances(botConfig.accountId());
        for (final Share share : shares) {
            final BigDecimal availableBalance = availableBalances.get(share.currency());
            final DecisionData decisionData = prepareDecisionData(botConfig, share, availableBalance);
            decisionDataList.add(decisionData);
        }
        decisionsData.setDecisionDataList(decisionDataList);
        return decisionsData;
    }

    private DecisionData prepareDecisionData(final BotConfig botConfig, final Share share, final BigDecimal availableBalance) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setPosition(extOperationsService.getSecurity(botConfig.accountId(), share.figi()));
        decisionData.setLastOperations(getLastWeekOperations(botConfig.accountId(), share.figi()));
        decisionData.setShare(share);
        decisionData.setAvailableLots(getAvailableLots(share.figi(), availableBalance, botConfig.commission()));
        return decisionData;
    }

    private List<Operation> getLastWeekOperations(final String accountId, final String figi) {
        final OffsetDateTime now = context.getCurrentDateTime();
        final Interval interval = Interval.of(now.minusWeeks(1), now);
        return extOperationsService.getOperations(accountId, interval, figi);
    }

    private long getAvailableLots(final String figi, final BigDecimal availableBalance, final BigDecimal commission) {
        final BigDecimal lastPrice = extMarketDataService.getPrice(figi, context.getCurrentDateTime());
        final BigDecimal lastPriceWithCommission = DecimalUtils.addFraction(lastPrice, commission);
        return DecimalUtils.divide(availableBalance, lastPriceWithCommission).longValue();
    }

    private void performOperations(final String accountId, final Map<String, Decision> figiesToDecisions) {
        for (final Map.Entry<String, Decision> entry : figiesToDecisions.entrySet()) {
            final Decision decision = entry.getValue();
            if (decision.getAction() == DecisionAction.WAIT) {
                log.debug("Decision is {}. Do nothing", decision.toPrettyString());
                return;
            }

            final String figi = entry.getKey();
            final Long quantity = decision.getQuantity();
            final OrderDirection direction = decision.getAction() == DecisionAction.BUY
                    ? OrderDirection.ORDER_DIRECTION_BUY
                    : OrderDirection.ORDER_DIRECTION_SELL;
            final OrderType orderType = OrderType.ORDER_TYPE_MARKET;
            final PostOrderResponse postOrderResponse = ordersService.postOrder(accountId, figi, quantity, null, direction, orderType, null);
            log.info("Placed order:\n{}", postOrderResponse);
        }
    }

}