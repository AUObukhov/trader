package ru.obukhov.trader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.tinkoff.invest.openapi.exceptions.WrongTokenException;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;

import java.util.concurrent.CompletionException;

@ExtendWith(MockitoExtension.class)
class TokenValidationStartupListenerContextTest {

    @Mock
    private MarketService marketService;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void test_throwsException_whenTokenIsWrong() {
        final WrongTokenException wrongTokenException = new WrongTokenException();
        final String message = "ru.tinkoff.invest.openapi.exceptions.WrongTokenException: Попытка использовать неверный токен";
        final CompletionException completionException = new CompletionException(message, wrongTokenException);
        Mockito.when(marketService.getInstruments(InstrumentType.STOCK)).thenThrow(completionException);
        contextRunner
                .withBean(MarketService.class, () -> marketService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> Assertions.assertThrows(CompletionException.class, () -> publishApplicationStartedEvent(context), message));
    }

    @Test
    void test_skipsUnknownExceptions() {
        Mockito.when(marketService.getInstruments(InstrumentType.STOCK)).thenThrow(new RuntimeException());
        contextRunner
                .withBean(MarketService.class, () -> marketService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> Assertions.assertDoesNotThrow(() -> publishApplicationStartedEvent(context)));
    }

    private void publishApplicationStartedEvent(ConfigurableApplicationContext context) {
        final SpringApplication springApplication = Mockito.mock(SpringApplication.class);
        final ApplicationStartedEvent applicationStartedEvent = new ApplicationStartedEvent(springApplication, new String[0], context);
        context.publishEvent(applicationStartedEvent);
    }

}