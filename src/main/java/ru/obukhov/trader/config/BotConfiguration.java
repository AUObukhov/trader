package ru.obukhov.trader.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import ru.obukhov.trader.bot.impl.ScheduledBotFactory;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.interfaces.BotFactory;
import ru.obukhov.trader.bot.strategy.Strategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.GoldenCrossStrategy;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration of bots beans, which need qualifying of dependencies
 */
@Configuration
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BotConfiguration {

    @Bean
    public Strategy conservativeStrategy(TradingProperties tradingProperties) {
        return new ConservativeStrategy(tradingProperties);
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
    @Autowired
    public BotFactory scheduledBotFactory(
            TradingProperties tradingProperties,
            MarketService realMarketService,
            RealTinkoffService realTinkoffService,
            Set<GoldenCrossStrategy> strategies,
            OperationsService realOperationsService,
            OrdersService realOrdersService,
            PortfolioService realPortfolioService,
            BotConfig botConfig
    ) {
        Set<Strategy> strategySet = Set.of(strategies.stream().findFirst().orElseThrow());
        return new ScheduledBotFactory(
                tradingProperties,
                realMarketService,
                realTinkoffService,
                strategySet,
                realOperationsService,
                realOrdersService,
                realPortfolioService,
                botConfig
        );
    }

    @Bean
    public Bot scheduledBot(BotFactory scheduledBotFactory) {
        Set<Bot> bots = scheduledBotFactory.createBots();
        Assert.isTrue(bots.size() == 1, "Expected single scheduled bot");

        return bots.iterator().next();
    }

}