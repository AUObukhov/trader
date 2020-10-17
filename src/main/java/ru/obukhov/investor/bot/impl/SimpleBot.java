package ru.obukhov.investor.bot.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.DataSupplier;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class SimpleBot implements Bot {

    protected final DataSupplier dataSupplier;
    protected final Decider decider;
    protected final OrdersService ordersService;

    public SimpleBot(DataSupplier dataSupplier,
                     Decider decider,
                     OrdersService ordersService) {
        this.dataSupplier = dataSupplier;
        this.decider = decider;
        this.ordersService = ordersService;
    }

    @Override
    public void processTicker(String ticker) {
        try {
            List<Order> orders = ordersService.getOrders(ticker);
            if (!orders.isEmpty()) {
                log.info("There are not completed orders by ticker '" + ticker + "'. Do nothing");
                return;
            }

            DecisionData data = dataSupplier.getData(ticker);
            if (data.getCurrentPrice() == null) {
                log.info("There are no candles by ticker '" + ticker + "'. Do nothing");
                return;
            }

            Decision decision = decider.decide(data);
            performOperation(ticker, decision, data.getCurrentPrice());
        } catch (Exception ex) {
            log.error("Exception while process ticker " + ticker + ". Do nothing", ex);
        }
    }

    protected void performOperation(String ticker, Decision decision, BigDecimal currentPrice) {
        if (decision == Decision.WAIT) {
            log.info("Decision is wait. Do nothing");
            return;
        }

        Operation operation = decision == Decision.BUY ? Operation.Buy : Operation.Sell;
        PlacedOrder order = ordersService.placeOrder(ticker, 1, operation, currentPrice);
        log.info("Placed order " + order);
    }

}