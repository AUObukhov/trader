package ru.obukhov.investor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.bot.impl.FakeBot;
import ru.obukhov.investor.bot.impl.ScheduledBot;
import ru.obukhov.investor.bot.impl.SimulatorImpl;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.Decider;
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
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.obukhov.investor.service.interfaces.StatisticsService;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public Bot fakeBot(Decider decider,
                       MarketService fakeMarketService,
                       OperationsService fakeOperationsService,
                       OrdersService fakeOrdersService,
                       PortfolioService fakePortfolioService,
                       FakeTinkoffService fakeTinkoffService) {

        return new FakeBot(decider,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService);

    }

    @Bean
    public Bot scheduledBot(Decider decider,
                            MarketService realMarketService,
                            OperationsService realOperationsService,
                            OrdersService realOrdersService,
                            PortfolioService realPortfolioService,
                            BotProperties botProperties,
                            TradingProperties tradingProperties) {

        return new ScheduledBot(decider,
                realMarketService,
                realOperationsService,
                realOrdersService,
                realPortfolioService,
                botProperties,
                tradingProperties);

    }

    @Bean
    public Simulator simulatorImpl(Bot fakeBot, FakeTinkoffService fakeTinkoffService) {
        return new SimulatorImpl(fakeBot, fakeTinkoffService);
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
    public MarketService realMarketService(TinkoffService realTinkoffService) {
        return new MarketServiceImpl(realTinkoffService);
    }

    @Bean
    public MarketService fakeMarketService(TinkoffService fakeTinkoffService) {
        return new MarketServiceImpl(fakeTinkoffService);
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