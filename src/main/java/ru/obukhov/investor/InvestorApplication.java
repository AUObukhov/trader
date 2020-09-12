package ru.obukhov.investor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.Assert;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.ThrottledMarketContext;
import ru.tinkoff.invest.openapi.MarketContext;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties(TradingProperties.class)
@RequiredArgsConstructor
public class InvestorApplication implements ApplicationContextAware {

    private static String token;

    public static void main(String[] args) {
        Assert.notNull(args[0], "token expected");
        token = args[0];

        SpringApplication.run(InvestorApplication.class, args);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ConnectionService connectionService = applicationContext.getBean(ConnectionService.class);
        connectionService.setToken(token);
        MarketContext marketContext = connectionService.getMarketContext();

        ThrottledMarketContext throttledMarketContext = applicationContext.getBean(ThrottledMarketContext.class);
        throttledMarketContext.setInnerContext(marketContext);
    }

}