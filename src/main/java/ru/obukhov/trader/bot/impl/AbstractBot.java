package ru.obukhov.trader.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.interfaces.Decider;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBot implements Bot {

    private static final int LAST_CANDLES_COUNT = 1000;

    protected final Decider decider;
    protected final MarketService marketService;
    protected final OperationsService operationsService;
    protected final OrdersService ordersService;
    protected final PortfolioService portfolioService;

    @Override
    public DecisionData processTicker(String ticker) {
        DecisionData decisionData = new DecisionData();
        try {
            List<Order> orders = ordersService.getOrders(ticker);
            if (orders.isEmpty()) {
                fillDecisionData(decisionData, ticker);
                if (CollectionUtils.isEmpty(decisionData.getCurrentCandles())) {
                    log.info("There are no candles by ticker '{}'. Do nothing", ticker);
                } else {
                    Decision decision = decider.decide(decisionData);
                    performOperation(ticker, decision);
                }
            } else {
                log.info("There are not completed orders by ticker '{}'. Do nothing", ticker);
            }
        } catch (Exception ex) {
            String msg = String.format("Exception while process ticker '%s'. Do nothing", ticker);
            log.error(msg, ex);
        }

        return decisionData;
    }

    private void fillDecisionData(DecisionData decisionData, String ticker) {

        decisionData.setBalance(portfolioService.getAvailableBalance(Currency.RUB));
        decisionData.setPosition(portfolioService.getPosition(ticker));
        decisionData.setCurrentCandles(marketService.getLastCandles(ticker, LAST_CANDLES_COUNT));
        decisionData.setLastOperations(getLastWeekOperations(ticker));
        decisionData.setInstrument(marketService.getInstrument(ticker));

    }

    private List<ru.tinkoff.invest.openapi.models.operations.Operation> getLastWeekOperations(String ticker) {
        OffsetDateTime to = OffsetDateTime.now();
        OffsetDateTime from = to.minusWeeks(1);
        return operationsService.getOperations(Interval.of(from, to), ticker);
    }

    protected void performOperation(String ticker, Decision decision) {
        if (decision.getAction() == DecisionAction.WAIT) {
            log.debug("Decision is {}. Do nothing", decision.toPrettyString());
            return;
        }

        Operation operation = decision.getAction() == DecisionAction.BUY ? Operation.Buy : Operation.Sell;
        PlacedOrder order = ordersService.placeOrder(ticker, decision.getLots(), operation, null);
        log.info("Placed order {}", order);
    }

}