package ru.obukhov.trader.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.ScheduledBotsProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.OperationsService;
import ru.obukhov.trader.market.impl.OrdersService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.SandboxService;
import ru.obukhov.trader.market.impl.StatisticsServiceImpl;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.trading.bots.impl.ScheduledBot;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.tinkoff.invest.openapi.OpenApi;

import java.util.List;

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
        return new MarketService(marketProperties, realTinkoffService);
    }

    @Bean
    @ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
    public SandboxService sandboxService(final OpenApi opeApi, final MarketService realMarketService) {
        return new SandboxService(opeApi, realMarketService);
    }

    @Bean
    public OperationsService realOperationsService(final TinkoffService realTinkoffService) {
        return new OperationsService(realTinkoffService);
    }

    @Bean
    public OrdersService realOrdersService(final TinkoffService realTinkoffService, final MarketService realMarketService) {
        return new OrdersService(realTinkoffService, realMarketService);
    }

    @Bean
    public PortfolioService realPortfolioService(final TinkoffService realTinkoffService) {
        return new PortfolioService(realTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(final MarketService realMarketService, final ApplicationContext applicationContext) {
        return new StatisticsServiceImpl(realMarketService, applicationContext);
    }

    @Bean
    public List<ScheduledBot> scheduledBots(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final RealTinkoffService realTinkoffService,
            final TradingStrategyFactory strategyFactory,
            final SchedulingProperties schedulingProperties,
            final ScheduledBotsProperties scheduledBotsProperties,
            final MarketProperties marketProperties
    ) {
        return scheduledBotsProperties.getBotConfigs().stream()
                .map(botConfig -> new ScheduledBot(
                        marketService,
                        operationsService,
                        ordersService,
                        portfolioService,
                        realTinkoffService,
                        strategyFactory.createStrategy(botConfig),
                        schedulingProperties,
                        botConfig,
                        marketProperties
                )).toList();
    }
}