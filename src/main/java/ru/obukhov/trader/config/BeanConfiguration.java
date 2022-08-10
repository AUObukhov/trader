package ru.obukhov.trader.config;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.ScheduledBotsProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.grafana.GrafanaService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.impl.UserService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.trading.bots.impl.RunnableBot;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.time.Duration;
import java.time.OffsetDateTime;
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
    public UserService realUserService(final TinkoffService realTinkoffService) {
        return new UserService(realTinkoffService);
    }

    @Bean
    public TinkoffService realTinkoffService(
            final InstrumentsService instrumentsService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final UsersService usersService
    ) {
        return new RealTinkoffService(instrumentsService, operationsService, ordersService, usersService);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public TinkoffService fakeTinkoffService(
            final MarketProperties marketProperties,
            final TinkoffServices tinkoffServices,
            final BotConfig botConfig,
            final BalanceConfig balanceConfig,
            final OffsetDateTime currentDateTime
    ) {
        final Share share = tinkoffServices.marketInstrumentsService().getShare(botConfig.ticker());
        if (share == null) {
            throw new IllegalArgumentException("Not found share for ticker '" + botConfig.ticker() + "'");
        }

        return new FakeTinkoffService(
                marketProperties,
                tinkoffServices,
                botConfig.accountId(),
                currentDateTime,
                Currency.valueOfIgnoreCase(share.getCurrency()),
                botConfig.commission(),
                balanceConfig
        );
    }

    @Bean
    public ExtMarketDataService realExtMarketDataService(
            final MarketProperties marketProperties,
            final TinkoffService realTinkoffService,
            final MarketDataService marketDataService
    ) {
        return new ExtMarketDataService(marketProperties, realTinkoffService, marketDataService);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ExtMarketDataService fakeExtMarketDataService(
            final MarketProperties marketProperties,
            final TinkoffService fakeTinkoffService,
            final MarketDataService marketDataService
    ) {
        return new ExtMarketDataService(marketProperties, fakeTinkoffService, marketDataService);
    }

    @Bean
    public MarketInstrumentsService marketInstrumentsService(final InstrumentsService instrumentsService) {
        return new MarketInstrumentsService(instrumentsService);
    }

    @Bean
    public MarketOperationsService realOperationsService(final TinkoffService realTinkoffService) {
        return new MarketOperationsService(realTinkoffService);
    }

    @Bean
    public MarketOrdersService realOrdersService(final TinkoffService realTinkoffService, final ExtMarketDataService realExtMarketDataService) {
        return new MarketOrdersService(realTinkoffService);
    }

    @Bean
    public PortfolioService realPortfolioService(final TinkoffService realTinkoffService) {
        return new PortfolioService(realTinkoffService);
    }

    @Bean
    public StatisticsService statisticsService(final ExtMarketDataService realExtMarketDataService, final ApplicationContext applicationContext) {
        return new StatisticsService(realExtMarketDataService, applicationContext);
    }

    @Bean
    public GrafanaService realGrafanaService(final ExtMarketDataService realExtMarketDataService, final StatisticsService statisticsService) {
        return new GrafanaService(realExtMarketDataService, statisticsService);
    }

    @Bean
    public TinkoffServices realTinkoffServices(
            final ExtMarketDataService realExtMarketDataService,
            final MarketInstrumentsService marketInstrumentsService,
            final MarketOperationsService realOperationsService,
            final MarketOrdersService realOrdersService,
            final PortfolioService realPortfolioService,
            final RealTinkoffService realTinkoffService
    ) {
        return new TinkoffServices(
                realExtMarketDataService,
                marketInstrumentsService,
                realOperationsService,
                realOrdersService,
                realPortfolioService,
                realTinkoffService
        );
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