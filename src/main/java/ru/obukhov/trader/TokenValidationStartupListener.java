package ru.obukhov.trader;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.market.impl.ExtUsersService;

/**
 * Class for failing application on startup if token is invalid.
 * Tinkoff is hiding unauthorized error now, so application is failed in case of any exception
 */
@Slf4j
@Component
@AllArgsConstructor
public class TokenValidationStartupListener implements ApplicationListener<ApplicationStartedEvent> {

    private final ExtUsersService extUsersService;

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent applicationStartedEvent) {
        extUsersService.getAccounts();
    }

}