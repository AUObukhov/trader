package ru.obukhov.trader.bot.impl;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.strategy.Strategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.DumbStrategy;
import ru.obukhov.trader.bot.strategy.impl.TrendReversalStrategy;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketServiceImpl;
import ru.obukhov.trader.market.impl.OperationsServiceImpl;
import ru.obukhov.trader.market.impl.OrdersServiceImpl;
import ru.obukhov.trader.market.impl.PortfolioServiceImpl;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@DependsOn("trendReversalStrategy")
public class FakeBotFactory extends AbstractBotFactory {

    public FakeBotFactory(TradingProperties tradingProperties,
                          MarketService realMarketService,
                          RealTinkoffService realTinkoffService,
                          Set<Strategy> strategies) {

        super(tradingProperties,
                realMarketService,
                realTinkoffService,
                strategies);

    }

    @Override
    public Set<Bot> createBots() {
        return strategies.stream()
                .map(this::createBot)
                .collect(Collectors.toSet());
    }

    private Bot createBot(Strategy strategy) {
        String name = getBotName(strategy);
        FakeTinkoffService fakeTinkoffService =
                new FakeTinkoffService(tradingProperties, realMarketService, realTinkoffService);
        MarketService fakeMarketService = new MarketServiceImpl(tradingProperties, fakeTinkoffService);
        OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);

        return FakeBotImpl.create(
                name,
                strategy,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService
        );
    }

    private String getBotName(Strategy strategy) {
        if (strategy instanceof ConservativeStrategy) {
            return "Conservative bot";
        } else if (strategy instanceof DumbStrategy) {
            return "Dumb bot";
        } else if (strategy instanceof TrendReversalStrategy) {
            TrendReversalStrategy trendReversalStrategy = (TrendReversalStrategy) strategy;
            return String.format("Trend reversal bot (%s|%s)",
                    trendReversalStrategy.getExtremumPriceIndex(), trendReversalStrategy.getLastPricesCount());
        } else {
            throw new IllegalArgumentException("Unknown strategy class: " + strategy.getClass());
        }
    }

}