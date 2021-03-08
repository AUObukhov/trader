package ru.obukhov.investor.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.bot.impl.ConservativeDecider;
import ru.obukhov.investor.bot.impl.DumbDecider;
import ru.obukhov.investor.bot.impl.TrendReversalDecider;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.market.impl.MarketServiceImpl;
import ru.obukhov.investor.market.impl.OperationsServiceImpl;
import ru.obukhov.investor.market.impl.OrdersServiceImpl;
import ru.obukhov.investor.market.impl.PortfolioServiceImpl;
import ru.obukhov.investor.market.impl.RealTinkoffService;
import ru.obukhov.investor.market.impl.SandboxServiceImpl;
import ru.obukhov.investor.market.impl.StatisticsServiceImpl;
import ru.obukhov.investor.market.interfaces.ConnectionService;
import ru.obukhov.investor.market.interfaces.MarketService;
import ru.obukhov.investor.market.interfaces.OperationsService;
import ru.obukhov.investor.market.interfaces.OrdersService;
import ru.obukhov.investor.market.interfaces.PortfolioService;
import ru.obukhov.investor.market.interfaces.SandboxService;
import ru.obukhov.investor.market.interfaces.StatisticsService;
import ru.obukhov.investor.market.interfaces.TinkoffService;

import java.util.HashSet;
import java.util.Set;

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
                                             ConfigurableListableBeanFactory beanFactory) {

        Set<Decider> deciders = new HashSet<>();
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 47, 50));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 95, 100));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 180, 200));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 275, 300));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 475, 500));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 950, 1000));

        return deciders;
    }

    private TrendReversalDecider createAndRegisterTrendReversalDecider(ConfigurableListableBeanFactory beanFactory,
                                                                       TradingProperties tradingProperties,
                                                                       int extremumPriceIndex,
                                                                       int lastPricesCount) {
        String name = String.format("trendReversalDecider (%s|%s)", extremumPriceIndex, lastPricesCount);
        TrendReversalDecider decider = new TrendReversalDecider(tradingProperties, lastPricesCount, extremumPriceIndex);
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