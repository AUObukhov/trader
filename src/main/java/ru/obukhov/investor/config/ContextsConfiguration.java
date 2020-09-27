package ru.obukhov.investor.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.bot.impl.SchedulerImpl;
import ru.obukhov.investor.service.context.ThrottledMarketContext;
import ru.obukhov.investor.service.context.ThrottledOperationsContext;
import ru.obukhov.investor.service.context.ThrottledOrdersContext;
import ru.obukhov.investor.service.context.ThrottledPortfolioContext;
import ru.obukhov.investor.service.context.ThrottledSandboxContext;
import ru.obukhov.investor.service.interfaces.ConnectionService;

/**
 * Bean for post initializing of contexts
 * <br/>
 * Also creates {@link ru.obukhov.investor.bot.interfaces.Scheduler} bean, because if to create it with other beans,
 * it will start schedule before context initialization
 */
@Configuration
public class ContextsConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    private final ConnectionService connectionService;
    private final ThrottledMarketContext throttledMarketContext;
    private final ThrottledOperationsContext throttledOperationsContext;
    private final ThrottledOrdersContext throttledOrdersContext;
    private final ThrottledPortfolioContext throttledPortfolioContext;
    private final ThrottledSandboxContext throttledSandboxContext;

    @Autowired
    public ContextsConfiguration(ConnectionService connectionService,
                                 ThrottledMarketContext throttledMarketContext,
                                 ThrottledOperationsContext throttledOperationsContext,
                                 ThrottledOrdersContext throttledOrdersContext,
                                 ThrottledPortfolioContext throttledPortfolioContext,
                                 @Autowired(required = false) ThrottledSandboxContext throttledSandboxContext) {

        this.connectionService = connectionService;
        this.throttledMarketContext = throttledMarketContext;
        this.throttledOperationsContext = throttledOperationsContext;
        this.throttledOrdersContext = throttledOrdersContext;
        this.throttledPortfolioContext = throttledPortfolioContext;
        this.throttledSandboxContext = throttledSandboxContext;
    }

    @Override
    public void onApplicationEvent(@NotNull final ApplicationReadyEvent event) {

        throttledMarketContext.setInnerContext(connectionService.getMarketContext());
        throttledOperationsContext.setInnerContext(connectionService.getOperationsContext());
        throttledOrdersContext.setInnerContext(connectionService.getOrdersContext());
        throttledPortfolioContext.setInnerContext(connectionService.getPortfolioContext());

        if (throttledSandboxContext != null) {
            throttledSandboxContext.setInnerContext(connectionService.getSandboxContext());
        }

        event.getApplicationContext()
                .getBeanFactory()
                .createBean(SchedulerImpl.class);
    }

}