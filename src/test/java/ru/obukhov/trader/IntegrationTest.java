package ru.obukhov.trader;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.util.Collections;

/**
 * Common ancestor for tests with Spring context initialization.
 * Used to mock calls to Tinkoff API
 */
@ActiveProfiles("test")
@SuppressWarnings("java:S2187") // Sonar rule: TestCases should contain tests
public abstract class IntegrationTest {

    @MockBean
    protected InstrumentsService instrumentsService;
    @MockBean
    protected MarketDataService marketDataService;
    @MockBean
    protected UsersService usersService;
    @MockBean
    protected OperationsService operationsService;
    @MockBean
    protected OrdersService ordersService;

    @BeforeEach
    void init() {
        Mockito.when(usersService.getAccountsSync()).thenReturn(Collections.emptyList());
    }

}