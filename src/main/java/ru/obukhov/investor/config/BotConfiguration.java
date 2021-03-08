package ru.obukhov.investor.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.BotFactory;

/**
 * Configuration of bots beans, which need qualifying of dependencies
 */
@Configuration
@DependsOn("beanConfiguration")
@RequiredArgsConstructor
public class BotConfiguration {

    private final BotFactory scheduledBotFactory;

    @Bean
    public Bot scheduledBot() {
        return scheduledBotFactory.createDumbBot();
    }

}