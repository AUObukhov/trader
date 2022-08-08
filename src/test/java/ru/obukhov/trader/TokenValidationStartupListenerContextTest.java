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
import ru.obukhov.trader.market.impl.UserService;

@ExtendWith(MockitoExtension.class)
class TokenValidationStartupListenerContextTest {

    @Mock
    private UserService userService;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void test_throwsException_whenApiCallThrowsException() {
        final String message = "some exception";
        final RuntimeException exception = new RuntimeException(message);
        Mockito.when(userService.getAccounts()).thenThrow(exception);
        contextRunner
                .withBean(UserService.class, () -> userService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> Assertions.assertThrows(RuntimeException.class, () -> publishApplicationStartedEvent(context), message)
                );
    }

    private void publishApplicationStartedEvent(ConfigurableApplicationContext context) {
        final SpringApplication springApplication = Mockito.mock(SpringApplication.class);
        final ApplicationStartedEvent applicationStartedEvent = new ApplicationStartedEvent(springApplication, new String[0], context);
        context.publishEvent(applicationStartedEvent);
    }

}