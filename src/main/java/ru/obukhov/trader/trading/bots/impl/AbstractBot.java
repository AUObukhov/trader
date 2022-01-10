package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBot implements Bot {

    private static final int LAST_CANDLES_COUNT = 1000;

    protected final MarketService marketService;
    protected final OperationsService operationsService;
    protected final OrdersService ordersService;
    protected final PortfolioService portfolioService;

    protected final TradingStrategy strategy;
    protected final StrategyCache strategyCache;

    @Override
    public List<Candle> processBotConfig(final BotConfig botConfig, final OffsetDateTime previousStartTime, final OffsetDateTime now) {
        final DecisionData decisionData = new DecisionData();

        final String ticker = botConfig.getTicker();
        final List<Order> orders = ordersService.getOrders(ticker);
        if (orders.isEmpty()) {
            final List<Candle> currentCandles = marketService.getLastCandles(ticker, LAST_CANDLES_COUNT, botConfig.getCandleResolution());
            decisionData.setCurrentCandles(currentCandles);

            if (currentCandles.isEmpty()) {
                log.info("There are no candles by ticker '{}'. Do nothing", ticker);
            } else if (currentCandles.get(0).getTime().equals(previousStartTime)) {
                log.debug("Candles scope already processed for ticker '{}'. Do nothing", ticker);
            } else {
                fillDecisionData(botConfig, decisionData, ticker, now);
                final Decision decision = strategy.decide(decisionData, strategyCache);
                performOperation(botConfig.getBrokerAccountId(), ticker, decision);
            }
            return currentCandles;
        } else {
            log.info("There are not completed orders by ticker '{}'. Do nothing", ticker);
            return Collections.emptyList();
        }
    }

    private void fillDecisionData(
            final BotConfig botConfig,
            final DecisionData decisionData,
            final String ticker,
            final OffsetDateTime now
    ) {
        final MarketInstrument instrument = marketService.getInstrument(ticker);

        decisionData.setBalance(portfolioService.getAvailableBalance(botConfig.getBrokerAccountId(), instrument.getCurrency()));
        decisionData.setPosition(portfolioService.getPosition(botConfig.getBrokerAccountId(), ticker));
        decisionData.setLastOperations(getLastWeekOperations(botConfig.getBrokerAccountId(), ticker, now));
        decisionData.setInstrument(instrument);
        decisionData.setCommission(botConfig.getCommission());
    }

    private List<Operation> getLastWeekOperations(@Nullable final String brokerAccountId, final String ticker, final OffsetDateTime now) {
        final OffsetDateTime from = now.minusWeeks(1);
        return operationsService.getOperations(brokerAccountId, Interval.of(from, now), ticker);
    }

    private void performOperation(@Nullable final String brokerAccountId, final String ticker, final Decision decision) {
        if (decision.getAction() == DecisionAction.WAIT) {
            log.debug("Decision is {}. Do nothing", decision.toPrettyString());
            return;
        }

        final OperationType operation = decision.getAction() == DecisionAction.BUY ? OperationType.BUY : OperationType.SELL;
        final PlacedMarketOrder order = ordersService.placeMarketOrder(brokerAccountId, ticker, decision.getLots(), operation);
        log.info("Placed order:\n{}", order);
    }

}