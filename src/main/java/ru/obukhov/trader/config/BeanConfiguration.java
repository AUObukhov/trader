package ru.obukhov.trader.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.trader.bot.impl.ConservativeDecider;
import ru.obukhov.trader.bot.impl.DumbDecider;
import ru.obukhov.trader.bot.impl.TrendReversalDecider;
import ru.obukhov.trader.bot.interfaces.Decider;
import ru.obukhov.trader.market.impl.MarketServiceImpl;
import ru.obukhov.trader.market.impl.OperationsServiceImpl;
import ru.obukhov.trader.market.impl.OrdersServiceImpl;
import ru.obukhov.trader.market.impl.PortfolioServiceImpl;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.SandboxServiceImpl;
import ru.obukhov.trader.market.impl.StatisticsServiceImpl;
import ru.obukhov.trader.market.interfaces.ConnectionService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.interfaces.SandboxService;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.interfaces.TinkoffService;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public Decider conservativeDecider(TradingProperties tradingProperties) {
        return new ConservativeDecider(tradingProperties);
    }

    @Bean
    public Decider dumbDecider(TradingProperties tradingProperties) {
        return new DumbDecider(tradingProperties);
    }

    @Bean
    public Set<Decider> trendReversalDecider(TradingProperties tradingProperties,
                                             TrendReversalDeciderProperties trendReversalDeciderProperties,
                                             ConfigurableListableBeanFactory beanFactory) {

        return trendReversalDeciderProperties.getConfigs().stream()
                .map(config -> createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, config))
                .collect(Collectors.toSet());
    }

    private TrendReversalDecider createAndRegisterTrendReversalDecider(ConfigurableListableBeanFactory beanFactory,
                                                                       TradingProperties tradingProperties,
                                                                       TrendReversalDeciderProperties.DeciderConfig config) {

        String name = String.format("trendReversalDecider (%s|%s)",
                config.getExtremumPriceIndex(), config.getLastPricesCount());

        TrendReversalDecider decider = new TrendReversalDecider(
                tradingProperties,
                config.getLastPricesCount(),
                config.getExtremumPriceIndex());

        beanFactory.registerSingleton(name, decider);
        return decider;
    }

    @Bean
    public TinkoffService realTinkoffService(ConnectionService connectionService) {
        return new RealTinkoffService(connectionService);
    }

    @Bean
    public MarketService realMarketService(TradingProperties tradingProperties, TinkoffService realTinkoffService) {
        return new MarketServiceImpl(tradingProperties, realTinkoffService);
    }

    @Bean
    @ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
    public SandboxService sandboxService(ConnectionService connectionService, MarketService realMarketService) {
        return new SandboxServiceImpl(connectionService, realMarketService);
    }

    @Bean
    public OperationsService realOperationsService(TinkoffService realTinkoffService, MarketService realMarketService) {
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