package ru.obukhov.trader;

import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Common ancestor for tests with Spring context initialization.
 * Used to prevent token validation with real request to Tinkoff API
 */
@SuppressWarnings("java:S2187") // Sonar rule: TestCases should contain tests
public class ContextTest {

    @MockBean
    private TokenValidationStartupListener tokenValidationStartupListener;

}