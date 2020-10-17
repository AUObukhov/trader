package ru.obukhov.investor.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.obukhov.investor.bot.impl.ScheduledBot;
import ru.obukhov.investor.bot.impl.SimpleBot;
import ru.obukhov.investor.bot.impl.SimulatorImpl;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.DataSupplier;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.interfaces.MarketMock;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.service.interfaces.OrdersService;

/**
 * Configuration of beans, which need qualifying of dependencies
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public Bot simpleBot(@Qualifier("historicalDataSupplier") DataSupplier dataSupplier,
                         Decider decider,
                         @Qualifier("ordersServiceMock") OrdersService ordersService) {

        return new SimpleBot(dataSupplier, decider, ordersService);

    }

    @Bean
    public Bot scheduledBot(@Qualifier("realDataSupplier") DataSupplier dataSupplier,
                            Decider decider,
                            @Qualifier("ordersServiceImpl") OrdersService ordersService,
                            BotProperties botProperties,
                            TradingProperties tradingProperties) {

        return new ScheduledBot(dataSupplier, decider, ordersService, botProperties, tradingProperties);

    }

    @Bean
    public Simulator simulatorImpl(@Qualifier("simpleBot") Bot bot, MarketMock marketMock) {
        return new SimulatorImpl(bot, marketMock);
    }

}