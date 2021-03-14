package ru.obukhov.trader.bot.impl;

import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.interfaces.Decider;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduledBotFactory extends AbstractBotFactory {

    private final OperationsService realOperationsService;
    private final OrdersService realOrdersService;
    private final PortfolioService realPortfolioService;
    private final BotConfig botConfig;

    public ScheduledBotFactory(TradingProperties tradingProperties,
                               MarketService realMarketService,
                               RealTinkoffService realTinkoffService,
                               Decider conservativeDecider,
                               Decider dumbDecider,
                               Set<TrendReversalDecider> trendReversalDeciders,
                               OperationsService realOperationsService,
                               OrdersService realOrdersService,
                               PortfolioService realPortfolioService,
                               BotConfig botConfig) {

        super(tradingProperties,
                realMarketService,
                realTinkoffService,
                conservativeDecider,
                dumbDecider,
                trendReversalDeciders);

        this.realOperationsService = realOperationsService;
        this.realOrdersService = realOrdersService;
        this.realPortfolioService = realPortfolioService;
        this.botConfig = botConfig;
    }

    @Override
    public Bot createConservativeBot() {
        return createScheduledBot(conservativeDecider);
    }

    @Override
    public Bot createDumbBot() {
        return createScheduledBot(dumbDecider);
    }

    @Override
    public Set<Bot> createTrendReversalBots() {
        return trendReversalDeciders.stream()
                .map(this::createScheduledBot)
                .collect(Collectors.toSet());
    }

    private Bot createScheduledBot(Decider decider) {

        return new ScheduledBot(decider,
                realMarketService,
                realOperationsService,
                realOrdersService,
                realPortfolioService,
                botConfig,
                tradingProperties);

    }

}