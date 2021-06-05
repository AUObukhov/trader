package ru.obukhov.trader.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
    @NotNull
    public DecisionData processTicker(final String ticker, final OffsetDateTime previousStartTime) {
        final DecisionData decisionData = new DecisionData();

        final List<Order> orders = ordersService.getOrders(ticker);
        if (orders.isEmpty()) {
            final List<Candle> currentCandles =
                    marketService.getLastCandles(ticker, LAST_CANDLES_COUNT, CANDLE_RESOLUTION);
            decisionData.setCurrentCandles(currentCandles);

            if (currentCandles.isEmpty()) {
                log.info("There are no candles by ticker '{}'. Do nothing", ticker);
            } else if (currentCandles.get(0).getTime().equals(previousStartTime)) {
                log.debug("Candles scope already processed for ticker '{}'. Do nothing", ticker);
            } else {
                fillDecisionData(decisionData, ticker);
                final Decision decision = strategy.decide(decisionData);
                performOperation(ticker, decision);
            }
        } else {
            log.info("There are not completed orders by ticker '{}'. Do nothing", ticker);
        }

        return decisionData;
    }

    private void fillDecisionData(final DecisionData decisionData, final String ticker) {
        final MarketInstrument instrument = marketService.getInstrument(ticker);

        decisionData.setBalance(portfolioService.getAvailableBalance(instrument.getCurrency()));
        decisionData.setPosition(portfolioService.getPosition(ticker));
        decisionData.setLastOperations(getLastWeekOperations(ticker));
        decisionData.setInstrument(instrument);
    }

    private List<Operation> getLastWeekOperations(final String ticker) {
        final OffsetDateTime to = OffsetDateTime.now();
        final OffsetDateTime from = to.minusWeeks(1);
        return operationsService.getOperations(Interval.of(from, to), ticker);
    }

    private void performOperation(final String ticker, final Decision decision) {
        if (decision.getAction() == DecisionAction.WAIT) {
            log.debug("Decision is {}. Do nothing", decision.toPrettyString());
            return;
        }

        final OperationType operation = decision.getAction() == DecisionAction.BUY ? OperationType.BUY : OperationType.SELL;
        final PlacedMarketOrder order = ordersService.placeMarketOrder(ticker, decision.getLots(), operation);
        log.info("Placed order {}", order);
    }

}