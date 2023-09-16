package ru.obukhov.trader.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.web.context.support.GenericWebApplicationContext;
import ru.obukhov.trader.config.properties.ScheduledBotsProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.RealContext;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.trading.bots.RunnableBot;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@SuppressWarnings("unused")
public class RunnableBotsBeanConfiguration {

    public RunnableBotsBeanConfiguration(
            final ScheduledBotsProperties scheduledBotsProperties,
            final TradingStrategyFactory strategyFactory,
            final ServicesContainer services,
            final RealContext realContext,
            final SchedulingProperties schedulingProperties,
            final Environment environment,
            final TaskScheduler taskScheduler,
            final GenericWebApplicationContext genericWebApplicationContext
    ) {
        final List<BotConfig> botConfigs = scheduledBotsProperties.getBotConfigs();
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());

        for (int i = 0; i < botConfigs.size(); i++) {
            final BotConfig botConfig = botConfigs.get(i);
            final String beanName = "scheduledBot" + (i + 1);
            final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);
            genericWebApplicationContext.registerBean(beanName, RunnableBot.class, services, realContext, strategy, schedulingProperties, botConfig);

            if (activeProfiles.contains("prod")) {
                final RunnableBot bot = (RunnableBot) genericWebApplicationContext.getBean(beanName);
                final PeriodicTrigger trigger = new PeriodicTrigger(schedulingProperties.getDelay());
                trigger.setInitialDelay(schedulingProperties.getDelay());
                taskScheduler.schedule(bot, trigger);
            }
        }
    }

}