package ru.obukhov.trader.trading.bots;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
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
import ru.tinkoff.piapi.contract.v1.Share;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Contains method for trading.
 * Descendants can define own invocation order and frequency.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class Bot {

    private static final int LAST_CANDLES_COUNT = 1000;

    protected final ExtMarketDataService extMarketDataService;
    protected final ExtInstrumentsService extInstrumentsService;
    protected final ExtOperationsService extOperationsService;
    protected final ExtOrdersService ordersService;
    protected final Context context;

    protected final TradingStrategy strategy;
    protected final StrategyCache strategyCache;

    protected Bot(final TinkoffServices tinkoffServices, final TradingStrategy strategy, final StrategyCache strategyCache) {
        this.extMarketDataService = tinkoffServices.extMarketDataService();
        this.extInstrumentsService = tinkoffServices.extInstrumentsService();
        this.extOperationsService = tinkoffServices.operationsService();
        this.ordersService = tinkoffServices.ordersService();
        this.context = tinkoffServices.realContext();
        this.strategy = strategy;
        this.strategyCache = strategyCache;
    }

    /**
     * Perform one trading step
     *
     * @param botConfig         bot configuration
     * @param previousStartTime dateTime of previous call of the method. Null for the first call.
     *                          Used to prevent repeated processing when no new candle
     * @return list of last candles
     */
    public List<Candle> processBotConfig(final BotConfig botConfig, final OffsetDateTime previousStartTime) {
        final DecisionData decisionData = new DecisionData();

        final String ticker = botConfig.ticker();
        final List<Order> orders = ordersService.getOrders(ticker);
        if (orders.isEmpty()) {
            final List<Candle> currentCandles = extMarketDataService.getLastCandles(ticker, LAST_CANDLES_COUNT, botConfig.candleInterval());
            decisionData.setCurrentCandles(currentCandles);

            if (currentCandles.isEmpty()) {
                log.info("There are no candles by ticker '{}'. Do nothing", ticker);
            } else if (currentCandles.get(0).getTime().equals(previousStartTime)) {
                log.debug("Candles scope already processed for ticker '{}'. Do nothing", ticker);
            } else {
                fillDecisionData(botConfig, decisionData, ticker);
                final Decision decision = strategy.decide(decisionData, strategyCache);
                performOperation(botConfig.accountId(), ticker, decision);
            }
            return currentCandles;
        } else {
            log.info("There are not completed orders by ticker '{}'. Do nothing", ticker);
            return Collections.emptyList();
        }
    }

    private void fillDecisionData(final BotConfig botConfig, final DecisionData decisionData, final String ticker) {
        final Share share = extInstrumentsService.getShare(ticker);
        final Currency currency = Currency.valueOfIgnoreCase(share.getCurrency());

        decisionData.setBalance(extOperationsService.getAvailableBalance(botConfig.accountId(), currency));
        decisionData.setPosition(extOperationsService.getSecurity(botConfig.accountId(), ticker));
        decisionData.setLastOperations(getLastWeekOperations(botConfig.accountId(), ticker));
        decisionData.setShare(share);
        decisionData.setCommission(botConfig.commission());
    }

    private List<Operation> getLastWeekOperations(final String accountId, final String ticker) {
        final OffsetDateTime now = context.getCurrentDateTime();
        final Interval interval = Interval.of(now.minusWeeks(1), now);
        return extOperationsService.getOperations(accountId, interval, ticker);
    }

    private void performOperation(final String accountId, final String ticker, final Decision decision) {
        if (decision.getAction() == DecisionAction.WAIT) {
            log.debug("Decision is {}. Do nothing", decision.toPrettyString());
            return;
        }

        final OrderDirection direction = decision.getAction() == DecisionAction.BUY
                ? OrderDirection.ORDER_DIRECTION_BUY
                : OrderDirection.ORDER_DIRECTION_SELL;
        final PostOrderResponse postOrderResponse = ordersService.postOrder(
                accountId,
                ticker,
                decision.getQuantityLots(),
                null,
                direction,
                OrderType.ORDER_TYPE_MARKET,
                null
        );
        log.info("Placed order:\n{}", postOrderResponse);
    }

}