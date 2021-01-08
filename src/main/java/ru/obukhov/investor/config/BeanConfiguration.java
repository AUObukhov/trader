package ru.obukhov.investor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.bot.impl.DumbDecider;
import ru.obukhov.investor.bot.impl.FakeBotImpl;
import ru.obukhov.investor.bot.impl.GollumDecider;
import ru.obukhov.investor.bot.impl.ScheduledBot;
import ru.obukhov.investor.bot.impl.SimulatorImpl;
import ru.obukhov.investor.bot.impl.TrendReversalDecider;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.interfaces.FakeBot;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.service.impl.FakeTinkoffService;
import ru.obukhov.investor.service.impl.MarketServiceImpl;
import ru.obukhov.investor.service.impl.OperationsServiceImpl;
import ru.obukhov.investor.service.impl.OrdersServiceImpl;
import ru.obukhov.investor.service.impl.PortfolioServiceImpl;
import ru.obukhov.investor.service.impl.RealTinkoffService;
import ru.obukhov.investor.service.impl.StatisticsServiceImpl;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.ExcelService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.obukhov.investor.service.interfaces.StatisticsService;

import java.util.Set;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public Decider gollumDecider(TradingProperties tradingProperties) {
        return new GollumDecider(tradingProperties);
    }

    @Bean
    public Decider dumbDecider(TradingProperties tradingProperties) {
        return new DumbDecider(tradingProperties);
    }

    @Bean
    public Decider trendReversalDecider(TradingProperties tradingProperties,
                                        TrendReversalDeciderProperties deciderProperties) {
        return new TrendReversalDecider(tradingProperties, deciderProperties);
    }

    @Bean
    public FakeBot gollumFakeBot(Decider gollumDecider,
                                 MarketService fakeMarketService,
                                 OperationsService fakeOperationsService,
                                 OrdersService fakeOrdersService,
                                 PortfolioService fakePortfolioService,
                                 FakeTinkoffService fakeTinkoffService) {

        return new FakeBotImpl("Gollum bot",
                gollumDecider,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService);

    }

    @Bean
    public FakeBot dumbFakeBot(Decider dumbDecider,
                               MarketService fakeMarketService,
                               OperationsService fakeOperationsService,
                               OrdersService fakeOrdersService,
                               PortfolioService fakePortfolioService,
                               FakeTinkoffService fakeTinkoffService) {

        return new FakeBotImpl("Dumb bot",
                dumbDecider,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService);

    }

    @Bean
    public FakeBot trendReversalFakeBot(Decider trendReversalDecider,
                                        MarketService fakeMarketService,
                                        OperationsService fakeOperationsService,
                                        OrdersService fakeOrdersService,
                                        PortfolioService fakePortfolioService,
                                        FakeTinkoffService fakeTinkoffService) {

        return new FakeBotImpl("Trend reversal bot",
                trendReversalDecider,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService);

    }

    @Bean
    public Bot scheduledBot(Decider dumbDecider,
                            MarketService realMarketService,
                            OperationsService realOperationsService,
                            OrdersService realOrdersService,
                            PortfolioService realPortfolioService,
                            BotProperties botProperties,
                            TradingProperties tradingProperties) {

        return new ScheduledBot(dumbDecider,
                realMarketService,
                realOperationsService,
                realOrdersService,
                realPortfolioService,
                botProperties,
                tradingProperties);

    }

    @Bean
    public Simulator simulatorImpl(Set<FakeBot> fakeBots,
                                   MarketService fakeMarketService,
                                   TinkoffService fakeTinkoffService,
                                   ExcelService excelService,
                                   TradingProperties tradingProperties) {

        return new SimulatorImpl(fakeBots,
                fakeMarketService,
                (FakeTinkoffService) fakeTinkoffService,
                excelService,
                tradingProperties);
    }

    @Bean
    public TinkoffService realTinkoffService(ConnectionService connectionService) {
        return new RealTinkoffService(connectionService);
    }

    @Bean
    public TinkoffService fakeTinkoffService(TradingProperties tradingProperties,
                                             MarketService realMarketService,
                                             RealTinkoffService realTinkoffService) {
        return new FakeTinkoffService(tradingProperties, realMarketService, realTinkoffService);
    }

    @Bean
    public MarketService realMarketService(TradingProperties tradingProperties, TinkoffService realTinkoffService) {
        return new MarketServiceImpl(tradingProperties, realTinkoffService);
    }

    @Bean
    public MarketService fakeMarketService(TradingProperties tradingProperties, TinkoffService fakeTinkoffService) {
        return new MarketServiceImpl(tradingProperties, fakeTinkoffService);
    }

    @Bean
    public OperationsService realOperationsService(TinkoffService realTinkoffService, MarketService realMarketService) {
        return new OperationsServiceImpl(realTinkoffService);
    }

    @Bean
    public OperationsService fakeOperationsService(TinkoffService fakeTinkoffService, MarketService fakeMarketService) {
        return new OperationsServiceImpl(fakeTinkoffService);
    }

    @Bean
    public OrdersService realOrdersService(TinkoffService realTinkoffService, MarketService realMarketService) {
        return new OrdersServiceImpl(realTinkoffService, realMarketService);
    }

    @Bean
    public OrdersService fakeOrdersService(TinkoffService fakeTinkoffService, MarketService fakeMarketService) {
        return new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
    }

    @Bean
    public PortfolioService realPortfolioService(TinkoffService realTinkoffService) {
        return new PortfolioServiceImpl(realTinkoffService);
    }

    @Bean
    public PortfolioService fakePortfolioService(TinkoffService fakeTinkoffService) {
        return new PortfolioServiceImpl(fakeTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(MarketService realMarketService) {
        return new StatisticsServiceImpl(realMarketService);
    }

}