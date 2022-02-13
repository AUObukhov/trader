package ru.obukhov.trader;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.web.client.exceptions.WrongTokenException;

/**
 * Class for failing application on startup if token is invalid
 */
@Slf4j
@Component
@AllArgsConstructor
public class TokenValidationStartupListener implements ApplicationListener<ApplicationStartedEvent> {

    private final MarketService marketService;

    @SneakyThrows
    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent applicationStartedEvent) {
        try {
            marketService.getInstruments(InstrumentType.STOCK);
        } catch (final Exception exception) {
            if (exception.getCause() instanceof WrongTokenException) {
                throw exception;
            } else {
                log.error("Failed to call Tinkoff API after startup", exception);
            }
        }
    }

}