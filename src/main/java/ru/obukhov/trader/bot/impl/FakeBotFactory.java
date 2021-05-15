package ru.obukhov.trader.bot.impl;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.strategy.Strategy;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@DependsOn({"trendReversalStrategy", "goldenCrossStrategy"})
public class FakeBotFactory extends AbstractBotFactory {

    public FakeBotFactory(
            TradingProperties tradingProperties,
            MarketService realMarketService,
            RealTinkoffService realTinkoffService,
            Collection<Strategy> strategies
    ) {
        super(tradingProperties, realMarketService, realTinkoffService, new HashSet<>(strategies));
    }

    @Override
    public Set<Bot> createBots() {
        return strategies.stream()
                .map(this::createBot)
                .collect(Collectors.toSet());
    }

    private Bot createBot(Strategy strategy) {
        FakeTinkoffService fakeTinkoffService =
                new FakeTinkoffService(tradingProperties, realMarketService, realTinkoffService);
        MarketService fakeMarketService = new MarketServiceImpl(tradingProperties, fakeTinkoffService);
        OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);

        return FakeBotImpl.create(
                strategy,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService
        );
    }

}