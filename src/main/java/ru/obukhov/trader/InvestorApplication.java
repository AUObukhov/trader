package ru.obukhov.trader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.TradingProperties;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAspectJAutoProxy
@EnableConfigurationProperties({TradingProperties.class, BotConfig.class})
public class InvestorApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(InvestorApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(InvestorApplication.class);
    }
}