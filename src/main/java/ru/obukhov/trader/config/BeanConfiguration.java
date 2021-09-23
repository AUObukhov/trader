package ru.obukhov.trader.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.market.impl.MarketServiceImpl;
import ru.obukhov.trader.market.impl.OperationsServiceImpl;
import ru.obukhov.trader.market.impl.OrdersServiceImpl;
import ru.obukhov.trader.market.impl.PortfolioServiceImpl;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.SandboxServiceImpl;
import ru.obukhov.trader.market.impl.StatisticsServiceImpl;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.interfaces.SandboxService;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.trading.bots.impl.ScheduledBot;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.tinkoff.invest.openapi.OpenApi;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
@SuppressWarnings("unused")
public class BeanConfiguration {

    @Bean
    public TinkoffService realTinkoffService(final OpenApi opeApi) {
        return new RealTinkoffService(opeApi);
    }

    @Bean
    public MarketService realMarketService(final MarketProperties marketProperties, final TinkoffService realTinkoffService) {
        return new MarketServiceImpl(marketProperties, realTinkoffService);
    }

    @Bean
    @ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
    public SandboxService sandboxService(final OpenApi opeApi, final MarketService realMarketService) {
        return new SandboxServiceImpl(opeApi, realMarketService);
    }

    @Bean
    public OperationsService realOperationsService(final TinkoffService realTinkoffService) {
        return new OperationsServiceImpl(realTinkoffService);
    }

    @Bean
    public OrdersService realOrdersService(final TinkoffService realTinkoffService, final MarketService realMarketService) {
        return new OrdersServiceImpl(realTinkoffService, realMarketService);
    }

    @Bean
    public PortfolioService realPortfolioService(final TinkoffService realTinkoffService) {
        return new PortfolioServiceImpl(realTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(final MarketService realMarketService, final ApplicationContext applicationContext) {
        return new StatisticsServiceImpl(realMarketService, applicationContext);
    }

    @Bean
    public ScheduledBot scheduledBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final ScheduledBotProperties scheduledBotProperties,
            final MarketProperties marketProperties,
            final TradingStrategyFactory strategyFactory
    ) {
        return new ScheduledBot(
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                strategyFactory.createStrategy(scheduledBotProperties.getBotConfig()),
                scheduledBotProperties,
                marketProperties
        );

    }
}