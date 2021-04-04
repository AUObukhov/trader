package ru.obukhov.trader.config;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.Assert;
import ru.obukhov.trader.bot.impl.ScheduledBotFactory;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.interfaces.BotFactory;
import ru.obukhov.trader.bot.strategy.impl.DumbStrategy;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

import java.util.Set;

/**
 * Configuration of bots beans, which need qualifying of dependencies
 */
@Configuration
@DependsOn("beanConfiguration")
@RequiredArgsConstructor
public class BotConfiguration {

    @Bean
    @Autowired
    public BotFactory scheduledBotFactory(
            TradingProperties tradingProperties,
            MarketService realMarketService,
            RealTinkoffService realTinkoffService,
            DumbStrategy strategy,
            OperationsService realOperationsService,
            OrdersService realOrdersService,
            PortfolioService realPortfolioService,
            BotConfig botConfig
    ) {

        return new ScheduledBotFactory(
                tradingProperties,
                realMarketService,
                realTinkoffService,
                Sets.newHashSet(strategy),
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