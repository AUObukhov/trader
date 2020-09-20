package ru.obukhov.investor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import ru.obukhov.investor.service.interfaces.ConnectionService;

/**
 * Bean for passing token from application args to connectionService bean
 */
@Component
@RequiredArgsConstructor
public class ApplicationRunnerImpl implements ApplicationRunner {

    private final ConnectionService connectionService;

    @Override
    public void run(ApplicationArguments args) {
        String token = args.getSourceArgs()[0];
        Assert.notNull(token, "token expected");
        connectionService.setToken(token);
    }

}