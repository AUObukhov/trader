package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.exception.TickerNotFoundException;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class SimpleBot implements Bot {

    private static final int LAST_PRICES_COUNT = 1000;

    protected final Decider decider;
    protected final MarketService marketService;
    protected final OperationsService operationsService;
    protected final OrdersService ordersService;
    protected final PortfolioService portfolioService;

    @Override
    public void processTicker(String ticker) {
        try {
            List<Order> orders = ordersService.getOrders(ticker);
            if (!orders.isEmpty()) {
                log.info("There are not completed orders by ticker '" + ticker + "'. Do nothing");
                return;
            }
            DecisionData data = getData(ticker);
            if (CollectionUtils.isEmpty(data.getCurrentPrices())) {
                log.info("There are no candles by ticker '" + ticker + "'. Do nothing");
                return;
            }

            Decision decision = decider.decide(data);
            performOperation(ticker, decision);
        } catch (TickerNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Exception while process ticker " + ticker + ". Do nothing", ex);
        }
    }

    private DecisionData getData(String ticker) {

        return DecisionData.builder()
                .balance(portfolioService.getAvailableBalance(Currency.RUB))
                .position(portfolioService.getPosition(ticker))
                .currentPrices(getCurrentPrices(ticker))
                .lastOperations(getLastWeekOperations(ticker))
                .instrument(marketService.getInstrument(ticker))
                .build();

    }

    private List<BigDecimal> getCurrentPrices(String ticker) {
        return marketService.getLastCandles(ticker, LAST_PRICES_COUNT).stream()
                .map(Candle::getClosePrice)
                .collect(Collectors.toList());
    }

    private List<ru.tinkoff.invest.openapi.models.operations.Operation> getLastWeekOperations(String ticker) {
        OffsetDateTime to = OffsetDateTime.now();
        OffsetDateTime from = to.minusWeeks(1);
        return operationsService.getOperations(Interval.of(from, to), ticker);
    }

    protected void performOperation(String ticker, Decision decision) {
        if (decision == Decision.WAIT) {
            log.info("Decision is wait. Do nothing");
            return;
        }

        Operation operation = decision == Decision.BUY ? Operation.Buy : Operation.Sell;
        PlacedOrder order = ordersService.placeOrder(ticker, 1, operation, null);
        log.info("Placed order " + order);
    }

}