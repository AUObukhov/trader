package ru.obukhov.investor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.Assert;
import ru.obukhov.investor.config.TokenHolder;
import ru.obukhov.investor.config.TradingProperties;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TradingProperties.class)
public class InvestorApplication {

    public static void main(String[] args) {
        Assert.notNull(args[0], "token expected");
        TokenHolder.setToken(args[0]);

        SpringApplication.run(InvestorApplication.class, args);
    }

}
