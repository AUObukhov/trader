package ru.obukhov.investor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.service.interfaces.ConnectionService;

/**
 * Bean for passing token from application args to connectionService bean
 */
@Component
@RequiredArgsConstructor
public class ApplicationRunnerImpl implements ApplicationRunner {

    private final TradingProperties tradingProperties;
    private final ConnectionService connectionService;

    @Override
    public void run(ApplicationArguments args) {
        String token = tradingProperties.getToken();

        Assert.notNull(token, "token expected");
        connectionService.setToken(token);
    }

}