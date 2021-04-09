package ru.obukhov.trader.bot.impl;

import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.strategy.Strategy;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledBotFactory extends AbstractBotFactory {

    private final OperationsService realOperationsService;
    private final OrdersService realOrdersService;
    private final PortfolioService realPortfolioService;
    private final BotConfig botConfig;

    public ScheduledBotFactory(
            TradingProperties tradingProperties,
            MarketService realMarketService,
            RealTinkoffService realTinkoffService,
            Collection<Strategy> strategies,
            OperationsService realOperationsService,
            OrdersService realOrdersService,
            PortfolioService realPortfolioService,
            BotConfig botConfig
    ) {

        super(tradingProperties, realMarketService, realTinkoffService, new HashSet<>(strategies));

        this.realOperationsService = realOperationsService;
        this.realOrdersService = realOrdersService;
        this.realPortfolioService = realPortfolioService;
        this.botConfig = botConfig;
    }

    @Override
    public Set<Bot> createBots() {
        return strategies.stream()
                .map(this::createScheduledBot)
                .collect(Collectors.toSet());
    }

    private Bot createScheduledBot(Strategy strategy) {

        return ScheduledBot.create(
                strategy,
                realMarketService,
                realOperationsService,
                realOrdersService,
                realPortfolioService,
                botConfig,
                tradingProperties
        );

    }

}