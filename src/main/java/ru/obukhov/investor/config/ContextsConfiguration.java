package ru.obukhov.investor.config;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.service.context.ThrottledMarketContext;
import ru.obukhov.investor.service.context.ThrottledOperationsContext;
import ru.obukhov.investor.service.context.ThrottledOrdersContext;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;

/**
 * Bean for post initializing of contexts
 */
@Configuration
@AllArgsConstructor
public class ContextsConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private final ConnectionService connectionService;
    private final ThrottledMarketContext throttledMarketContext;
    private final ThrottledOperationsContext throttledOperationsContext;
    private final ThrottledOrdersContext throttledOrdersContext;

    @Override
    public void onApplicationEvent(@NotNull final ApplicationReadyEvent event) {

        MarketContext marketContext = connectionService.getMarketContext();
        throttledMarketContext.setInnerContext(marketContext);

        OperationsContext operationsContext = connectionService.getOperationsContext();
        throttledOperationsContext.setInnerContext(operationsContext);

        OrdersContext ordersContext = connectionService.getOrdersContext();
        throttledOrdersContext.setInnerContext(ordersContext);

    }

}