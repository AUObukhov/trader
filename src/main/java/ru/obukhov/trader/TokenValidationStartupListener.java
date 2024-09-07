package ru.obukhov.trader;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.market.impl.ExtUsersService;

@Slf4j
@Component
@AllArgsConstructor
public class TokenValidationStartupListener implements ApplicationListener<ApplicationStartedEvent> {

    private final ExtUsersService extUsersService;

    @Override
    public void onApplicationEvent(@NotNull ApplicationStartedEvent applicationStartedEvent) {
        extUsersService.getAccounts();
    }

}