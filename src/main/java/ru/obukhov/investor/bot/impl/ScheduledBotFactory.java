package ru.obukhov.investor.bot.impl;

import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.config.BotProperties;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.market.impl.RealTinkoffService;
import ru.obukhov.investor.market.interfaces.MarketService;
import ru.obukhov.investor.market.interfaces.OperationsService;
import ru.obukhov.investor.market.interfaces.OrdersService;
import ru.obukhov.investor.market.interfaces.PortfolioService;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScheduledBotFactory extends AbstractBotFactory {

    private final OperationsService realOperationsService;
    private final OrdersService realOrdersService;
    private final PortfolioService realPortfolioService;
    private final BotProperties botProperties;

    public ScheduledBotFactory(TradingProperties tradingProperties,
                               MarketService realMarketService,
                               RealTinkoffService realTinkoffService,
                               Decider conservativeDecider,
                               Decider dumbDecider,
                               Set<TrendReversalDecider> trendReversalDeciders,
                               OperationsService realOperationsService,
                               OrdersService realOrdersService,
                               PortfolioService realPortfolioService,
                               BotProperties botProperties) {

        super(tradingProperties,
                realMarketService,
                realTinkoffService,
                conservativeDecider,
                dumbDecider,
                trendReversalDeciders);

        this.realOperationsService = realOperationsService;
        this.realOrdersService = realOrdersService;
        this.realPortfolioService = realPortfolioService;
        this.botProperties = botProperties;
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
                botProperties,
                tradingProperties);

    }

}