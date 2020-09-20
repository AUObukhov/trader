package ru.obukhov.investor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.obukhov.investor.config.TradingProperties;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TradingProperties.class)
public class InvestorApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(InvestorApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(InvestorApplication.class);
    }
}