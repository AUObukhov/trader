package ru.obukhov.trader.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBot implements Bot {

    private static final int LAST_CANDLES_COUNT = 1000;
    private static final CandleResolution CANDLE_RESOLUTION = CandleResolution._1MIN;

    protected final TradingStrategy strategy;
    protected final MarketService marketService;
    protected final OperationsService operationsService;
    protected final OrdersService ordersService;
    protected final PortfolioService portfolioService;

    @Override
    public DecisionData processTicker(String ticker, OffsetDateTime previousStartTime) {
        try {
            DecisionData decisionData = new DecisionData();

            List<Order> orders = ordersService.getOrders(ticker);
            if (orders.isEmpty()) {
                List<Candle> currentCandles =
                        marketService.getLastCandles(ticker, LAST_CANDLES_COUNT, CANDLE_RESOLUTION);
                decisionData.setCurrentCandles(currentCandles);

                if (currentCandles.isEmpty()) {
                    log.info("There are no candles by ticker '{}'. Do nothing", ticker);
                } else if (currentCandles.get(0).getTime().equals(previousStartTime)) {
                    log.info("Candles scope already processed for ticker '{}'. Do nothing", ticker);
                } else {
                    fillDecisionData(decisionData, ticker);
                    Decision decision = strategy.decide(decisionData);
                    performOperation(ticker, decision);
                }
            } else {
                log.info("There are not completed orders by ticker '{}'. Do nothing", ticker);
            }

            return decisionData;
        } catch (Exception ex) {
            log.error("Exception while process ticker '{}'. No decision data", ticker, ex);
            return null;
        }
    }

    private void fillDecisionData(DecisionData decisionData, String ticker) {
        MarketInstrument instrument = marketService.getInstrument(ticker);

        decisionData.setBalance(portfolioService.getAvailableBalance(instrument.getCurrency()));
        decisionData.setPosition(portfolioService.getPosition(ticker));
        decisionData.setLastOperations(getLastWeekOperations(ticker));
        decisionData.setInstrument(instrument);
    }

    private List<Operation> getLastWeekOperations(String ticker) {
        OffsetDateTime to = OffsetDateTime.now();
        OffsetDateTime from = to.minusWeeks(1);
        return operationsService.getOperations(Interval.of(from, to), ticker);
    }

    private void performOperation(String ticker, Decision decision) {
        if (decision.getAction() == DecisionAction.WAIT) {
            log.debug("Decision is {}. Do nothing", decision.toPrettyString());
            return;
        }

        OperationType operation = decision.getAction() == DecisionAction.BUY ? OperationType.BUY : OperationType.SELL;
        PlacedMarketOrder order = ordersService.placeMarketOrder(ticker, decision.getLots(), operation);
        log.info("Placed order {}", order);
    }

}