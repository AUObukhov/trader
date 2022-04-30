package ru.obukhov.trader.config;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.ScheduledBotsProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.SandboxService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.trading.bots.impl.RunnableBot;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.client.service.interfaces.MarketClient;
import ru.obukhov.trader.web.client.service.interfaces.OperationsClient;
import ru.obukhov.trader.web.client.service.interfaces.OrdersClient;
import ru.obukhov.trader.web.client.service.interfaces.PortfolioClient;
import ru.obukhov.trader.web.client.service.interfaces.SandboxClient;
import ru.obukhov.trader.web.client.service.interfaces.UserClient;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.UsersService;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
@SuppressWarnings("unused")
public class BeanConfiguration {

    @Bean
    public InvestApi investApi(final TradingProperties tradingProperties) {
        return InvestApi.create(tradingProperties.getToken());
    }

    @Bean
    public InstrumentsService instrumentsService(final InvestApi investApi) {
        return investApi.getInstrumentsService();
    }

    @Bean
    public MarketDataService marketDataService(final InvestApi investApi) {
        return investApi.getMarketDataService();
    }

    @Bean
    public ru.tinkoff.piapi.core.OperationsService operationsService(final InvestApi investApi) {
        return investApi.getOperationsService();
    }

    @Bean
    public ru.tinkoff.piapi.core.OrdersService ordersService(final InvestApi investApi) {
        return investApi.getOrdersService();
    }

    @Bean
    public UsersService usersService(final InvestApi investApi) {
        return investApi.getUserService();
    }

    @Bean
    public TinkoffService realTinkoffService(
            final InstrumentsService instrumentsService,
            final MarketDataService marketDataService,
            final ru.tinkoff.piapi.core.OperationsService operationsService,
            final ru.tinkoff.piapi.core.OrdersService ordersService,
            final UsersService usersService,
            final MarketClient marketClient,
            final OperationsClient operationsClient,
            final OrdersClient ordersClient,
            final PortfolioClient portfolioClient,
            final UserClient userClient
    ) {
        return new RealTinkoffService(
                instrumentsService, marketDataService, operationsService, ordersService, usersService,
                marketClient, operationsClient, ordersClient, portfolioClient, userClient
        );
    }

    @Bean
    public MarketService realMarketService(final MarketProperties marketProperties, final TinkoffService realTinkoffService) {
        return new MarketService(marketProperties, realTinkoffService);
    }

    @Bean
    @ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
    public SandboxService sandboxService(final MarketService realMarketService, final SandboxClient sandboxClient) {
        return new SandboxService(realMarketService, sandboxClient);
    }

    @Bean
    public MarketOperationsService realOperationsService(final TinkoffService realTinkoffService) {
        return new MarketOperationsService(realTinkoffService);
    }

    @Bean
    public MarketOrdersService realOrdersService(final TinkoffService realTinkoffService, final MarketService realMarketService) {
        return new MarketOrdersService(realTinkoffService, realMarketService);
    }

    @Bean
    public PortfolioService realPortfolioService(final TinkoffService realTinkoffService) {
        return new PortfolioService(realTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(final MarketService realMarketService, final ApplicationContext applicationContext) {
        return new StatisticsService(realMarketService, applicationContext);
    }

    @Bean
    public List<RunnableBot> scheduledBots(
            final Environment environment,
            final TinkoffServices tinkoffServices,
            final TradingStrategyFactory strategyFactory,
            final SchedulingProperties schedulingProperties,
            final ScheduledBotsProperties scheduledBotsProperties,
            final MarketProperties marketProperties,
            final TaskScheduler taskScheduler
    ) {
        return scheduledBotsProperties.getBotConfigs().stream()
                .map(botConfig -> {
                    final RunnableBot bot = new RunnableBot(
                            tinkoffServices,
                            strategyFactory.createStrategy(botConfig),
                            schedulingProperties,
                            botConfig,
                            marketProperties
                    );
                    registerScheduledJob(environment, schedulingProperties, taskScheduler, bot);
                    return bot;
                }).toList();
    }

    private void registerScheduledJob(
            final Environment environment,
            final SchedulingProperties schedulingProperties,
            final TaskScheduler taskScheduler,
            final RunnableBot bot
    ) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            final PeriodicTrigger trigger = new PeriodicTrigger(schedulingProperties.getDelay());
            trigger.setInitialDelay(schedulingProperties.getDelay());
            taskScheduler.schedule(bot, trigger);
        }
    }

    @Bean
    public OkHttpClient okHttpClient(final List<Interceptor> interceptors) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().pingInterval(Duration.ofSeconds(5));
        clientBuilder.interceptors().addAll(interceptors);
        return clientBuilder.build();
    }


}