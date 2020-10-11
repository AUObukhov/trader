package ru.obukhov.investor.bot.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.interfaces.DataSupplier;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.interfaces.Scheduler;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.BotProperties;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class SchedulerImpl implements Scheduler {

    private final Decider decider;
    private final DataSupplier dataSupplier;
    private final BotProperties botProperties;
    private final OrdersService ordersService;
    private final TradingProperties tradingProperties;

    @Scheduled(fixedDelayString = "${bot.delay}")
    public void tick() {
        if (!DateUtils.isWorkTimeNow(tradingProperties.getWorkStartTime(), tradingProperties.getWorkDuration())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        botProperties.getTickers().forEach(this::processTicker);
    }

    private void processTicker(String ticker) {
        try {
            List<Order> orders = ordersService.getOrders(ticker);
            if (!orders.isEmpty()) {
                log.info("There are not completed orders by ticker " + ticker + ". Do nothing");
                return;
            }

            DecisionData data = dataSupplier.getData(ticker);
            Decision decision = decider.decide(data);
            performOperation(ticker, decision);
        } catch (Exception ex) {
            log.error("Exception while process ticker " + ticker + ". Do nothing", ex);
        }
    }

    private void performOperation(String ticker, Decision decision) {
        if (decision == Decision.WAIT) {
            log.info("Decision is wait. Do nothing");
            return;
        }

        Operation operation = decision == Decision.BUY ? Operation.Buy : Operation.Sell;
        PlacedOrder order = ordersService.placeOrder(ticker, 1, operation, null);
        log.info("Placed order " + order);
    }

}