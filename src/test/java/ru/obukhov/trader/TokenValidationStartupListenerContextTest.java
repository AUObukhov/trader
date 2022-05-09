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
import ru.obukhov.trader.market.impl.SandboxService;
import ru.obukhov.trader.market.impl.UserService;
import ru.obukhov.trader.web.client.exceptions.WrongTokenException;

import java.io.IOException;
import java.util.concurrent.CompletionException;

@ExtendWith(MockitoExtension.class)
class TokenValidationStartupListenerContextTest {

    @Mock
    private UserService userService;
    @Mock
    private SandboxService sandboxService;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void test_throwsException_whenTokenIsWrong_whenSandboxServiceIsNull() {
        final WrongTokenException wrongTokenException = new WrongTokenException();
        final String message = "ru.tinkoff.invest.openapi.exceptions.WrongTokenException: Попытка использовать неверный токен";
        final CompletionException completionException = new CompletionException(message, wrongTokenException);
        Mockito.when(userService.getAccounts()).thenThrow(completionException);
        contextRunner
                .withBean(UserService.class, () -> userService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> {
                    Assertions.assertThrows(CompletionException.class, () -> publishApplicationStartedEvent(context), message);
                    verifyCheckByMarket();
                });
    }

    @Test
    void test_throwsException_whenTokenIsWrong_whenSandboxServiceIsNotNull() throws IOException {
        final WrongTokenException wrongTokenException = new WrongTokenException();
        final String message = "ru.tinkoff.invest.openapi.exceptions.WrongTokenException: Попытка использовать неверный токен";
        final CompletionException completionException = new CompletionException(message, wrongTokenException);
        Mockito.doThrow(completionException).when(sandboxService).register();
        contextRunner
                .withBean(UserService.class, () -> userService)
                .withBean(SandboxService.class, () -> sandboxService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> {
                    Assertions.assertThrows(CompletionException.class, () -> publishApplicationStartedEvent(context), message);
                    verifyCheckBySandbox();
                });
    }

    @Test
    void test_skipsUnknownExceptions_whenSandboxServiceIsNull() {
        Mockito.when(userService.getAccounts()).thenThrow(new RuntimeException());
        contextRunner
                .withBean(UserService.class, () -> userService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> {
                    Assertions.assertDoesNotThrow(() -> publishApplicationStartedEvent(context));
                    verifyCheckByMarket();
                });
    }

    @Test
    void test_skipsUnknownExceptions_whenSandboxServiceIsNotNull() throws IOException {
        Mockito.doThrow(new RuntimeException()).when(sandboxService).register();
        contextRunner
                .withBean(UserService.class, () -> userService)
                .withBean(SandboxService.class, () -> sandboxService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> {
                    Assertions.assertDoesNotThrow(() -> publishApplicationStartedEvent(context));
                    verifyCheckBySandbox();
                });
    }

    @Test
    void test_callsGetInstruments_whenSandboxServiceIsNull() {
        contextRunner
                .withBean(UserService.class, () -> userService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> {
                    publishApplicationStartedEvent(context);
                    verifyCheckByMarket();
                });
    }

    @Test
    void test_registersSandbox_whenSandboxServiceIsNotNull() {
        contextRunner
                .withBean(UserService.class, () -> userService)
                .withBean(SandboxService.class, () -> sandboxService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> {
                    publishApplicationStartedEvent(context);
                    verifyCheckBySandbox();
                });
    }

    private void publishApplicationStartedEvent(ConfigurableApplicationContext context) {
        final SpringApplication springApplication = Mockito.mock(SpringApplication.class);
        final ApplicationStartedEvent applicationStartedEvent = new ApplicationStartedEvent(springApplication, new String[0], context);
        context.publishEvent(applicationStartedEvent);
    }

    private void verifyCheckByMarket() throws IOException {
        Mockito.verify(userService, Mockito.times(1)).getAccounts();
        Mockito.verify(sandboxService, Mockito.never()).register();
    }

    private void verifyCheckBySandbox() throws IOException {
        Mockito.verify(userService, Mockito.never()).getAccounts();
        Mockito.verify(sandboxService, Mockito.times(1)).register();
    }

}