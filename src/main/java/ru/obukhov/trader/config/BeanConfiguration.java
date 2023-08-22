package ru.obukhov.trader.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import ru.obukhov.trader.common.model.transform.DoubleToQuotationConverter;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.ScheduledBotsProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.grafana.GrafanaService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.impl.RealContext;
import ru.obukhov.trader.market.impl.RealExtInstrumentsService;
import ru.obukhov.trader.market.impl.RealExtOperationsService;
import ru.obukhov.trader.market.impl.RealExtOrdersService;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.transform.AccountSerializer;
import ru.obukhov.trader.market.model.transform.BondSerializer;
import ru.obukhov.trader.market.model.transform.CurrencySerializer;
import ru.obukhov.trader.market.model.transform.EtfSerializer;
import ru.obukhov.trader.market.model.transform.InstrumentSerializer;
import ru.obukhov.trader.market.model.transform.MoneyValueSerializer;
import ru.obukhov.trader.market.model.transform.OrderStageSerializer;
import ru.obukhov.trader.market.model.transform.OrderStateSerializer;
import ru.obukhov.trader.market.model.transform.QuotationDeserializer;
import ru.obukhov.trader.market.model.transform.QuotationSerializer;
import ru.obukhov.trader.market.model.transform.ShareSerializer;
import ru.obukhov.trader.market.model.transform.TimestampSerializer;
import ru.obukhov.trader.market.model.transform.TradingDaySerializer;
import ru.obukhov.trader.market.model.transform.TradingScheduleSerializer;
import ru.obukhov.trader.trading.bots.RunnableBot;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.text.SimpleDateFormat;
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
    public RealContext realContext() {
        return new RealContext();
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public Context fakeContext(
            final Timestamp currentTimestamp,
            final String accountId,
            final String currency,
            final Quotation initialBalance
    ) {
        return new FakeContext(currentTimestamp, accountId, currency, initialBalance);
    }

    @Bean
    public ExtMarketDataService extMarketDataService(
            final RealExtInstrumentsService realExtInstrumentsService,
            final MarketDataService marketDataService,
            @Lazy final ExtMarketDataService extMarketDataService
    ) {
        return new ExtMarketDataService(realExtInstrumentsService, marketDataService, extMarketDataService);
    }

    @Bean
    public RealExtInstrumentsService realExtInstrumentsService(final InstrumentsService instrumentsService) {
        return new RealExtInstrumentsService(instrumentsService);
    }

    @Bean
    public ExtOperationsService realExtOperationsService(final OperationsService operationsService) {
        return new RealExtOperationsService(operationsService);
    }

    @Bean
    public RealExtOrdersService realExtOrdersService(final OrdersService ordersService) {
        return new RealExtOrdersService(ordersService);
    }

    @Bean
    public StatisticsService statisticsService(final ExtMarketDataService realExtMarketDataService, final ApplicationContext applicationContext) {
        return new StatisticsService(realExtMarketDataService, applicationContext);
    }

    @Bean
    public GrafanaService grafanaService(final ExtMarketDataService realExtMarketDataService, final StatisticsService statisticsService) {
        return new GrafanaService(realExtMarketDataService, statisticsService);
    }

    @Bean
    public ServicesContainer services(
            final ExtMarketDataService realExtMarketDataService,
            final ExtInstrumentsService realExtInstrumentsService,
            final ExtOperationsService realExtOperationsService,
            final RealExtOrdersService realExtOrdersService
    ) {
        return new ServicesContainer(realExtMarketDataService, realExtInstrumentsService, realExtOperationsService, realExtOrdersService);
    }

    @Bean
    public List<RunnableBot> scheduledBots(
            final Environment environment,
            final ServicesContainer services,
            final RealContext realContext,
            final TradingStrategyFactory strategyFactory,
            final SchedulingProperties schedulingProperties,
            final ScheduledBotsProperties scheduledBotsProperties,
            final TaskScheduler taskScheduler
    ) {
        return scheduledBotsProperties.getBotConfigs().stream()
                .map(botConfig -> {
                    final RunnableBot bot = new RunnableBot(
                            services,
                            realContext,
                            strategyFactory.createStrategy(botConfig),
                            schedulingProperties,
                            botConfig
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
    @Primary
    public ObjectMapper objectMapper() {
        final SimpleModule tinkoffModule = new SimpleModule()
                .addSerializer(new AccountSerializer())
                .addSerializer(new InstrumentSerializer())
                .addSerializer(new ShareSerializer())
                .addSerializer(new BondSerializer())
                .addSerializer(new EtfSerializer())
                .addSerializer(new OrderStateSerializer())
                .addSerializer(new OrderStageSerializer())
                .addSerializer(new CurrencySerializer())
                .addSerializer(new TradingScheduleSerializer())
                .addSerializer(new TradingDaySerializer())
                .addSerializer(new QuotationSerializer())
                .addSerializer(new TimestampSerializer())
                .addSerializer(new MoneyValueSerializer())
                .addDeserializer(Quotation.class, new QuotationDeserializer());
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat(DateUtils.OFFSET_DATE_TIME_FORMAT))
                .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
                .registerModule(tinkoffModule);
    }

    @Bean
    public ConversionService conversionService() {
        DefaultConversionService service = new DefaultConversionService();
        service.addConverter(new DoubleToQuotationConverter());
        return service;
    }

}