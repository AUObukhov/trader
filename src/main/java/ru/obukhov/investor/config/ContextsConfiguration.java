package ru.obukhov.investor.config;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.ThrottledMarketContext;
import ru.obukhov.investor.service.ThrottledOperationsContext;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OperationsContext;

/**
 * Bean for post initializing contexts
 */
@Configuration
@AllArgsConstructor
public class ContextsConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private final ConnectionService connectionService;
    private final ThrottledMarketContext throttledMarketContext;
    private final ThrottledOperationsContext throttledOperationsContext;

    @Override
    public void onApplicationEvent(@NotNull final ApplicationReadyEvent event) {

        MarketContext marketContext = connectionService.getMarketContext();
        throttledMarketContext.setInnerContext(marketContext);

        OperationsContext operationsContext = connectionService.getOperationsContext();
        throttledOperationsContext.setInnerContext(operationsContext);

    }

}