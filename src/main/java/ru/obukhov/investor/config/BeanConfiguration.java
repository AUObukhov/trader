package ru.obukhov.investor.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import ru.obukhov.investor.bot.impl.ConservativeDecider;
import ru.obukhov.investor.bot.impl.DumbDecider;
import ru.obukhov.investor.bot.impl.FakeBotImpl;
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
import ru.obukhov.investor.service.impl.SandboxServiceImpl;
import ru.obukhov.investor.service.impl.StatisticsServiceImpl;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.ExcelService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.obukhov.investor.service.interfaces.SandboxService;
import ru.obukhov.investor.service.interfaces.StatisticsService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public Decider conservativeDecider(TradingProperties tradingProperties) {
        return new ConservativeDecider(tradingProperties);
    }

    @Bean
    public Decider dumbDecider(TradingProperties tradingProperties) {
        return new DumbDecider(tradingProperties);
    }

    @Bean
    public Set<Decider> trendReversalDecider(TradingProperties tradingProperties,
                                             ConfigurableListableBeanFactory beanFactory) {
        Set<Decider> deciders = new HashSet<>();
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 47, 50));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 95, 100));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 180, 200));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 275, 300));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 475, 500));
        deciders.add(createAndRegisterTrendReversalDecider(beanFactory, tradingProperties, 950, 1000));

        return deciders;
    }

    @NotNull
    private TrendReversalDecider createAndRegisterTrendReversalDecider(ConfigurableListableBeanFactory beanFactory,
                                                                       TradingProperties tradingProperties,
                                                                       int extremumPriceIndex,
                                                                       int lastPricesCount) {
        String name = String.format("trendReversalDecider (%s|%s)", extremumPriceIndex, lastPricesCount);
        TrendReversalDecider decider = new TrendReversalDecider(tradingProperties, lastPricesCount, extremumPriceIndex);
        beanFactory.registerSingleton(name, decider);
        return decider;
    }

    @Bean
    public FakeBot conservativeFakeBot(Decider conservativeDecider,
                                       TradingProperties tradingProperties,
                                       MarketService realMarketService,
                                       RealTinkoffService realTinkoffService) {

        FakeTinkoffService fakeTinkoffService =
                new FakeTinkoffService(tradingProperties, realMarketService, realTinkoffService);
        MarketService fakeMarketService = new MarketServiceImpl(tradingProperties, fakeTinkoffService);
        OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);

        return new FakeBotImpl("Conservative bot",
                conservativeDecider,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService);

    }

    @Bean
    public FakeBot dumbFakeBot(Decider dumbDecider,
                               TradingProperties tradingProperties,
                               MarketService realMarketService,
                               RealTinkoffService realTinkoffService) {

        FakeTinkoffService fakeTinkoffService =
                new FakeTinkoffService(tradingProperties, realMarketService, realTinkoffService);
        MarketService fakeMarketService = new MarketServiceImpl(tradingProperties, fakeTinkoffService);
        OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);

        return new FakeBotImpl("Dumb bot",
                dumbDecider,
                fakeMarketService,
                fakeOperationsService,
                fakeOrdersService,
                fakePortfolioService,
                fakeTinkoffService);

    }

    @Bean
    @DependsOn("trendReversalDecider")
    public List<FakeBot> trendReversalFakeBots(Set<TrendReversalDecider> trendReversalDeciders,
                                               TradingProperties tradingProperties,
                                               MarketService realMarketService,
                                               RealTinkoffService realTinkoffService,
                                               ConfigurableListableBeanFactory beanFactory) {
        List<FakeBot> fakeBots = trendReversalDeciders.stream()
                .map(decider -> trendReversalFakeBot(
                        decider,
                        tradingProperties,
                        realMarketService,
                        realTinkoffService))
                .collect(Collectors.toList());
        fakeBots.forEach(bot -> beanFactory.registerSingleton(bot.getName(), bot));
        return fakeBots;
    }

    private FakeBot trendReversalFakeBot(TrendReversalDecider trendReversalDecider,
                                         TradingProperties tradingProperties,
                                         MarketService realMarketService,
                                         RealTinkoffService realTinkoffService) {


        FakeTinkoffService fakeTinkoffService =
                new FakeTinkoffService(tradingProperties, realMarketService, realTinkoffService);
        MarketService fakeMarketService = new MarketServiceImpl(tradingProperties, fakeTinkoffService);
        OperationsService fakeOperationsService = new OperationsServiceImpl(fakeTinkoffService);
        OrdersService fakeOrdersService = new OrdersServiceImpl(fakeTinkoffService, fakeMarketService);
        PortfolioService fakePortfolioService = new PortfolioServiceImpl(fakeTinkoffService);

        String name = String.format("Trend reversal bot (%s|%s)",
                trendReversalDecider.getExtremumPriceIndex(), trendReversalDecider.getLastPricesCount());
        return new FakeBotImpl(name,
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
    @DependsOn("trendReversalFakeBots")
    public Simulator simulatorImpl(Set<FakeBot> fakeBots,
                                   ExcelService excelService) {

        return new SimulatorImpl(fakeBots, excelService);
    }

    @Bean
    public TinkoffService realTinkoffService(ConnectionService connectionService) {
        return new RealTinkoffService(connectionService);
    }

    @Bean
    public MarketService realMarketService(TradingProperties tradingProperties, TinkoffService realTinkoffService) {
        return new MarketServiceImpl(tradingProperties, realTinkoffService);
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
    public OrdersService realOrdersService(TinkoffService realTinkoffService, MarketService realMarketService) {
        return new OrdersServiceImpl(realTinkoffService, realMarketService);
    }

    @Bean
    public PortfolioService realPortfolioService(TinkoffService realTinkoffService) {
        return new PortfolioServiceImpl(realTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(MarketService realMarketService) {
        return new StatisticsServiceImpl(realMarketService);
    }

}