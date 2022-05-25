package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PlacedMarketOrder;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Contains method for trading.
 * Descendants can define own invocation order and frequency.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBot implements Bot {

    private static final int LAST_CANDLES_COUNT = 1000;

    protected final MarketService marketService;
    protected final MarketInstrumentsService marketInstrumentsService;
    protected final MarketOperationsService operationsService;
    protected final MarketOrdersService ordersService;
    protected final PortfolioService portfolioService;
    protected final TinkoffService tinkoffService;

    protected final TradingStrategy strategy;
    protected final StrategyCache strategyCache;

    protected AbstractBot(final TinkoffServices tinkoffServices, final TradingStrategy strategy, final StrategyCache strategyCache) {
        this.marketService = tinkoffServices.marketService();
        this.marketInstrumentsService = tinkoffServices.marketInstrumentsService();
        this.operationsService = tinkoffServices.operationsService();
        this.ordersService = tinkoffServices.ordersService();
        this.portfolioService = tinkoffServices.portfolioService();
        this.tinkoffService = tinkoffServices.realTinkoffService();
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
    @Override
    public List<Candle> processBotConfig(final BotConfig botConfig, final OffsetDateTime previousStartTime) throws IOException {
        final DecisionData decisionData = new DecisionData();

        final String ticker = botConfig.getTicker();
        final List<Order> orders = ordersService.getOrders(ticker);
        if (orders.isEmpty()) {
            final List<Candle> currentCandles = marketService.getLastCandles(ticker, LAST_CANDLES_COUNT, botConfig.getCandleInterval());
            decisionData.setCurrentCandles(currentCandles);

            if (currentCandles.isEmpty()) {
                log.info("There are no candles by ticker '{}'. Do nothing", ticker);
            } else if (currentCandles.get(0).getTime().equals(previousStartTime)) {
                log.debug("Candles scope already processed for ticker '{}'. Do nothing", ticker);
            } else {
                fillDecisionData(botConfig, decisionData, ticker);
                final Decision decision = strategy.decide(decisionData, strategyCache);
                performOperation(botConfig.getBrokerAccountId(), ticker, decision);
            }
            return currentCandles;
        } else {
            log.info("There are not completed orders by ticker '{}'. Do nothing", ticker);
            return Collections.emptyList();
        }
    }

    private void fillDecisionData(final BotConfig botConfig, final DecisionData decisionData, final String ticker) throws IOException {
        final Share share = marketInstrumentsService.getShare(ticker);
        final Currency currency = Currency.valueOf(share.getCurrency());

        decisionData.setBalance(portfolioService.getAvailableBalance(botConfig.getBrokerAccountId(), currency));
        decisionData.setPosition(portfolioService.getSecurity(botConfig.getBrokerAccountId(), ticker));
        decisionData.setLastOperations(getLastWeekOperations(botConfig.getBrokerAccountId(), ticker));
        decisionData.setShare(share);
        decisionData.setCommission(botConfig.getCommission());
    }

    private List<Operation> getLastWeekOperations(@Nullable final String brokerAccountId, final String ticker) throws IOException {
        final OffsetDateTime now = tinkoffService.getCurrentDateTime();
        final Interval interval = Interval.of(now.minusWeeks(1), now);
        return operationsService.getOperations(brokerAccountId, interval, ticker);
    }

    private void performOperation(@Nullable final String brokerAccountId, final String ticker, final Decision decision) throws IOException {
        if (decision.getAction() == DecisionAction.WAIT) {
            log.debug("Decision is {}. Do nothing", decision.toPrettyString());
            return;
        }

        final OperationType operation = decision.getAction() == DecisionAction.BUY
                ? OperationType.OPERATION_TYPE_BUY
                : OperationType.OPERATION_TYPE_SELL;
        final PlacedMarketOrder order = ordersService.placeMarketOrder(brokerAccountId, ticker, decision.getQuantityLots(), operation);
        log.info("Placed order:\n{}", order);
    }

}