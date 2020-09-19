package ru.obukhov.investor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.obukhov.investor.config.TradingProperties;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TradingProperties.class)
public class InvestorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestorApplication.class, args);
    }

}