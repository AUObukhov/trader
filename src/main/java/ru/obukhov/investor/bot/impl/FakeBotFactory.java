package ru.obukhov.investor.bot.impl;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.interfaces.FakeBot;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.market.impl.FakeTinkoffService;
import ru.obukhov.investor.market.impl.MarketServiceImpl;
import ru.obukhov.investor.market.impl.OperationsServiceImpl;
import ru.obukhov.investor.market.impl.OrdersServiceImpl;
import ru.obukhov.investor.market.impl.PortfolioServiceImpl;
import ru.obukhov.investor.market.impl.RealTinkoffService;
import ru.obukhov.investor.market.interfaces.MarketService;
import ru.obukhov.investor.market.interfaces.OperationsService;
import ru.obukhov.investor.market.interfaces.OrdersService;
import ru.obukhov.investor.market.interfaces.PortfolioService;

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