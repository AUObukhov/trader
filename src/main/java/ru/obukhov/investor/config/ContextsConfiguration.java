package ru.obukhov.investor.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.service.context.ThrottledMarketContext;
import ru.obukhov.investor.service.context.ThrottledOperationsContext;
import ru.obukhov.investor.service.context.ThrottledOrdersContext;
import ru.obukhov.investor.service.context.ThrottledSandboxContext;
import ru.obukhov.investor.service.interfaces.ConnectionService;

/**
 * Bean for post initializing of contexts
 */
@Configuration
public class ContextsConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private final ConnectionService connectionService;
    private final ThrottledMarketContext throttledMarketContext;
    private final ThrottledOperationsContext throttledOperationsContext;
    private final ThrottledOrdersContext throttledOrdersContext;
    private final ThrottledSandboxContext throttledSandboxContext;

    @Autowired
    public ContextsConfiguration(ConnectionService connectionService,
                                 ThrottledMarketContext throttledMarketContext,
                                 ThrottledOperationsContext throttledOperationsContext,
                                 ThrottledOrdersContext throttledOrdersContext,
                                 @Autowired(required = false) ThrottledSandboxContext throttledSandboxContext) {

        this.connectionService = connectionService;
        this.throttledMarketContext = throttledMarketContext;
        this.throttledOperationsContext = throttledOperationsContext;
        this.throttledOrdersContext = throttledOrdersContext;
        this.throttledSandboxContext = throttledSandboxContext;
    }

    @Override
    public void onApplicationEvent(@NotNull final ApplicationReadyEvent event) {

        throttledMarketContext.setInnerContext(connectionService.getMarketContext());
        throttledOperationsContext.setInnerContext(connectionService.getOperationsContext());
        throttledOrdersContext.setInnerContext(connectionService.getOrdersContext());

        if (throttledSandboxContext != null) {
            throttledSandboxContext.setInnerContext(connectionService.getSandboxContext());
        }

    }

}