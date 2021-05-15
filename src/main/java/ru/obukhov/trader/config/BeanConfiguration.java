package ru.obukhov.trader.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.trader.bot.strategy.Strategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.DumbStrategy;
import ru.obukhov.trader.bot.strategy.impl.GoldenCrossStrategy;
import ru.obukhov.trader.bot.strategy.impl.TrendReversalStrategy;
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
import ru.tinkoff.invest.openapi.OpenApi;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
@SuppressWarnings("unused")
public class BeanConfiguration {

    @Bean
    public Strategy conservativeStrategy(TradingProperties tradingProperties) {
        return new ConservativeStrategy(tradingProperties);
    }

    @Bean
    public Strategy dumbStrategy(TradingProperties tradingProperties) {
        return new DumbStrategy(tradingProperties);
    }

    @Bean
    public Set<Strategy> trendReversalStrategy(
            TradingProperties tradingProperties,
            TrendReversalStrategyProperties trendReversalStrategyProperties,
            ConfigurableListableBeanFactory beanFactory
    ) {
        return trendReversalStrategyProperties.getConfigs().stream()
                .map(config -> createAndRegisterTrendReversalStrategy(beanFactory, tradingProperties, config))
                .collect(Collectors.toSet());
    }

    private TrendReversalStrategy createAndRegisterTrendReversalStrategy(
            ConfigurableListableBeanFactory beanFactory,
            TradingProperties tradingProperties,
            TrendReversalStrategyProperties.StrategyConfig config
    ) {
        TrendReversalStrategy strategy = new TrendReversalStrategy(
                tradingProperties,
                config.getLastPricesCount(),
                config.getExtremumPriceIndex()
        );

        beanFactory.registerSingleton(strategy.getName(), strategy);

        return strategy;
    }

    @Bean
    public Set<Strategy> goldenCrossStrategy(
            ConfigurableListableBeanFactory beanFactory,
            TradingProperties tradingProperties,
            GoldenCrossStrategyProperties strategyProperties
    ) {
        return strategyProperties.getConfigs().stream()
                .map(config -> createAndRegisterGoldenCrossStrategy(beanFactory, tradingProperties, config))
                .collect(Collectors.toSet());
    }

    private GoldenCrossStrategy createAndRegisterGoldenCrossStrategy(
            ConfigurableListableBeanFactory beanFactory,
            TradingProperties tradingProperties,
            GoldenCrossStrategyProperties.StrategyConfig config
    ) {
        GoldenCrossStrategy strategy = new GoldenCrossStrategy(
                tradingProperties,
                config.getSmallWindow(),
                config.getBigWindow(),
                config.getIndexCoefficient()
        );

        beanFactory.registerSingleton(strategy.getName(), strategy);

        return strategy;
    }

    @Bean
    public TinkoffService realTinkoffService(OpenApi opeApi) {
        return new RealTinkoffService(opeApi);
    }

    @Bean
    public MarketService realMarketService(TradingProperties tradingProperties, TinkoffService realTinkoffService) {
        return new MarketServiceImpl(tradingProperties, realTinkoffService);
    }

    @Bean
    @ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
    public SandboxService sandboxService(OpenApi opeApi, MarketService realMarketService) {
        return new SandboxServiceImpl(opeApi, realMarketService);
    }

    @Bean
    public OperationsService realOperationsService(TinkoffService realTinkoffService) {
        return new OperationsServiceImpl(realTinkoffService);
    }

    @Bean
    public OrdersService realOrdersService(TinkoffService realTinkoffService, MarketService realMarketService) {
        return new OrdersServiceImpl(realTinkoffService, realMarketService);
    }

    @Bean
    public PortfolioService realPortfolioService(TinkoffService realTinkoffService) {
        return new PortfolioServiceImpl(realTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(MarketService realMarketService) {
        return new StatisticsServiceImpl(realMarketService);
    }

}