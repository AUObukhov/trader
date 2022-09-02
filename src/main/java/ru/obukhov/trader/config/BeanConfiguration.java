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
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.impl.RealContext;
import ru.obukhov.trader.market.impl.RealExtOperationsService;
import ru.obukhov.trader.market.impl.RealExtOrdersService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.trading.bots.RunnableBot;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.math.BigDecimal;
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
    public ExtUsersService extUsersService(final UsersService usersService) {
        return new ExtUsersService(usersService);
    }

    @Bean
    public Context realContext() {
        return new RealContext();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Context fakeContext(
            final MarketProperties marketProperties,
            final TinkoffServices tinkoffServices,
            final OffsetDateTime currentDateTime,
            final String accountId,
            final Currency currency,
            final BigDecimal initialBalance
    ) {
        return new FakeContext(marketProperties, tinkoffServices, currentDateTime, accountId, currency, initialBalance);
    }

    @Bean
    public ExtMarketDataService realExtMarketDataService(
            final MarketProperties marketProperties,
            final Context realContext,
            final ExtInstrumentsService extInstrumentsService,
            final MarketDataService marketDataService
    ) {
        return new ExtMarketDataService(marketProperties, realContext, extInstrumentsService, marketDataService);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ExtMarketDataService fakeExtMarketDataService(
            final MarketProperties marketProperties,
            final Context fakeContext,
            final ExtInstrumentsService extInstrumentsService,
            final MarketDataService marketDataService
    ) {
        return new ExtMarketDataService(marketProperties, fakeContext, extInstrumentsService, marketDataService);
    }

    @Bean
    public ExtInstrumentsService extInstrumentsService(final InstrumentsService instrumentsService) {
        return new ExtInstrumentsService(instrumentsService);
    }

    @Bean
    public ExtOperationsService realExtOperationsService(
            final OperationsService operationsService,
            final ExtInstrumentsService extInstrumentsService
    ) {
        return new RealExtOperationsService(operationsService, extInstrumentsService);
    }

    @Bean
    public RealExtOrdersService realExtOrdersService(
            final OrdersService ordersService,
            final ExtMarketDataService realExtMarketDataService,
            final ExtInstrumentsService extInstrumentsService
    ) {
        return new RealExtOrdersService(ordersService, extInstrumentsService);
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
            final ExtInstrumentsService extInstrumentsService,
            final ExtOperationsService realExtOperationsService,
            final RealExtOrdersService realExtOrdersService,
            final RealContext realContext
    ) {
        return new TinkoffServices(
                realExtMarketDataService,
                extInstrumentsService,
                realExtOperationsService,
                realExtOrdersService,
                realContext
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