package ru.obukhov.trader.bot.impl;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.bot.interfaces.Bot;
import ru.obukhov.trader.bot.interfaces.Decider;
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
                          Set<Decider> deciders) {

        super(tradingProperties,
                realMarketService,
                realTinkoffService,
                deciders);

    }

    @Override
    public Set<Bot> createBots() {
        return deciders.stream()
                .map(this::createBot)
                .collect(Collectors.toSet());
    }

    private Bot createBot(Decider decider) {
        String name = getBotName(decider);
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

    private String getBotName(Decider decider) {
        if (decider instanceof ConservativeDecider) {
            return "Conservative bot";
        } else if (decider instanceof DumbDecider) {
            return "Dumb bot";
        } else if (decider instanceof TrendReversalDecider) {
            TrendReversalDecider trendReversalDecider = (TrendReversalDecider) decider;
            return String.format("Trend reversal bot (%s|%s)",
                    trendReversalDecider.getExtremumPriceIndex(), trendReversalDecider.getLastPricesCount());
        } else {
            throw new IllegalArgumentException("Unknown decider class: " + decider.getClass());
        }
    }

}