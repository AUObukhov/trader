package ru.obukhov.trader;

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
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class TokenValidationStartupListenerContextTest {

    @Mock
    private ExtUsersService extUsersService;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(applicationContext -> applicationContext.getEnvironment().setActiveProfiles("test"))
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void test_throwsException_whenApiCallThrowsException() {
        final String message = "some exception";
        final RuntimeException exception = new RuntimeException(message);
        Mockito.when(extUsersService.getAccounts()).thenThrow(exception);
        contextRunner
                .withBean(ExtUsersService.class, () -> extUsersService)
                .withBean(TokenValidationStartupListener.class)
                .run(context -> AssertUtils.assertThrowsWithMessage(RuntimeException.class, () -> publishApplicationStartedEvent(context), message));
    }

    private void publishApplicationStartedEvent(ConfigurableApplicationContext context) {
        final SpringApplication springApplication = Mockito.mock(SpringApplication.class);
        final String[] args = new String[0];
        final Duration timeTaken = Duration.ofSeconds(10);
        final ApplicationStartedEvent applicationStartedEvent = new ApplicationStartedEvent(springApplication, args, context, timeTaken);
        context.publishEvent(applicationStartedEvent);
    }

}