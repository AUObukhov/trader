package ru.obukhov.trader.bot.impl;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.interfaces.Decider;
import ru.obukhov.trader.bot.interfaces.FakeBot;
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
@DependsOn("trendReversalDecider")
public class FakeBotFactory extends AbstractBotFactory {

    public FakeBotFactory(TradingProperties tradingProperties,
                          MarketService realMarketService,
                          RealTinkoffService realTinkoffService,
                          Decider conservativeDecider,
                          Decider dumbDecider,
                          Set<TrendReversalDecider> trendReversalDeciders) {

        super(tradingProperties,
                realMarketService,
                realTinkoffService,
                conservativeDecider,
                dumbDecider,
                trendReversalDeciders);

    }

    @Override
    public Bot createConservativeBot() {
        return createFakeBot("Conservative bot", conservativeDecider);
    }

    @Override
    public Bot createDumbBot() {
        return createFakeBot("Dumb bot", dumbDecider);
    }

    @Override
    public Set<Bot> createTrendReversalBots() {
        return trendReversalDeciders.stream()
                .map(this::createTrendReversalBot)
                .collect(Collectors.toSet());
    }

    private Bot createTrendReversalBot(TrendReversalDecider trendReversalDecider) {
        String name = String.format("Trend reversal bot (%s|%s)",
                trendReversalDecider.getExtremumPriceIndex(), trendReversalDecider.getLastPricesCount());

        return createFakeBot(name, trendReversalDecider);
    }

    private FakeBot createFakeBot(String name, Decider decider) {
        FakeTinkoffService fakeTinkoffService =
                new FakeTinkoffService(tradingProperties, realMarketService, realTinkoffService);
        MarketService fakeMarketService = new MarketServiceImpl(tradingProperties, fakeTinkoffService);
        OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);

        return new FakeBotImpl(name,
                decider,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService);
    }
}