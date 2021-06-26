package ru.obukhov.trader.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
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
    public MarketService realMarketService(
            final TradingProperties tradingProperties,
            final TinkoffService realTinkoffService
    ) {
        return new MarketServiceImpl(tradingProperties, realTinkoffService);
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
    public OrdersService realOrdersService(
            final TinkoffService realTinkoffService,
            final MarketService realMarketService
    ) {
        return new OrdersServiceImpl(realTinkoffService, realMarketService);
    }

    @Bean
    public PortfolioService realPortfolioService(final TinkoffService realTinkoffService) {
        return new PortfolioServiceImpl(realTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(final MarketService realMarketService) {
        return new StatisticsServiceImpl(realMarketService);
    }

    @Bean
    public ScheduledBot scheduledBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final ScheduledBotProperties scheduledBotProperties,
            final TradingProperties tradingProperties,
            final TradingStrategyFactory strategyFactory
    ) {
        final TradingStrategy strategy = strategyFactory.createStrategy(
                scheduledBotProperties.getStrategyType(),
                scheduledBotProperties.getStrategyParams()
        );
        return new ScheduledBot(
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                strategy,
                scheduledBotProperties.getCandleResolution(),
                scheduledBotProperties,
                tradingProperties
        );

    }
}