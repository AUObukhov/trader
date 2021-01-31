package ru.obukhov.investor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.bot.impl.*;
import ru.obukhov.investor.bot.interfaces.*;
import ru.obukhov.investor.service.impl.*;
import ru.obukhov.investor.service.interfaces.*;

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
        return new TrendReversalDecider(tradingProperties,
                deciderProperties.getLastPricesCount(),
                deciderProperties.getExtremumPriceIndex());
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
                                   TinkoffService fakeTinkoffService,
                                   ExcelService excelService) {

        return new SimulatorImpl(fakeBots, (FakeTinkoffService) fakeTinkoffService, excelService);
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
    @ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
    public SandboxService sandboxService(ConnectionService connectionService, MarketService realMarketService) {
        return new SandboxServiceImpl(connectionService, realMarketService);
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